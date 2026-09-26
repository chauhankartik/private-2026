mod ch02_variables;
mod ch03_ownership;
mod ch04_data_structures;
mod ch05_control_flow;
mod ch06_polymorphism;
mod ch07_error_handling;
mod ch08_concurrency;

fn main() {
    println!("=====================================");
    println!("    Rust for Java Developers Demo    ");
    println!("=====================================\n");

    ch02_variables::run();
    ch03_ownership::run();
    ch04_data_structures::run();
    ch05_control_flow::run();
    ch06_polymorphism::run();
    ch07_error_handling::run();
    ch08_concurrency::run();
}
