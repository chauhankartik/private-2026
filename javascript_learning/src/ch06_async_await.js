export async function run() {
    console.log("--- Chapter 6: Async / Await ---");
    
    function fetchUserData(userId) {
        return new Promise((resolve, reject) => {
            setTimeout(() => {
                if (userId === 1) resolve({ id: 1, name: "Kartik" });
                else reject("User not found!");
            }, 100);
        });
    }

    try {
        const data = await fetchUserData(1);
        console.log("Async/Await Success:", data.name);
        
        const badData = await fetchUserData(2); // Will throw
    } catch (error) {
        console.log("Caught Error:", error);
    }
    console.log();
}
