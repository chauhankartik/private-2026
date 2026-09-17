export async function run() {
    console.log("--- Chapter 4: Objects & Prototypes ---");
    
    // Standard Object Literal
    const user = {
        name: "Alice",
        greet() {
            console.log(`Hi, I'm ${this.name}`);
        }
    };
    
    // Prototypal Inheritance
    const admin = Object.create(user);
    admin.role = "SuperUser";
    admin.name = "Admin Alice"; // Shadowing
    admin.greet();
    
    // ES6 Classes
    class Animal {
        constructor(name) {
            this.name = name;
        }
        speak() {
            console.log(`${this.name} makes a noise.`);
        }
    }
    
    class Dog extends Animal {
        speak() {
            console.log(`${this.name} barks!`);
        }
    }
    
    const dog = new Dog("Rex");
    dog.speak();
    console.log();
}
