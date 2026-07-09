from redis.asyncio import Redis

from app.config.settings import Settings


def create_redis_client(settings: Settings) -> Redis:
    return Redis.from_url(settings.redis.url, decode_responses=True)

