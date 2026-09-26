package io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;
import java.nio.charset.StandardCharsets;

/**
 * Modern Java NIO Channels & Buffers Masterclass (java.nio)
 *
 * Demonstrates:
 * 1. ByteBuffer State Machine Mechanics (Capacity, Position, Limit, Mark, flip(), clear(), compact())
 * 2. Heap ByteBuffers vs Direct Off-Heap ByteBuffers
 * 3. FileChannel File Read/Write Operations
 * 4. Scattered / Gathered Vectorized I/O (GatheringByteChannel / ScatteringByteChannel)
 */
class NIOChannelsBuffersDemo {

    private static final String TEMP_NIO_FILE = "target_nio_channel.tmp";
    private static final String TEMP_SCATTER_GATHER_FILE = "target_scatter_gather.tmp";

    public static void main(String[] args) throws IOException {
        System.out.println("=== 1. ByteBuffer State Machine Lifecycle & Operations ===");
        demonstrateBufferLifecycle();

        System.out.println("\n=== 2. FileChannel Read & Write with Heap vs Direct Buffers ===");
        demonstrateFileChannelReadWrite(TEMP_NIO_FILE);

        System.out.println("\n=== 3. Vectorized I/O: Gathering Write & Scattering Read ===");
        demonstrateScatteringAndGathering(TEMP_SCATTER_GATHER_FILE);

        // Clean up temporary files
        cleanupFiles(TEMP_NIO_FILE, TEMP_SCATTER_GATHER_FILE);
        System.out.println("\n[SUCCESS] NIO Channels & Buffers demonstrations completed cleanly.");
    }

    /**
     * Demonstrates buffer invariant state changes (capacity, position, limit, mark).
     */
    private static void demonstrateBufferLifecycle() {
        // Allocate a ByteBuffer of capacity 16
        ByteBuffer buffer = ByteBuffer.allocate(16);
        printBufferState("Initial Allocation", buffer);

        // Write data into the buffer
        buffer.put((byte) 'H');
        buffer.put((byte) 'e');
        buffer.put((byte) 'l');
        buffer.put((byte) 'l');
        buffer.put((byte) 'o');
        printBufferState("After Writing 'Hello' (5 bytes)", buffer);

        // Flip buffer to prepare for reading
        buffer.flip();
        printBufferState("After flip() [Switch to Read Mode]", buffer);

        // Read 2 bytes
        byte b1 = buffer.get();
        byte b2 = buffer.get();
        System.out.printf("  Read bytes: '%c', '%c'%n", (char) b1, (char) b2);
        printBufferState("After Reading 2 bytes", buffer);

        // Compact remaining unread bytes ('l', 'l', 'o') to the front and switch to write mode
        buffer.compact();
        printBufferState("After compact() [Preserved unread 3 bytes, ready for writing]", buffer);

        // Write additional data
        buffer.put((byte) '!');
        buffer.flip();
        printBufferState("After adding '!' and flip()", buffer);

        // Read all bytes remaining
        StringBuilder sb = new StringBuilder();
        while (buffer.hasRemaining()) {
            sb.append((char) buffer.get());
        }
        System.out.println("  Final Buffer Content Read: '" + sb + "'");
    }

    /**
     * Demonstrates FileChannel operations using Direct ByteBuffers (Off-Heap).
     */
    private static void demonstrateFileChannelReadWrite(String filePath) throws IOException {
        String data = "Java NIO FileChannel Deep-Dive with Off-Heap Direct ByteBuffers!\n";
        byte[] bytes = data.getBytes(StandardCharsets.UTF_8);

        // 1. Write data using FileChannel and Direct Off-Heap ByteBuffer
        try (FileOutputStream fos = new FileOutputStream(filePath);
             FileChannel writeChannel = fos.getChannel()) {

            // Allocate Off-Heap Native Memory via Direct ByteBuffer
            ByteBuffer directBuffer = ByteBuffer.allocateDirect(bytes.length);
            directBuffer.put(bytes);
            directBuffer.flip(); // Prepare for channel writing

            int bytesWritten = writeChannel.write(directBuffer);
            System.out.printf("FileChannel Write: Written %d bytes using DirectByteBuffer (IsDirect: %b)%n",
                    bytesWritten, directBuffer.isDirect());
        }

        // 2. Read data back using FileChannel and Direct ByteBuffer
        try (FileInputStream fis = new FileInputStream(filePath);
             FileChannel readChannel = fis.getChannel()) {

            ByteBuffer readBuffer = ByteBuffer.allocateDirect(1024);
            int bytesRead = readChannel.read(readBuffer);
            readBuffer.flip(); // Prepare for buffer reading

            byte[] destination = new byte[readBuffer.remaining()];
            readBuffer.get(destination);

            System.out.printf("FileChannel Read: Read %d bytes from channel -> '%s'%n",
                    bytesRead, new String(destination, StandardCharsets.UTF_8).trim());
        }
    }

    /**
     * Demonstrates Vectorized Gathering Writes and Scattering Reads across multiple buffers.
     */
    private static void demonstrateScatteringAndGathering(String filePath) throws IOException {
        // Header Buffer (10 bytes)
        ByteBuffer header = ByteBuffer.allocate(10);
        header.put("HDR-001   ".getBytes(StandardCharsets.UTF_8));
        header.flip();

        // Body Buffer (20 bytes)
        ByteBuffer body = ByteBuffer.allocate(20);
        body.put("BODY: Payload Data  ".getBytes(StandardCharsets.UTF_8));
        body.flip();

        // 1. Gathering Write: Write multiple buffers into a single file channel in one syscall
        try (FileOutputStream fos = new FileOutputStream(filePath);
             FileChannel writeChannel = fos.getChannel()) {

            GatheringByteChannel gatheringChannel = writeChannel;
            long bytesWritten = gatheringChannel.write(new ByteBuffer[]{header, body});
            System.out.printf("Gathering Write: Written total %d bytes from 2 distinct buffers in 1 call.%n", bytesWritten);
        }

        // 2. Scattering Read: Read file data into multiple distinct buffers in one syscall
        ByteBuffer readHeader = ByteBuffer.allocate(10);
        ByteBuffer readBody = ByteBuffer.allocate(20);

        try (FileInputStream fis = new FileInputStream(filePath);
             FileChannel readChannel = fis.getChannel()) {

            ScatteringByteChannel scatteringChannel = readChannel;
            long bytesRead = scatteringChannel.read(new ByteBuffer[]{readHeader, readBody});

            readHeader.flip();
            readBody.flip();

            System.out.printf("Scattering Read: Total %d bytes read into separate buffers:%n", bytesRead);
            System.out.println("  [Header Buffer]: '" + new String(readHeader.array(), StandardCharsets.UTF_8) + "'");
            System.out.println("  [Body Buffer]:   '" + new String(readBody.array(), StandardCharsets.UTF_8) + "'");
        }
    }

    private static void printBufferState(String stepLabel, ByteBuffer buf) {
        System.out.printf("  %-45s -> Pos: %2d, Limit: %2d, Capacity: %2d%n",
                stepLabel, buf.position(), buf.limit(), buf.capacity());
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
