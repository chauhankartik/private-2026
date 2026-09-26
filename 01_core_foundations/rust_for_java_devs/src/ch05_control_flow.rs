// Continuing from ch04's Message enum conceptually
enum Coin {
    Penny,
    Nickel,
    Dime,
    Quarter,
}

fn value_in_cents(coin: Coin) -> u8 {
    // Java: switch statement
    // Rust: match statement (Must be exhaustive!)
    match coin {
        Coin::Penny => {
            println!("Lucky penny!");
            1
        }
        Coin::Nickel => 5,
        Coin::Dime => 10,
        Coin::Quarter => 25,
    }
}

pub fn run() {
    println!("--- Chapter 5: Control Flow & Pattern Matching ---");
    let val = value_in_cents(Coin::Penny);
    println!("Coin value: {}", val);

    // Using match with Option
    let some_number = Some(5);
    match some_number {
        Some(x) => println!("Got a number: {}", x),
        None => println!("Got nothing!"),
    }
    println!();
}
