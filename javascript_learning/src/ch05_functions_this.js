export async function run() {
    console.log("--- Chapter 5: Functions and 'this' ---");
    return new Promise((resolve) => {
        const person = {
            name: "Bob",
            
            sayName: function() {
                console.log("Standard function:", this.name);
            },
            
            sayNameArrow: () => {
                console.log("Arrow function:", this === undefined ? "undefined 'this'" : this.name);
            },
            
            delayedGreet: function() {
                setTimeout(() => {
                    console.log(`Delayed Hello from ${this.name} (Arrow callback preserved 'this')`);
                    console.log();
                    resolve();
                }, 100);
            }
        };

        person.sayName();
        person.sayNameArrow();
        person.delayedGreet();
    });
}
