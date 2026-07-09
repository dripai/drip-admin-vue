# Drip Admin Vue Agent Standards

本文件是本仓库的项目级 agent 标准。切换会话后，必须先按本文件执行；如果用户在当前会话给出更具体的指令，以当前会话指令为准。

## 执行规则

- 不发送可选的过程性说明；必要说明保持直接、简短。
- 修改前先查看 `git status --short --branch`，不得还原用户未明确要求还原的改动。
- 搜索文本和文件优先使用 `rg` 或 `rg --files`。
- Windows 下源码、Markdown、SQL、Vue、TypeScript、Java、Go、Rust、Python 默认按 UTF-8 处理。
- 小范围源码改动优先使用 `apply_patch`；脚本化改动必须显式使用 UTF-8 读写。
- 不用 PowerShell 重定向恢复源码，不用 `Get-Content -Encoding Default` 读取源码。
- 保持单一、当前实现路径；不得添加兼容分支、旧参数、备用请求形态、隐式 fallback 或自动降级路径。
- 必需文件、依赖、数据表或数据层缺失时，返回明确错误或空数据，不静默切换到旧路径或慢路径。

## 项目定位

Drip Admin Vue 是面向开发者的前后端分离管理系统框架。后端 API、前端管理端和移动端目录拆分维护，用作企业后台、权限系统、配置中心、定时任务、日志审计、字典管理、打印模板等业务模块的开发基础。

根目录约定：

```text
backend/   Java Spring Boot 后端，当前基准实现
frontend/  Vue 管理端
mobile/    移动端工程目录
scripts/   脚本目录
pic/       项目界面截图
```

## 当前技术栈

Java 后端：

- Java 25
- Spring Boot 4.1
- Spring Web MVC
- Spring Validation
- Spring JDBC
- Spring Data Redis
- Spring AOP
- MySQL
- MyBatis-Plus 3.5
- Sa-Token 1.45
- SpringDoc OpenAPI 3.0
- Spring Boot Admin 4.0
- EasyExcel 4.0
- Maven

前端管理端：

- Vue 3
- TypeScript
- Vite 8
- Ant Design Vue 4
- Pinia
- Vue Router
- Axios
- Sass
- vue-plugin-hiprint
- Vitest
- ESLint
- Prettier
- pnpm

## 多后端技术栈

Java 后端是接口契约源，Go、Rust、Python 后端必须复刻 Java 的 API、响应结构、权限码和业务语义。

Go 后端使用：

- Gin
- GORM
- GORM MySQL Driver
- go-redis
- Viper
- zap
- Gin binding validator
- swaggo/swag
- excelize
- testing
- testify

Rust 后端使用：

- Axum
- Tokio
- Tower
- SeaORM
- SeaORM MySQL backend
- deadpool-redis
- config
- tracing
- tracing-subscriber
- validator
- utoipa
- utoipa-swagger-ui
- cargo test

Python 后端使用：

- FastAPI
- Uvicorn
- SQLAlchemy 2 async
- asyncmy
- redis-py asyncio
- Pydantic v2
- pydantic-settings
- FastAPI OpenAPI
- openpyxl
- pytest
- httpx

## 多后端目录布局

新增或重构 Go、Rust、Python 后端时，必须以 Java 当前分层为基础做语言化映射。目录结构是强制约束，不是建议；首版实现也必须符合本节规则，不允许以“先跑通”为理由把多个层或多个业务模块写进大文件。

```text
backend-go/
├── cmd/server/
├── internal/common/
├── internal/config/
├── internal/infrastructure/
└── internal/modules/system/
    ├── controller/
    ├── dto/
    ├── entity/
    ├── service/
    ├── vo/
    └── router.go

backend-rust/
├── src/common/
├── src/config/
├── src/infrastructure/
└── src/modules/system/
    ├── controller/
    ├── dto/
    ├── entity/
    ├── service/
    ├── vo/
    └── router.rs

backend-python/
├── app/common/
├── app/config/
├── app/infrastructure/
└── app/modules/system/
    ├── controller/
    ├── dto/
    ├── entity/
    ├── service/
    ├── vo/
    └── router.py
```

Java 当前分层：

```text
backend/src/main/java/com/drip/admin/common/
backend/src/main/java/com/drip/admin/config/
backend/src/main/java/com/drip/admin/infrastructure/
backend/src/main/java/com/drip/admin/modules/system/
backend/src/main/java/com/drip/admin/shared/
```

业务模块必须继续按 `controller`、`dto`、`entity`、`mapper`、`service`、`vo` 分层组织。非 Java 后端没有独立 `mapper` 层时，数据库访问逻辑必须放在 `service` 或明确的 repository/dao 层，不得写在 controller 中。

分层职责：

- `controller`：只处理路由入参、调用 service、返回 `ApiResponse`，不写业务规则和复杂数据库查询。
- `dto`：只放请求 DTO、查询 DTO、参数校验结构。
- `entity`：只放数据库表映射结构，文件名按表命名。
- `service`：只放业务逻辑、事务、数据库查询、权限业务判断、外部资源调用。
- `vo`：只放响应对象和前端展示对象。
- `router`：只负责挂载路由和依赖装配，不写业务逻辑。

文件粒度强制规则：

- 每个业务模块必须独立文件，不得把多个业务模块合并到一个大文件。
- `controller` 按业务拆分，例如 `user_controller.go`、`role_controller.go`、`menu_controller.go`、`dept_controller.go`、`config_controller.go`、`dict_controller.go`、`job_controller.go`、`log_controller.go`、`online_user_controller.go`、`print_template_controller.go`。
- `service` 按业务拆分，例如 `user_service.go`、`role_service.go`、`menu_service.go`、`dept_service.go`、`config_service.go`。
- `dto` 按业务拆分，例如 `user_dto.go`、`role_dto.go`、`menu_dto.go`。
- `entity` 必须按表拆分，例如 `sys_user.go`、`sys_role.go`、`sys_menu.go`、`sys_dept.go`。
- `vo` 按业务拆分，例如 `user_vo.go`、`auth_vo.go`、`menu_vo.go`。

明确禁止：

- 不得出现承载所有实体的 `models.go`、`entity.go`。
- 不得出现承载多个业务模块的 `handlers.go`、`controllers.go`、`services.go`。
- 不得把 `controller`、`service`、`entity`、`dto`、`vo` 混在同一个文件。
- 不得在 `modules/system/` 根目录放主要业务实现文件；根目录只允许保留 `router`、模块装配文件和少量跨层常量。
- 不得为了减少文件数量牺牲分层结构。

结构验收：

- 新增或重构任一后端后，必须检查分层目录是否存在。
- 必须增加结构测试或静态检查，确保 `controller/`、`dto/`、`entity/`、`service/`、`vo/` 存在。
- 必须检查系统模块根目录不存在大杂烩实现文件。
- Go 后端完成验收时，`backend-go/internal/modules/system/` 根目录不得承载主要业务逻辑。

## API 契约

- API 基础路径是 `/api`，Java 后端默认端口是 `9001`。
- Java Controller 是唯一接口契约源。新增或调整接口时，先调整 Java 后端和契约测试，再同步其他语言后端。
- Go、Rust、Python 后端的 API 路径、HTTP Method、Path 参数、Query 参数、Body 字段、校验规则、HTTP 状态码、响应字段必须和 Java 后端一致。
- 权限码必须和 Java `@RequirePermission` 以及 `sys_menu.permission_code` 一致，不得按语言重新命名。
- 登录、退出、在线用户、菜单树、角色权限、操作日志、登录日志、定时任务、字典、配置、打印模板等系统模块的业务语义必须对齐 Java 后端。

统一响应结构：

```json
{
  "code": 0,
  "message": "success",
  "data": {}
}
```

失败响应必须保持：

```json
{
  "code": 400000,
  "message": "错误信息",
  "data": null
}
```

分页响应数据结构必须保持：

```json
{
  "list": [],
  "total": "0",
  "page": 1,
  "pageSize": 10
}
```

JSON 序列化规则：

- Java `Long` 和 `long` 输出为字符串；其他语言后端必须对齐，ID、父 ID、总数等长整型字段不要输出为 JSON number。
- 成功响应固定 `code = 0`、`message = "success"`。
- 失败响应固定 `data = null`。

异常码约定：

- `400000` 参数错误、请求体 JSON 错误、普通业务错误。
- `401000` 未登录或 token 失效。
- `403000` 无权限。
- `404000` 资源不存在。
- `409000` 数据冲突。
- `500000` 系统内部错误或服务端配置错误。

## 权限与会话

- Token 名称是 `Authorization`。
- Sa-Token 当前语义必须在其他语言后端中等价实现：超时时间 `28800` 秒，活跃超时 `1800` 秒，不允许并发登录，不共享 token，token 风格为 uuid。
- 当前用户、角色码、权限码、菜单树以 Java `AuthService` 语义为准。
- `SUPER_ADMIN` 拥有所有启用菜单的权限码。

## 数据库标准

- 表结构和初始化数据只由根目录 `scripts/db/schema.sql` 维护。
- 数据库维护脚本只使用 `scripts/db/manage_database.py`。
- 允许的数据库脚本命令是 `export-baseline` 和 `apply`。
- 不在任何后端源码、资源文件或启动逻辑中创建表、改表、删表或维护结构迁移。
- 不重新引入 Flyway、Liquibase、`flyway_schema_history`、ORM migrations 或后端内置迁移目录。
- Go 不使用 `AutoMigrate`。
- Rust 不使用 `sea-orm-migration`。
- Python 不使用 `Base.metadata.create_all()`。
- 当前仓库只保留一份 SQL 文件：`scripts/db/schema.sql`。
- 新增表结构或种子数据时，更新 `scripts/db/schema.sql`，再运行相关契约测试。
- MyBatis-Plus ID 策略是 `assign_id`；数据插入需要使用应用分配 ID，不使用数据库 `AUTO_INCREMENT`。

## 明确保留的产品决定

- Excel 导出列标题允许由前端请求传入；服务端继续校验列 `key`，不强制服务端绑定固定标题。
- 后端公共分页 `PageQuery` 只对空值给默认值，不静默修正非法页码或非法 `pageSize`。
- 数据库不使用 `migrations/` 目录；根目录单 SQL 文件是当前唯一结构来源。

## 测试标准

- Java 后端改动后在 `backend/` 下运行 `mvn test`。
- 不把后端契约测试重新合并成单个 `BackendContractTests`。
- 当前契约测试按职责拆分维护：
  - `common/CommonContractTests.java`
  - `contract/ControllerMappingContractTests.java`
  - `contract/DatabaseSchemaContractTests.java`
  - `contract/MybatisMappingContractTests.java`
  - `contract/SecurityContractTests.java`
  - `system/SystemModuleContractTests.java`
- 新增 Go、Rust、Python 后端时，必须增加契约测试，校验 API 路径、请求响应结构、权限码、分页结构、异常码和 Long 字符串序列化与 Java 一致。

## 文档标准

- README 面向开发者介绍项目定位、技术栈、启动方式、数据库脚本和截图。
- 涉及中文文档、SQL 种子数据或源码中文文案时，必须保持 UTF-8，不得引入 `????`、`锟`、`脙`、`脗`、`盲赂`、`氓` 等乱码。
- 技术栈和接口标准发生变化时，同步更新本文件。
