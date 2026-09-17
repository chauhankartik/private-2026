export async function run() {
    console.log("--- Chapter 2: Scope & Closures ---");
    
    // Block Scope
    if (true) {
        var oldSchool = "I leak out of the block";
        let modern = "I am trapped in the block";
        const constant = "I am trapped and cannot be reassigned";
    }
    console.log(oldSchool); // Works
    // console.log(modern); // ReferenceError if uncommented

    // Closures
    function createCounter() {
        let count = 0; 
        return function() {
            count++;
            return count;
        };
    }

    const counter = createCounter();
    console.log("Counter call 1:", counter());
    console.log("Counter call 2:", counter());
    console.log();
}
