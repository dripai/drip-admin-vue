package controller

import (
	"net/http"

	"drip-admin/backend-go/internal/common"
	"github.com/gin-gonic/gin"
	"github.com/swaggo/swag"
)

// Root redirects to Swagger UI.
// @Summary API root
// @Tags common
// @Success 302
// @Router / [get]
func (ctl *Controller) Root(c *gin.Context) {
	ctl.svc.Root(c)
}

// SwaggerUI redirects to the Swagger UI index page.
// @Summary Swagger UI
// @Tags common
// @Success 302
// @Router /swagger-ui.html [get]
func (ctl *Controller) SwaggerUI(c *gin.Context) {
	ctl.svc.SwaggerUI(c)
}

// Favicon returns an empty favicon response.
// @Summary Favicon
// @Tags common
// @Success 204
// @Router /favicon.ico [get]
func (ctl *Controller) Favicon(c *gin.Context) {
	ctl.svc.Favicon(c)
}

// Health returns service health.
// @Summary Health check
// @Tags common
// @Produce json
// @Success 200 {object} common.ApiResponse
// @Router /health [get]
func (ctl *Controller) Health(c *gin.Context) {
	ctl.svc.Health(c)
}

// OpenAPIDocs returns generated Swagger JSON.
// @Summary OpenAPI document
// @Tags common
// @Produce json
// @Success 200 {object} object
// @Failure 500 {object} common.ApiResponse
// @Router /v3/api-docs [get]
func (ctl *Controller) OpenAPIDocs(c *gin.Context) {
	doc, err := swag.ReadDoc()
	if err != nil {
		common.Fail(c, http.StatusInternalServerError, 500000, "OpenAPI document is not registered")
		return
	}
	c.Data(http.StatusOK, "application/json; charset=utf-8", []byte(doc))
}
