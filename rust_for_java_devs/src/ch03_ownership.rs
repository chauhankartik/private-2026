pub fn run() {
    println!("--- Chapter 3: Ownership & Borrowing ---");
    // Java: Garbage Collector cleans up.
    // Rust: Ownership rules.
    
    let s1 = String::from("hello");
    let s2 = s1; // s1's value is MOVED to s2. s1 is no longer valid.
    // println!("s1 = {}", s1); // This would not compile!
    println!("s2 = {}", s2);

    // Borrowing (References)
    let s3 = String::from("borrow me");
    let len = calculate_length(&s3); // Passing a reference (borrowing)
    println!("The length of '{}' is {}.", s3, len);

    // Mutable Borrowing
    let mut s4 = String::from("hello");
    change(&mut s4);
    println!("Changed string: {}", s4);
    println!();
}

fn calculate_length(s: &String) -> usize { // s is a reference to a String
    s.len()
} // Here, s goes out of scope. But because it does not have ownership of what it refers to, nothing happens.

fn change(some_string: &mut String) {
    some_string.push_str(", world");
}
