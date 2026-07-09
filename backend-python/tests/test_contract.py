from pathlib import Path

from fastapi.testclient import TestClient

from app.config.settings import FileSettings, JobSettings, MysqlSettings, RedisSettings, ServerSettings, Settings, TokenSettings
from app.main import create_app


def test_public_response_contract() -> None:
    settings = Settings(
        server=ServerSettings(),
        mysql=MysqlSettings(host="localhost", port=3307, database="drip-manager", username="root", password="root"),
        redis=RedisSettings(),
        token=TokenSettings(),
        job=JobSettings(),
        file=FileSettings(),
    )
    app = create_app(settings)
    client = TestClient(app)

    response = client.get("/api/health")

    assert response.status_code == 200
    assert response.json() == {"code": 0, "message": "success", "data": {"status": "UP"}}
    assert "/api/system/publicConfig" in app.openapi()["paths"]


def test_python_layered_structure_contract() -> None:
    system = Path(__file__).parents[1] / "app" / "modules" / "system"
    for directory in ("controller", "dto", "entity", "service", "vo"):
        assert (system / directory).is_dir()
    assert not (system / "models.py").exists()
    assert not (system / "services.py").exists()


def test_database_contract_has_no_schema_mutation() -> None:
    root = Path(__file__).parents[1]
    text = "\n".join(path.read_text(encoding="utf-8") for path in (root / "app").rglob("*.py"))

    assert "create_all" not in text
    assert "CREATE TABLE" not in text
    assert not (root / "migrations").exists()
