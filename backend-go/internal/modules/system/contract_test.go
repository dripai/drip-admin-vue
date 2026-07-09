package system

import (
	"encoding/json"
	"os"
	"path/filepath"
	"sort"
	"strings"
	"testing"

	"drip-admin/backend-go/internal/common"
	"drip-admin/backend-go/internal/config"
	"github.com/gin-gonic/gin"
	"github.com/stretchr/testify/require"
)

func TestRouteContract(t *testing.T) {
	gin.SetMode(gin.TestMode)
	t.Setenv("DRIP_GO_CONFIG", filepath.Join("..", "..", "..", "config.yaml"))
	cfg, err := config.Load()
	require.NoError(t, err)
	router := NewServer(cfg, nil, nil, nil).Router()
	actual := map[string]bool{}
	for _, route := range router.Routes() {
		actual[route.Method+" "+route.Path] = true
	}
	for _, route := range expectedRoutes() {
		require.True(t, actual[route], route)
	}
}

func TestResponseContract(t *testing.T) {
	payload, err := json.Marshal(common.ApiResponse{Code: 0, Message: "success", Data: common.PageResult[string]{
		List:     []string{},
		Total:    common.Int64String(0),
		Page:     1,
		PageSize: 10,
	}})
	require.NoError(t, err)
	require.JSONEq(t, `{"code":0,"message":"success","data":{"list":[],"total":"0","page":1,"pageSize":10}}`, string(payload))

	payload, err = json.Marshal(common.ApiResponse{Code: 400000, Message: "错误信息", Data: nil})
	require.NoError(t, err)
	require.JSONEq(t, `{"code":400000,"message":"错误信息","data":null}`, string(payload))
}

func TestPermissionContract(t *testing.T) {
	source, err := os.ReadFile("router.go")
	require.NoError(t, err)
	text := string(source)
	for _, permission := range expectedPermissions() {
		require.Contains(t, text, `RequirePermission("`+permission+`")`)
	}
}

func TestLayeredStructureContract(t *testing.T) {
	for _, dir := range []string{"controller", "dto", "entity", "service", "vo"} {
		info, err := os.Stat(dir)
		require.NoError(t, err, dir)
		require.True(t, info.IsDir(), dir)
	}

	for _, file := range []string{
		"auth.go",
		"config_handlers.go",
		"controllers.go",
		"dict_logs_jobs_print.go",
		"dto.go",
		"entity.go",
		"handlers.go",
		"models.go",
		"server.go",
		"services.go",
		"user_role_menu_dept.go",
		"service/dict_log_job_print_service.go",
		"service/user_role_menu_dept_service.go",
	} {
		_, err := os.Stat(file)
		require.True(t, os.IsNotExist(err), file)
	}

	for _, file := range []string{
		"controller/auth_controller.go",
		"controller/config_controller.go",
		"controller/dept_controller.go",
		"controller/dict_controller.go",
		"controller/file_controller.go",
		"controller/job_controller.go",
		"controller/log_controller.go",
		"controller/menu_controller.go",
		"controller/online_user_controller.go",
		"controller/print_template_controller.go",
		"controller/role_controller.go",
		"controller/user_controller.go",
		"dto/user_save_request.go",
		"dto/role_save_request.go",
		"dto/menu_save_request.go",
		"dto/dept_save_request.go",
		"dto/config_save_request.go",
		"dto/dict_type_save_request.go",
		"dto/dict_item_save_request.go",
		"dto/job_save_request.go",
		"dto/print_template_save_request.go",
		"entity/sys_user.go",
		"entity/sys_role.go",
		"entity/sys_menu.go",
		"entity/sys_dept.go",
		"service/user_service.go",
		"service/role_service.go",
		"service/menu_service.go",
		"service/dept_service.go",
		"service/config_service.go",
		"service/dict_service.go",
		"service/online_user_service.go",
		"service/login_log_service.go",
		"service/login_security_service.go",
		"service/log_service.go",
		"service/job_service.go",
		"service/file_service.go",
		"service/operation_log_service.go",
		"service/permission_service.go",
		"service/print_template_service.go",
		"vo/user_list_vo.go",
		"vo/role_permission_vo.go",
		"vo/menu_tree_vo.go",
		"vo/dept_tree_vo.go",
		"vo/operation_log_vo.go",
	} {
		require.FileExists(t, file)
	}
}

func TestDatabaseContract(t *testing.T) {
	var goFiles []string
	require.NoError(t, filepath.Walk(".", func(path string, info os.FileInfo, err error) error {
		if err != nil {
			return err
		}
		if !info.IsDir() && strings.HasSuffix(path, ".go") {
			goFiles = append(goFiles, path)
		}
		return nil
	}))
	sort.Strings(goFiles)
	for _, file := range goFiles {
		data, err := os.ReadFile(file)
		require.NoError(t, err)
		text := string(data)
		require.NotContains(t, text, "Auto"+"Migrate")
		require.NotContains(t, strings.ToUpper(text), "CREATE "+"TABLE")
		require.NotContains(t, strings.ToUpper(text), "ALTER "+"TABLE")
	}
	_, err := os.Stat(filepath.Join("..", "..", "..", "migra"+"tions"))
	require.True(t, os.IsNotExist(err))
}

func expectedRoutes() []string {
	return []string{
		"GET /api/",
		"GET /api/favicon.ico",
		"GET /api/health",
		"GET /api/v3/api-docs",
		"GET /api/swagger-ui/*any",
		"GET /api/system/publicConfig",
		"POST /api/system/login",
		"POST /api/system/logout",
		"GET /api/system/me",
		"PUT /api/system/password",
		"PUT /api/system/profile",
		"GET /api/system/user",
		"GET /api/system/user/:id",
		"POST /api/system/user",
		"PUT /api/system/user/:id",
		"DELETE /api/system/user/:id",
		"PUT /api/system/user/:id/status",
		"POST /api/system/user/:id/unlock",
		"PUT /api/system/user/:id/role",
		"POST /api/system/user/:id/resetPassword",
		"GET /api/system/role",
		"GET /api/system/role/option",
		"GET /api/system/role/:id",
		"GET /api/system/role/:id/user",
		"GET /api/system/role/:id/permission",
		"POST /api/system/role",
		"PUT /api/system/role/:id",
		"DELETE /api/system/role/:id",
		"PUT /api/system/role/:id/status",
		"PUT /api/system/role/:id/permission",
		"GET /api/system/menu",
		"POST /api/system/menu",
		"PUT /api/system/menu/:id",
		"DELETE /api/system/menu/:id",
		"PUT /api/system/menu/:id/status",
		"GET /api/system/dept",
		"GET /api/system/dept/:id",
		"POST /api/system/dept",
		"PUT /api/system/dept/:id",
		"DELETE /api/system/dept/:id",
		"PUT /api/system/dept/:id/status",
		"GET /api/system/config",
		"POST /api/system/config",
		"PUT /api/system/config/:id",
		"DELETE /api/system/config/:id",
		"PUT /api/system/config/:id/status",
		"GET /api/system/dict/type",
		"POST /api/system/dict/type",
		"PUT /api/system/dict/type/:id",
		"DELETE /api/system/dict/type/:id",
		"GET /api/system/dict/type/:id/item",
		"POST /api/system/dict/item",
		"PUT /api/system/dict/item/:id",
		"DELETE /api/system/dict/item/:id",
		"PUT /api/system/dict/item/:id/status",
		"POST /api/system/dict/cache/refresh",
		"GET /api/system/onlineUser",
		"GET /api/system/onlineUser/:tokenId",
		"POST /api/system/onlineUser/:tokenId/kickout",
		"GET /api/system/loginLog",
		"GET /api/system/loginLog/:id",
		"GET /api/system/operationLog",
		"GET /api/system/operationLog/:id",
		"GET /api/system/job",
		"GET /api/system/job/scripts",
		"GET /api/system/job/:id",
		"POST /api/system/job",
		"PUT /api/system/job/:id",
		"DELETE /api/system/job/:id",
		"PUT /api/system/job/:id/status",
		"POST /api/system/job/:id/run",
		"GET /api/system/job/:id/runLog",
		"GET /api/system/jobRunLog",
		"POST /api/system/files",
		"GET /api/system/print-template",
		"GET /api/system/print-template/:id",
		"POST /api/system/print-template",
		"POST /api/system/print-template/:id/copy",
		"PUT /api/system/print-template/:id",
		"DELETE /api/system/print-template/:id",
		"PUT /api/system/print-template/:id/status",
	}
}

func expectedPermissions() []string {
	return []string{
		"system:user:list",
		"system:user:detail",
		"system:user:create",
		"system:user:update",
		"system:user:delete",
		"system:user:disable",
		"system:user:unlock",
		"system:user:assignRole",
		"system:user:resetPassword",
		"system:role:list",
		"system:role:permission",
		"system:role:create",
		"system:role:update",
		"system:role:delete",
		"system:menu:list",
		"system:menu:create",
		"system:menu:update",
		"system:menu:delete",
		"system:menu:status",
		"system:dept:list",
		"system:dept:create",
		"system:dept:update",
		"system:dept:delete",
		"system:config:list",
		"system:config:create",
		"system:config:update",
		"system:config:delete",
		"system:dict:list",
		"system:dict:create",
		"system:dict:update",
		"system:dict:delete",
		"system:online:list",
		"system:online:kickout",
		"system:loginLog:list",
		"system:operationLog:list",
		"system:job:list",
		"system:job:create",
		"system:job:update",
		"system:job:delete",
		"system:job:run",
		"system:job:history",
		"system:file:upload",
		"system:printTemplate:list",
		"system:printTemplate:create",
		"system:printTemplate:update",
		"system:printTemplate:delete",
	}
}
