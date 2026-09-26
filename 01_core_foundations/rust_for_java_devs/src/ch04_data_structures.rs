// Java: Classes
// Rust: Structs for data
struct User {
    username: String,
    email: String,
    sign_in_count: u64,
    active: bool,
}

// Java: Simple Enums
// Rust: Powerful Enums (Algebraic Data Types)
enum Message {
    Quit,
    Move { x: i32, y: i32 }, // Anonymous struct inside enum
    Write(String),           // Holds a String
    ChangeColor(i32, i32, i32), // Holds a tuple
}

pub fn run() {
    println!("--- Chapter 4: Data Structures ---");
    let user1 = User {
        email: String::from("someone@example.com"),
        username: String::from("someusername123"),
        active: true,
        sign_in_count: 1,
    };
    println!("User {} ({})", user1.username, user1.email);

    let msg = Message::Write(String::from("hello enum"));
    if let Message::Write(text) = msg {
         println!("Message holds: {}", text);
    }
    println!();
}
