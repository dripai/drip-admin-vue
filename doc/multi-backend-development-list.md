# 多后端开发清单

本清单用于按 Java 基准后端扩展 Go、Python、Rust 三种后端。所有后端必须保持 Java 后端的 API 路径、HTTP Method、请求参数、响应结构、权限码、分页结构、异常码和 Long 字符串序列化一致。

## 总体顺序

1. Go 后端：优先实现，验证多后端契约和目录映射。
2. Python 后端：第二实现，适合快速补齐接口和测试。
3. Rust 后端：第三实现，在契约稳定后推进强类型实现。

## 全局前置清单

- [x] 从 Java Controller 提取接口契约：路径、Method、Path 参数、Query 参数、Body、响应 VO、权限码。
- [x] 从 Java DTO 提取校验契约：必填、长度、枚举、正则、分页默认值。
- [x] 从 Java `GlobalExceptionHandler` 提取异常码和 HTTP 状态码映射。
- [x] 从 Java `ApiResponse`、`PageResult`、`JacksonConfig` 提取响应结构、分页结构和 Long 字符串序列化规则。
- [x] 从 `scripts/db/schema.sql` 提取表结构、字段类型、索引、种子数据和菜单权限码。
- [x] 明确所有语言后端都不创建 `migrations/` 目录，不在启动逻辑中执行建表、改表、删表。

## Go 后端开发清单

技术栈：Gin、GORM、GORM MySQL Driver、go-redis、Viper、zap、Gin binding validator、swaggo/swag、excelize、testing、testify。

状态：已完成首版实现，代码位于 `backend-go/`，并已通过 `go test ./...`。

1. 工程骨架
   - [x] 创建 `backend-go/`。
   - [x] 创建 `cmd/server/`、`internal/common/`、`internal/config/`、`internal/infrastructure/`、`internal/modules/system/`。
   - [x] 初始化 `go.mod`，锁定 Gin、GORM、MySQL driver、go-redis、Viper、zap、swaggo、excelize、testify。
   - [x] 建立 `cmd/server/main.go`，统一挂载 `/api`。
   - [x] 建立配置加载，默认对齐 Java：端口 `9001`、MySQL、Redis、Token、任务脚本目录。

2. 公共契约
   - [x] 实现 `ApiResponse{code,message,data}`，成功固定 `code=0`、`message=success`。
   - [x] 实现失败响应 `data=null`。
   - [x] 实现 `PageQuery`：`page` 空值默认 `1`，`pageSize` 空值默认 `10`，非法值返回 `400000`。
   - [x] 实现 `PageResult{list,total,page,pageSize}`，`total` 输出为字符串。
   - [x] 实现 ID、父 ID、总数等 int64 字段 JSON 字符串序列化。
   - [x] 实现统一异常码：`400000`、`401000`、`403000`、`404000`、`409000`、`500000`。

3. 基础设施
   - [x] MySQL 连接池使用 GORM，禁止 `AutoMigrate`。
   - [x] Redis 客户端用于登录会话、在线用户、登录失败次数和缓存。
   - [x] zap 日志接入请求日志、错误日志和操作日志写入。
   - [x] 实现应用分配 ID，不依赖数据库 `AUTO_INCREMENT`。
   - [x] 实现文件上传存储，接口字段对齐 Java。
   - [x] 实现定时任务脚本执行器，脚本目录对齐 Java `../scripts`。

4. 认证与权限
   - [x] 实现 `/api/system/login`、`/api/system/logout`、`/api/system/me`。
   - [x] 实现 `/api/system/password`、`/api/system/profile`。
   - [x] Token 名称固定为 `Authorization`，token 风格为 uuid。
   - [x] 实现超时 `28800` 秒、活跃超时 `1800` 秒、不允许并发登录、不共享 token。
   - [x] 实现 `SUPER_ADMIN` 拥有所有启用菜单权限码。
   - [x] 实现 Gin 权限中间件，权限码完全复用 Java `@RequirePermission`。

5. 系统模块
   - [x] 健康检查和根路径：`/api/health`、`/api/`、`/api/favicon.ico`。
   - [x] 菜单模块：列表树、创建、更新、删除、状态切换。
   - [x] 角色模块：分页、详情、用户列表、权限详情、选项、创建、更新、删除、状态、授权。
   - [x] 部门模块：树、详情、创建、更新、删除、状态。
   - [x] 用户模块：分页、详情、创建、更新、删除、状态、解锁、分配角色、重置密码。
   - [x] 配置模块：分页、公开配置、创建、更新、删除、状态。
   - [x] 字典模块：字典类型、字典项、状态、缓存刷新。
   - [x] 在线用户模块：分页、详情、踢出。
   - [x] 日志模块：登录日志、操作日志分页和详情。
   - [x] 定时任务模块：任务分页、详情、脚本列表、创建、更新、删除、状态、手工运行、运行日志。
   - [x] 文件模块：上传。
   - [x] 打印模板模块：分页、详情、创建、复制、更新、删除、状态。

6. 导出与文档
   - [x] 实现 Excel 导出，列标题允许请求传入，服务端校验列 `key`。
   - [x] 实现 OpenAPI 文档，接口路径和模型与 Java 对齐。
   - [x] 增加 Go README，说明启动、配置、测试和数据库脚本使用方式。

7. 测试验收
   - [x] 路由契约测试：所有路径和 Method 与 Java 一致。
   - [x] 权限契约测试：所有权限码与 Java 一致。
   - [x] 响应契约测试：成功、失败、分页、Long 字符串序列化一致。
   - [x] 数据库契约测试：无 `AutoMigrate`、无 DDL、无 `migrations/`。
   - [x] 系统模块接口测试覆盖认证、权限、用户、角色、菜单、配置、字典、任务、日志、打印模板。

## Python 后端开发清单

技术栈：FastAPI、Uvicorn、SQLAlchemy 2 async、asyncmy、redis-py asyncio、Pydantic v2、pydantic-settings、FastAPI OpenAPI、openpyxl、pytest、httpx。

1. 工程骨架
   - [x] 创建 `backend-python/`。
   - [x] 创建 `app/common/`、`app/config/`、`app/infrastructure/`、`app/modules/system/`。
   - [x] 初始化 Python 项目依赖，锁定 FastAPI、Uvicorn、SQLAlchemy 2、asyncmy、redis、Pydantic、openpyxl、pytest、httpx。
   - [x] 建立 ASGI 入口，统一挂载 `/api`。
   - [x] 建立配置加载，默认对齐 Java：端口 `9001`、MySQL、Redis、Token、任务脚本目录。

2. 公共契约
   - [x] 实现 `ApiResponse` Pydantic 模型，字段固定为 `code`、`message`、`data`。
   - [x] 实现失败响应 `data=None` 并序列化为 JSON `null`。
   - [x] 实现 `PageQuery`：空值默认，非法值返回 `400000`。
   - [x] 实现 `PageResult`：`list`、`total`、`page`、`pageSize`。
   - [x] 实现 ID、父 ID、总数等整型字段输出为字符串。
   - [x] 实现统一异常处理和 HTTP 状态码映射。

3. 基础设施
   - [x] SQLAlchemy 2 async 连接 MySQL，禁止 `Base.metadata.create_all()`。
   - [x] redis-py asyncio 管理会话、在线用户、登录失败次数和缓存。
   - [x] 接入结构化日志和操作日志写入。
   - [x] 实现应用分配 ID，不依赖数据库 `AUTO_INCREMENT`。
   - [x] 实现文件上传存储。
   - [x] 实现定时任务脚本执行器。

4. 认证与权限
   - [x] 实现 `/api/system/login`、`/api/system/logout`、`/api/system/me`。
   - [x] 实现 `/api/system/password`、`/api/system/profile`。
   - [x] Token 名称固定为 `Authorization`，token 风格为 uuid。
   - [x] 实现超时 `28800` 秒、活跃超时 `1800` 秒、不允许并发登录、不共享 token。
   - [x] 实现 `SUPER_ADMIN` 权限规则。
   - [x] 实现 FastAPI dependency 权限校验，权限码完全复用 Java。

5. 系统模块
   - [x] 健康检查和根路径。
   - [x] 菜单模块。
   - [x] 角色模块。
   - [x] 部门模块。
   - [x] 用户模块。
   - [x] 配置模块。
   - [x] 字典模块。
   - [x] 在线用户模块。
   - [x] 登录日志和操作日志模块。
   - [x] 定时任务和运行日志模块。
   - [x] 文件上传模块。
   - [x] 打印模板模块。

6. 导出与文档
   - [x] 使用 openpyxl 实现 Excel 导出，列标题允许请求传入，服务端校验列 `key`。
   - [x] 使用 FastAPI OpenAPI 输出接口文档，字段和响应模型与 Java 对齐。
   - [x] 增加 Python README，说明启动、配置、测试和数据库脚本使用方式。

7. 测试验收
   - [x] pytest + httpx 路由契约测试。
   - [x] 权限码契约测试。
   - [x] 成功、失败、分页、Long 字符串序列化契约测试。
   - [x] 数据库契约测试：无 `create_all`、无 DDL、无 `migrations/`。
   - [x] 系统模块接口测试覆盖核心业务。

## Rust 后端开发清单

技术栈：Axum、Tokio、Tower、Rbatis、rbdc-mysql、deadpool-redis、config、tracing、tracing-subscriber、validator、utoipa、utoipa-swagger-ui、rust_xlsxwriter、cargo test。

状态：已完成首版工程骨架、公共契约、分层文件结构、全量路由挂载、Redis 会话基础设施、配置模块、字典模块、部门模块和契约测试；其余业务服务仍需继续按模块接入数据库和完整业务语义。

1. 工程骨架
   - [x] 创建 `backend-rust/`。
   - [x] 创建 `src/common/`、`src/config/`、`src/infrastructure/`、`src/modules/system/`。
   - [x] 初始化 Cargo 项目，锁定 Axum、Tokio、Tower、Rbatis、rbdc-mysql、Redis、config、tracing、validator、utoipa、rust_xlsxwriter。
   - [x] 建立服务入口，统一挂载 `/api`。
   - [x] 建立配置加载，默认对齐 Java：端口 `9001`、MySQL、Redis、Token、任务脚本目录。

2. 公共契约
   - [x] 实现 `ApiResponse<T>`，字段固定为 `code`、`message`、`data`。
   - [x] 失败响应使用 `Option<T>::None` 序列化为 JSON `null`。
   - [x] 实现 `PageQuery`：空值默认，非法值返回 `400000`。
   - [x] 实现 `PageResult<T>`：`list`、`total`、`page`、`pageSize`。
   - [x] 实现 ID、父 ID、总数等 i64 字段输出为字符串。
   - [x] 实现统一错误类型和 HTTP 状态码映射。

3. 基础设施
   - [x] Rbatis 连接 MySQL，禁止引入 ORM migration、建表或改表逻辑。
   - [x] deadpool-redis 管理会话、在线用户、登录失败次数和缓存。
   - [ ] tracing 接入请求日志、错误日志和操作日志写入。
   - [x] 实现应用分配 ID，不依赖数据库 `AUTO_INCREMENT`。
   - [x] 实现文件上传存储。
   - [ ] 实现定时任务脚本执行器。

4. 认证与权限
   - [ ] 实现 `/api/system/login`、`/api/system/logout`、`/api/system/me`。
   - [ ] 实现 `/api/system/password`、`/api/system/profile`。
   - [x] Token 名称固定为 `Authorization`，token 风格为 uuid。
   - [x] 实现超时 `28800` 秒、活跃超时 `1800` 秒、不允许并发登录、不共享 token。
   - [ ] 实现 `SUPER_ADMIN` 权限规则。
   - [ ] 实现 Tower middleware 权限校验，权限码完全复用 Java。

5. 系统模块
   - [x] 健康检查和根路径。
   - [ ] 菜单模块。
   - [ ] 角色模块。
   - [x] 部门模块。
   - [ ] 用户模块。
   - [x] 配置模块。
   - [x] 字典模块。
   - [ ] 在线用户模块。
   - [ ] 登录日志和操作日志模块。
   - [ ] 定时任务和运行日志模块。
   - [ ] 文件上传模块。
   - [ ] 打印模板模块。

6. 导出与文档
   - [x] 使用 rust_xlsxwriter 实现 Excel 导出，列标题允许请求传入，服务端校验列 `key`。
   - [ ] 使用 utoipa 输出 OpenAPI 文档，字段和响应模型与 Java 对齐。
   - [x] 增加 Rust README，说明启动、配置、测试和数据库脚本使用方式。

7. 测试验收
   - [x] cargo test 路由契约测试。
   - [x] 权限码契约测试。
   - [x] 成功、失败、分页、Long 字符串序列化契约测试。
   - [x] 数据库契约测试：无 ORM migration、无 DDL、无 `migrations/`。
   - [ ] 系统模块接口测试覆盖核心业务。

## 完成定义

- [ ] 三种后端均可独立启动，并挂载相同 `/api` 路径。
- [ ] 三种后端均使用同一份 `scripts/db/schema.sql`，不维护后端内置迁移。
- [ ] 三种后端均通过各自契约测试，且契约测试结果能证明与 Java 后端一致。
- [ ] 前端无需改动 API 调用即可切换到任一后端。
- [ ] README 或对应后端 README 说明启动、配置、测试和数据库初始化方式。
