package io;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * Classic Java I/O Streams Masterclass (java.io)
 *
 * Demonstrates:
 * 1. Byte Streams (FileInputStream, FileOutputStream) vs Buffered Streams (BufferedInputStream, BufferedOutputStream)
 * 2. Character Streams (FileReader, FileWriter, BufferedReader, BufferedWriter) & Charsets
 * 3. Decorator Pattern Composition in java.io
 * 4. Micro-benchmark measuring System Call Overhead Reduction via User-Space Buffering
 * 5. Auto-closing resource management (Try-With-Resources)
 */
class ClassicIOStreamsDemo {

    private static final String TEMP_BYTE_FILE = "target_classic_bytes.tmp";
    private static final String TEMP_CHAR_FILE = "target_classic_chars.tmp";
    private static final int FILE_SIZE_BYTES = 2 * 1024 * 1024; // 2 MB

    public static void main(String[] args) {
        System.out.println("=== 1. Classic java.io Byte Streams & Decorator Pattern ===");
        createTestFile(TEMP_BYTE_FILE, FILE_SIZE_BYTES);

        System.out.println("\n=== 2. Benchmarking Unbuffered vs Buffered Read Performance ===");
        benchmarkUnbufferedVsBuffered(TEMP_BYTE_FILE);

        System.out.println("\n=== 3. Character Streams & UTF-8 Encoding ===");
        demonstrateCharacterStreams(TEMP_CHAR_FILE);

        System.out.println("\n=== 4. DataInputStream / DataOutputStream Primtives ===");
        demonstrateDataStreams("target_data_primitives.tmp");

        // Clean up temporary files
        cleanupFiles(TEMP_BYTE_FILE, TEMP_CHAR_FILE, "target_data_primitives.tmp");
        System.out.println("\n[SUCCESS] Classic I/O Stream demonstrations completed cleanly.");
    }

    /**
     * Creates a dummy binary test file of specified size.
     */
    private static void createTestFile(String filePath, int sizeInBytes) {
        try (OutputStream out = new BufferedOutputStream(new FileOutputStream(filePath))) {
            byte[] pattern = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789\n".getBytes(StandardCharsets.UTF_8);
            int written = 0;
            while (written < sizeInBytes) {
                int toWrite = Math.min(pattern.length, sizeInBytes - written);
                out.write(pattern, 0, toWrite);
                written += toWrite;
            }
            out.flush();
            System.out.printf("Created binary test file '%s' (%d KB)%n", filePath, sizeInBytes / 1024);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to create test file", e);
        }
    }

    /**
     * Compares performance of reading a 2 MB file byte-by-byte unbuffered vs buffered.
     */
    private static void benchmarkUnbufferedVsBuffered(String filePath) {
        // Unbuffered Read (High OS System Call Overhead)
        long startTime = System.currentTimeMillis();
        long unbufferedByteCount = 0;
        try (InputStream in = new FileInputStream(filePath)) {
            int b;
            while ((b = in.read()) != -1) {
                unbufferedByteCount++;
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        long unbufferedDuration = System.currentTimeMillis() - startTime;
        System.out.printf("Unbuffered FileInputStream (byte-by-byte): Read %d bytes in %d ms%n",
                unbufferedByteCount, unbufferedDuration);

        // Buffered Read (Decorated with 8 KB User-Space Buffer)
        startTime = System.currentTimeMillis();
        long bufferedByteCount = 0;
        try (InputStream in = new BufferedInputStream(new FileInputStream(filePath), 8192)) {
            int b;
            while ((b = in.read()) != -1) {
                bufferedByteCount++;
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        long bufferedDuration = System.currentTimeMillis() - startTime;
        System.out.printf("BufferedInputStream (8KB buffer):          Read %d bytes in %d ms%n",
                bufferedByteCount, bufferedDuration);

        double speedup = (double) unbufferedDuration / Math.max(1, bufferedDuration);
        System.out.printf(">>> Buffering Speedup Factor: %.2fx faster due to reduced OS syscalls!%n", speedup);
    }

    /**
     * Demonstrates Character Readers/Writers and UTF-8 handling.
     */
    private static void demonstrateCharacterStreams(String filePath) {
        String content = "Hello World! Special Chars: Java File I/O Masterclass\n";

        // Writing text using BufferedWriter & OutputStreamWriter with explicit UTF-8 encoding
        try (Writer writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(filePath), StandardCharsets.UTF_8))) {
            writer.write(content);
            writer.write("Line 2: Character stream decorator pattern in action.\n");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        // Reading text line by line using BufferedReader
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8))) {
            System.out.println("Read Text File Contents:");
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("  [LINE]: " + line);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Demonstrates typed primitive data serialization using DataStreams.
     */
    private static void demonstrateDataStreams(String filePath) {
        // Write structured binary data
        try (DataOutputStream out = new DataOutputStream(
                new BufferedOutputStream(new FileOutputStream(filePath)))) {
            out.writeInt(42);
            out.writeDouble(3.1415926535);
            out.writeBoolean(true);
            out.writeUTF("Structured Typed Primitive String");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        // Read structured binary data in exact written order
        try (DataInputStream in = new DataInputStream(
                new BufferedInputStream(new FileInputStream(filePath)))) {
            int intVal = in.readInt();
            double doubleVal = in.readDouble();
            boolean boolVal = in.readBoolean();
            String stringVal = in.readUTF();

            System.out.printf("DataInputStream Output -> Int: %d, Double: %.5f, Bool: %b, UTF: '%s'%n",
                    intVal, doubleVal, boolVal, stringVal);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static void cleanupFiles(String... filePaths) {
        for (String path : filePaths) {
            File file = new File(path);
            if (file.exists()) {
                file.delete();
            }
        }
    }
}
