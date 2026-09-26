package io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.EnumSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Concurrent File Locking & Asynchronous I/O Masterclass
 *
 * Demonstrates:
 * 1. Process File Synchronization via FileLock (Exclusive vs Shared Locks)
 * 2. Non-blocking Asynchronous File I/O via AsynchronousFileChannel
 * 3. Future-based vs Callback-based (CompletionHandler) Asynchronous Operations
 */
class FileLockingAsyncIODemo {

    private static final String LOCK_TARGET_FILE = "target_file_lock.tmp";
    private static final String ASYNC_TARGET_FILE = "target_async_io.tmp";

    public static void main(String[] args) throws Exception {
        System.out.println("=== 1. Inter-Process File Synchronization (FileLock) ===");
        demonstrateFileLocking(LOCK_TARGET_FILE);

        System.out.println("\n=== 2. Asynchronous File I/O with Future (AsynchronousFileChannel) ===");
        demonstrateAsyncIOWithFuture(ASYNC_TARGET_FILE);

        System.out.println("\n=== 3. Asynchronous File I/O with CompletionHandler Callbacks ===");
        demonstrateAsyncIOWithCallback(ASYNC_TARGET_FILE);

        // Clean up temporary files
        cleanupFiles(LOCK_TARGET_FILE, ASYNC_TARGET_FILE);
        System.out.println("\n[SUCCESS] Concurrent File Locking & Async I/O demonstrations completed cleanly.");
    }

    /**
     * Demonstrates Exclusive and Shared File Locking.
     */
    private static void demonstrateFileLocking(String filePath) throws IOException {
        // Create initial file
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write("Locking test file initial data.".getBytes(StandardCharsets.UTF_8));
        }

        // 1. Acquire Shared Lock (Read-Only access allowed concurrently)
        try (RandomAccessFile raf = new RandomAccessFile(filePath, "r");
             FileChannel readChannel = raf.getChannel();
             FileLock sharedLock = readChannel.lock(0, Long.MAX_VALUE, true)) { // shared = true

            System.out.printf("Acquired Shared Lock: Shared = %b, IsValid = %b%n",
                    sharedLock.isShared(), sharedLock.isValid());
        }

        // 2. Acquire Exclusive Lock (Write access exclusive to current process)
        try (RandomAccessFile raf = new RandomAccessFile(filePath, "rw");
             FileChannel writeChannel = raf.getChannel();
             FileLock exclusiveLock = writeChannel.tryLock(0, Long.MAX_VALUE, false)) { // shared = false

            if (exclusiveLock != null) {
                System.out.printf("Acquired Exclusive Lock: Shared = %b, Overlaps = %b%n",
                        exclusiveLock.isShared(), exclusiveLock.overlaps(0, 100));

                // Write modified contents under lock protection
                writeChannel.position(writeChannel.size());
                writeChannel.write(ByteBuffer.wrap("\nAppended protected write under Exclusive Lock.".getBytes(StandardCharsets.UTF_8)));
            }
        }
    }

    /**
     * Demonstrates non-blocking file write/read using AsynchronousFileChannel and Future.
     */
    private static void demonstrateAsyncIOWithFuture(String filePath) throws Exception {
        Path path = Paths.get(filePath);

        // Open AsynchronousFileChannel for WRITE and READ
        try (AsynchronousFileChannel asyncChannel = AsynchronousFileChannel.open(
                path, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.READ)) {

            ByteBuffer writeBuffer = ByteBuffer.wrap("Asynchronous File Channel Data via Future!\n".getBytes(StandardCharsets.UTF_8));

            // Write asynchronously at position 0
            Future<Integer> writeFuture = asyncChannel.write(writeBuffer, 0);

            // Execute non-blocking work while I/O completes in background
            while (!writeFuture.isDone()) {
                System.out.println("  Doing CPU computations while Async Write completes...");
                Thread.sleep(10);
            }

            int bytesWritten = writeFuture.get();
            System.out.printf("Async Write Complete: Wrote %d bytes via Future.%n", bytesWritten);

            // Read asynchronously at position 0
            ByteBuffer readBuffer = ByteBuffer.allocate(1024);
            Future<Integer> readFuture = asyncChannel.read(readBuffer, 0);

            int bytesRead = readFuture.get(); // Blocks until read is complete
            readBuffer.flip();

            byte[] data = new byte[readBuffer.remaining()];
            readBuffer.get(data);
            System.out.printf("Async Read Complete: Read %d bytes via Future -> '%s'%n",
                    bytesRead, new String(data, StandardCharsets.UTF_8).trim());
        }
    }

    /**
     * Demonstrates non-blocking file I/O using CompletionHandler callbacks and a custom thread pool.
     */
    private static void demonstrateAsyncIOWithCallback(String filePath) throws Exception {
        Path path = Paths.get(filePath);
        CountDownLatch latch = new CountDownLatch(1);

        // Open Async channel with dedicated background ThreadPool Executor
        try (AsynchronousFileChannel asyncChannel = AsynchronousFileChannel.open(
                path,
                EnumSet.of(StandardOpenOption.READ),
                Executors.newFixedThreadPool(2))) {

            ByteBuffer buffer = ByteBuffer.allocate(1024);

            // Initiate non-blocking read operation with CompletionHandler callback
            asyncChannel.read(buffer, 0, "Attachment Context Payload", new CompletionHandler<Integer, String>() {
                @Override
                public void completed(Integer result, String attachment) {
                    System.out.printf("  >>> CompletionHandler Callback Executed [Thread: %s]!%n", Thread.currentThread().getName());
                    System.out.printf("      Bytes Read: %d, Attachment Context: '%s'%n", result, attachment);

                    buffer.flip();
                    byte[] data = new byte[buffer.remaining()];
                    buffer.get(data);
                    System.out.println("      Callback Buffer Read: '" + new String(data, StandardCharsets.UTF_8).trim() + "'");
                    latch.countDown();
                }

                @Override
                public void failed(Throwable exc, String attachment) {
                    System.err.println("Async Operation Failed: " + exc.getMessage());
                    latch.countDown();
                }
            });

            System.out.println("Initiated non-blocking Async read. Waiting for callback execution...");
            latch.await(3, TimeUnit.SECONDS);
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
