package io;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Modern Java NIO.2 Filesystem Subsystem Masterclass (java.nio.file)
 *
 * Demonstrates:
 * 1. Path manipulation (resolve, relativize, normalize, absolute paths)
 * 2. High-level atomic file operations with Files utility class
 * 3. Recursive directory tree walking with FileVisitor
 * 4. Real-time directory change monitoring via WatchService (inotify/kqueue)
 */
class NIO2PathFilesWatchServiceDemo {

    private static final String BASE_DIR = "target_nio2_demo";

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("=== 1. Path API Manipulation & Resolution ===");
        demonstratePathOperations();

        System.out.println("\n=== 2. Files Utility Class & POSIX File Attributes ===");
        demonstrateFilesOperations();

        System.out.println("\n=== 3. Recursive Directory Traversal with SimpleFileVisitor ===");
        demonstrateDirectoryTreeWalking();

        System.out.println("\n=== 4. Directory Monitoring via WatchService ===");
        demonstrateWatchService();

        // Clean up root demo directory
        cleanupDirectory(Paths.get(BASE_DIR));
        System.out.println("\n[SUCCESS] NIO.2 Filesystem Subsystem demonstrations completed cleanly.");
    }

    /**
     * Demonstrates Path resolution, relativization, and normalization.
     */
    private static void demonstratePathOperations() {
        Path rootPath = Paths.get("/home/user/project");
        Path relativePath = Paths.get("src/main/java/App.java");

        // 1. Resolve relative path against root
        Path resolvedPath = rootPath.resolve(relativePath);
        System.out.println("  Path.resolve():      " + resolvedPath);

        // 2. Relativize two paths
        Path pathA = Paths.get("/home/user/project/src/A.java");
        Path pathB = Paths.get("/home/user/project/target/B.class");
        Path relativizedPath = pathA.relativize(pathB);
        System.out.println("  Path.relativize():   " + relativizedPath);

        // 3. Normalize redundant elements (e.g. '.' and '..')
        Path redundantPath = Paths.get("/home/user/project/./src/../target/./App.class");
        System.out.println("  Raw Path:            " + redundantPath);
        System.out.println("  Path.normalize():    " + redundantPath.normalize());
    }

    /**
     * Demonstrates Files utility class operations.
     */
    private static void demonstrateFilesOperations() throws IOException {
        Path baseDirPath = Paths.get(BASE_DIR);
        Path subDirPath = baseDirPath.resolve("sub_directory");
        Path sampleFilePath = subDirPath.resolve("sample.txt");

        // Create directories
        Files.createDirectories(subDirPath);
        System.out.println("  Created Directories: " + subDirPath);

        // Write string content to file
        Files.writeString(sampleFilePath, "NIO.2 High-Level Files API Content!\nLine 2");
        System.out.println("  Files.writeString(): Wrote to " + sampleFilePath);

        // Read attributes
        BasicFileAttributes attrs = Files.readAttributes(sampleFilePath, BasicFileAttributes.class);
        System.out.printf("  File Attributes -> Size: %d bytes, Created: %s, IsRegular: %b%n",
                attrs.size(), attrs.creationTime(), attrs.isRegularFile());

        // Check file permissions if POSIX filesystem is supported
        if (FileSystems.getDefault().supportedFileAttributeViews().contains("posix")) {
            Set<PosixFilePermission> perms = Files.getPosixFilePermissions(sampleFilePath);
            System.out.println("  POSIX Permissions:   " + perms);
        }
    }

    /**
     * Demonstrates directory traversal using FileVisitor.
     */
    private static void demonstrateDirectoryTreeWalking() throws IOException {
        Path rootDir = Paths.get(BASE_DIR);

        // Create additional sub-files for traversal
        Files.writeString(rootDir.resolve("file1.log"), "Log 1");
        Files.writeString(rootDir.resolve("sub_directory").resolve("file2.log"), "Log 2");

        System.out.println("Walking Directory Tree starting from '" + rootDir + "':");
        Files.walkFileTree(rootDir, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                System.out.println("  [DIR]:  " + dir);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                System.out.println("  [FILE]: " + file + " (" + attrs.size() + " bytes)");
                return FileVisitResult.CONTINUE;
            }
        });
    }

    /**
     * Demonstrates real-time filesystem directory monitoring using WatchService.
     */
    private static void demonstrateWatchService() throws IOException, InterruptedException {
        Path watchDir = Paths.get(BASE_DIR).resolve("watch_target");
        Files.createDirectories(watchDir);

        // 1. Create WatchService from default FileSystem
        try (WatchService watchService = FileSystems.getDefault().newWatchService()) {

            // 2. Register directory for ENTRY_CREATE, ENTRY_MODIFY, and ENTRY_DELETE events
            watchDir.register(watchService,
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_MODIFY,
                    StandardWatchEventKinds.ENTRY_DELETE);

            System.out.println("Registered WatchService for directory: " + watchDir);

            // 3. Trigger filesystem event on separate worker thread
            Thread eventThread = new Thread(() -> {
                try {
                    Thread.sleep(200);
                    Path createdFile = watchDir.resolve("monitored_event.txt");
                    Files.writeString(createdFile, "WatchService event trigger data!");
                    Thread.sleep(100);
                    if (Files.exists(createdFile)) {
                        Files.delete(createdFile);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            eventThread.start();

            // 4. Poll for WatchKey event notifications
            System.out.println("Polling WatchService for events (timeout 3 seconds)...");
            WatchKey key = watchService.poll(3, TimeUnit.SECONDS);

            if (key != null) {
                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();
                    Path contextPath = (Path) event.context();
                    System.out.printf("  >>> [WatchService Event]: Kind = %-14s, File = %s%n",
                            kind.name(), contextPath);
                }
                key.reset();
            } else {
                System.out.println("  No WatchService events detected during polling window.");
            }
            eventThread.join();
        }
    }

    private static void cleanupDirectory(Path path) throws IOException {
        if (Files.exists(path)) {
            Files.walkFileTree(path, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }
}
