import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

/**
 * ============================================================
 *  04. BYTECODE INSTRUMENTATION & DYNAMIC REDEFINITION
 * ============================================================
 *
 *  Demonstrates:
 *   1. Bytecode manipulation and instrumentation using java.lang.instrument.
 *   2. Implementing ClassFileTransformer to intercept class loading.
 *   3. How APM agents (NewRelic, Datadog, Dynatrace, ByteBuddy, ASM)
 *      modify class bytecode dynamically as ClassLoaders load bytes into JVM.
 * ============================================================
 */
class BytecodeRedefinitionAgentDemo {

    public static void main(String[] args) throws Exception {
        System.out.println("=== BYTECODE INSTRUMENTATION DEMO ===");

        // 1. Create custom agent transformer
        LoggingClassTransformer transformer = new LoggingClassTransformer();

        // 2. Simulate ClassLoader passing bytecode through transformer chain
        byte[] originalBytecode = new byte[]{0x00, 0x01, 0x02}; // Dummy bytecode representation
        byte[] transformedBytecode = transformer.transform(
            ClassLoader.getSystemClassLoader(),
            "com/example/OrderService",
            null,
            null,
            originalBytecode
        );

        System.out.println("Original Bytecode Size   : " + originalBytecode.length);
        System.out.println("Transformed Bytecode Size: " + transformedBytecode.length);
        System.out.println("Transformation Applied   : " + (transformedBytecode != originalBytecode));
    }

    /**
     * Custom ClassFileTransformer that intercepts class loading and inspects bytecode.
     */
    public static class LoggingClassTransformer implements ClassFileTransformer {

        @Override
        public byte[] transform(
            ClassLoader loader,
            String className,
            Class<?> classBeingRedefined,
            ProtectionDomain protectionDomain,
            byte[] classfileBuffer
        ) throws IllegalClassFormatException {

            if (className != null && className.startsWith("com/example/")) {
                System.out.println("[JVM Agent] Intercepted Class Loading: " + className);
                System.out.println("[JVM Agent] ClassLoader: " + loader);
                System.out.println("[JVM Agent] Original Bytecode Size: " + classfileBuffer.length + " bytes");

                // In a production agent (using ByteBuddy or ASM), we would insert
                // entry/exit timing logs or security checks here.
                return classfileBuffer; // Return modified or original buffer
            }

            return null; // Return null indicates no transformation
        }
    }
}
