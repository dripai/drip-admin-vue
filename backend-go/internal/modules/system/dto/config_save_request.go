package dto

type ConfigSaveRequest struct {
	ConfigName  string `json:"configName"`
	ConfigKey   string `json:"configKey"`
	ConfigValue string `json:"configValue"`
	ValueType   string `json:"valueType"`
	Status      *int   `json:"status"`
	Remark      string `json:"remark"`
}
