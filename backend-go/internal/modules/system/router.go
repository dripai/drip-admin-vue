package system

import (
	_ "drip-admin/backend-go/docs"
	"drip-admin/backend-go/internal/config"
	"drip-admin/backend-go/internal/modules/system/controller"
	"drip-admin/backend-go/internal/modules/system/service"
	"github.com/gin-contrib/cors"
	"github.com/gin-gonic/gin"
	"github.com/redis/go-redis/v9"
	swaggerFiles "github.com/swaggo/files"
	ginSwagger "github.com/swaggo/gin-swagger"
	"go.uber.org/zap"
	"gorm.io/gorm"
)

type Server struct {
	cfg    config.Config
	db     *gorm.DB
	redis  *redis.Client
	logger *zap.Logger
}

func NewServer(cfg config.Config, db *gorm.DB, redisClient *redis.Client, logger *zap.Logger) *Server {
	return &Server{cfg: cfg, db: db, redis: redisClient, logger: logger}
}

func (s *Server) Router() *gin.Engine {
	gin.SetMode(gin.ReleaseMode)
	router := gin.New()
	svc := service.NewServer(s.cfg, s.db, s.redis, s.logger)
	router.Use(svc.RequestLoggerMiddleware(), svc.RecoveryMiddleware())
	router.Use(cors.New(cors.Config{
		AllowOriginFunc:  func(string) bool { return true },
		AllowMethods:     []string{"GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"},
		AllowHeaders:     []string{"*"},
		AllowCredentials: true,
	}))

	ctl := controller.New(svc)
	api := router.Group("/api")
	api.GET("/", ctl.Root)
	api.GET("/favicon.ico", ctl.Favicon)
	api.GET("/health", ctl.Health)
	api.GET("/v3/api-docs", ctl.OpenAPIDocs)
	api.GET("/swagger-ui.html", ctl.SwaggerUI)
	api.GET("/swagger-ui/*any", ginSwagger.WrapHandler(swaggerFiles.Handler, ginSwagger.URL("/api/v3/api-docs")))
	api.GET("/system/publicConfig", ctl.PublicConfig)
	api.POST("/system/login", ctl.Login)

	protected := api.Group("")
	protected.Use(svc.AuthMiddleware(), svc.OperationLogMiddleware())
	registerProtectedRoutes(protected, svc, ctl)
	return router
}

func registerProtectedRoutes(r *gin.RouterGroup, svc *service.Server, ctl *controller.Controller) {
	system := r.Group("/system")
	system.POST("/logout", ctl.Logout)
	system.GET("/me", ctl.Me)
	system.PUT("/password", ctl.Password)
	system.PUT("/profile", ctl.Profile)

	system.GET("/user", svc.RequirePermission("system:user:list"), ctl.Users)
	system.GET("/user/:id", svc.RequirePermission("system:user:detail"), ctl.User)
	system.POST("/user", svc.RequirePermission("system:user:create"), ctl.CreateUser)
	system.PUT("/user/:id", svc.RequirePermission("system:user:update"), ctl.UpdateUser)
	system.DELETE("/user/:id", svc.RequirePermission("system:user:delete"), ctl.DeleteUser)
	system.PUT("/user/:id/status", svc.RequirePermission("system:user:disable"), ctl.UserStatus)
	system.POST("/user/:id/unlock", svc.RequirePermission("system:user:unlock"), ctl.UnlockUser)
	system.PUT("/user/:id/role", svc.RequirePermission("system:user:assignRole"), ctl.UserRoles)
	system.POST("/user/:id/resetPassword", svc.RequirePermission("system:user:resetPassword"), ctl.ResetPassword)

	system.GET("/role", svc.RequirePermission("system:role:list"), ctl.Roles)
	system.GET("/role/option", svc.RequirePermission("system:role:list"), ctl.RoleOptions)
	system.GET("/role/:id", svc.RequirePermission("system:role:list"), ctl.Role)
	system.GET("/role/:id/user", svc.RequirePermission("system:role:list"), ctl.RoleUsers)
	system.GET("/role/:id/permission", svc.RequirePermission("system:role:permission"), ctl.RolePermissions)
	system.POST("/role", svc.RequirePermission("system:role:create"), ctl.CreateRole)
	system.PUT("/role/:id", svc.RequirePermission("system:role:update"), ctl.UpdateRole)
	system.DELETE("/role/:id", svc.RequirePermission("system:role:delete"), ctl.DeleteRole)
	system.PUT("/role/:id/status", svc.RequirePermission("system:role:update"), ctl.RoleStatus)
	system.PUT("/role/:id/permission", svc.RequirePermission("system:role:permission"), ctl.AssignRoleMenus)

	system.GET("/menu", svc.RequirePermission("system:menu:list"), ctl.Menus)
	system.POST("/menu", svc.RequirePermission("system:menu:create"), ctl.CreateMenu)
	system.PUT("/menu/:id", svc.RequirePermission("system:menu:update"), ctl.UpdateMenu)
	system.DELETE("/menu/:id", svc.RequirePermission("system:menu:delete"), ctl.DeleteMenu)
	system.PUT("/menu/:id/status", svc.RequirePermission("system:menu:status"), ctl.MenuStatus)

	system.GET("/dept", svc.RequirePermission("system:dept:list"), ctl.Depts)
	system.GET("/dept/:id", svc.RequirePermission("system:dept:list"), ctl.Dept)
	system.POST("/dept", svc.RequirePermission("system:dept:create"), ctl.CreateDept)
	system.PUT("/dept/:id", svc.RequirePermission("system:dept:update"), ctl.UpdateDept)
	system.DELETE("/dept/:id", svc.RequirePermission("system:dept:delete"), ctl.DeleteDept)
	system.PUT("/dept/:id/status", svc.RequirePermission("system:dept:update"), ctl.DeptStatus)

	system.GET("/config", svc.RequirePermission("system:config:list"), ctl.Configs)
	system.POST("/config", svc.RequirePermission("system:config:create"), ctl.CreateConfig)
	system.PUT("/config/:id", svc.RequirePermission("system:config:update"), ctl.UpdateConfig)
	system.DELETE("/config/:id", svc.RequirePermission("system:config:delete"), ctl.DeleteConfig)
	system.PUT("/config/:id/status", svc.RequirePermission("system:config:update"), ctl.ConfigStatus)

	system.GET("/dict/type", svc.RequirePermission("system:dict:list"), ctl.DictTypes)
	system.POST("/dict/type", svc.RequirePermission("system:dict:create"), ctl.CreateDictType)
	system.PUT("/dict/type/:id", svc.RequirePermission("system:dict:update"), ctl.UpdateDictType)
	system.DELETE("/dict/type/:id", svc.RequirePermission("system:dict:delete"), ctl.DeleteDictType)
	system.GET("/dict/type/:id/item", svc.RequirePermission("system:dict:list"), ctl.DictItems)
	system.POST("/dict/item", svc.RequirePermission("system:dict:create"), ctl.CreateDictItem)
	system.PUT("/dict/item/:id", svc.RequirePermission("system:dict:update"), ctl.UpdateDictItem)
	system.DELETE("/dict/item/:id", svc.RequirePermission("system:dict:delete"), ctl.DeleteDictItem)
	system.PUT("/dict/item/:id/status", svc.RequirePermission("system:dict:update"), ctl.DictItemStatus)
	system.POST("/dict/cache/refresh", svc.RequirePermission("system:dict:update"), ctl.RefreshDictCache)

	system.GET("/onlineUser", svc.RequirePermission("system:online:list"), ctl.OnlineUsers)
	system.GET("/onlineUser/:tokenId", svc.RequirePermission("system:online:list"), ctl.OnlineUser)
	system.POST("/onlineUser/:tokenId/kickout", svc.RequirePermission("system:online:kickout"), ctl.KickoutOnlineUser)

	system.GET("/loginLog", svc.RequirePermission("system:loginLog:list"), ctl.LoginLogs)
	system.GET("/loginLog/:id", svc.RequirePermission("system:loginLog:list"), ctl.LoginLog)
	system.GET("/operationLog", svc.RequirePermission("system:operationLog:list"), ctl.OperationLogs)
	system.GET("/operationLog/:id", svc.RequirePermission("system:operationLog:list"), ctl.OperationLog)

	system.GET("/job", svc.RequirePermission("system:job:list"), ctl.Jobs)
	system.GET("/job/scripts", svc.RequirePermission("system:job:list"), ctl.JobScripts)
	system.GET("/job/:id", svc.RequirePermission("system:job:list"), ctl.Job)
	system.POST("/job", svc.RequirePermission("system:job:create"), ctl.CreateJob)
	system.PUT("/job/:id", svc.RequirePermission("system:job:update"), ctl.UpdateJob)
	system.DELETE("/job/:id", svc.RequirePermission("system:job:delete"), ctl.DeleteJob)
	system.PUT("/job/:id/status", svc.RequirePermission("system:job:update"), ctl.JobStatus)
	system.POST("/job/:id/run", svc.RequirePermission("system:job:run"), ctl.RunJob)
	system.GET("/job/:id/runLog", svc.RequirePermission("system:job:list"), ctl.JobLogs)
	system.GET("/jobRunLog", svc.RequirePermission("system:job:history"), ctl.JobRunLogs)

	system.POST("/files", svc.RequirePermission("system:file:upload"), ctl.UploadFile)

	printTemplate := system.Group("/print-template")
	printTemplate.GET("", svc.RequirePermission("system:printTemplate:list"), ctl.PrintTemplates)
	printTemplate.GET("/:id", svc.RequirePermission("system:printTemplate:list"), ctl.PrintTemplate)
	printTemplate.POST("", svc.RequirePermission("system:printTemplate:create"), ctl.CreatePrintTemplate)
	printTemplate.POST("/:id/copy", svc.RequirePermission("system:printTemplate:create"), ctl.CopyPrintTemplate)
	printTemplate.PUT("/:id", svc.RequirePermission("system:printTemplate:update"), ctl.UpdatePrintTemplate)
	printTemplate.DELETE("/:id", svc.RequirePermission("system:printTemplate:delete"), ctl.DeletePrintTemplate)
	printTemplate.PUT("/:id/status", svc.RequirePermission("system:printTemplate:update"), ctl.PrintTemplateStatus)
}
