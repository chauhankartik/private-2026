mod parser;
mod storage;
mod execute;

use std::io::{self, Write};
use parser::{prepare_statement, PrepareError};
use storage::Table;
use execute::{execute_statement, ExecuteResult};

fn main() {
    let mut table = Table::db_open("test.db").unwrap();

    loop {
        print!("minidb > ");
        io::stdout().flush().unwrap();

        let mut input = String::new();
        if io::stdin().read_line(&mut input).is_err() {
            eprintln!("Error reading input");
            continue;
        }

        let input = input.trim();
        if input.is_empty() {
            continue;
        }

        if input.starts_with('.') {
            match input {
                ".exit" => {
                    table.db_close().unwrap();
                    println!("Bye!");
                    break;
                }
                _ => {
                    println!("Unrecognized meta-command: '{}'", input);
                }
            }
            continue;
        }

        match prepare_statement(input) {
            Ok(statement) => {
                match execute_statement(&statement, &mut table) {
                    ExecuteResult::Success => println!("Executed."),
                    ExecuteResult::TableFull => println!("Error: Table full."),
                    ExecuteResult::Error(e) => println!("Error: {}", e),
                }
            }
            Err(PrepareError::UnrecognizedStatement) => {
                println!("Unrecognized keyword at start of '{}'.", input);
            }
            Err(PrepareError::SyntaxError) => {
                println!("Syntax error. Could not parse statement.");
            }
        }
    }
}
