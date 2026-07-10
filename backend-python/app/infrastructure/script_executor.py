import shlex
import subprocess
from pathlib import Path

from app.common.errors import bad_request

_EXTENSIONS = {
    "shell": {".sh"},
    "bat": {".bat", ".cmd"},
    "powershell": {".ps1"},
    "ps1": {".ps1"},
    "python": {".py"},
}


class ScriptExecutor:
    def __init__(self, script_dir: str) -> None:
        self.root = Path(script_dir).resolve()

    def list_scripts(self, executor_type: str) -> list[str]:
        executor_type = executor_type.strip().lower()
        extensions = self._extensions(executor_type)
        if not self.root.is_dir():
            return []
        return sorted(path.name for path in self.root.iterdir() if path.is_file() and path.suffix.lower() in extensions)

    def execute(self, executor_type: str, script_file: str, script_args: str | None = None) -> None:
        command = self.command(executor_type, script_file, script_args)
        subprocess.run(command, cwd=self.root, timeout=1800, check=True)

    def validate_script_file(self, executor_type: str, script_file: str) -> None:
        extensions = self._extensions(executor_type)
        self._candidate(script_file, extensions)

    def command(self, executor_type: str, script_file: str, script_args: str | None = None) -> list[str]:
        executor_type = executor_type.strip().lower()
        script = self._resolve_script(script_file, self._extensions(executor_type))
        commands = {
            "shell": ["bash", str(script)],
            "bat": ["cmd.exe", "/c", str(script)],
            "powershell": ["powershell.exe", "-ExecutionPolicy", "Bypass", "-File", str(script)],
            "ps1": ["powershell.exe", "-ExecutionPolicy", "Bypass", "-File", str(script)],
            "python": ["python", str(script)],
        }
        return commands[executor_type] + shlex.split(script_args or "", posix=False)

    @staticmethod
    def _extensions(executor_type: str) -> set[str]:
        executor_type = executor_type.strip().lower()
        if executor_type not in _EXTENSIONS:
            raise bad_request("executorType is not supported")
        return _EXTENSIONS[executor_type]

    def _resolve_script(self, script_file: str, extensions: set[str]) -> Path:
        candidate = self._candidate(script_file, extensions)
        if not candidate.is_file():
            raise bad_request("scriptFile does not exist")
        return candidate

    def _candidate(self, script_file: str, extensions: set[str]) -> Path:
        if not script_file.strip():
            raise bad_request("scriptFile is required")
        candidate = (self.root / script_file).resolve()
        if self.root not in candidate.parents or candidate.suffix.lower() not in extensions:
            raise bad_request("script path is not allowed")
        return candidate
