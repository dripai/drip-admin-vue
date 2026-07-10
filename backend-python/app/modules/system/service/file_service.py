from fastapi import UploadFile
from sqlalchemy.ext.asyncio import AsyncSession

from app.infrastructure.file_storage import FileStorage
from app.modules.system.service.config_service import ConfigService


class FileService:
    def __init__(self, db: AsyncSession, upload_dir: str) -> None:
        self.config = ConfigService(db)
        self.upload_dir = upload_dir

    async def upload(self, file: UploadFile) -> dict[str, str]:
        max_size = await self.config.required_int_config("upload.maxSizeBytes")
        extensions = await self.config.required_config("upload.allowedExtensions")
        allowed = {item.strip().lower().lstrip(".") for item in extensions.split(",") if item.strip()}
        return await FileStorage(self.upload_dir, max_size, allowed).save(file)
