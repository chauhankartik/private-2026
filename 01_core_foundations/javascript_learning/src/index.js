import { run as runCh01 } from './ch01_event_loop.js';
import { run as runCh02 } from './ch02_scope_closures.js';
import { run as runCh03 } from './ch03_types_coercion.js';
import { run as runCh04 } from './ch04_objects_prototypes.js';
import { run as runCh05 } from './ch05_functions_this.js';
import { run as runCh06 } from './ch06_async_await.js';
import { run as runCh07 } from './ch07_modules.js';

async function main() {
    console.log("=====================================");
    console.log("  JavaScript Learning Examples Demo  ");
    console.log("=====================================\n");
    
    await runCh01();
    await runCh02();
    await runCh03();
    await runCh04();
    await runCh05();
    await runCh06();
    await runCh07();
}

main();
