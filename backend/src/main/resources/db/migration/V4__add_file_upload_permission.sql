insert into sys_menu (parent_id, name, type, path, component, permission_code, icon, sort, visible, status)
select 1, '文件上传', 'BUTTON', null, null, 'system:file:upload', null, 120, 0, 1
where not exists (
  select 1 from sys_menu where permission_code = 'system:file:upload'
);

insert into sys_role_menu (role_id, menu_id)
select r.id, m.id
from sys_role r
join sys_menu m on m.permission_code = 'system:file:upload'
where r.role_code = 'SUPER_ADMIN'
  and not exists (
    select 1 from sys_role_menu rm where rm.role_id = r.id and rm.menu_id = m.id
  );
