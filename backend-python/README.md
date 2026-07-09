# Drip Admin Python Backend

Python implementation of the Java `/api` contract, using FastAPI, SQLAlchemy 2 async, asyncmy, and Redis.

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
```

The service never creates or migrates database schema. `scripts/db/schema.sql` remains the only schema and seed-data source.
