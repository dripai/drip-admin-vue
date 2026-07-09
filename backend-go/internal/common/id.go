package common

import (
	"sync/atomic"
	"time"
)

const idEpochMillis int64 = 1767225600000

var idSequence uint64

func NewID() Int64String {
	now := time.Now().UnixMilli() - idEpochMillis
	seq := atomic.AddUint64(&idSequence, 1) & 0x0fff
	return Int64String((now << 12) | int64(seq))
}
