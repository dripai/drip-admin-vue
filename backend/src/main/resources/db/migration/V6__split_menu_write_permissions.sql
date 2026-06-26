update sys_menu
set name = '新增菜单',
    permission_code = 'system:menu:create',
    sort = 31
where permission_code = 'system:menu:write';

insert into sys_menu (parent_id, name, type, path, component, permission_code, icon, sort, visible, status)
select parent_id, name, 'BUTTON', null, null, permission_code, null, sort, 0, 1
from (
  select 15 parent_id, '编辑菜单' name, 'system:menu:update' permission_code, 32 sort union all
  select 15, '删除菜单', 'system:menu:delete', 33 union all
  select 15, '变更菜单状态', 'system:menu:status', 34
) p
where not exists (
  select 1 from sys_menu m where m.permission_code = p.permission_code
);

insert into sys_role_menu (role_id, menu_id)
select r.id, m.id
from sys_role r
join sys_menu m on m.permission_code in (
  'system:menu:create',
  'system:menu:update',
  'system:menu:delete',
  'system:menu:status'
)
where r.role_code = 'SUPER_ADMIN'
  and not exists (
    select 1 from sys_role_menu rm where rm.role_id = r.id and rm.menu_id = m.id
  );
