import threading
import time

_EPOCH_MS = 1704067200000
_LOCK = threading.Lock()
_LAST_MS = 0
_SEQUENCE = 0


def new_id() -> int:
    global _LAST_MS, _SEQUENCE
    with _LOCK:
        now = int(time.time() * 1000)
        if now == _LAST_MS:
            _SEQUENCE = (_SEQUENCE + 1) & 0xFFF
            if _SEQUENCE == 0:
                while now <= _LAST_MS:
                    now = int(time.time() * 1000)
        else:
            _SEQUENCE = 0
        _LAST_MS = now
        return ((now - _EPOCH_MS) << 12) | _SEQUENCE
