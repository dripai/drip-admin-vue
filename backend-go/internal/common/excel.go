package common

import (
	"bytes"
	"strings"

	"github.com/xuri/excelize/v2"
)

type ExportColumnRequest struct {
	Key   string `json:"key"`
	Title string `json:"title"`
}

type ExportColumn[T any] struct {
	Value func(T) any
}

func BuildExcel[T any](rows []T, columns []ExportColumnRequest, allowed map[string]ExportColumn[T], maxRows int) ([]byte, error) {
	if maxRows <= 0 {
		return nil, NewBusinessError(500000, "excel export max rows invalid")
	}
	if len(rows) > maxRows {
		return nil, NewBusinessError(400000, "excel export rows exceed limit")
	}
	if len(columns) == 0 {
		return nil, NewBusinessError(400000, "export columns required")
	}
	file := excelize.NewFile()
	sheet := "Sheet1"
	for index, col := range columns {
		key := strings.TrimSpace(col.Key)
		title := strings.TrimSpace(col.Title)
		resolver, ok := allowed[key]
		if !ok || key == "" {
			return nil, NewBusinessError(400000, "export column not allowed")
		}
		if title == "" {
			return nil, NewBusinessError(400000, "column title is required")
		}
		cell, _ := excelize.CoordinatesToCellName(index+1, 1)
		_ = file.SetCellValue(sheet, cell, title)
		columns[index] = ExportColumnRequest{Key: key, Title: title}
		allowed[key] = resolver
	}
	for rowIndex, row := range rows {
		for colIndex, col := range columns {
			cell, _ := excelize.CoordinatesToCellName(colIndex+1, rowIndex+2)
			_ = file.SetCellValue(sheet, cell, allowed[col.Key].Value(row))
		}
	}
	var out bytes.Buffer
	if err := file.Write(&out); err != nil {
		return nil, err
	}
	return out.Bytes(), nil
}
