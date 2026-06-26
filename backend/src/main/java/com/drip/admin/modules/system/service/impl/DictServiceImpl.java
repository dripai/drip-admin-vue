package com.drip.admin.modules.system.service.impl;

import com.drip.admin.modules.system.mapper.SysDictItemMapper;
import com.drip.admin.modules.system.mapper.SysDictTypeMapper;
import com.drip.admin.modules.system.service.DictService;

import com.drip.admin.common.exception.BusinessException;
import com.drip.admin.common.response.PageResult;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.drip.admin.shared.utils.AdminUtils.intOf;
import static com.drip.admin.shared.utils.AdminUtils.parseInt;
import static com.drip.admin.shared.utils.AdminUtils.requireNonBlank;
import static com.drip.admin.shared.utils.AdminUtils.snakeToCamel;
import static com.drip.admin.shared.utils.AdminUtils.stringOf;

@Service
public class DictServiceImpl implements DictService {
    private static final Set<String> TYPE_COLUMNS = Set.of("dict_name", "dict_code", "status", "remark");
    private static final Set<String> ITEM_COLUMNS = Set.of("dict_type_id", "label", "value", "color", "sort", "status");
    private final JdbcTemplate jdbc;
    private final SysDictTypeMapper dictTypeMapper;
    private final SysDictItemMapper dictItemMapper;
    private final Map<String, List<Map<String, Object>>> dictCache = new HashMap<>();

    public DictServiceImpl(JdbcTemplate jdbc, SysDictTypeMapper dictTypeMapper, SysDictItemMapper dictItemMapper) {
        this.jdbc = jdbc;
        this.dictTypeMapper = dictTypeMapper;
        this.dictItemMapper = dictItemMapper;
    }

    public PageResult<Map<String, Object>> types(Map<String, String> q) {
        int page = Math.max(1, parseInt(q.get("page"), 1));
        int pageSize = Math.min(100, Math.max(1, parseInt(q.get("pageSize"), 20)));
        List<Object> args = new ArrayList<>();
        StringBuilder where = new StringBuilder(" where deleted = 0");
        for (String filter : List.of("dict_name", "dict_code", "status")) {
            String value = q.getOrDefault(filter, q.get(snakeToCamel(filter)));
            if (value != null && !value.isBlank()) {
                where.append(" and ").append(filter).append(" like ?");
                args.add("%" + value + "%");
            }
        }
        Long total = jdbc.queryForObject("select count(1) from sys_dict_type" + where, Long.class, args.toArray());
        List<Object> listArgs = new ArrayList<>(args);
        listArgs.add((page - 1) * pageSize);
        listArgs.add(pageSize);
        List<Map<String, Object>> rows = jdbc.queryForList(
            "select * from sys_dict_type" + where + " order by created_at desc limit ?, ?",
            listArgs.toArray()
        );
        return new PageResult<>(rows, total == null ? 0 : total, page, pageSize);
    }

    public List<Map<String, Object>> items(long dictTypeId) {
        typeDetail(dictTypeId);
        return jdbc.queryForList(
            "select * from sys_dict_item where dict_type_id = ? and deleted = 0 order by sort asc, id asc",
            dictTypeId
        );
    }

    public Map<String, Object> typeDetail(long id) {
        return detail("sys_dict_type", id);
    }

    public Map<String, Object> itemDetail(long id) {
        return detail("sys_dict_item", id);
    }

    @Transactional
    public Long createType(Map<String, Object> body) {
        requireNonBlank(body, "dict_name", "dictName");
        requireNonBlank(body, "dict_code", "dictCode");
        Long id = insert("sys_dict_type", body, TYPE_COLUMNS);
        refreshCache();
        return id;
    }

    @Transactional
    public void updateType(long id, Map<String, Object> body) {
        typeDetail(id);
        update("sys_dict_type", id, body, TYPE_COLUMNS);
        refreshCache();
    }

    @Transactional
    public void deleteType(long id) {
        typeDetail(id);
        Long count = jdbc.queryForObject("select count(1) from sys_dict_item where dict_type_id = ? and deleted = 0", Long.class, id);
        if (count != null && count > 0) {
            throw new BusinessException(400501, "字典类型存在字典项，不能删除");
        }
        jdbc.update("update sys_dict_type set deleted = 1 where id = ?", id);
        refreshCache();
    }

    @Transactional
    public Long createItem(Map<String, Object> body) {
        requireNonBlank(body, "label");
        requireNonBlank(body, "value");
        typeDetail(intOf(value(body, "dict_type_id")));
        Long id = insert("sys_dict_item", body, ITEM_COLUMNS);
        refreshCache();
        return id;
    }

    @Transactional
    public void updateItem(long id, Map<String, Object> body) {
        itemDetail(id);
        Object dictTypeId = value(body, "dict_type_id");
        if (dictTypeId != null) {
            typeDetail(intOf(dictTypeId));
        }
        update("sys_dict_item", id, body, ITEM_COLUMNS);
        refreshCache();
    }

    @Transactional
    public void deleteItem(long id) {
        Map<String, Object> item = itemDetail(id);
        Map<String, Object> type = typeDetail(intOf(item.get("dict_type_id")));
        String dictCode = stringOf(type.get("dict_code"));
        String itemValue = stringOf(item.get("value"));
        if ("common_status".equals(dictCode) && commonStatusValueReferenced(itemValue)) {
            throw new BusinessException(400501, "字典项被引用，不能删除");
        }
        jdbc.update("update sys_dict_item set deleted = 1 where id = ?", id);
        refreshCache();
    }

    @Transactional
    public void updateItemStatus(long id, int status) {
        itemDetail(id);
        jdbc.update("update sys_dict_item set status = ? where id = ? and deleted = 0", status, id);
        refreshCache();
    }

    public void refreshCache() {
        dictCache.clear();
        for (Map<String, Object> type : jdbc.queryForList("select * from sys_dict_type where deleted = 0 and status = 1")) {
            dictCache.put(stringOf(type.get("dict_code")), jdbc.queryForList(
                "select * from sys_dict_item where dict_type_id = ? and deleted = 0 order by sort asc, id asc",
                type.get("id")
            ));
        }
    }

    private boolean commonStatusValueReferenced(String value) {
        int status = intOf(value);
        List<String> tables = List.of("sys_user", "sys_role", "sys_menu", "sys_dept", "sys_dict_type", "sys_dict_item", "sys_config", "sys_job");
        for (String table : tables) {
            Long count = jdbc.queryForObject("select count(1) from " + table + " where status = ? and deleted = 0", Long.class, status);
            if (count != null && count > 0) {
                return true;
            }
        }
        return false;
    }

    private Map<String, Object> detail(String table, long id) {
        List<Map<String, Object>> rows = jdbc.queryForList("select * from " + table + " where id = ? and deleted = 0", id);
        if (rows.isEmpty()) {
            throw new BusinessException(404000, "资源不存在");
        }
        return new LinkedHashMap<>(rows.getFirst());
    }

    private Long insert(String table, Map<String, Object> body, Set<String> allowed) {
        LinkedHashMap<String, Object> values = columns(body, allowed);
        if (values.isEmpty()) {
            throw new BusinessException(400000, "请求参数错误");
        }
        String cols = String.join(", ", values.keySet());
        String placeholders = values.keySet().stream().map(k -> "?").collect(Collectors.joining(", "));
        jdbc.update("insert into " + table + " (" + cols + ") values (" + placeholders + ")", values.values().toArray());
        return jdbc.queryForObject("select last_insert_id()", Long.class);
    }

    private void update(String table, long id, Map<String, Object> body, Set<String> allowed) {
        LinkedHashMap<String, Object> values = columns(body, allowed);
        if (values.isEmpty()) {
            return;
        }
        String set = values.keySet().stream().map(k -> k + " = ?").collect(Collectors.joining(", "));
        List<Object> args = new ArrayList<>(values.values());
        args.add(id);
        jdbc.update("update " + table + " set " + set + " where id = ? and deleted = 0", args.toArray());
    }

    private static LinkedHashMap<String, Object> columns(Map<String, Object> body, Set<String> allowed) {
        LinkedHashMap<String, Object> values = new LinkedHashMap<>();
        for (String col : allowed) {
            Object raw = value(body, col);
            if (raw != null) {
                values.put(col, raw);
            }
        }
        return values;
    }

    private static Object value(Map<String, Object> body, String column) {
        String camel = snakeToCamel(column);
        if (body.containsKey(column)) {
            return body.get(column);
        }
        if (body.containsKey(camel)) {
            return body.get(camel);
        }
        return null;
    }
}
