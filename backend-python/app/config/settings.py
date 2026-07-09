from pathlib import Path
from typing import Any

import yaml
from pydantic import BaseModel, Field
from pydantic_settings import BaseSettings, SettingsConfigDict


class ServerSettings(BaseModel):
    host: str = "0.0.0.0"
    port: int = 9001


class MysqlSettings(BaseModel):
    host: str
    port: int
    database: str
    username: str
    password: str

    @property
    def dsn(self) -> str:
        return f"mysql+asyncmy://{self.username}:{self.password}@{self.host}:{self.port}/{self.database}?charset=utf8mb4"


class RedisSettings(BaseModel):
    host: str = "localhost"
    port: int = 6379
    password: str = ""
    db: int = 0

    @property
    def url(self) -> str:
        credential = f":{self.password}@" if self.password else ""
        return f"redis://{credential}{self.host}:{self.port}/{self.db}"


class TokenSettings(BaseModel):
    name: str = "Authorization"
    timeout_seconds: int = Field(28800, alias="timeout-seconds")
    active_timeout_seconds: int = Field(1800, alias="active-timeout-seconds")


class JobSettings(BaseModel):
    script_dir: str = Field("../scripts", alias="script-dir")


class FileSettings(BaseModel):
    upload_dir: str = Field("../uploads", alias="upload-dir")


class Settings(BaseSettings):
    model_config = SettingsConfigDict(env_prefix="DRIP_PY_", env_nested_delimiter="__", extra="ignore")

    server: ServerSettings = Field(default_factory=ServerSettings)
    mysql: MysqlSettings
    redis: RedisSettings = Field(default_factory=RedisSettings)
    token: TokenSettings = Field(default_factory=TokenSettings)
    job: JobSettings = Field(default_factory=JobSettings)
    file: FileSettings = Field(default_factory=FileSettings)


def load_settings(config_path: str | Path | None = None) -> Settings:
    path = Path(config_path or Path.cwd() / "config.yaml")
    if not path.is_file():
        raise RuntimeError(f"config file not found: {path}")
    data: dict[str, Any] = yaml.safe_load(path.read_text(encoding="utf-8")) or {}
    return Settings.model_validate(data)

