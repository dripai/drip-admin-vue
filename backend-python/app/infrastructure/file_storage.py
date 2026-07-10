from pathlib import Path
from uuid import uuid4

from fastapi import UploadFile

from app.common.errors import bad_request


class FileStorage:
    def __init__(self, upload_dir: str, max_size_bytes: int, allowed_extensions: set[str]) -> None:
        self.root = Path(upload_dir).resolve()
        self.max_size_bytes = max_size_bytes
        self.allowed_extensions = {item.lower().lstrip(".") for item in allowed_extensions}

    async def save(self, file: UploadFile) -> dict[str, str]:
        filename = Path(file.filename or "").name
        extension = Path(filename).suffix.lower().lstrip(".")
        if not filename or extension not in self.allowed_extensions:
            raise bad_request("file extension is not allowed")
        content = await file.read(self.max_size_bytes + 1)
        if not content:
            raise bad_request("file must not be empty")
        if len(content) > self.max_size_bytes:
            raise bad_request("file exceeds max upload size")
        self.root.mkdir(parents=True, exist_ok=True)
        file_id = f"local-{uuid4().hex}"
        target = self.root / f"{file_id}.{extension}"
        target.write_bytes(content)
        return {"fileId": file_id, "url": "", "fileName": filename, "size": str(len(content))}
