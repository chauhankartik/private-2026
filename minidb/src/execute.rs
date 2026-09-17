use crate::parser::{Statement, StatementType};
use crate::storage::{Table, ROW_SIZE, TABLE_MAX_ROWS, Row};

pub enum ExecuteResult {
    Success,
    TableFull,
    Error(String),
}

pub fn execute_statement(statement: &Statement, table: &mut Table) -> ExecuteResult {
    match statement.statement_type {
        StatementType::Insert => execute_insert(statement, table),
        StatementType::Select => execute_select(statement, table),
    }
}

fn execute_insert(statement: &Statement, table: &mut Table) -> ExecuteResult {
    if table.num_rows >= TABLE_MAX_ROWS {
        return ExecuteResult::TableFull;
    }

    let row_to_insert = statement.row_to_insert.as_ref().unwrap();
    let row_bytes = row_to_insert.to_bytes();

    match table.row_slot(table.num_rows) {
        Ok((page, byte_offset)) => {
            page[byte_offset..byte_offset + ROW_SIZE].copy_from_slice(&row_bytes);
            table.num_rows += 1;
            ExecuteResult::Success
        }
        Err(e) => ExecuteResult::Error(e.to_string()),
    }
}

fn execute_select(_statement: &Statement, table: &mut Table) -> ExecuteResult {
    for i in 0..table.num_rows {
        match table.row_slot(i) {
            Ok((page, byte_offset)) => {
                let row = Row::from_bytes(&page[byte_offset..byte_offset + ROW_SIZE]);
                println!("({}, {}, {})", row.id, row.get_username(), row.get_email());
            }
            Err(e) => return ExecuteResult::Error(e.to_string()),
        }
    }
    ExecuteResult::Success
}
