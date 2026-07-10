# Drip Admin Rust Backend

Rust 后端用于复刻 Java 后端的 `/api` 接口契约，目录和职责按项目多后端规范拆分。

## 技术栈

- Axum、Tokio、Tower
- Rbatis、rbdc-mysql
- deadpool-redis
- config、tracing、tracing-subscriber
- validator
- utoipa、utoipa-swagger-ui
- rust_xlsxwriter
- cargo test

## 启动

```bash
cargo run
```

默认读取 `config.yaml`。也可以通过环境变量指定配置文件：

```bash
DRIP_RUST_CONFIG=config.yaml cargo run
```

## 测试

```bash
cargo test
```

## 数据库

数据库结构只使用根目录 `scripts/db/schema.sql`。Rust 后端不维护 migration，不在启动逻辑中建表、改表或删表。
