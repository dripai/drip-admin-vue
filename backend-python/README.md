# Drip Admin Python Backend

Python implementation of the Java `/api` contract, using FastAPI, SQLAlchemy 2 async, asyncmy, and Redis.

Implemented system modules include authentication and permissions, users, roles, menus, departments, configuration,
dictionaries, online users, login and operation logs, scheduled script jobs and run logs, file uploads, print
templates, OpenAPI documentation, and the shared Excel export service.

## Prerequisites

- Python 3.11+
- MySQL and Redis configured in `config.yaml`
- Database initialized only through `../scripts/db/manage_database.py apply`

## Run

```bash
./setup.sh
```

Default startup uses the `dev` command:

```bash
./start.sh
```

Explicit commands:

```bash
./start.sh dev
./start.sh prod
./start.sh build
```

`dev` starts `uvicorn app.main:app --reload` on port `9001` by default. `prod` starts the same ASGI app without reload.
Override startup settings with environment variables such as `APP_HOST=127.0.0.1`, `APP_PORT=9002`,
`APP_MODULE=app.main:app`, `VENV_DIR=.venv`, or `PYTHON_BIN=python3`.

The API is mounted at `/api`; OpenAPI is available at `/api/v3/api-docs` and Swagger UI at `/api/swagger-ui.html`.

## Test

```powershell
pytest
ruff check app tests
```

The contract tests verify Java-compatible routes, permissions, response and pagination shapes, string serialization for
long integer fields, layered module structure, and ORM mappings against `scripts/db/schema.sql`.

## Runtime Rules

- The service never creates or migrates database schema. `scripts/db/schema.sql` remains the only schema and seed-data source.
- Session tokens use the `Authorization` header, UUID values, an eight-hour absolute timeout, and a 30-minute active timeout.
- File size and extension limits come from `upload.maxSizeBytes` and `upload.allowedExtensions` in `sys_config`.
- Job executors support Python and operating-system script types only; Java execution is not supported.
