package controller

import "drip-admin/backend-go/internal/modules/system/service"

type Controller struct {
	svc *service.Server
}

func New(svc *service.Server) *Controller {
	return &Controller{svc: svc}
}
