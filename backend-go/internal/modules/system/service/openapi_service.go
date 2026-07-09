package service

import "github.com/gin-gonic/gin"

func (s *Server) OpenAPIDocs(c *gin.Context) {
	paths := gin.H{}
	addOpenAPIPath(paths, "post", "/api/system/login")
	addOpenAPIPath(paths, "post", "/api/system/logout")
	addOpenAPIPath(paths, "get", "/api/system/me")
	addOpenAPIPath(paths, "put", "/api/system/password")
	addOpenAPIPath(paths, "put", "/api/system/profile")
	addOpenAPIPath(paths, "get", "/api/system/user")
	addOpenAPIPath(paths, "get", "/api/system/user/{id}")
	addOpenAPIPath(paths, "post", "/api/system/user")
	addOpenAPIPath(paths, "put", "/api/system/user/{id}")
	addOpenAPIPath(paths, "delete", "/api/system/user/{id}")
	addOpenAPIPath(paths, "put", "/api/system/user/{id}/status")
	addOpenAPIPath(paths, "post", "/api/system/user/{id}/unlock")
	addOpenAPIPath(paths, "put", "/api/system/user/{id}/role")
	addOpenAPIPath(paths, "post", "/api/system/user/{id}/resetPassword")
	addOpenAPIPath(paths, "get", "/api/system/role")
	addOpenAPIPath(paths, "get", "/api/system/role/{id}")
	addOpenAPIPath(paths, "get", "/api/system/role/{id}/user")
	addOpenAPIPath(paths, "get", "/api/system/role/{id}/permission")
	addOpenAPIPath(paths, "get", "/api/system/role/option")
	addOpenAPIPath(paths, "post", "/api/system/role")
	addOpenAPIPath(paths, "put", "/api/system/role/{id}")
	addOpenAPIPath(paths, "delete", "/api/system/role/{id}")
	addOpenAPIPath(paths, "put", "/api/system/role/{id}/status")
	addOpenAPIPath(paths, "put", "/api/system/role/{id}/permission")
	addOpenAPIPath(paths, "get", "/api/system/menu")
	addOpenAPIPath(paths, "post", "/api/system/menu")
	addOpenAPIPath(paths, "put", "/api/system/menu/{id}")
	addOpenAPIPath(paths, "delete", "/api/system/menu/{id}")
	addOpenAPIPath(paths, "put", "/api/system/menu/{id}/status")
	addOpenAPIPath(paths, "get", "/api/system/dept")
	addOpenAPIPath(paths, "get", "/api/system/dept/{id}")
	addOpenAPIPath(paths, "post", "/api/system/dept")
	addOpenAPIPath(paths, "put", "/api/system/dept/{id}")
	addOpenAPIPath(paths, "delete", "/api/system/dept/{id}")
	addOpenAPIPath(paths, "put", "/api/system/dept/{id}/status")
	addOpenAPIPath(paths, "get", "/api/system/config")
	addOpenAPIPath(paths, "get", "/api/system/publicConfig")
	addOpenAPIPath(paths, "post", "/api/system/config")
	addOpenAPIPath(paths, "put", "/api/system/config/{id}")
	addOpenAPIPath(paths, "delete", "/api/system/config/{id}")
	addOpenAPIPath(paths, "put", "/api/system/config/{id}/status")
	addOpenAPIPath(paths, "get", "/api/system/dict/type")
	addOpenAPIPath(paths, "post", "/api/system/dict/type")
	addOpenAPIPath(paths, "put", "/api/system/dict/type/{id}")
	addOpenAPIPath(paths, "delete", "/api/system/dict/type/{id}")
	addOpenAPIPath(paths, "get", "/api/system/dict/type/{id}/item")
	addOpenAPIPath(paths, "post", "/api/system/dict/item")
	addOpenAPIPath(paths, "put", "/api/system/dict/item/{id}")
	addOpenAPIPath(paths, "delete", "/api/system/dict/item/{id}")
	addOpenAPIPath(paths, "put", "/api/system/dict/item/{id}/status")
	addOpenAPIPath(paths, "post", "/api/system/dict/cache/refresh")
	addOpenAPIPath(paths, "get", "/api/system/onlineUser")
	addOpenAPIPath(paths, "get", "/api/system/onlineUser/{tokenId}")
	addOpenAPIPath(paths, "post", "/api/system/onlineUser/{tokenId}/kickout")
	addOpenAPIPath(paths, "get", "/api/system/loginLog")
	addOpenAPIPath(paths, "get", "/api/system/loginLog/{id}")
	addOpenAPIPath(paths, "get", "/api/system/operationLog")
	addOpenAPIPath(paths, "get", "/api/system/operationLog/{id}")
	addOpenAPIPath(paths, "get", "/api/system/job")
	addOpenAPIPath(paths, "get", "/api/system/job/{id}")
	addOpenAPIPath(paths, "get", "/api/system/job/scripts")
	addOpenAPIPath(paths, "post", "/api/system/job")
	addOpenAPIPath(paths, "put", "/api/system/job/{id}")
	addOpenAPIPath(paths, "delete", "/api/system/job/{id}")
	addOpenAPIPath(paths, "put", "/api/system/job/{id}/status")
	addOpenAPIPath(paths, "post", "/api/system/job/{id}/run")
	addOpenAPIPath(paths, "get", "/api/system/job/{id}/runLog")
	addOpenAPIPath(paths, "get", "/api/system/jobRunLog")
	addOpenAPIPath(paths, "post", "/api/system/files")
	addOpenAPIPath(paths, "get", "/api/system/print-template")
	addOpenAPIPath(paths, "get", "/api/system/print-template/{id}")
	addOpenAPIPath(paths, "post", "/api/system/print-template")
	addOpenAPIPath(paths, "post", "/api/system/print-template/{id}/copy")
	addOpenAPIPath(paths, "put", "/api/system/print-template/{id}")
	addOpenAPIPath(paths, "delete", "/api/system/print-template/{id}")
	addOpenAPIPath(paths, "put", "/api/system/print-template/{id}/status")

	c.JSON(200, gin.H{
		"openapi": "3.0.0",
		"info": gin.H{
			"title":   "Drip Admin Go API",
			"version": "1.0.0",
		},
		"paths": paths,
		"components": gin.H{
			"schemas": gin.H{
				"ApiResponse": gin.H{
					"type":       "object",
					"properties": gin.H{"code": gin.H{"type": "integer"}, "message": gin.H{"type": "string"}, "data": gin.H{"nullable": true}},
				},
				"PageResult": gin.H{
					"type":       "object",
					"properties": gin.H{"list": gin.H{"type": "array", "items": gin.H{}}, "total": gin.H{"type": "string"}, "page": gin.H{"type": "integer"}, "pageSize": gin.H{"type": "integer"}},
				},
			},
		},
	})
}

func addOpenAPIPath(paths gin.H, method string, path string) {
	current, ok := paths[path].(gin.H)
	if !ok {
		current = gin.H{}
		paths[path] = current
	}
	current[method] = gin.H{
		"responses": gin.H{
			"200": gin.H{"description": "success"},
		},
	}
}
