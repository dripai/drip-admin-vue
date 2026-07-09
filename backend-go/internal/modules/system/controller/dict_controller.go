package controller

import (
	"drip-admin/backend-go/internal/common"
	"drip-admin/backend-go/internal/modules/system/dto"
	"github.com/gin-gonic/gin"
)

var (
	_ = common.ApiResponse{}
	_ = dto.DictTypeSaveRequest{}
)

// DictTypes returns dictionary types.
// @Summary List dictionary types
// @Tags dict
// @Produce json
// @Security ApiKeyAuth
// @Success 200 {object} common.ApiResponse
// @Failure 401 {object} common.ApiResponse
// @Failure 403 {object} common.ApiResponse
// @Router /system/dict/type [get]
func (ctl *Controller) DictTypes(c *gin.Context) {
	ctl.svc.DictTypes(c)
}

// CreateDictType creates a dictionary type.
// @Summary Create dictionary type
// @Tags dict
// @Accept json
// @Produce json
// @Security ApiKeyAuth
// @Param request body dto.DictTypeSaveRequest true "Request"
// @Success 200 {object} common.ApiResponse
// @Failure 400 {object} common.ApiResponse
// @Failure 401 {object} common.ApiResponse
// @Failure 403 {object} common.ApiResponse
// @Router /system/dict/type [post]
func (ctl *Controller) CreateDictType(c *gin.Context) {
	ctl.svc.CreateDictType(c)
}

// UpdateDictType updates a dictionary type.
// @Summary Update dictionary type
// @Tags dict
// @Accept json
// @Produce json
// @Security ApiKeyAuth
// @Param id path string true "Dictionary type ID"
// @Param request body dto.DictTypeSaveRequest true "Request"
// @Success 200 {object} common.ApiResponse
// @Failure 400 {object} common.ApiResponse
// @Failure 401 {object} common.ApiResponse
// @Failure 403 {object} common.ApiResponse
// @Router /system/dict/type/{id} [put]
func (ctl *Controller) UpdateDictType(c *gin.Context) {
	ctl.svc.UpdateDictType(c)
}

// DeleteDictType deletes a dictionary type.
// @Summary Delete dictionary type
// @Tags dict
// @Produce json
// @Security ApiKeyAuth
// @Param id path string true "Dictionary type ID"
// @Success 200 {object} common.ApiResponse
// @Failure 401 {object} common.ApiResponse
// @Failure 403 {object} common.ApiResponse
// @Failure 409 {object} common.ApiResponse
// @Router /system/dict/type/{id} [delete]
func (ctl *Controller) DeleteDictType(c *gin.Context) {
	ctl.svc.DeleteDictType(c)
}

// DictItems returns dictionary items for a type.
// @Summary List dictionary items
// @Tags dict
// @Produce json
// @Security ApiKeyAuth
// @Param id path string true "Dictionary type ID"
// @Success 200 {object} common.ApiResponse
// @Failure 401 {object} common.ApiResponse
// @Failure 403 {object} common.ApiResponse
// @Router /system/dict/type/{id}/item [get]
func (ctl *Controller) DictItems(c *gin.Context) {
	ctl.svc.DictItems(c)
}

// CreateDictItem creates a dictionary item.
// @Summary Create dictionary item
// @Tags dict
// @Accept json
// @Produce json
// @Security ApiKeyAuth
// @Param request body dto.DictItemSaveRequest true "Request"
// @Success 200 {object} common.ApiResponse
// @Failure 400 {object} common.ApiResponse
// @Failure 401 {object} common.ApiResponse
// @Failure 403 {object} common.ApiResponse
// @Router /system/dict/item [post]
func (ctl *Controller) CreateDictItem(c *gin.Context) {
	ctl.svc.CreateDictItem(c)
}

// UpdateDictItem updates a dictionary item.
// @Summary Update dictionary item
// @Tags dict
// @Accept json
// @Produce json
// @Security ApiKeyAuth
// @Param id path string true "Dictionary item ID"
// @Param request body dto.DictItemSaveRequest true "Request"
// @Success 200 {object} common.ApiResponse
// @Failure 400 {object} common.ApiResponse
// @Failure 401 {object} common.ApiResponse
// @Failure 403 {object} common.ApiResponse
// @Router /system/dict/item/{id} [put]
func (ctl *Controller) UpdateDictItem(c *gin.Context) {
	ctl.svc.UpdateDictItem(c)
}

// DeleteDictItem deletes a dictionary item.
// @Summary Delete dictionary item
// @Tags dict
// @Produce json
// @Security ApiKeyAuth
// @Param id path string true "Dictionary item ID"
// @Success 200 {object} common.ApiResponse
// @Failure 401 {object} common.ApiResponse
// @Failure 403 {object} common.ApiResponse
// @Router /system/dict/item/{id} [delete]
func (ctl *Controller) DeleteDictItem(c *gin.Context) {
	ctl.svc.DeleteDictItem(c)
}

// DictItemStatus updates a dictionary item status.
// @Summary Update dictionary item status
// @Tags dict
// @Accept json
// @Produce json
// @Security ApiKeyAuth
// @Param id path string true "Dictionary item ID"
// @Param request body dto.StatusUpdateRequest true "Request"
// @Success 200 {object} common.ApiResponse
// @Failure 400 {object} common.ApiResponse
// @Failure 401 {object} common.ApiResponse
// @Failure 403 {object} common.ApiResponse
// @Router /system/dict/item/{id}/status [put]
func (ctl *Controller) DictItemStatus(c *gin.Context) {
	ctl.svc.DictItemStatus(c)
}

// RefreshDictCache refreshes dictionary cache.
// @Summary Refresh dictionary cache
// @Tags dict
// @Produce json
// @Security ApiKeyAuth
// @Success 200 {object} common.ApiResponse
// @Failure 401 {object} common.ApiResponse
// @Failure 403 {object} common.ApiResponse
// @Router /system/dict/cache/refresh [post]
func (ctl *Controller) RefreshDictCache(c *gin.Context) {
	ctl.svc.RefreshDictCache(c)
}
