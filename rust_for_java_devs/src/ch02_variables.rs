pub fn run() {
    println!("--- Chapter 2: Variables & Mutability ---");
    // In Java, variables are mutable by default.
    // In Rust, they are immutable by default.
    let immutable_var = 10;
    println!("Immutable var: {}", immutable_var);
    // immutable_var = 20; // This would cause a compile error

    let mut mutable_var = 20;
    println!("Mutable var before: {}", mutable_var);
    mutable_var = 30;
    println!("Mutable var after: {}", mutable_var);

    // Strings
    // Java: String name = "Alice";
    // Rust has &str (string slice, fixed size/reference) and String (heap allocated, growable)
    let string_slice: &str = "Hello"; // Usually baked into the binary or references a String
    let mut growable_string: String = String::from("World");
    growable_string.push_str("!");
    
    println!("{} {}", string_slice, growable_string);
    println!();
}
