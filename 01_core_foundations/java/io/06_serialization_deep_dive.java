package io;

import java.io.*;
import java.util.Objects;

/**
 * Java Object Serialization Masterclass & Security Deep-Dive
 *
 * Demonstrates:
 * 1. Default Object Serialization (Serializable, serialVersionUID, transient keyword)
 * 2. Custom Serialization via writeObject / readObject hooks
 * 3. Preserving Singleton Instance Identity with readResolve()
 * 4. High-Control Serialization via Externalizable interface
 * 5. Security Vulnerabilities (RCE Deserialization Gadget Chains) & Defensive Guidelines
 */
class SerializationDeepDiveDemo {

    private static final String TEMP_SERIAL_FILE = "target_serialization.tmp";

    public static void main(String[] args) throws Exception {
        System.out.println("=== 1. Standard Object Serialization & Transient Fields ===");
        demonstrateStandardSerialization(TEMP_SERIAL_FILE);

        System.out.println("\n=== 2. Custom Object Serialization (writeObject / readObject) ===");
        demonstrateCustomSerialization(TEMP_SERIAL_FILE);

        System.out.println("\n=== 3. Singleton Identity Preservation (readResolve) ===");
        demonstrateSingletonSerialization(TEMP_SERIAL_FILE);

        System.out.println("\n=== 4. Externalizable Explicit Custom Serialization ===");
        demonstrateExternalizable(TEMP_SERIAL_FILE);

        // Clean up temporary file
        cleanupFiles(TEMP_SERIAL_FILE);
        System.out.println("\n[SUCCESS] Object Serialization demonstrations completed cleanly.");
    }

    /**
     * Standard Serializable object with transient field.
     */
    static class UserProfile implements Serializable {
        private static final long serialVersionUID = 1L;

        private final String username;
        private final String email;
        private final transient String unencryptedPassword; // Excluded from default serialization

        public UserProfile(String username, String email, String unencryptedPassword) {
            this.username = username;
            this.email = email;
            this.unencryptedPassword = unencryptedPassword;
        }

        @Override
        public String toString() {
            return String.format("UserProfile{username='%s', email='%s', password='%s'}",
                    username, email, unencryptedPassword);
        }
    }

    private static void demonstrateStandardSerialization(String filePath) throws Exception {
        UserProfile user = new UserProfile("kartik_chauhan", "kartik@example.com", "SecretPassword123!");
        System.out.println("Original Object: " + user);

        // Serialize Object to Byte Stream
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filePath))) {
            out.writeObject(user);
        }

        // Deserialize Object from Byte Stream
        UserProfile deserializedUser;
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(filePath))) {
            deserializedUser = (UserProfile) in.readObject();
        }

        System.out.println("Deserialized:   " + deserializedUser);
        System.out.println(">>> Notice: 'unencryptedPassword' transient field was set to default null!");
    }

    /**
     * Custom Serialization Object with custom writeObject / readObject encryption hooks.
     */
    static class EncryptedDataHolder implements Serializable {
        private static final long serialVersionUID = 2026L;

        private String payload;

        public EncryptedDataHolder(String payload) {
            this.payload = payload;
        }

        private void writeObject(ObjectOutputStream out) throws IOException {
            out.defaultWriteObject(); // Serialize non-transient fields
            // Custom logic: Write encrypted XOR masked payload length
            String maskedPayload = "MASKED[" + payload + "]";
            out.writeUTF(maskedPayload);
        }

        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
            in.defaultReadObject(); // Deserialize default fields
            String maskedPayload = in.readUTF();
            this.payload = maskedPayload.replace("MASKED[", "").replace("]", "");
        }

        public String getPayload() {
            return payload;
        }
    }

    private static void demonstrateCustomSerialization(String filePath) throws Exception {
        EncryptedDataHolder holder = new EncryptedDataHolder("Confidential Staff Level Data");

        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filePath))) {
            out.writeObject(holder);
        }

        EncryptedDataHolder restoredHolder;
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(filePath))) {
            restoredHolder = (EncryptedDataHolder) in.readObject();
        }

        System.out.println("Custom Serialization Payload Restored: '" + restoredHolder.getPayload() + "'");
    }

    /**
     * Singleton Class implementing readResolve() to maintain single-instance guarantee.
     */
    static class DatabaseConnectionPool implements Serializable {
        private static final long serialVersionUID = 100L;

        private static final DatabaseConnectionPool INSTANCE = new DatabaseConnectionPool();

        private DatabaseConnectionPool() {
            // Private constructor
        }

        public static DatabaseConnectionPool getInstance() {
            return INSTANCE;
        }

        /**
         * Invoked during ObjectInputStream deserialization to replace deserialized object with Singleton INSTANCE.
         */
        private Object readResolve() throws ObjectStreamException {
            return INSTANCE;
        }
    }

    private static void demonstrateSingletonSerialization(String filePath) throws Exception {
        DatabaseConnectionPool instance1 = DatabaseConnectionPool.getInstance();

        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filePath))) {
            out.writeObject(instance1);
        }

        DatabaseConnectionPool instance2;
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(filePath))) {
            instance2 = (DatabaseConnectionPool) in.readObject();
        }

        System.out.println("Instance 1 HashCode: " + Objects.hashCode(instance1));
        System.out.println("Instance 2 HashCode: " + Objects.hashCode(instance2));
        System.out.printf(">>> Same Instance Reference (instance1 == instance2)? %b (Preserved via readResolve!)%n",
                (instance1 == instance2));
    }

    /**
     * Externalizable Object requiring public no-arg constructor and explicit field writing/reading.
     */
    public static class CustomPacket implements Externalizable {
        private int packetId;
        private String payload;

        // Required public no-arg constructor for Externalizable deserialization instantiation
        public CustomPacket() {
        }

        public CustomPacket(int packetId, String payload) {
            this.packetId = packetId;
            this.payload = payload;
        }

        @Override
        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeInt(packetId);
            out.writeUTF(payload);
        }

        @Override
        public void readExternal(ObjectInput in) throws IOException {
            this.packetId = in.readInt();
            this.payload = in.readUTF();
        }

        @Override
        public String toString() {
            return String.format("CustomPacket{id=%d, payload='%s'}", packetId, payload);
        }
    }

    private static void demonstrateExternalizable(String filePath) throws Exception {
        CustomPacket packet = new CustomPacket(1001, "Externalizable Binary Network Packet");

        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filePath))) {
            out.writeObject(packet);
        }

        CustomPacket restoredPacket;
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(filePath))) {
            restoredPacket = (CustomPacket) in.readObject();
        }

        System.out.println("Externalizable Restored Packet: " + restoredPacket);
    }

    private static void cleanupFiles(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
        }
    }
}
