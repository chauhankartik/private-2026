package io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * High-Performance Java I/O Masterclass: Memory-Mapped Files (mmap) & Zero-Copy Transfers
 *
 * Demonstrates:
 * 1. MappedByteBuffer (mmap syscall) for high-throughput random access memory-mapped file I/O
 * 2. FileChannel.transferTo() / transferFrom() for Kernel Zero-Copy file transfers (Linux sendfile)
 * 3. Performance benchmark: Streams vs Channels vs Memory-Mapped File I/O
 */
class MemoryMappedZeroCopyDemo {

    private static final String SOURCE_FILE = "target_mmap_source.tmp";
    private static final String DEST_ZERO_COPY = "target_zero_copy_dest.tmp";
    private static final int LARGE_FILE_SIZE = 10 * 1024 * 1024; // 10 MB

    public static void main(String[] args) throws IOException {
        System.out.println("=== 1. Creating 10 MB Source File for Benchmarking ===");
        createLargeSourceFile(SOURCE_FILE, LARGE_FILE_SIZE);

        System.out.println("\n=== 2. Memory-Mapped File Operations (MappedByteBuffer / mmap) ===");
        demonstrateMemoryMappedFile(SOURCE_FILE);

        System.out.println("\n=== 3. Kernel Zero-Copy Transfer (FileChannel.transferTo / sendfile) ===");
        demonstrateZeroCopyTransfer(SOURCE_FILE, DEST_ZERO_COPY);

        System.out.println("\n=== 4. Benchmarking File Transfer Performance across Mechanisms ===");
        benchmarkTransferMechanisms(SOURCE_FILE);

        // Clean up temporary files
        cleanupFiles(SOURCE_FILE, DEST_ZERO_COPY, "target_stream_dest.tmp", "target_channel_dest.tmp");
        System.out.println("\n[SUCCESS] Memory-Mapped & Zero-Copy I/O demonstrations completed cleanly.");
    }

    /**
     * Creates a dummy 10 MB source file.
     */
    private static void createLargeSourceFile(String filePath, int sizeInBytes) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(filePath, "rw");
             FileChannel channel = raf.getChannel()) {
            MappedByteBuffer out = channel.map(FileChannel.MapMode.READ_WRITE, 0, sizeInBytes);
            byte b = 65; // ASCII 'A'
            for (int i = 0; i < sizeInBytes; i++) {
                out.put(b);
                b = (byte) (65 + (i % 26));
            }
            out.force(); // Commit pages to disk via msync
            System.out.printf("Created 10 MB binary file mapped via MappedByteBuffer at '%s'%n", filePath);
        }
    }

    /**
     * Demonstrates reading and modifying a file directly in kernel Page Cache memory using MappedByteBuffer.
     */
    private static void demonstrateMemoryMappedFile(String filePath) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(filePath, "rw");
             FileChannel channel = raf.getChannel()) {

            long fileLength = channel.size();
            // Map first 100 bytes into Virtual Memory
            MappedByteBuffer mmapBuf = channel.map(FileChannel.MapMode.READ_WRITE, 0, Math.min(100, fileLength));

            System.out.printf("MappedByteBuffer IsLoaded: %b, Capacity: %d bytes%n", mmapBuf.isLoaded(), mmapBuf.capacity());

            // Read original header
            byte[] header = new byte[10];
            mmapBuf.get(header);
            System.out.println("  Original Header (First 10 Bytes): '" + new String(header) + "'");

            // Overwrite first 10 bytes directly in Page Cache memory without read/write syscalls
            mmapBuf.position(0);
            mmapBuf.put("MODIFIED!!".getBytes());
            mmapBuf.force(); // Synchronize page cache back to underlying storage device

            // Re-read modified header
            mmapBuf.position(0);
            byte[] newHeader = new byte[10];
            mmapBuf.get(newHeader);
            System.out.println("  Updated Header in mmap Memory:    '" + new String(newHeader) + "'");
        }
    }

    /**
     * Demonstrates Kernel Zero-Copy file transfer using FileChannel.transferTo().
     */
    private static void demonstrateZeroCopyTransfer(String sourcePath, String destPath) throws IOException {
        try (FileInputStream fis = new FileInputStream(sourcePath);
             FileChannel sourceChannel = fis.getChannel();
             FileOutputStream fos = new FileOutputStream(destPath);
             FileChannel destChannel = fos.getChannel()) {

            long fileSize = sourceChannel.size();
            long totalTransferred = 0;

            long start = System.nanoTime();
            // transferTo invokes Linux sendfile(2) syscall, executing zero user-space memory copies
            while (totalTransferred < fileSize) {
                long transferred = sourceChannel.transferTo(totalTransferred, fileSize - totalTransferred, destChannel);
                if (transferred <= 0) break;
                totalTransferred += transferred;
            }
            long durationMs = (System.nanoTime() - start) / 1_000_000;

            System.out.printf("Zero-Copy transferTo(): Transferred %d bytes in %d ms (Zero User-Space Copies)%n",
                    totalTransferred, durationMs);
        }
    }

    /**
     * Compares transfer speeds of Classic Streams vs FileChannel Buffers vs Zero-Copy transferTo.
     */
    private static void benchmarkTransferMechanisms(String sourcePath) throws IOException {
        String streamDest = "target_stream_dest.tmp";
        String channelDest = "target_channel_dest.tmp";
        String zeroCopyDest = "target_zero_copy_bench.tmp";

        // 1. Stream Copy (Buffered 64 KB)
        long start = System.currentTimeMillis();
        try (FileInputStream in = new FileInputStream(sourcePath);
             FileOutputStream out = new FileOutputStream(streamDest)) {
            byte[] buf = new byte[64 * 1024];
            int read;
            while ((read = in.read(buf)) != -1) {
                out.write(buf, 0, read);
            }
        }
        long streamTime = System.currentTimeMillis() - start;

        // 2. Channel Direct ByteBuffer Copy
        start = System.currentTimeMillis();
        try (FileInputStream in = new FileInputStream(sourcePath);
             FileChannel inChan = in.getChannel();
             FileOutputStream out = new FileOutputStream(channelDest);
             FileChannel outChan = out.getChannel()) {
            ByteBuffer buffer = ByteBuffer.allocateDirect(64 * 1024);
            while (inChan.read(buffer) != -1) {
                buffer.flip();
                outChan.write(buffer);
                buffer.clear();
            }
        }
        long channelTime = System.currentTimeMillis() - start;

        // 3. Zero-Copy transferTo
        start = System.currentTimeMillis();
        try (FileInputStream in = new FileInputStream(sourcePath);
             FileChannel inChan = in.getChannel();
             FileOutputStream out = new FileOutputStream(zeroCopyDest);
             FileChannel outChan = out.getChannel()) {
            inChan.transferTo(0, inChan.size(), outChan);
        }
        long zeroCopyTime = System.currentTimeMillis() - start;

        System.out.println("\n--- Benchmark Results (10 MB File Copy) ---");
        System.out.printf("  1. Buffered Stream (64KB Heap Array): %3d ms%n", streamTime);
        System.out.printf("  2. FileChannel (64KB Direct Buffer):  %3d ms%n", channelTime);
        System.out.printf("  3. Zero-Copy FileChannel.transferTo: %3d ms%n", zeroCopyTime);

        cleanupFiles(zeroCopyDest);
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
