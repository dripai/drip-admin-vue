package service

import (
	"context"
	"encoding/json"
	"sort"
	"strings"

	"drip-admin/backend-go/internal/common"
	"github.com/gin-gonic/gin"
)

func (s *Server) OnlineUsers(c *gin.Context) {
	page, err := common.ParsePage(c)
	if err != nil {
		common.HandleError(c, err)
		return
	}
	rows, err := s.onlineSessionRows(c.Request.Context())
	if err != nil {
		common.HandleError(c, err)
		return
	}
	rows = filterOnlineRows(rows, "username", c.Query("username"))
	rows = filterOnlineRows(rows, "ip", c.Query("ip"))
	rows = filterOnlineRows(rows, "deviceType", c.Query("deviceType"))
	start := (page.Page - 1) * page.PageSize
	if start > len(rows) {
		start = len(rows)
	}
	end := start + page.PageSize
	if end > len(rows) {
		end = len(rows)
	}
	currentToken := c.GetHeader(s.cfg.Token.Name)
	out := make([]OnlineUserVo, 0, end-start)
	for _, row := range rows[start:end] {
		out = append(out, row.toVo(currentToken))
	}
	common.Success(c, common.PageResult[OnlineUserVo]{List: out, Total: common.Int64String(len(rows)), Page: page.Page, PageSize: page.PageSize})
}

func (s *Server) OnlineUser(c *gin.Context) {
	tokenID := c.Param("tokenId")
	session, err := s.sessionByToken(c.Request.Context(), tokenID)
	if err != nil {
		common.HandleError(c, err)
		return
	}
	if session.TokenID == "" {
		common.HandleError(c, common.NewBusinessError(404000, "online session not found"))
		return
	}
	common.Success(c, session.toVo(c.GetHeader(s.cfg.Token.Name)))
}

func (s *Server) KickoutOnlineUser(c *gin.Context) {
	tokenID := c.Param("tokenId")
	if tokenID == c.GetHeader(s.cfg.Token.Name) {
		common.HandleError(c, common.NewBusinessError(400000, "operation failed"))
		return
	}
	successOrError(c, nil, s.removeSession(c.Request.Context(), tokenID))
}

func (session SessionData) toVo(currentToken string) OnlineUserVo {
	return OnlineUserVo{TokenID: session.TokenID, UserID: session.UserID, Username: session.Username, RealName: session.RealName, DeviceType: session.DeviceType, IP: session.IP, UserAgent: session.UserAgent, LoginAt: session.LoginAt, LastActiveAt: session.LastActiveAt, ExpireAt: session.ExpireAt, Current: session.TokenID == currentToken}
}

func (s *Server) onlineSessionRows(ctx context.Context) ([]SessionData, error) {
	rows := []SessionData{}
	iter := s.redis.Scan(ctx, 0, sessionKeyPrefix+"*", 100).Iterator()
	for iter.Next(ctx) {
		value, err := s.redis.Get(ctx, iter.Val()).Result()
		if err != nil {
			continue
		}
		var row SessionData
		if err := json.Unmarshal([]byte(value), &row); err == nil {
			rows = append(rows, row)
		}
	}
	if err := iter.Err(); err != nil {
		return nil, common.NewBusinessError(500000, "failed to read online session")
	}
	sort.Slice(rows, func(i, j int) bool { return rows[i].LastActiveAt > rows[j].LastActiveAt })
	return rows, nil
}

func filterOnlineRows(rows []SessionData, field string, value string) []SessionData {
	if strings.TrimSpace(value) == "" {
		return rows
	}
	needle := strings.ToLower(strings.TrimSpace(value))
	out := rows[:0]
	for _, row := range rows {
		var current string
		switch field {
		case "username":
			current = row.Username
		case "ip":
			current = row.IP
		case "deviceType":
			current = row.DeviceType
		}
		if strings.Contains(strings.ToLower(current), needle) {
			out = append(out, row)
		}
	}
	return out
}
