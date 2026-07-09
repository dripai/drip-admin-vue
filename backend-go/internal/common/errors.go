package common

import (
	"errors"

	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
)

type BusinessError struct {
	Code    int
	Message string
}

func NewBusinessError(code int, message string) BusinessError {
	return BusinessError{Code: code, Message: message}
}

func (e BusinessError) Error() string {
	return e.Message
}

func (e BusinessError) HTTPStatus() int {
	switch e.Code {
	case 401000:
		return 401
	case 403000:
		return 403
	case 404000:
		return 404
	case 409000:
		return 409
	default:
		if e.Code >= 500000 {
			return 500
		}
		return 400
	}
}

func HandleError(c *gin.Context, err error) {
	if err == nil {
		return
	}
	var business BusinessError
	if errors.As(err, &business) {
		Fail(c, business.HTTPStatus(), business.Code, business.Message)
		return
	}
	if errors.Is(err, gorm.ErrRecordNotFound) {
		Fail(c, 404, 404000, "operation failed")
		return
	}
	Fail(c, 500, 500000, "系统内部错误")
}

func NotFound(message string) BusinessError {
	if message == "" {
		message = "operation failed"
	}
	return NewBusinessError(404000, message)
}
