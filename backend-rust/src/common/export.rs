use crate::common::AppError;
use rust_xlsxwriter::Workbook;
use serde_json::Value;
use std::collections::HashMap;

#[derive(Debug, Clone)]
pub struct ExportColumnRequest {
    pub key: String,
    pub title: String,
}

pub fn export_rows(
    rows: &[HashMap<String, Value>],
    selected_columns: &[ExportColumnRequest],
    allowed_keys: &[&str],
    max_rows: usize,
) -> Result<Vec<u8>, AppError> {
    if rows.len() > max_rows {
        return Err(AppError::bad_request("export rows exceed limit"));
    }
    let mut workbook = Workbook::new();
    let worksheet = workbook.add_worksheet();
    for (column_index, column) in selected_columns.iter().enumerate() {
        if !allowed_keys.contains(&column.key.as_str()) {
            return Err(AppError::bad_request("invalid export column"));
        }
        worksheet
            .write_string(0, column_index as u16, &column.title)
            .map_err(|err| AppError::system(err.to_string()))?;
    }
    for (row_index, row) in rows.iter().enumerate() {
        for (column_index, column) in selected_columns.iter().enumerate() {
            let value = row
                .get(&column.key)
                .map(value_to_cell)
                .unwrap_or_else(String::new);
            worksheet
                .write_string((row_index + 1) as u32, column_index as u16, value)
                .map_err(|err| AppError::system(err.to_string()))?;
        }
    }
    workbook
        .save_to_buffer()
        .map_err(|err| AppError::system(err.to_string()))
}

fn value_to_cell(value: &Value) -> String {
    match value {
        Value::Null => String::new(),
        Value::String(value) => value.clone(),
        Value::Number(value) => value.to_string(),
        Value::Bool(value) => value.to_string(),
        other => other.to_string(),
    }
}
