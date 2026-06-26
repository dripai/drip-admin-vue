# 后端系统设计提示词模板

下面提示词用于让 AI 生成一套完整、可落地、精简实用的后端系统设计方案。适用于后台管理系统、业务中台、运营后台、权限系统、配置系统、数据管理平台等后端服务设计。

```text
你是一名资深后端架构师、系统设计专家和企业级后台系统工程师。请基于当前主流后端工程实践，为我设计一套“后台管理通用框架后端系统”。

一、总体目标

设计一套精简、实用、可复用、可扩展的后台管理系统后端框架，用于承载企业内部系统、数据管理平台、运营后台、权限系统、业务配置平台等场景。

系统设计要求：
1. 功能描述必须完整、具体、可落地，不要大而空洞的架构描述。
2. 后端接口必须围绕真实后台管理业务场景设计。
3. 不设计无业务价值的模块、接口、字段、定时任务或复杂中间件。
4. 不保留过时兼容参数、备用请求格式、隐式 fallback 查询路径。
5. 重复能力必须抽象为通用模块、基础服务、公共组件或中间件。
6. 权限、用户、角色、菜单、部门、字典、日志、系统配置等基础能力必须完整。
7. 所有接口需要明确请求参数、响应结构、权限点、校验规则、错误场景。
8. 系统边界清晰，不把所有业务都塞进一个超大服务或超大表。
9. 缺少必要依赖、表、配置或数据层时，返回明确错误或空数据，不要静默降级。
10. 设计结果应能直接指导后端开发、接口联调和数据库建模。

二、技术栈约束

请默认使用一种清晰、主流、适合企业后台的技术栈，并说明选择理由。

固定技术栈：
- JDK 25
- Spring Boot 4.1.x
- Sa-Token 1.45.x，使用 `sa-token-spring-boot4-starter`，用于登录认证、接口鉴权、角色权限、按钮权限、会话管理和强制下线
- MyBatis-Plus 3.5.x，使用 `mybatis-plus-spring-boot4-starter`，用于 ORM、分页、条件构造、通用 CRUD 和 Mapper 扩展
- MySQL 8.0，固定作为业务数据库
- Redis，用于 token 会话、验证码、限流、在线用户和必要缓存
- Flyway，用于数据库版本管理
- OpenAPI / Swagger，用于接口文档
- JUnit 5，用于单元测试

技术栈约束：
1. 不使用 Spring Security。
2. 不使用 JPA。
3. 数据库固定使用 MySQL 8.0，不同时兼容 MySQL 和 PostgreSQL。
4. 不引入 Testcontainers。
5. 不设计多套鉴权框架、多套 ORM 或多套数据库适配分支。
6. 如果依赖版本不兼容或缺少必要依赖，必须返回明确错误，不要切换到替代技术栈。

说明：
- Spring Boot 4 已提供 Java 25 一等支持。
- Sa-Token 1.45.x 已提供 Spring Boot 4 相关 starter / common 依赖，可用于 Spring Boot 4 项目。
- MyBatis-Plus 3.5.x 已提供 Spring Boot 4 starter，可用于 Spring Boot 4 项目。
- MyBatis-Plus 支持多种数据库，但为了减少兼容分支，本模板固定使用 MySQL 8.0。

本地开发数据库连接约定：
- 数据库类型：MySQL 8.0。
- 主机：localhost。
- 端口：3307。
- 数据库名：drip-manager。
- 用户名：root。
- 密码：root。
- JDBC URL：`jdbc:mysql://localhost:3307/drip-manager?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true`。
- 本地开发环境 `application-dev.yml` 必须使用以上连接信息。
- 该连接信息用于 AI 在本地开发阶段自动创建数据库表、执行 Flyway 迁移、写入初始化数据和验证表结构。
- 数据库表结构必须通过 Flyway 迁移脚本管理，不允许绕过迁移脚本在业务代码中隐式建表或改表。
- 生产环境不得硬编码本地数据库账号密码，必须通过环境变量或部署配置注入。

三、MyBatis-Plus SQL 编写规范

JDK 25 已支持 Java text block。后端项目默认不使用 XML Mapper，SQL 统一放在 Java 代码中，避免 XML 和 Java 双入口维护。

规范：
1. 基础 CRUD 使用 MyBatis-Plus BaseMapper、IService、LambdaQueryWrapper、LambdaUpdateWrapper。
2. 简单自定义 SQL 使用 Mapper 接口注解，例如 `@Select`、`@Update`、`@Insert`、`@Delete`。
3. 多行 SQL 使用 Java text block 编写，保持 SQL 可读。
4. 动态 SQL 不在注解里拼接复杂字符串，优先使用 MyBatis-Plus Wrapper。
5. Wrapper 无法表达的复杂动态 SQL 使用 `@SelectProvider`、`@UpdateProvider` 等 Java Provider 类。
6. 不生成 `src/main/resources/mapper/*.xml`。
7. 不在 SQL 中使用 `${}` 拼接用户输入。
8. 排序字段、表名、列名等必须使用后端白名单映射，不能直接接收前端原始字段拼接 SQL。

示例：
```java
@Select("""
    select
      id, username, real_name, phone, status, created_at
    from sys_user
    where deleted = 0
      and username = #{username}
    """)
UserEntity findActiveByUsername(@Param("username") String username);
```

四、架构分层

请设计清晰的后端分层结构：

1. Controller 层
   - 只处理 HTTP 入参、权限注解、响应封装。
   - 不写复杂业务逻辑。
   - 每个接口都要有明确路径、方法、参数和返回值。

2. Application / Service 层
   - 承载业务编排。
   - 控制事务边界。
   - 调用领域服务、Repository、外部服务。
   - 不直接拼接 SQL。

3. Domain 层
   - 定义核心业务对象、枚举、领域规则。
   - 权限、用户状态、菜单类型、日志类型等规则应集中表达。

4. Repository / Mapper 层
   - 负责数据库访问。
   - 查询条件清晰。
   - 不做业务决策。

5. Infrastructure 层
   - 统一封装 Redis、对象存储、消息队列、邮件短信、第三方接口等基础设施。
   - 没有明确业务需要时，不要引入额外中间件。

五、核心模块

请至少设计以下后端模块，并为每个模块说明：
- 模块职责
- 数据表
- 核心字段
- 接口列表
- 请求参数
- 响应结构
- 权限点
- 校验规则
- 错误场景
- 事务边界
- 是否需要登录日志或业务操作日志

1. 认证模块

功能：
- 登录
- 退出登录
- 获取当前用户信息
- Sa-Token 会话续期策略
- 修改当前用户密码
- 获取当前用户菜单和权限码

要求：
- 默认不使用 access token + refresh token 双 token 机制。
- 使用 Sa-Token 单 token 会话机制，支持活跃续期、最大会话时长、在线用户查看和强制下线。
- 支持设备类型 deviceType，用于区分电脑后台、移动端、条码枪 / PDA 或其他客户端。
- 登录请求必须携带 deviceType，deviceType 由客户端自动设置或由具体客户端固定写入，不允许用户手动选择。
- deviceType 是客户端上报的字符串，不固定为后端枚举。
- 后端只校验 deviceType 非空、长度和字符安全，不将 deviceType 作为权限判断依据。
- 登录成功返回 token、expireAt、idleTimeout、maxSessionDuration、deviceType。
- 有效业务请求触发活跃续期，用户持续操作时保持在线。
- 超过 idleTimeout 未操作则会话过期。
- 超过 maxSessionDuration 必须重新登录，不允许无限续期。
- 前端收到 401 后清理登录态并跳转登录页。
- `GET /api/auth/me` 一次性返回当前用户基础信息、角色编码列表、菜单树和权限码列表，作为前端初始化权限上下文的唯一接口。
- 密码必须加密存储，不允许明文。
- 登录失败要记录失败原因，但响应不能泄露敏感信息。
- 禁用用户不能登录。
- 删除用户不能登录。
- token 过期返回 401。
- 无权限访问返回 403。

接口示例：
- POST /api/auth/login
- POST /api/auth/logout
- GET /api/auth/me
- PUT /api/auth/password

2. 用户管理模块

功能：
- 用户分页列表
- 用户详情
- 新增用户
- 编辑用户
- 删除用户
- 启用 / 禁用用户
- 重置密码
- 分配角色
- 按部门查询用户

查询条件：
- 用户名
- 姓名
- 手机号
- 状态
- 角色 ID
- 部门 ID
- 创建时间范围

用户字段：
- id
- username
- realName
- phone
- email
- status
- deptId
- remark
- createdAt
- updatedAt
- lastLoginAt

权限点示例：
- system:user:list
- system:user:detail
- system:user:create
- system:user:update
- system:user:delete
- system:user:disable
- system:user:reset-password
- system:user:assign-role

校验要求：
- username 全局唯一。
- phone 格式合法，可按产品要求决定是否唯一。
- email 格式合法。
- 不能删除当前登录用户。
- 不能禁用当前登录用户。
- 普通管理员不能操作超级管理员。

3. 角色管理模块

功能：
- 角色分页列表
- 角色详情
- 新增角色
- 编辑角色
- 删除角色
- 启用 / 禁用角色
- 配置菜单权限
- 配置按钮权限
- 查看角色关联用户

角色字段：
- id
- roleName
- roleCode
- status
- remark
- createdAt
- updatedAt

权限点示例：
- system:role:list
- system:role:create
- system:role:update
- system:role:delete
- system:role:permission

校验要求：
- roleCode 全局唯一。
- 内置角色禁止删除。
- 已分配用户的角色删除前必须明确处理策略。
- 权限保存必须使用权限 ID 数组，不允许传权限名称。

4. 菜单与权限模块

功能：
- 菜单树查询
- 新增目录
- 新增菜单
- 新增按钮权限
- 编辑菜单
- 删除菜单
- 调整排序
- 启用 / 禁用

菜单字段：
- id
- parentId
- name
- type
- path
- component
- permissionCode
- icon
- sort
- visible
- status
- createdAt
- updatedAt

菜单类型：
- DIRECTORY
- MENU
- BUTTON

规则：
- BUTTON 类型不出现在左侧菜单。
- 删除父级菜单前必须校验是否存在子节点。
- permissionCode 需要唯一。
- 菜单树必须按 sort 升序返回。

5. 部门管理模块

功能：
- 部门树
- 部门详情
- 新增部门
- 编辑部门
- 删除部门
- 启用 / 禁用部门
- 调整上级部门

部门字段：
- id
- parentId
- deptName
- deptCode
- leaderUserId
- sort
- status
- createdAt
- updatedAt

规则：
- 不能把部门移动到自己的子部门下。
- 存在子部门时不能直接删除。
- 存在用户时不能直接删除。

6. 字典管理模块

功能：
- 字典类型分页列表
- 字典项列表
- 新增字典类型
- 编辑字典类型
- 删除字典类型
- 新增字典项
- 编辑字典项
- 删除字典项
- 启用 / 禁用字典项
- 字典缓存刷新

字典类型字段：
- id
- dictName
- dictCode
- status
- remark

字典项字段：
- id
- dictTypeId
- label
- value
- color
- sort
- status

规则：
- dictCode 全局唯一。
- 同一字典类型下 value 唯一。
- 被业务引用的字典项删除前必须明确返回错误。

7. 日志管理模块

功能：
- 记录登录日志
- 登录日志分页查询
- 登录日志详情
- 记录业务操作日志
- 业务操作日志分页查询
- 业务操作日志详情
- 按操作人、模块、类型、状态、时间范围查询业务操作日志

登录日志字段：
- id
- userId
- username
- realName
- loginType
- status
- failureReason
- ip
- userAgent
- loginAt

业务操作日志字段：
- id
- operatorId
- operatorName
- module
- action
- method
- path
- requestParams
- responseStatus
- errorMessage
- costMs
- createdAt

要求：
- 日志只读，不提供普通删除接口。
- 登录、登出、登录失败写入登录日志表。
- 新增、编辑、删除、启用、禁用、授权、重置密码、备份、恢复、手动执行任务等关键变更写入业务操作日志表。
- 查询、详情、列表刷新等高频只读访问不写入数据库日志表，可写入文件日志。
- 敏感字段如 password、token、secret 必须脱敏。
- 业务操作日志承担关键操作追踪职责，不单独设计重复的日志分析模块。

8. 系统配置模块

功能：
- 配置分页列表
- 新增配置
- 编辑配置
- 删除配置
- 启用 / 禁用配置
- 按分组查询配置

配置字段：
- id
- configName
- configKey
- configValue
- groupCode
- sensitive
- builtin
- status
- remark
- updatedAt

规则：
- configKey 全局唯一。
- builtin 配置禁止删除。
- sensitive 配置查询时默认脱敏。
- 更新配置后刷新配置缓存。

9. 在线用户模块

功能：
- 在线用户分页列表
- 在线用户详情
- 强制用户下线

字段：
- userId
- username
- realName
- tokenId
- deviceType
- ip
- userAgent
- loginAt
- lastActiveAt
- expireAt

规则：
- 在线用户数据来自 Sa-Token 会话和 Redis。
- 在线用户列表必须直接展示客户端上报的 deviceType 原始值，不做额外文案映射。
- 强制下线必须校验权限。
- 默认不允许强制下线当前登录用户。
- token 失效或用户退出后不应继续出现在在线用户列表。

10. 定时任务模块

功能：
- 定时任务分页列表
- 定时任务详情
- 新增任务
- 编辑任务
- 删除任务
- 启用 / 禁用任务
- 手动执行任务
- 执行记录查询

任务字段：
- id
- jobName
- jobCode
- cronExpression
- beanName
- methodName
- params
- status
- remark
- createdAt
- updatedAt

执行记录字段：
- id
- jobId
- jobName
- status
- startedAt
- finishedAt
- costMs
- errorMessage

规则：
- jobCode 全局唯一。
- cronExpression 必须校验格式。
- 任务调用目标必须来自后端白名单，不允许前端传任意类名或脚本。
- 手动执行、删除、禁用任务必须记录业务操作日志。

11. 数据库管理模块

功能：
- 数据库备份列表
- 创建备份
- 下载备份
- 恢复备份
- 删除备份记录

备份字段：
- id
- backupName
- filePath
- fileSize
- status
- createdBy
- createdAt
- remark

规则：
- 创建备份、恢复备份、删除备份必须校验权限。
- 恢复备份属于高风险操作，必须记录业务操作日志。
- 备份文件路径不能由前端传入。
- 下载备份必须校验权限。
- 备份与恢复脚本失败时返回明确错误，不静默降级。

六、通用后端能力

必须设计以下公共能力，避免各模块重复实现。

1. 统一响应结构

成功响应：
{
  "code": 0,
  "message": "success",
  "data": {}
}

失败响应：
{
  "code": 400001,
  "message": "用户名已存在",
  "data": null
}

分页响应：
{
  "list": [],
  "total": 0,
  "page": 1,
  "pageSize": 20
}

2. 统一异常处理

异常类型：
- 参数校验异常
- 认证失败异常
- 权限不足异常
- 业务异常
- 数据不存在异常
- 数据冲突异常
- 外部服务异常
- 系统内部异常

要求：
- Controller 不手写 try-catch。
- 业务异常必须有明确错误码。
- 500 错误不能向前端暴露堆栈。

3. 参数校验

要求：
- 新增、编辑、查询参数分别定义 DTO。
- 必填字段使用校验注解。
- 字符串长度、枚举值、手机号、邮箱、时间范围都要校验。
- 分页参数统一校验 page >= 1，pageSize 有最大限制。

4. 权限控制

要求：
- 使用 Sa-Token 完成登录认证、角色校验和权限码校验。
- 支持路由 / 页面权限和按钮权限。
- 使用注解或统一拦截器校验权限码。
- 权限码命名格式为 module:resource:action。
- 超级管理员权限逻辑集中处理。
- 不要在每个接口中手写重复权限判断。
- 当前通用后台不内置数据权限。
- 涉及财务、报表、多组织数据隔离等业务时，在具体业务模块中按业务维度单独设计数据权限，不在基础 RBAC 中提前抽象 dataScope。
- 后续业务数据权限可以通过独立业务权限表、查询条件构造器或业务拦截器扩展，但不得影响当前用户、角色、菜单、按钮权限的基础模型。

5. 事务管理

要求：
- 默认启用 Spring 声明式事务管理。
- 事务边界统一放在 Application / Service 层，Controller 层不声明事务。
- 涉及新增、编辑、删除、授权、重置密码、启用 / 禁用、菜单权限保存、角色权限保存、系统配置更新、定时任务状态变更、数据库备份恢复等写操作，必须明确事务边界。
- 查询接口默认不声明事务；存在一致性读取要求的复杂查询可以使用只读事务。
- 一个业务用例应只有一个清晰的事务入口，避免多个 Service 随意嵌套事务导致边界不清。
- 事务内不执行耗时外部调用，例如远程 HTTP 请求、文件上传、邮件短信发送、长时间脚本执行。
- 数据库状态与缓存状态需要保持一致时，缓存刷新、缓存删除、消息通知等副作用应在事务提交后执行。
- 批量写操作必须明确失败策略：全部回滚或部分成功，并在接口响应中清楚表达。
- 业务异常和系统异常不能被吞掉；需要回滚的异常必须继续抛出，保证事务可以正确回滚。
- 业务操作日志应在主业务成功后记录；日志写入失败不能回滚主业务，但必须写入内部文件日志。

6. 日志记录

要求：
- 登录、登出、登录失败记录到登录日志表。
- 新增、编辑、删除、启用、禁用、授权、重置密码、备份、恢复、手动执行任务等关键变更记录到业务操作日志表。
- 查询、详情、列表刷新等高频只读访问不写入数据库日志表，可写入文件日志。
- 使用注解或拦截器记录业务操作日志。
- 可配置模块名称和操作类型。
- 自动记录操作人、请求路径、耗时、结果。
- 敏感字段脱敏。
- 日志写入失败不能影响主业务，但需要记录内部文件日志。
- 不单独设计额外日志模块，业务操作日志承担关键操作追踪职责。

7. 字典能力

要求：
- 字典类型和字典项统一维护。
- 高频字典可缓存。
- 字典缓存失效策略明确。
- 业务接口返回字典 value，前端根据字典渲染 label。

8. 文件上传能力

只有业务明确需要时才设计。

要求：
- 限制文件类型和大小。
- 返回文件 ID、URL、文件名、大小。
- 文件访问权限明确。
- 不要默认允许任意文件上传。

七、数据库设计要求

请输出完整数据库表设计，包括：
- 表名
- 表说明
- 字段名
- 字段类型
- 是否必填
- 默认值
- 索引
- 唯一约束
- 外键或逻辑关联
- 软删除策略
- 创建时间、更新时间字段

至少包含：
- sys_user
- sys_role
- sys_user_role
- sys_menu
- sys_role_menu
- sys_dept
- sys_dict_type
- sys_dict_item
- sys_login_log
- sys_operation_log
- sys_job
- sys_job_run_log
- sys_db_backup
- sys_config

数据库设计原则：
1. 主键统一使用 bigint 或 uuid，选择一种并保持一致。
2. 时间字段统一使用 created_at、updated_at。
3. 状态字段统一枚举，不混用字符串和数字含义。
4. 唯一键必须服务于真实业务约束。
5. 关联表需要联合唯一索引。
6. 常用查询条件需要索引。
7. 不要滥用 JSON 字段存核心关系数据。

八、接口设计要求

每个接口请输出：
- 接口名称
- HTTP 方法
- URL
- 权限码
- 请求参数
- 请求示例
- 响应示例
- 错误码
- 业务规则

接口路径规范：
- POST /api/auth/login
- POST /api/auth/logout
- GET /api/auth/me
- PUT /api/auth/password
- GET /api/system/users
- GET /api/system/users/{id}
- POST /api/system/users
- PUT /api/system/users/{id}
- DELETE /api/system/users/{id}
- PATCH /api/system/users/{id}/status
- PUT /api/system/users/{id}/roles
- GET /api/system/roles
- GET /api/system/roles/{id}
- POST /api/system/roles
- PUT /api/system/roles/{id}
- DELETE /api/system/roles/{id}
- PATCH /api/system/roles/{id}/status
- PUT /api/system/roles/{id}/permissions
- GET /api/system/menus
- POST /api/system/menus
- PUT /api/system/menus/{id}
- DELETE /api/system/menus/{id}
- PATCH /api/system/menus/{id}/status
- GET /api/system/depts
- POST /api/system/depts
- PUT /api/system/depts/{id}
- DELETE /api/system/depts/{id}
- PATCH /api/system/depts/{id}/status
- GET /api/system/dicts/types
- POST /api/system/dicts/types
- PUT /api/system/dicts/types/{id}
- DELETE /api/system/dicts/types/{id}
- GET /api/system/dicts/types/{id}/items
- POST /api/system/dicts/items
- PUT /api/system/dicts/items/{id}
- DELETE /api/system/dicts/items/{id}
- PATCH /api/system/dicts/items/{id}/status
- GET /api/system/configs
- POST /api/system/configs
- PUT /api/system/configs/{id}
- DELETE /api/system/configs/{id}
- PATCH /api/system/configs/{id}/status
- GET /api/system/login-logs
- GET /api/system/login-logs/{id}
- GET /api/system/operation-logs
- GET /api/system/operation-logs/{id}
- GET /api/system/online-users
- GET /api/system/online-users/{tokenId}
- POST /api/system/online-users/{tokenId}/kickout
- GET /api/system/jobs
- GET /api/system/jobs/{id}
- POST /api/system/jobs
- PUT /api/system/jobs/{id}
- DELETE /api/system/jobs/{id}
- PATCH /api/system/jobs/{id}/status
- POST /api/system/jobs/{id}/run
- GET /api/system/jobs/{id}/run-logs
- GET /api/system/database/backups
- POST /api/system/database/backups
- GET /api/system/database/backups/{id}/download
- POST /api/system/database/backups/{id}/restore
- DELETE /api/system/database/backups/{id}

规范要求：
1. 查询用 GET。
2. 新增用 POST。
3. 全量更新用 PUT。
4. 局部状态变更用 PATCH。
5. 删除用 DELETE。
6. 批量操作使用明确的 batch 路径。
7. 不要把动作藏在 query 参数中。

九、错误码设计

请设计统一错误码体系：

示例：
- 0：成功
- 400000：请求参数错误
- 401000：未登录或 token 失效
- 403000：无权限
- 404000：资源不存在
- 409000：数据冲突
- 500000：系统内部错误

业务错误示例：
- 400101：用户名已存在
- 400102：手机号格式错误
- 400201：角色编码已存在
- 400301：菜单权限标识已存在
- 400401：部门存在子节点，不能删除
- 400501：字典项被引用，不能删除

十、安全设计

必须覆盖：
1. 密码加密
2. Sa-Token 单 token 会话、活跃续期、空闲超时和最大会话时长
3. 防止越权访问
4. 参数校验
5. SQL 注入防护
6. XSS 输入处理建议
7. 敏感字段脱敏
8. 登录日志和业务操作日志
9. 登录失败限制
10. CORS 配置

不要设计：
- 明文密码
- 前端决定权限
- 接口只靠菜单隐藏做权限控制
- 日志记录完整 token 或密码
- 万能管理员绕过关键业务操作日志

十一、测试设计

请输出测试方案：

1. 单元测试
   - Service 业务规则
   - 权限判断
   - 参数校验
   - 字典缓存

2. 集成测试
   - 登录流程
   - 用户增删改查
   - 角色授权
   - 菜单树查询
   - 登录日志记录
   - 业务操作日志记录
   - 定时任务执行记录
   - 数据库备份记录

3. 接口测试
   - 正常请求
   - 参数缺失
   - 无 token
   - 无权限
   - 数据不存在
   - 数据冲突

4. 数据库测试
   - 唯一约束
   - 分页查询
   - 软删除过滤
   - 关联表唯一索引

十二、项目目录结构

请设计清晰目录结构，例如：

src/main/java/com/drip/admin/
  common/
    response/
    exception/
    validation/
    security/
    log/
  config/
  modules/
    auth/
      controller/
      service/
      dto/
      vo/
    system/
      user/
      role/
      menu/
      dept/
      dict/
      config/
      log/
      online/
      job/
      database/
  infrastructure/
    redis/
    storage/
    external/
  shared/
    enums/
    utils/

src/main/resources/
  db/migration/
  application.yml
  application-dev.yml
  application-prod.yml

十三、需要输出的最终内容

请完整输出以下内容：

1. 系统设计总览
   - 技术栈
   - 架构分层
   - 模块边界
   - 核心流程

2. 功能清单
   - 按模块列出功能、接口、字段、权限点、业务规则。

3. 数据库设计
   - 完整表结构。
   - 索引和唯一约束。
   - 初始化数据建议。

4. API 设计
   - 每个模块的接口路径、请求参数、响应示例、错误码。

5. 权限设计
   - RBAC 模型。
   - 菜单权限。
   - 按钮权限。
   - 路由与接口权限关系。

6. 公共能力设计
   - 统一响应。
   - 统一异常。
   - 参数校验。
   - 登录日志。
   - 业务操作日志。
   - 字典缓存。
   - 安全控制。

7. 关键代码示例
   - 统一响应对象。
   - 全局异常处理。
   - 权限注解或拦截器。
   - 登录接口。
   - 用户分页查询。
   - 角色授权保存。
   - 业务操作日志注解。
   - 登录日志记录。
   - Sa-Token 权限校验。

8. 测试方案
   - 单元测试。
   - 集成测试。
   - 接口测试。
   - 关键测试用例。

9. 设计约束检查
   - 明确说明不做哪些无用模块。
   - 明确说明哪些能力被封装复用。
   - 明确说明如何减少重复代码。
   - 明确说明如何避免隐式 fallback 和兼容分支。

10. 可运行交付清单
   - 项目启动方式。
   - 必要环境变量。
   - 数据库迁移脚本。
   - 初始化数据脚本。
   - Swagger / OpenAPI 访问地址。
   - 健康检查接口。
   - 本地验证步骤。

11. MVP 迭代拆分
   - P0：第一版必须完成，缺少则系统不可用。
   - P1：常用增强能力。
   - 每个优先级都必须说明对应模块、接口和验收标准。

十四、额外约束

必须避免以下问题：
1. 不要只讲微服务、DDD、高并发等概念，必须落到表、接口、字段、权限和代码结构。
2. 不要设计没有业务价值的中间件。
3. 不要生成多个兼容路径或 fallback 查询路径。
4. 不要用“大数据大屏”“智能分析”等空泛功能填充内容。
5. 不要把所有模块放到一个巨大 Controller。
6. 不要让前端决定权限和数据范围。
7. 不要每个模块重复写分页、异常、权限、日志逻辑。
8. 不要默认引入消息队列、搜索引擎、分布式事务，除非有明确业务理由。
9. 不要省略错误场景。
10. 不要省略数据库约束。

十五、可运行交付要求

最终结果必须能指导或直接生成一个可运行后台管理系统，不只停留在设计说明。

必须包含：
1. 可启动的后端项目结构。
2. 数据库迁移脚本。
3. 初始化管理员、角色、菜单、权限、部门、字典和系统配置数据。
4. 本地启动命令。
5. 必要环境变量说明。
6. 健康检查接口。
7. Swagger / OpenAPI 访问路径。
8. 至少覆盖登录、当前用户、菜单权限、用户管理、角色授权、登录日志、业务操作日志的测试用例。

第一版必须形成完整闭环：
登录 -> 获取当前用户信息 -> 获取菜单权限 -> 用户管理 -> 角色授权 -> 权限生效 -> 登录日志和业务操作日志记录。

十六、MVP 优先级

请将功能拆为：

P0：第一版必须完成
- 登录、退出登录、获取当前用户信息。
- 获取当前用户菜单树和权限码。
- 用户管理。
- 角色管理。
- 菜单与按钮权限管理。
- 角色授权后权限立即或重新登录后生效。
- 登录日志记录和查询。
- 业务操作日志记录和查询。
- 数据库迁移和初始化数据。
- 统一响应、统一异常、参数校验、权限拦截。

P1：常用增强
- 部门管理。
- 字典管理。
- 系统配置。
- 登录失败限制。
- 字典缓存。
- 在线用户管理。
- 定时任务管理。
- 数据库管理：备份与恢复。
- 文件上传基础能力预留，不设计具体业务流程。
- 批量导入 / 导出基础能力预留，不设计具体业务流程。

不单独设置 P2。后续扩展必须先明确业务场景，再进入新的迭代清单。

日志处理原则：
- 不单独设计额外日志模块或报表模块。
- 登录日志用于记录登录、登出、登录失败、IP、设备信息等会话事件。
- 业务操作日志用于记录新增、编辑、删除、启用、禁用、授权、重置密码、备份、恢复、手动执行任务等关键变更事件。
- 业务操作日志承担关键操作追踪职责。

每个优先级必须输出：
- 功能范围。
- 涉及接口。
- 涉及数据表。
- 权限点。
- 验收标准。

十七、前后端联调契约

必须明确以下接口响应结构，保证前端可以直接联调：

1. 登录响应结构
   - token。
   - expireAt。
   - idleTimeout。
   - maxSessionDuration。
   - deviceType。
   - 不返回 refreshToken。

2. 当前用户信息结构
   - 接口：GET /api/auth/me。
   - 用户 ID。
   - 用户名。
   - 姓名。
   - 头像可选。
   - 部门信息。
   - 角色列表。
   - 权限码列表。
   - 菜单树。

3. 菜单树结构
   - id。
   - parentId。
   - name。
   - path。
   - component。
   - icon。
   - sort。
   - visible。
   - permissionCode。
   - children。

4. 分页请求和响应结构
   - page。
   - pageSize。
   - total。
   - list。
   - 查询条件。

5. 字典数据结构
   - dictCode。
   - label。
   - value。
   - color。
   - sort。
   - status。

6. 错误响应结构
   - code。
   - message。
   - data。
   - requestId 可选。

7. 权限约束
   - 按钮权限由后端返回的 permissionCode 判断。
   - 前端只负责菜单、路由和按钮显示控制。
   - 后端接口必须使用 Sa-Token 校验权限码。

8. 业务操作日志结构
   - id。
   - operatorId。
   - operatorName。
   - module。
   - action。
   - method。
   - path。
   - requestParams。
   - responseStatus。
   - errorMessage。
   - costMs。
   - createdAt。

十八、验收标准

每个 P0 功能必须提供验收标准，格式如下：

- 场景。
- 前置条件。
- 操作步骤。
- 预期结果。
- 失败场景。

必须至少覆盖以下验收场景：
1. 正确账号密码可以登录。
2. 错误账号密码不能登录，响应不泄露敏感信息。
3. 禁用用户不能登录。
4. 未登录访问业务接口返回 401。
5. 无权限访问接口返回 403。
6. 当前用户可以获取自己的菜单树和权限码。
7. 角色移除菜单权限后，用户重新登录不再返回该菜单。
8. 普通管理员不能操作超级管理员。
9. 不能删除当前登录用户。
10. 删除存在子节点的菜单必须失败。
11. 关键变更操作必须写入业务操作日志。

十九、迭代闭环要求

每一轮开发或设计输出必须包含：
1. 本轮完成的功能。
2. 可运行验证方式。
3. 已通过的测试或验收用例。
4. 未完成项。
5. 下一轮最小任务。

不要一次性扩展大量非核心功能。每轮优先保证一个用户可用的业务闭环。

最终目标：
生成一套可以直接指导后端开发落地的后台管理通用框架方案，并能进一步指导生成可运行代码。要求实用、克制、结构清晰、接口完整、数据模型清晰、公共能力复用充分、验收标准明确、具备快速迭代闭环。
```
