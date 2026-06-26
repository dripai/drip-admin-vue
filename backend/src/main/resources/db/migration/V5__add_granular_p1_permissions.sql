insert into sys_menu (parent_id, name, type, path, component, permission_code, icon, sort, visible, status)
select parent_id, name, 'BUTTON', null, null, permission_code, null, sort, 0, 1
from (
  select 17 parent_id, '新增部门' name, 'system:dept:create' permission_code, 41 sort union all
  select 17, '编辑部门', 'system:dept:update', 42 union all
  select 17, '删除部门', 'system:dept:delete', 43 union all
  select 18, '新增字典', 'system:dict:create', 51 union all
  select 18, '编辑字典', 'system:dict:update', 52 union all
  select 18, '删除字典', 'system:dict:delete', 53 union all
  select 19, '新增配置', 'system:config:create', 61 union all
  select 19, '编辑配置', 'system:config:update', 62 union all
  select 19, '删除配置', 'system:config:delete', 63 union all
  select 22, '强制下线', 'system:online:kickout', 91 union all
  select 23, '新增任务', 'system:job:create', 101 union all
  select 23, '编辑任务', 'system:job:update', 102 union all
  select 23, '删除任务', 'system:job:delete', 103 union all
  select 23, '执行任务', 'system:job:run', 104 union all
  select 24, '删除备份', 'system:database:backup:delete', 114
) p
where not exists (
  select 1 from sys_menu m where m.permission_code = p.permission_code
);

insert into sys_role_menu (role_id, menu_id)
select r.id, m.id
from sys_role r
join sys_menu m on m.permission_code in (
  'system:dept:create',
  'system:dept:update',
  'system:dept:delete',
  'system:dict:create',
  'system:dict:update',
  'system:dict:delete',
  'system:config:create',
  'system:config:update',
  'system:config:delete',
  'system:online:kickout',
  'system:job:create',
  'system:job:update',
  'system:job:delete',
  'system:job:run',
  'system:database:backup:delete'
)
where r.role_code = 'SUPER_ADMIN'
  and not exists (
    select 1 from sys_role_menu rm where rm.role_id = r.id and rm.menu_id = m.id
  );
