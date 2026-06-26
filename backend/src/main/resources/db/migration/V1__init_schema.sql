create table if not exists sys_user (
  id bigint primary key auto_increment,
  username varchar(64) not null,
  password_hash varchar(128) not null,
  password_salt varchar(64) not null,
  real_name varchar(64) not null,
  phone varchar(32) null,
  email varchar(128) null,
  avatar varchar(255) null,
  status tinyint not null default 1,
  dept_id bigint null,
  remark varchar(255) null,
  last_login_at datetime null,
  deleted tinyint not null default 0,
  created_at datetime not null default current_timestamp,
  updated_at datetime not null default current_timestamp on update current_timestamp,
  unique key uk_sys_user_username (username),
  key idx_sys_user_dept (dept_id),
  key idx_sys_user_status (status)
);

create table if not exists sys_role (
  id bigint primary key auto_increment,
  role_name varchar(64) not null,
  role_code varchar(64) not null,
  builtin tinyint not null default 0,
  status tinyint not null default 1,
  remark varchar(255) null,
  deleted tinyint not null default 0,
  created_at datetime not null default current_timestamp,
  updated_at datetime not null default current_timestamp on update current_timestamp,
  unique key uk_sys_role_code (role_code)
);

create table if not exists sys_user_role (
  id bigint primary key auto_increment,
  user_id bigint not null,
  role_id bigint not null,
  created_at datetime not null default current_timestamp,
  unique key uk_sys_user_role (user_id, role_id),
  key idx_sys_user_role_role (role_id)
);

create table if not exists sys_menu (
  id bigint primary key auto_increment,
  parent_id bigint not null default 0,
  name varchar(64) not null,
  type varchar(16) not null,
  path varchar(255) null,
  component varchar(255) null,
  permission_code varchar(128) null,
  icon varchar(64) null,
  sort int not null default 0,
  visible tinyint not null default 1,
  status tinyint not null default 1,
  deleted tinyint not null default 0,
  created_at datetime not null default current_timestamp,
  updated_at datetime not null default current_timestamp on update current_timestamp,
  unique key uk_sys_menu_permission (permission_code),
  key idx_sys_menu_parent (parent_id),
  key idx_sys_menu_sort (sort)
);

create table if not exists sys_role_menu (
  id bigint primary key auto_increment,
  role_id bigint not null,
  menu_id bigint not null,
  created_at datetime not null default current_timestamp,
  unique key uk_sys_role_menu (role_id, menu_id),
  key idx_sys_role_menu_menu (menu_id)
);

create table if not exists sys_dept (
  id bigint primary key auto_increment,
  parent_id bigint not null default 0,
  dept_name varchar(64) not null,
  dept_code varchar(64) not null,
  leader_user_id bigint null,
  sort int not null default 0,
  status tinyint not null default 1,
  deleted tinyint not null default 0,
  created_at datetime not null default current_timestamp,
  updated_at datetime not null default current_timestamp on update current_timestamp,
  unique key uk_sys_dept_code (dept_code),
  key idx_sys_dept_parent (parent_id)
);

create table if not exists sys_dict_type (
  id bigint primary key auto_increment,
  dict_name varchar(64) not null,
  dict_code varchar(64) not null,
  status tinyint not null default 1,
  remark varchar(255) null,
  deleted tinyint not null default 0,
  created_at datetime not null default current_timestamp,
  updated_at datetime not null default current_timestamp on update current_timestamp,
  unique key uk_sys_dict_type_code (dict_code)
);

create table if not exists sys_dict_item (
  id bigint primary key auto_increment,
  dict_type_id bigint not null,
  label varchar(64) not null,
  value varchar(64) not null,
  color varchar(32) null,
  sort int not null default 0,
  status tinyint not null default 1,
  deleted tinyint not null default 0,
  created_at datetime not null default current_timestamp,
  updated_at datetime not null default current_timestamp on update current_timestamp,
  unique key uk_sys_dict_item_value (dict_type_id, value),
  key idx_sys_dict_item_type (dict_type_id)
);

create table if not exists sys_login_log (
  id bigint primary key auto_increment,
  user_id bigint null,
  username varchar(64) not null,
  real_name varchar(64) null,
  login_type varchar(32) not null,
  status varchar(16) not null,
  failure_reason varchar(255) null,
  ip varchar(64) null,
  user_agent varchar(512) null,
  device_type varchar(64) null,
  login_at datetime not null default current_timestamp,
  key idx_sys_login_log_username (username),
  key idx_sys_login_log_status (status),
  key idx_sys_login_log_login_at (login_at)
);

create table if not exists sys_operation_log (
  id bigint primary key auto_increment,
  operator_id bigint null,
  operator_name varchar(64) null,
  module varchar(64) not null,
  action varchar(64) not null,
  method varchar(16) not null,
  path varchar(255) not null,
  request_params text null,
  response_status varchar(16) not null,
  error_message varchar(512) null,
  cost_ms bigint not null default 0,
  created_at datetime not null default current_timestamp,
  key idx_sys_operation_log_operator (operator_id),
  key idx_sys_operation_log_module (module),
  key idx_sys_operation_log_created_at (created_at)
);

create table if not exists sys_job (
  id bigint primary key auto_increment,
  job_name varchar(64) not null,
  job_code varchar(64) not null,
  cron_expression varchar(64) not null,
  bean_name varchar(128) not null,
  method_name varchar(128) not null,
  params varchar(1024) null,
  status tinyint not null default 1,
  remark varchar(255) null,
  deleted tinyint not null default 0,
  created_at datetime not null default current_timestamp,
  updated_at datetime not null default current_timestamp on update current_timestamp,
  unique key uk_sys_job_code (job_code)
);

create table if not exists sys_job_run_log (
  id bigint primary key auto_increment,
  job_id bigint not null,
  job_name varchar(64) not null,
  status varchar(16) not null,
  started_at datetime not null,
  finished_at datetime null,
  cost_ms bigint not null default 0,
  error_message varchar(512) null,
  key idx_sys_job_run_log_job (job_id),
  key idx_sys_job_run_log_started (started_at)
);

create table if not exists sys_db_backup (
  id bigint primary key auto_increment,
  backup_name varchar(128) not null,
  file_path varchar(512) not null,
  file_size bigint not null default 0,
  status varchar(16) not null,
  created_by bigint not null,
  remark varchar(255) null,
  created_at datetime not null default current_timestamp,
  key idx_sys_db_backup_created_at (created_at)
);

create table if not exists sys_config (
  id bigint primary key auto_increment,
  config_name varchar(64) not null,
  config_key varchar(128) not null,
  config_value varchar(1024) not null,
  group_code varchar(64) not null,
  is_sensitive tinyint not null default 0,
  builtin tinyint not null default 0,
  status tinyint not null default 1,
  remark varchar(255) null,
  deleted tinyint not null default 0,
  created_at datetime not null default current_timestamp,
  updated_at datetime not null default current_timestamp on update current_timestamp,
  unique key uk_sys_config_key (config_key),
  key idx_sys_config_group (group_code)
);
