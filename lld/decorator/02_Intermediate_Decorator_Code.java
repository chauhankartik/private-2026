import java.io.*;
import java.util.Base64;

/**
 * ============================================================
 *  INTERMEDIATE DECORATOR PATTERN — REAL-WORLD DATA PIPELINE
 * ============================================================
 *
 * Now that you understand the coffee analogy, let's apply the
 * exact same pattern to something Google actually uses: 
 * a Data Processing Pipeline.
 *
 * Scenario: You have raw text data. You want to process it 
 * through multiple transformations:
 *   1. Compress it
 *   2. Encrypt it
 *   3. Add logging around each step
 *
 * Each step wraps the previous one — classic Decorator.
 *
 * This is EXACTLY how Java I/O Streams work under the hood.
 */

// ----------------------------------------------------
// 1. Component Interface 
// ----------------------------------------------------
interface DataSource {
    void writeData(String data);
    String readData();
}

// ----------------------------------------------------
// 2. Concrete Component (The Base — reads/writes a file)
// ----------------------------------------------------
class FileDataSource implements DataSource {
    private String filename;
    private String storedData; // simulating a file

    public FileDataSource(String filename) {
        this.filename = filename;
    }

    @Override
    public void writeData(String data) {
        storedData = data;
        System.out.println("  [FileDataSource] Wrote raw data to " + filename);
    }

    @Override
    public String readData() {
        System.out.println("  [FileDataSource] Read raw data from " + filename);
        return storedData;
    }
}

// ----------------------------------------------------
// 3. Abstract Decorator
// ----------------------------------------------------
abstract class DataSourceDecorator implements DataSource {
    protected DataSource wrappee;

    public DataSourceDecorator(DataSource source) {
        this.wrappee = source;
    }

    @Override
    public void writeData(String data) {
        wrappee.writeData(data);
    }

    @Override
    public String readData() {
        return wrappee.readData();
    }
}

// ----------------------------------------------------
// 4. Concrete Decorators
// ----------------------------------------------------

/**
 * DECORATOR A: Base64 Encoding (simulating encryption)
 *
 * On write: encode the data BEFORE passing it to the inner source.
 * On read:  decode the data AFTER getting it from the inner source.
 *
 * Notice: write transforms BEFORE delegating (pre-processing)
 *         read  transforms AFTER  delegating (post-processing)
 */
class EncryptionDecorator extends DataSourceDecorator {

    public EncryptionDecorator(DataSource source) {
        super(source);
    }

    @Override
    public void writeData(String data) {
        // Transform BEFORE delegating down
        String encrypted = Base64.getEncoder().encodeToString(data.getBytes());
        System.out.println("  [EncryptionDecorator] Encrypted data");
        wrappee.writeData(encrypted);
    }

    @Override
    public String readData() {
        // Delegate down FIRST, then transform the result
        String data = wrappee.readData();
        String decrypted = new String(Base64.getDecoder().decode(data));
        System.out.println("  [EncryptionDecorator] Decrypted data");
        return decrypted;
    }
}

/**
 * DECORATOR B: Compression (simulating with a simple transform)
 */
class CompressionDecorator extends DataSourceDecorator {

    public CompressionDecorator(DataSource source) {
        super(source);
    }

    @Override
    public void writeData(String data) {
        // In real life, this would use GZIPOutputStream
        String compressed = "[COMPRESSED:" + data.length() + "]" + data;
        System.out.println("  [CompressionDecorator] Compressed data (" + data.length() + " chars)");
        wrappee.writeData(compressed);
    }

    @Override
    public String readData() {
        String data = wrappee.readData();
        // Strip our compression header
        String decompressed = data.substring(data.indexOf("]") + 1);
        System.out.println("  [CompressionDecorator] Decompressed data");
        return decompressed;
    }
}

/**
 * DECORATOR C: Logging (adds behavior WITHOUT modifying data)
 *
 * This decorator doesn't change the data at all. It just logs timing info.
 * This is useful for monitoring and debugging.
 */
class LoggingDecorator extends DataSourceDecorator {

    public LoggingDecorator(DataSource source) {
        super(source);
    }

    @Override
    public void writeData(String data) {
        long start = System.nanoTime();
        System.out.println("  [LoggingDecorator] Write operation started");
        wrappee.writeData(data);
        long elapsed = System.nanoTime() - start;
        System.out.println("  [LoggingDecorator] Write completed in " + elapsed / 1000 + "µs");
    }

    @Override
    public String readData() {
        long start = System.nanoTime();
        System.out.println("  [LoggingDecorator] Read operation started");
        String data = wrappee.readData();
        long elapsed = System.nanoTime() - start;
        System.out.println("  [LoggingDecorator] Read completed in " + elapsed / 1000 + "µs");
        return data;
    }
}

// ----------------------------------------------------
// 5. Let's Run It!
// ----------------------------------------------------
public class IntermediateDecoratorDemo {
    public static void main(String[] args) {

        System.out.println("=== SCENARIO 1: Plain file write (no decoration) ===\n");
        DataSource plain = new FileDataSource("data.txt");
        plain.writeData("Hello Google Interview!");
        System.out.println("Read back: " + plain.readData());

        System.out.println("\n=== SCENARIO 2: File + Encryption ===\n");
        DataSource encrypted = new EncryptionDecorator(
                                    new FileDataSource("secret.txt")
                               );
        encrypted.writeData("My secret password");
        System.out.println("Read back: " + encrypted.readData());

        System.out.println("\n=== SCENARIO 3: File + Compression + Encryption + Logging ===\n");
        // Reading inside-out:
        //   FileDataSource → wrapped with Encryption → wrapped with Compression → wrapped with Logging
        //
        // On WRITE, data flows: Logging → Compression → Encryption → File
        // On READ,  data flows: Logging → File → Encryption → Compression → (returned to us)
        DataSource fullyDecorated = new LoggingDecorator(
                                        new CompressionDecorator(
                                            new EncryptionDecorator(
                                                new FileDataSource("production.dat")
                                            )
                                        )
                                    );
        fullyDecorated.writeData("Critical production data for Google SRE team");
        System.out.println("\nRead back: " + fullyDecorated.readData());
    }
}

/**
 * ⚠️ STAFF-LEVEL INSIGHT: ORDER MATTERS!
 *
 * new CompressionDecorator(new EncryptionDecorator(file))
 *   → Encrypts first, then compresses. 
 *   → BAD! Encrypted data looks random and compresses VERY POORLY.
 *
 * new EncryptionDecorator(new CompressionDecorator(file))
 *   → Compresses first, then encrypts.
 *   → GOOD! Compressed data is smaller, then we encrypt the small payload.
 *
 * In an interview, if you mention this, you demonstrate production awareness.
 *
 *
 * 🔗 CONNECT TO JAVA I/O (what the interviewer wants to hear):
 *
 *   "This is the same pattern Java uses for I/O Streams.
 *    InputStream is the Component interface.
 *    FileInputStream is the Concrete Component.
 *    BufferedInputStream, DataInputStream, GZIPInputStream are all Decorators.
 *    They all extend FilterInputStream (the abstract decorator base class)
 *    and wrap an inner InputStream."
 */
