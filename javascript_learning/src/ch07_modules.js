import multiply, { add } from './ch07_modules_helper.js';

export async function run() {
    console.log("--- Chapter 7: Ecosystem & Modules ---");
    console.log("2 + 3 =", add(2, 3));
    console.log("2 * 3 =", multiply(2, 3));
    console.log();
}
