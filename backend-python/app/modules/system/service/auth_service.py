import json
from datetime import UTC, datetime, timedelta
from uuid import uuid4

from redis.asyncio import Redis
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from app.common.errors import bad_request, unauthorized
from app.common.password import hash_password, new_salt
from app.config.settings import Settings
from app.modules.system.dto.auth_request import LoginRequest, PasswordRequest, ProfileUpdateRequest
from app.modules.system.entity import SysUser
from app.modules.system.service.login_log_service import LoginLogService
from app.modules.system.service.login_security_service import LoginSecurityService
from app.modules.system.service.menu_service import MenuService
from app.modules.system.service.permission_service import PermissionService
from app.modules.system.vo.auth_vo import AuthLoginVo, AuthMeVo

SESSION_PREFIX = "drip:online:"
TOKEN_PREFIX = "drip:token:"


class AuthService:
    def __init__(self, db: AsyncSession, redis: Redis, settings: Settings) -> None:
        self.db = db
        self.redis = redis
        self.settings = settings
        self.permissions = PermissionService(db)
        self.security = LoginSecurityService(db, redis)
        self.login_logs = LoginLogService(db)

    async def login(self, request: LoginRequest, client_ip: str, user_agent: str) -> AuthLoginVo:
        username = request.username.strip()
        if not username:
            raise bad_request("username is required")
        if not request.password.strip():
            raise bad_request("password is required")
        if not request.device_type.strip():
            raise bad_request("deviceType is required")

        user = await self.db.scalar(select(SysUser).where(SysUser.username == username))
        if user is None:
            await self.login_logs.write(
                None, username, None, "LOGIN", "FAIL", "invalid username or password",
                client_ip, user_agent, request.device_type,
            )
            raise unauthorized("\u7528\u6237\u540d\u6216\u5bc6\u7801\u9519\u8bef")
        if user.status != 1 or user.deleted == 1:
            await self.login_logs.write(
                user.id, username, user.real_name, "LOGIN", "FAIL", "\u8d26\u53f7\u5df2\u7981\u7528",
                client_ip, user_agent, request.device_type,
            )
            raise unauthorized("\u8d26\u53f7\u5df2\u7981\u7528")
        await self.security.assert_not_locked(username)
        if hash_password(request.password, user.password_salt) != user.password_hash:
            await self.login_logs.write(
                user.id, username, user.real_name, "LOGIN", "FAIL", "\u7528\u6237\u540d\u6216\u5bc6\u7801\u9519\u8bef",
                client_ip, user_agent, request.device_type,
            )
            remaining = await self.security.record_failure(username)
            raise unauthorized(f"\u7528\u6237\u540d\u6216\u5bc6\u7801\u9519\u8bef\uff0c\u8fd8\u5269{remaining}\u6b21\u673a\u4f1a")

        await self.security.clear(username)
        device_type = normalize_device_type(request.device_type)
        now = datetime.now(UTC)
        token = str(uuid4())
        session = {
            "tokenId": token,
            "userId": str(user.id),
            "username": user.username,
            "realName": user.real_name,
            "deviceType": device_type,
            "ip": client_ip,
            "userAgent": user_agent,
            "loginAt": now.isoformat(),
            "lastActiveAt": now.isoformat(),
            "expireAt": (now + timedelta(seconds=self.settings.token.active_timeout_seconds)).isoformat(),
            "tokenExpireAt": (now + timedelta(seconds=self.settings.token.timeout_seconds)).isoformat(),
        }
        await self._write_session(session)
        user.last_login_at = now.replace(tzinfo=None)
        await self.login_logs.write(
            user.id, user.username, user.real_name, "LOGIN", "SUCCESS", None,
            client_ip, user_agent, request.device_type,
        )
        return AuthLoginVo(
            token=token,
            expireAt=session["expireAt"],
            activeTimeoutSeconds=self.settings.token.active_timeout_seconds,
            tokenTimeoutSeconds=self.settings.token.timeout_seconds,
            deviceType=device_type,
        )

    async def current_session(self, token: str) -> dict:
        if not token:
            raise unauthorized()
        payload = await self.redis.get(f"{TOKEN_PREFIX}{token}")
        if not payload:
            raise unauthorized()
        session = json.loads(payload)
        now = datetime.now(UTC)
        token_expire_at = datetime.fromisoformat(session["tokenExpireAt"])
        if now >= token_expire_at:
            await self.redis.delete(f"{TOKEN_PREFIX}{token}", self._session_key(session))
            raise unauthorized()
        session["lastActiveAt"] = now.isoformat()
        session["expireAt"] = (now + timedelta(seconds=self.settings.token.active_timeout_seconds)).isoformat()
        await self._write_session(session)
        return session

    async def logout(self, session: dict) -> None:
        token = session["tokenId"]
        payload = await self.redis.get(f"{TOKEN_PREFIX}{token}")
        if payload:
            current = json.loads(payload)
            await self.login_logs.write(
                int(current["userId"]), current["username"], current.get("realName"),
                "LOGOUT", "SUCCESS", None, current.get("ip", ""),
                current.get("userAgent", ""), current.get("deviceType", ""),
            )
            await self.redis.delete(f"{TOKEN_PREFIX}{token}", self._session_key(session))

    async def me(self, session: dict) -> AuthMeVo:
        user_id = int(session["userId"])
        user = await self.db.scalar(select(SysUser).where(SysUser.id == user_id, SysUser.deleted == 0))
        if user is None:
            raise unauthorized()
        roles = await self.permissions.role_codes(user_id)
        permissions = await self.permissions.permission_codes(user_id)
        menus = await MenuService(self.db).menu_tree_for_user(user_id)
        return AuthMeVo(
            id=str(user.id), username=user.username, realName=user.real_name, phone=user.phone, email=user.email,
            avatar=user.avatar, deptId=str(user.dept_id) if user.dept_id is not None else None,
            roles=roles, permissions=permissions, menus=menus,
        )

    async def change_password(self, session: dict, request: PasswordRequest) -> None:
        if not request.old_password.strip() or not request.new_password.strip():
            raise bad_request("password is required")
        if not 8 <= len(request.new_password) <= 64:
            raise bad_request("newPassword length must be 8 to 64")
        user = await self.db.scalar(select(SysUser).where(SysUser.id == int(session["userId"])))
        if user is None or hash_password(request.old_password, user.password_salt) != user.password_hash:
            raise bad_request("\u65e7\u5bc6\u7801\u9519\u8bef")
        user.password_salt = new_salt()
        user.password_hash = hash_password(request.new_password, user.password_salt)
        await self.db.commit()

    async def update_profile(self, session: dict, request: ProfileUpdateRequest) -> None:
        if not request.real_name.strip():
            raise bad_request("realName is required")
        user = await self.db.scalar(select(SysUser).where(SysUser.id == int(session["userId"])))
        if user is None:
            raise unauthorized()
        user.real_name = request.real_name.strip()
        user.phone = request.phone.strip() if request.phone else None
        user.email = request.email.strip() if request.email else None
        await self.db.commit()
        session["realName"] = user.real_name
        await self._write_session(session)

    async def _write_session(self, session: dict) -> None:
        token_expire_at = datetime.fromisoformat(session["tokenExpireAt"])
        remaining_seconds = int((token_expire_at - datetime.now(UTC)).total_seconds())
        if remaining_seconds <= 0:
            await self.redis.delete(f"{TOKEN_PREFIX}{session['tokenId']}", self._session_key(session))
            raise unauthorized()
        ttl = min(self.settings.token.active_timeout_seconds, remaining_seconds)
        key = self._session_key(session)
        previous = await self.redis.get(key)
        if previous:
            await self.redis.delete(f"{TOKEN_PREFIX}{json.loads(previous)['tokenId']}")
        payload = json.dumps(session, separators=(",", ":"))
        await self.redis.set(key, payload, ex=ttl)
        await self.redis.set(f"{TOKEN_PREFIX}{session['tokenId']}", payload, ex=ttl)

    @staticmethod
    def _session_key(session: dict) -> str:
        return f"{SESSION_PREFIX}{session['userId']}:{session['deviceType']}"


def normalize_device_type(value: str) -> str:
    normalized = value.strip().lower()
    if normalized in {"web", "desktop", "windows", "mac", "linux", "pc"}:
        return "pc"
    if normalized in {"phone", "mobile"}:
        return "mobile"
    if normalized in {"pad", "ipad", "tablet"}:
        return "tablet"
    return "unknown"
