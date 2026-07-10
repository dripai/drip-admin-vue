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

```powershell
python -m venv .venv
.\.venv\Scripts\Activate.ps1
pip install -e ".[dev]"
uvicorn app.main:app --host 0.0.0.0 --port 9001
```

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
