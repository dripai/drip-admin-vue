import re
from io import BytesIO
from pathlib import Path

import pytest
from fastapi.testclient import TestClient
from openpyxl import load_workbook
from pydantic import ValidationError

from app.common.api_response import failure, success
from app.common.errors import BusinessError
from app.common.export import ExcelExportService, ExportColumn, ExportColumnRequest
from app.common.pagination import PageQuery, PageResult
from app.config.settings import (
    FileSettings,
    JobSettings,
    MysqlSettings,
    RedisSettings,
    ServerSettings,
    Settings,
    TokenSettings,
)
from app.infrastructure.script_executor import ScriptExecutor
from app.main import create_app
from app.modules.system.dto.config_request import ConfigSaveRequest
from app.modules.system.entity.base import Base
from app.modules.system.entity.sys_menu import SysMenu
from app.modules.system.service.menu_service import MenuService, _rows_with_ancestors

EXPECTED_ROUTES = {
    "/api/system/publicConfig": {"get"},
    "/api/system/login": {"post"},
    "/api/system/logout": {"post"},
    "/api/system/me": {"get"},
    "/api/system/password": {"put"},
    "/api/system/profile": {"put"},
    "/api/system/user": {"get", "post"},
    "/api/system/user/{id}": {"get", "put", "delete"},
    "/api/system/user/{id}/status": {"put"},
    "/api/system/user/{id}/unlock": {"post"},
    "/api/system/user/{id}/role": {"put"},
    "/api/system/user/{id}/resetPassword": {"post"},
    "/api/system/role": {"get", "post"},
    "/api/system/role/option": {"get"},
    "/api/system/role/{id}": {"get", "put", "delete"},
    "/api/system/role/{id}/user": {"get"},
    "/api/system/role/{id}/permission": {"get", "put"},
    "/api/system/role/{id}/status": {"put"},
    "/api/system/menu": {"get", "post"},
    "/api/system/menu/{id}": {"put", "delete"},
    "/api/system/menu/{id}/status": {"put"},
    "/api/system/dept": {"get", "post"},
    "/api/system/dept/{id}": {"get", "put", "delete"},
    "/api/system/dept/{id}/status": {"put"},
    "/api/system/config": {"get", "post"},
    "/api/system/config/{id}": {"put", "delete"},
    "/api/system/config/{id}/status": {"put"},
    "/api/system/dict/type": {"get", "post"},
    "/api/system/dict/type/{id}": {"put", "delete"},
    "/api/system/dict/type/{id}/item": {"get"},
    "/api/system/dict/item": {"post"},
    "/api/system/dict/item/{id}": {"put", "delete"},
    "/api/system/dict/item/{id}/status": {"put"},
    "/api/system/dict/cache/refresh": {"post"},
    "/api/system/onlineUser": {"get"},
    "/api/system/onlineUser/{tokenId}": {"get"},
    "/api/system/onlineUser/{tokenId}/kickout": {"post"},
    "/api/system/loginLog": {"get"},
    "/api/system/loginLog/{id}": {"get"},
    "/api/system/operationLog": {"get"},
    "/api/system/operationLog/{id}": {"get"},
    "/api/system/job": {"get", "post"},
    "/api/system/job/scripts": {"get"},
    "/api/system/job/{id}": {"get", "put", "delete"},
    "/api/system/job/{id}/status": {"put"},
    "/api/system/job/{id}/run": {"post"},
    "/api/system/job/{id}/runLog": {"get"},
    "/api/system/jobRunLog": {"get"},
    "/api/system/files": {"post"},
    "/api/system/print-template": {"get", "post"},
    "/api/system/print-template/{id}": {"get", "put", "delete"},
    "/api/system/print-template/{id}/copy": {"post"},
    "/api/system/print-template/{id}/status": {"put"},
}

EXPECTED_PERMISSIONS = {
    "system:user:list", "system:user:detail", "system:user:create", "system:user:update",
    "system:user:delete", "system:user:disable", "system:user:unlock", "system:user:assignRole",
    "system:user:resetPassword", "system:role:list", "system:role:permission", "system:role:create",
    "system:role:update", "system:role:delete", "system:menu:list", "system:menu:create",
    "system:menu:update", "system:menu:delete", "system:menu:status", "system:dept:list",
    "system:dept:create", "system:dept:update", "system:dept:delete", "system:config:list",
    "system:config:create", "system:config:update", "system:config:delete", "system:dict:list",
    "system:dict:create", "system:dict:update", "system:dict:delete", "system:online:list",
    "system:online:kickout", "system:loginLog:list", "system:operationLog:list", "system:job:list",
    "system:job:create", "system:job:update", "system:job:delete", "system:job:run",
    "system:job:history", "system:file:upload", "system:printTemplate:list",
    "system:printTemplate:create", "system:printTemplate:update", "system:printTemplate:delete",
}


@pytest.fixture
def app():
    settings = Settings(
        server=ServerSettings(),
        mysql=MysqlSettings(
            host="localhost",
            port=3307,
            database="drip-manager",
            username="root",
            password="root",
        ),
        redis=RedisSettings(),
        token=TokenSettings(),
        job=JobSettings(),
        file=FileSettings(),
    )
    return create_app(settings)


def test_route_and_openapi_contract(app) -> None:
    paths = app.openapi()["paths"]
    for path, methods in EXPECTED_ROUTES.items():
        assert path in paths, path
        assert methods <= set(paths[path]), path


def test_public_response_and_docs_contract(app) -> None:
    client = TestClient(app)
    health = client.get("/api/health")
    assert health.status_code == 200
    payload = health.json()
    assert payload["code"] == 0
    assert payload["message"] == "success"
    assert payload["data"]["status"] == "UP"
    assert payload["data"]["service"] == "drip-admin-backend"
    assert payload["data"]["timestamp"].endswith("Z")

    root = client.get("/api/", follow_redirects=False)
    assert root.status_code == 302
    assert root.headers["location"] == "swagger-ui/index.html"
    assert client.get("/api/favicon.ico").status_code == 204
    assert client.get("/api/v3/api-docs").status_code == 200
    assert client.get("/api/swagger-ui.html", follow_redirects=False).status_code == 200


def test_response_pagination_long_and_error_contract(app) -> None:
    page = PageResult(
        list=[{"id": "2070959624609583106", "parentId": "0"}],
        total="2070959624609583106",
        page=1,
        pageSize=10,
    )
    assert success(page).model_dump(by_alias=True) == {
        "code": 0,
        "message": "success",
        "data": {
            "list": [{"id": "2070959624609583106", "parentId": "0"}],
            "total": "2070959624609583106",
            "page": 1,
            "pageSize": 10,
        },
    }
    assert failure(400000, "error").model_dump() == {
        "code": 400000,
        "message": "error",
        "data": None,
    }
    with pytest.raises(BusinessError) as error:
        PageQuery(page=0, page_size=10)
    assert error.value.code == 400000

    response = TestClient(app).post(
        "/api/system/login",
        content="{",
        headers={"Content-Type": "application/json"},
    )
    assert response.status_code == 400
    assert response.json()["code"] == 400000
    assert response.json()["data"] is None


def test_permission_contract() -> None:
    controllers = Path(__file__).parents[1] / "app" / "modules" / "system" / "controller"
    source = "\n".join(path.read_text(encoding="utf-8") for path in controllers.glob("*.py"))
    for permission in EXPECTED_PERMISSIONS:
        assert f'require_permission("{permission}")' in source, permission


def test_auth_menu_tree_excludes_buttons_and_keeps_ancestors() -> None:
    rows = [
        SysMenu(
            id=1,
            parent_id=0,
            name="System",
            type="DIRECTORY",
            path="/system",
            component=None,
            permission_code="system",
            icon="settings",
            sort=1,
            visible=1,
            status=1,
            deleted=0,
        ),
        SysMenu(
            id=2,
            parent_id=1,
            name="Users",
            type="MENU",
            path="/system/user",
            component="system/user/index",
            permission_code="system:user:list",
            icon="user",
            sort=2,
            visible=1,
            status=1,
            deleted=0,
        ),
        SysMenu(
            id=3,
            parent_id=2,
            name="Create User",
            type="BUTTON",
            path=None,
            component=None,
            permission_code="system:user:create",
            icon=None,
            sort=3,
            visible=0,
            status=1,
            deleted=0,
        ),
    ]

    auth_rows = _rows_with_ancestors([3], rows)
    auth_tree = MenuService(db=None)._tree(auth_rows, include_buttons=False)  # type: ignore[arg-type]
    assert auth_tree == [
        {
            "id": "1",
            "parentId": "0",
            "name": "System",
            "type": "DIRECTORY",
            "path": "/system",
            "component": None,
            "permissionCode": "system",
            "icon": "settings",
            "sort": 1,
            "visible": 1,
            "status": 1,
            "children": [
                {
                    "id": "2",
                    "parentId": "1",
                    "name": "Users",
                    "type": "MENU",
                    "path": "/system/user",
                    "component": "system/user/index",
                    "permissionCode": "system:user:list",
                    "icon": "user",
                    "sort": 2,
                    "visible": 1,
                    "status": 1,
                    "children": [],
                }
            ],
        }
    ]

    management_tree = MenuService(db=None)._tree(rows)  # type: ignore[arg-type]
    assert management_tree[0]["children"][0]["children"][0]["type"] == "BUTTON"


def test_python_layered_structure_contract() -> None:
    system = Path(__file__).parents[1] / "app" / "modules" / "system"
    for directory in ("controller", "dto", "entity", "service", "vo"):
        assert (system / directory).is_dir()
    for name in (
        "file_controller.py", "log_controller.py", "online_user_controller.py",
        "print_template_controller.py", "file_service.py", "log_service.py",
        "online_user_service.py", "print_template_service.py", "sys_login_log.py",
        "sys_operation_log.py", "sys_print_template.py",
    ):
        assert any((system / directory / name).is_file() for directory in ("controller", "entity", "service")), name
    for forbidden in ("models.py", "services.py", "handlers.py", "controllers.py"):
        assert not (system / forbidden).exists()
    controller_source = "\n".join(
        path.read_text(encoding="utf-8") for path in (system / "controller").glob("*.py")
    )
    assert "from sqlalchemy import select" not in controller_source


def test_database_contract_matches_schema_and_has_no_mutation() -> None:
    root = Path(__file__).parents[1]
    source = "\n".join(path.read_text(encoding="utf-8") for path in (root / "app").rglob("*.py"))
    assert "create_all" not in source
    assert "CREATE TABLE" not in source
    assert "ALTER TABLE" not in source
    assert not (root / "migrations").exists()

    schema = (root.parent / "scripts" / "db" / "schema.sql").read_text(encoding="utf-8")
    tables = {
        table: set(re.findall(r"^\s*`([a-zA-Z0-9_]+)`", body, flags=re.MULTILINE))
        for table, body in re.findall(
            r"CREATE TABLE `([a-zA-Z0-9_]+)` \((.*?)\) ENGINE=",
            schema,
            flags=re.DOTALL,
        )
    }
    for table in Base.metadata.sorted_tables:
        assert table.name in tables
        assert set(table.columns.keys()) <= tables[table.name], table.name


def test_executor_config_dto_and_excel_contract(tmp_path: Path) -> None:
    (tmp_path / "backup.py").write_text("print('ok')\n", encoding="utf-8")
    executor = ScriptExecutor(str(tmp_path))
    assert executor.list_scripts("python") == ["backup.py"]
    with pytest.raises(BusinessError):
        executor.list_scripts("java")

    with pytest.raises(ValidationError):
        ConfigSaveRequest.model_validate(
            {
                "configName": "name",
                "configKey": "key",
                "configValue": "value",
                "configType": "string",
            }
        )

    data = ExcelExportService().export(
        rows=[{"name": "Alice", "age": 30}],
        max_rows=10,
        selected_columns=[
            ExportColumnRequest(key="name", title="Name"),
            ExportColumnRequest(key="age", title="Age"),
        ],
        allowed_columns={
            "name": ExportColumn(lambda row: row["name"]),
            "age": ExportColumn(lambda row: row["age"]),
        },
    )
    sheet = load_workbook(BytesIO(data)).active
    assert list(sheet.values) == [("Name", "Age"), ("Alice", 30)]
