package version_control_system;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Production-grade Java implementation of a Version Control System (Mini-Git).
 * Features Content-Addressable SHA-1 Object Storage, Tree/Blob Composite Hierarchy,
 * Staging Index, Branching, Checkout, and Commit DAG Log Traversal.
 */
public class VersionControlSystemDemo {

    public enum ObjectType { BLOB, TREE, COMMIT }

    // --- Abstract Base Git Object ---
    public static abstract class GitObject {
        protected String sha1;

        public String getSha1() {
            if (sha1 == null) {
                sha1 = computeSha1();
            }
            return sha1;
        }

        public abstract ObjectType getType();
        protected abstract String getRawContent();

        protected String computeSha1() {
            try {
                String payload = getType().name().toLowerCase() + " " + getRawContent();
                MessageDigest md = MessageDigest.getInstance("SHA-1");
                byte[] digest = md.digest(payload.getBytes(StandardCharsets.UTF_8));
                StringBuilder sb = new StringBuilder();
                for (byte b : digest) {
                    sb.append(String.format("%02x", b));
                }
                return sb.toString();
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException("SHA-1 algorithm not found", e);
            }
        }
    }

    // --- 1. Blob Object (Raw File Content) ---
    public static class Blob extends GitObject {
        private final String content;

        public Blob(String content) {
            this.content = content;
        }

        public String getContent() { return content; }

        @Override
        public ObjectType getType() { return ObjectType.BLOB; }

        @Override
        protected String getRawContent() { return content; }

        @Override
        public String toString() {
            return "Blob[" + getSha1().substring(0, 7) + "]";
        }
    }

    // --- 2. Tree Entry & Tree Object (Directory Hierarchy) ---
    public static class TreeEntry {
        public final String mode;
        public final String name;
        public final String sha1;

        public TreeEntry(String mode, String name, String sha1) {
            this.mode = mode;
            this.name = name;
            this.sha1 = sha1;
        }
    }

    public static class Tree extends GitObject {
        private final List<TreeEntry> entries = new ArrayList<>();

        public void addEntry(String mode, String name, String sha1) {
            entries.add(new TreeEntry(mode, name, sha1));
            entries.sort(Comparator.comparing(e -> e.name));
        }

        public List<TreeEntry> getEntries() { return entries; }

        @Override
        public ObjectType getType() { return ObjectType.TREE; }

        @Override
        protected String getRawContent() {
            StringBuilder sb = new StringBuilder();
            for (TreeEntry e : entries) {
                sb.append(e.mode).append(" ").append(e.sha1).append(" ").append(e.name).append("\n");
            }
            return sb.toString();
        }

        @Override
        public String toString() {
            return "Tree[" + getSha1().substring(0, 7) + " (" + entries.size() + " entries)]";
        }
    }

    // --- 3. Commit Object ---
    public static class Commit extends GitObject {
        private final String treeSha;
        private final List<String> parentShas;
        private final String author;
        private final long timestamp;
        private final String message;

        public Commit(String treeSha, List<String> parentShas, String author, String message) {
            this.treeSha = treeSha;
            this.parentShas = new ArrayList<>(parentShas);
            this.author = author;
            this.timestamp = System.currentTimeMillis();
            this.message = message;
        }

        public String getTreeSha() { return treeSha; }
        public List<String> getParentShas() { return parentShas; }
        public String getAuthor() { return author; }
        public String getMessage() { return message; }

        @Override
        public ObjectType getType() { return ObjectType.COMMIT; }

        @Override
        protected String getRawContent() {
            StringBuilder sb = new StringBuilder();
            sb.append("tree ").append(treeSha).append("\n");
            for (String p : parentShas) {
                sb.append("parent ").append(p).append("\n");
            }
            sb.append("author ").append(author).append(" ").append(timestamp).append("\n");
            sb.append("\n").append(message);
            return sb.toString();
        }

        @Override
        public String toString() {
            return String.format("Commit[%s] '%s' by %s", getSha1().substring(0, 7), message, author);
        }
    }

    // --- Object Repository (Content-Addressable Storage) ---
    public static class ObjectRepository {
        private final Map<String, GitObject> storage = new ConcurrentHashMap<>();

        public String put(GitObject obj) {
            String sha = obj.getSha1();
            storage.putIfAbsent(sha, obj);
            return sha;
        }

        public GitObject get(String sha) {
            return storage.get(sha);
        }
    }

    // --- Staging Area Index ---
    public static class StagingArea {
        private final Map<String, String> index = new ConcurrentHashMap<>(); // path -> blobSha

        public void stage(String path, String blobSha) {
            index.put(path, blobSha);
        }

        public Map<String, String> getIndex() { return index; }

        public void clear() {
            index.clear();
        }
    }

    // --- Branch ---
    public static class Branch {
        public final String name;
        public String commitSha;

        public Branch(String name, String commitSha) {
            this.name = name;
            this.commitSha = commitSha;
        }
    }

    // --- Main Repository Engine ---
    public static class GitRepository {
        private final ObjectRepository objectRepo = new ObjectRepository();
        private final StagingArea stagingArea = new StagingArea();
        private final Map<String, Branch> branches = new ConcurrentHashMap<>();
        private String activeBranch = "main";
        private final Map<String, String> workingDirectory = new HashMap<>(); // Simulated Working Tree

        public GitRepository() {
            branches.put("main", new Branch("main", null));
        }

        // Add file to Staging Index
        public synchronized void add(String path, String content) {
            workingDirectory.put(path, content);
            Blob blob = new Blob(content);
            String blobSha = objectRepo.put(blob);
            stagingArea.stage(path, blobSha);
            System.out.printf("[git add] Staged '%s' -> Blob SHA: %s\n", path, blobSha.substring(0, 7));
        }

        // Commit staged index
        public synchronized String commit(String message, String author) {
            if (stagingArea.getIndex().isEmpty()) {
                System.out.println("Nothing to commit (Staging area is empty).");
                return null;
            }

            // Create Tree from Index
            Tree rootTree = new Tree();
            for (Map.Entry<String, String> entry : stagingArea.getIndex().entrySet()) {
                rootTree.addEntry("100644", entry.getKey(), entry.getValue());
            }
            String treeSha = objectRepo.put(rootTree);

            // Create Commit Object
            Branch currentBranch = branches.get(activeBranch);
            List<String> parents = currentBranch.commitSha != null ?
                    Collections.singletonList(currentBranch.commitSha) : Collections.emptyList();

            Commit commit = new Commit(treeSha, parents, author, message);
            String commitSha = objectRepo.put(commit);

            // Advance Branch Pointer & Clear Index
            currentBranch.commitSha = commitSha;
            stagingArea.clear();

            System.out.printf(">>> [git commit] [%s %s] %s <<<\n",
                    activeBranch, commitSha.substring(0, 7), message);
            return commitSha;
        }

        // Create new Branch
        public synchronized void createBranch(String branchName) {
            Branch currentBranch = branches.get(activeBranch);
            if (branches.containsKey(branchName)) {
                System.out.println("Branch '" + branchName + "' already exists!");
                return;
            }
            branches.put(branchName, new Branch(branchName, currentBranch.commitSha));
            System.out.printf("[git branch] Created branch '%s' pointing to %s\n",
                    branchName, currentBranch.commitSha != null ? currentBranch.commitSha.substring(0, 7) : "NULL");
        }

        // Switch Branch
        public synchronized void checkout(String branchName) {
            if (!branches.containsKey(branchName)) {
                System.out.println("Branch '" + branchName + "' does not exist!");
                return;
            }
            this.activeBranch = branchName;
            System.out.println("[git checkout] Switched to branch '" + branchName + "'");
        }

        // Display Commit History Log
        public synchronized void log() {
            System.out.printf("\n--- Commit Log for branch '%s' ---\n", activeBranch);
            String currentSha = branches.get(activeBranch).commitSha;

            while (currentSha != null) {
                Commit commit = (Commit) objectRepo.get(currentSha);
                if (commit == null) break;
                System.out.printf("commit %s\nAuthor: %s\n    %s\n\n",
                        commit.getSha1(), commit.getAuthor(), commit.getMessage());
                currentSha = commit.getParentShas().isEmpty() ? null : commit.getParentShas().get(0);
            }
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Version Control System (Mini-Git) LLD Demo ===");

        GitRepository repo = new GitRepository();

        // 1. First Commit on main
        repo.add("README.md", "# My Project");
        repo.add("src/Main.java", "public class Main { public static void main(String[] a){} }");
        String c1 = repo.commit("Initial commit", "Kartik <kartik@example.com>");

        // 2. Create feature branch
        repo.createBranch("feature/login");

        // 3. Second Commit on main
        repo.add("docs/architecture.md", "System Architecture documentation");
        repo.commit("Add architecture docs", "Kartik <kartik@example.com>");

        // 4. Switch to feature branch & commit
        repo.checkout("feature/login");
        repo.add("src/LoginService.java", "public class LoginService {}");
        repo.commit("Add Login service implementation", "Alice <alice@example.com>");

        // 5. Print logs for feature branch
        repo.log();

        // 6. Switch back to main & print logs
        repo.checkout("main");
        repo.log();
    }
}
