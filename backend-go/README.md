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
go run ./cmd/server
```

默认配置与 Java 后端对齐：

- API 基础路径：`/api`
- 服务端口：`9001`
- MySQL：`localhost:3307/drip-manager`
- Redis：`localhost:6379`
- Token Header：`Authorization`

## 数据库

Go 后端不维护表结构，不在启动时执行建表或迁移。初始化数据库只使用根目录脚本：

```bash
python ../scripts/db/manage_database.py apply ../scripts/db/schema.sql
```

## 测试

```bash
cd backend-go
go test ./...
```

契约测试覆盖路由、响应结构、权限码、Long 字符串序列化和数据库维护边界。
