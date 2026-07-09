package common

import (
	"crypto/sha256"
	"encoding/hex"
	"fmt"
	"time"
)

func HashPassword(password string, salt string) string {
	sum := sha256.Sum256([]byte(salt + ":" + password))
	return hex.EncodeToString(sum[:])
}

func NewSalt() string {
	return fmt.Sprintf("salt%d", time.Now().UnixNano())
}
