export async function run() {
    console.log("--- Chapter 3: Types & Coercion ---");
    
    console.log('5 == "5" is', 5 == "5");   // true (loose)
    console.log('5 === "5" is', 5 === "5"); // false (strict)
    
    console.log('1 + "2" =', 1 + "2"); // "12"
    console.log('"5" - 1 =', "5" - 1); // 4
    
    if (!undefined) {
        console.log("Undefined is falsy!");
    }
    console.log();
}
