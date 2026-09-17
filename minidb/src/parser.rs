use crate::storage::Row;

pub enum StatementType {
    Insert,
    Select,
}

pub struct Statement {
    pub statement_type: StatementType,
    pub row_to_insert: Option<Row>,
}

pub enum PrepareError {
    UnrecognizedStatement,
    SyntaxError,
}

pub fn prepare_statement(input: &str) -> Result<Statement, PrepareError> {
    if input.starts_with("insert") {
        let parts: Vec<&str> = input.split_whitespace().collect();
        if parts.len() < 4 {
            return Err(PrepareError::SyntaxError);
        }
        
        let id = parts[1].parse::<u32>().map_err(|_| PrepareError::SyntaxError)?;
        let username = parts[2];
        let email = parts[3];
        
        let row = Row::new(id, username, email);
        
        Ok(Statement {
            statement_type: StatementType::Insert,
            row_to_insert: Some(row),
        })
    } else if input.starts_with("select") {
        Ok(Statement {
            statement_type: StatementType::Select,
            row_to_insert: None,
        })
    } else {
        Err(PrepareError::UnrecognizedStatement)
    }
}
