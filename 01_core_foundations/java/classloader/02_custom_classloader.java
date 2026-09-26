import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Method;

/**
 * ============================================================
 *  02. CUSTOM ENCRYPTED CLASSLOADER
 * ============================================================
 *
 *  Demonstrates:
 *   1. How to create a custom ClassLoader by extending java.lang.ClassLoader.
 *   2. Overriding findClass(String name) to preserve Parent-First delegation.
 *   3. Decrypting bytecode on-the-fly before invoking defineClass().
 *   4. Instantiating objects and calling methods reflection-style on classes
 *      loaded from custom locations.
 * ============================================================
 */
class CustomEncryptedClassLoaderDemo {

    // Simple XOR Cipher Key for bytecode obfuscation/encryption
    private static final byte KEY = (byte) 0xAA;

    public static void main(String[] args) throws Exception {
        System.out.println("=== CUSTOM ENCRYPTED CLASSLOADER DEMO ===");

        // 1. Obtain bytecode of a target sample class
        byte[] originalBytecode = loadSampleBytecode();
        System.out.println("Original Bytecode Length: " + originalBytecode.length + " bytes");

        // 2. Encrypt bytecode using XOR cipher
        byte[] encryptedBytecode = encryptBytecode(originalBytecode);
        System.out.println("Encrypted Bytecode Length: " + encryptedBytecode.length + " bytes");

        // 3. Instantiate EncryptedClassLoader
        EncryptedClassLoader classLoader = new EncryptedClassLoader(
            "SecretClassLoader",
            SampleService.class.getName(),
            encryptedBytecode
        );

        // 4. Load class dynamically through custom ClassLoader
        Class<?> loadedClass = classLoader.loadClass(SampleService.class.getName());
        System.out.println("Class Successfully Loaded: " + loadedClass.getName());
        System.out.println("Loaded by ClassLoader    : " + loadedClass.getClassLoader());

        // 5. Instantiate and execute method via reflection
        Object instance = loadedClass.getDeclaredConstructor().newInstance();
        Method method = loadedClass.getMethod("executeTask");
        String result = (String) method.invoke(instance);
        System.out.println("Execution Output         : " + result);
    }

    /**
     * Custom ClassLoader that decrypts encrypted bytecode before defining the class.
     */
    public static class EncryptedClassLoader extends ClassLoader {
        private final String name;
        private final String targetClassName;
        private final byte[] encryptedBytecode;

        public EncryptedClassLoader(String name, String targetClassName, byte[] encryptedBytecode) {
            super(ClassLoader.getSystemClassLoader()); // Parent = AppClassLoader
            this.name = name;
            this.targetClassName = targetClassName;
            this.encryptedBytecode = encryptedBytecode;
        }

        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            if (name.equals(targetClassName)) {
                // Decrypt bytecode on-the-fly
                byte[] decryptedBytecode = decryptBytecode(encryptedBytecode);

                // Convert raw byte array into java.lang.Class instance
                return defineClass(name, decryptedBytecode, 0, decryptedBytecode.length);
            }
            throw new ClassNotFoundException("Class " + name + " not found in " + this.name);
        }

        private byte[] decryptBytecode(byte[] bytes) {
            byte[] decrypted = new byte[bytes.length];
            for (int i = 0; i < bytes.length; i++) {
                decrypted[i] = (byte) (bytes[i] ^ KEY); // XOR decryption
            }
            return decrypted;
        }

        @Override
        public String toString() {
            return "EncryptedClassLoader{" + name + "}";
        }
    }

    /**
     * Target sample service class to be encrypted and loaded.
     */
    public static class SampleService {
        public String executeTask() {
            return "SampleService executed successfully from decrypted bytecode!";
        }
    }

    private static byte[] encryptBytecode(byte[] original) {
        byte[] encrypted = new byte[original.length];
        for (int i = 0; i < original.length; i++) {
            encrypted[i] = (byte) (original[i] ^ KEY);
        }
        return encrypted;
    }

    private static byte[] loadSampleBytecode() throws Exception {
        String path = SampleService.class.getName().replace('.', '/') + ".class";
        try (var is = ClassLoader.getSystemResourceAsStream(path);
             var baos = new ByteArrayOutputStream()) {
            if (is == null) throw new ClassNotFoundException("Resource not found: " + path);
            byte[] buf = new byte[4096];
            int r;
            while ((r = is.read(buf)) != -1) baos.write(buf, 0, r);
            return baos.toByteArray();
        }
    }
}
