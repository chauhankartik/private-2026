package composite;

import java.util.*;

/**
 * Production-grade Java demonstration of the Composite Pattern.
 * Scenario: Hierarchical File System (Files as Leaf nodes, Directories as Composite nodes).
 */
public class CompositeDemo {

    // Component Interface
    public interface FileSystemNode {
        String getName();
        long getSizeInBytes();
        void printStructure(String indent);
    }

    // Leaf Node: File
    public static class FileNode implements FileSystemNode {
        private final String name;
        private final long sizeInBytes;

        public FileNode(String name, long sizeInBytes) {
            this.name = name;
            this.sizeInBytes = sizeInBytes;
        }

        @Override
        public String getName() { return name; }

        @Override
        public long getSizeInBytes() { return sizeInBytes; }

        @Override
        public void printStructure(String indent) {
            System.out.println(indent + "- 📄 File: " + name + " (" + sizeInBytes + " bytes)");
        }
    }

    // Composite Node: Directory
    public static class DirectoryNode implements FileSystemNode {
        private final String name;
        private final List<FileSystemNode> children = new ArrayList<>();

        public DirectoryNode(String name) {
            this.name = name;
        }

        public void add(FileSystemNode node) {
            children.add(node);
        }

        public void remove(FileSystemNode node) {
            children.remove(node);
        }

        @Override
        public String getName() { return name; }

        @Override
        public long getSizeInBytes() {
            long total = 0;
            for (FileSystemNode child : children) {
                total += child.getSizeInBytes();
            }
            return total;
        }

        @Override
        public void printStructure(String indent) {
            System.out.println(indent + "+ 📁 Directory: " + name + " (Total Size: " + getSizeInBytes() + " bytes)");
            for (FileSystemNode child : children) {
                child.printStructure(indent + "   ");
            }
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Composite Pattern Demo ===");

        DirectoryNode root = new DirectoryNode("root");
        DirectoryNode docs = new DirectoryNode("Documents");
        DirectoryNode images = new DirectoryNode("Images");

        FileNode f1 = new FileNode("resume.pdf", 1200);
        FileNode f2 = new FileNode("notes.txt", 450);
        FileNode f3 = new FileNode("photo.jpg", 3500);

        docs.add(f1);
        docs.add(f2);
        images.add(f3);

        root.add(docs);
        root.add(images);
        root.add(new FileNode("config.sys", 100));

        // Uniform invocation across Composite tree
        root.printStructure("");
    }
}
