# Drip Admin Go Backend

Go 后端是 Java 后端的同契约实现，必须保持 `/api` 路径、请求字段、响应结构、权限码、分页结构、异常码和 Long 字符串序列化一致。

## 技术栈

- Gin
- GORM
- GORM MySQL Driver
- go-redis
- Viper
- zap
- Gin binding validator
- swaggo/gin-swagger
- excelize
- testing
- testify

## 目录

```text
backend-go/
├── cmd/server/
├── internal/common/
├── internal/config/
├── internal/infrastructure/
└── internal/modules/system/
```

## 启动

```bash
cd backend-go
./start.sh dev
./start.sh build
./start.sh prod
```

默认读取当前目录下的 `config.yaml`，也可以通过环境变量指定：

```bash
DRIP_GO_CONFIG=/path/to/config.yaml ./start.sh dev
```

`config.yaml` 中的默认配置与 Java 后端对齐：

- API 基础路径：`/api`
- 服务端口：`9001`
- 日志级别：`info`，可通过 `logging.level` 或 `DRIP_GO_LOGGING_LEVEL` 调整
- MySQL：`localhost:3307/drip-manager`
- Redis：`localhost:6379`
- Token Header：`Authorization`

## 数据库

Go 后端不维护表结构，不在启动时执行建表或迁移。初始化数据库只使用根目录脚本：

```bash
python ../scripts/db/manage_database.py apply ../scripts/db/schema.sql
```

## OpenAPI

OpenAPI 文档由 controller 注释通过 swaggo 生成，不在 service 中手写路径。访问地址：

- `/api/v3/api-docs`
- `/api/swagger-ui.html`
- `/api/swagger-ui/index.html`

接口注释调整后重新生成：

```bash
go run github.com/swaggo/swag/cmd/swag@v1.8.12 init -g cmd/server/main.go -o docs --parseInternal
```

## 测试

```bash
cd backend-go
go test ./...
```

契约测试覆盖路由、响应结构、权限码、Long 字符串序列化和数据库维护边界。
