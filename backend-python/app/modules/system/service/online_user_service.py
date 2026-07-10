import json

from redis.asyncio import Redis

from app.common.errors import bad_request, not_found
from app.common.pagination import PageQuery, PageResult
from app.modules.system.service.auth_service import SESSION_PREFIX, TOKEN_PREFIX


class OnlineUserService:
    def __init__(self, redis: Redis) -> None:
        self.redis = redis

    async def list_users(
        self,
        page: PageQuery,
        current_token: str,
        username: str | None,
        ip: str | None,
        device_type: str | None,
    ) -> PageResult[dict]:
        sessions = await self._sessions()
        sessions = [
            item
            for item in sessions
            if _contains(item.get("username"), username)
            and _contains(item.get("ip"), ip)
            and _contains(item.get("deviceType"), device_type)
        ]
        sessions.sort(key=lambda item: item.get("lastActiveAt", ""), reverse=True)
        total = len(sessions)
        start = (page.page - 1) * page.page_size
        rows = sessions[start : start + page.page_size]
        return PageResult(
            list=[self._vo(item, current_token) for item in rows],
            total=str(total),
            page=page.page,
            pageSize=page.page_size,
        )

    async def detail(self, token_id: str, current_token: str) -> dict:
        session = await self._session_by_token(token_id)
        if session is None:
            raise not_found("online session not found")
        return self._vo(session, current_token)

    async def kickout(self, token_id: str, current_token: str) -> None:
        if token_id == current_token:
            raise bad_request("operation failed")
        session = await self._session_by_token(token_id)
        if session is None:
            raise not_found("online session not found")
        await self.redis.delete(
            f"{TOKEN_PREFIX}{token_id}",
            f"{SESSION_PREFIX}{session['userId']}:{session['deviceType']}",
        )

    async def _sessions(self) -> list[dict]:
        rows: list[dict] = []
        async for key in self.redis.scan_iter(match=f"{SESSION_PREFIX}*"):
            payload = await self.redis.get(key)
            if not payload:
                continue
            try:
                rows.append(json.loads(payload))
            except json.JSONDecodeError:
                await self.redis.delete(key)
        return rows

    async def _session_by_token(self, token_id: str) -> dict | None:
        payload = await self.redis.get(f"{TOKEN_PREFIX}{token_id}")
        if not payload:
            return None
        try:
            return json.loads(payload)
        except json.JSONDecodeError:
            await self.redis.delete(f"{TOKEN_PREFIX}{token_id}")
            return None

    @staticmethod
    def _vo(session: dict, current_token: str) -> dict:
        return {
            "tokenId": session["tokenId"],
            "userId": str(session["userId"]),
            "username": session["username"],
            "realName": session.get("realName", ""),
            "deviceType": session.get("deviceType", ""),
            "ip": session.get("ip", ""),
            "userAgent": session.get("userAgent", ""),
            "loginAt": session.get("loginAt", ""),
            "lastActiveAt": session.get("lastActiveAt", ""),
            "expireAt": session.get("expireAt", ""),
            "current": session["tokenId"] == current_token,
        }


def _contains(current: str | None, expected: str | None) -> bool:
    if not expected or not expected.strip():
        return True
    return expected.strip().lower() in (current or "").lower()
