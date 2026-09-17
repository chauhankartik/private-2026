export async function run() {
    console.log("--- Chapter 1: The Event Loop ---");
    return new Promise((resolve) => {
        console.log("1. Script start");

        setTimeout(() => {
            console.log("3. Timeout finished");
            console.log();
            resolve();
        }, 100); 

        console.log("2. Script end");
    });
}
