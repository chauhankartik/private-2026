use std::fs::File;
use std::io::ErrorKind;

// Java: Exceptions (try/catch)
// Rust: Result<T, E> and Option<T>

fn divide(a: f64, b: f64) -> Result<f64, String> {
    if b == 0.0 {
        Err(String::from("Cannot divide by zero"))
    } else {
        Ok(a / b)
    }
}

pub fn run() {
    println!("--- Chapter 7: Error Handling ---");
    
    // Pattern matching on Result
    let f = File::open("hello.txt");

    if let Err(error) = f {
        if error.kind() == ErrorKind::NotFound {
            println!("File not found, which is expected for this demo.");
        } else {
            println!("Problem opening the file: {:?}", error);
        }
    }

    let result = divide(10.0, 2.0);
    match result {
        Ok(val) => println!("Result: {}", val),
        Err(e) => println!("Error: {}", e),
    }

    println!();
}
