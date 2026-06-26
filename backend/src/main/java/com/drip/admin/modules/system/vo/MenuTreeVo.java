package com.drip.admin.modules.system.vo;

import java.util.ArrayList;
import java.util.List;

public class MenuTreeVo {
    private Long id;
    private Long parentId;
    private String name;
    private String type;
    private String path;
    private String component;
    private String permissionCode;
    private String icon;
    private Integer sort;
    private Integer visible;
    private List<MenuTreeVo> children = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
    public String getComponent() { return component; }
    public void setComponent(String component) { this.component = component; }
    public String getPermissionCode() { return permissionCode; }
    public void setPermissionCode(String permissionCode) { this.permissionCode = permissionCode; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    public Integer getSort() { return sort; }
    public void setSort(Integer sort) { this.sort = sort; }
    public Integer getVisible() { return visible; }
    public void setVisible(Integer visible) { this.visible = visible; }
    public List<MenuTreeVo> getChildren() { return children; }
    public void setChildren(List<MenuTreeVo> children) { this.children = children; }
}
