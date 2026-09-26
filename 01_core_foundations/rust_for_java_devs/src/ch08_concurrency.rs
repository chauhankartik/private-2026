use std::thread;
use std::sync::{Arc, Mutex};

pub fn run() {
    println!("--- Chapter 8: Concurrency ---");
    // Java: new Thread(() -> { ... }).start();
    // Rust: thread::spawn
    
    // Shared state in Java: volatile or synchronized
    // Shared state in Rust: Arc (Atomic Reference Counting) and Mutex
    let counter = Arc::new(Mutex::new(0));
    let mut handles = vec![];

    for _ in 0..10 {
        let counter = Arc::clone(&counter);
        let handle = thread::spawn(move || {
            let mut num = counter.lock().unwrap();
            *num += 1;
        });
        handles.push(handle);
    }

    for handle in handles {
        handle.join().unwrap();
    }

    println!("Result of 10 threads incrementing: {}", *counter.lock().unwrap());
    println!();
}
