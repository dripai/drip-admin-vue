from redis.asyncio import Redis
from sqlalchemy.ext.asyncio import AsyncSession

from app.common.errors import BusinessError
from app.modules.system.service.config_service import ConfigService

LOGIN_FAILURE_PREFIX = "drip:login:fail:"


class LoginSecurityService:
    def __init__(self, db: AsyncSession, redis: Redis) -> None:
        self.redis = redis
        self.config = ConfigService(db)

    async def assert_not_locked(self, username: str) -> None:
        failures = await self.failure_count(username)
        maximum = await self.config.required_int_config("login.maxFailures")
        if failures >= maximum:
            ttl = await self.redis.ttl(self.key(username))
            raise BusinessError(401000, f"\u8d26\u53f7\u5df2\u9501\u5b9a\uff0c\u8bf7{_duration(ttl)}\u540e\u518d\u8bd5")

    async def record_failure(self, username: str) -> int:
        key = self.key(username)
        failures = int(await self.redis.incr(key))
        lock_seconds = await self.config.required_int_config("login.lockSeconds")
        await self.redis.expire(key, lock_seconds)
        maximum = await self.config.required_int_config("login.maxFailures")
        if failures >= maximum:
            raise BusinessError(401000, f"\u8d26\u53f7\u5df2\u9501\u5b9a\uff0c\u8bf7{_duration(lock_seconds)}\u540e\u518d\u8bd5")
        return maximum - failures

    async def failure_count(self, username: str) -> int:
        value = await self.redis.get(self.key(username))
        if value is None:
            return 0
        try:
            return int(value)
        except ValueError:
            await self.redis.delete(self.key(username))
            return 0

    async def clear(self, username: str) -> None:
        await self.redis.delete(self.key(username))

    @staticmethod
    def key(username: str) -> str:
        return f"{LOGIN_FAILURE_PREFIX}{username.strip().lower()}"


def _duration(seconds: int) -> str:
    if seconds <= 0:
        return "\u7a0d\u540e"
    minutes, remaining = divmod(seconds, 60)
    if minutes and remaining:
        return f"{minutes}\u5206{remaining}\u79d2"
    if minutes:
        return f"{minutes}\u5206\u949f"
    return f"{remaining}\u79d2"
