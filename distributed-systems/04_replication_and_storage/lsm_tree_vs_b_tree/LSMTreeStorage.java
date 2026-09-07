package lsm_tree_vs_b_tree;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Executable Java simulation of an LSM-Tree Storage Engine.
 * Demonstrates MemTable in-memory writes, SSTable flushing, Bloom Filter key checks, and background Compaction.
 */
public class LSMTreeStorage {

    // Simple Bloom Filter Simulation
    public static class SimpleBloomFilter {
        private final BitSet bitSet = new BitSet(1024);

        public void add(String key) {
            int h1 = Math.abs(key.hashCode() % 1024);
            int h2 = Math.abs((key.hashCode() * 31) % 1024);
            bitSet.set(h1);
            bitSet.set(h2);
        }

        public boolean mightContain(String key) {
            int h1 = Math.abs(key.hashCode() % 1024);
            int h2 = Math.abs((key.hashCode() * 31) % 1024);
            return bitSet.get(h1) && bitSet.get(h2);
        }
    }

    // Immutable SSTable File on Disk Simulation
    public static class SSTable {
        public final int id;
        public final Map<String, String> sortedData;
        public final SimpleBloomFilter bloomFilter;

        public SSTable(int id, Map<String, String> data) {
            this.id = id;
            this.sortedData = Collections.unmodifiableMap(new TreeMap<>(data));
            this.bloomFilter = new SimpleBloomFilter();
            for (String key : sortedData.keySet()) {
                bloomFilter.add(key);
            }
        }

        public String get(String key) {
            if (!bloomFilter.mightContain(key)) {
                return null; // Fast path: Key definitely not in this SSTable!
            }
            return sortedData.get(key);
        }
    }

    // Engine Main Class
    public static class LSMEngine {
        private ConcurrentSkipListMap<String, String> memTable = new ConcurrentSkipListMap<>();
        private final List<SSTable> ssTables = new ArrayList<>();
        private final int memTableThreshold;
        private int sstableCounter = 0;

        public LSMEngine(int memTableThreshold) {
            this.memTableThreshold = memTableThreshold;
        }

        public synchronized void put(String key, String value) {
            memTable.put(key, value);
            System.out.printf("[MemTable Put] Key:'%s' = '%s' (MemTable Size: %d/%d)\n",
                    key, value, memTable.size(), memTableThreshold);

            if (memTable.size() >= memTableThreshold) {
                flushMemTableToSSTable();
            }
        }

        public synchronized String get(String key) {
            // 1. Check MemTable
            if (memTable.containsKey(key)) {
                System.out.printf("[Read Path] Found '%s' in MemTable -> '%s'\n", key, memTable.get(key));
                return memTable.get(key);
            }

            // 2. Check SSTables (Most recent to oldest)
            for (int i = ssTables.size() - 1; i >= 0; i--) {
                SSTable ssTable = ssTables.get(i);
                String val = ssTable.get(key);
                if (val != null) {
                    System.out.printf("[Read Path] Found '%s' in SSTable #%d -> '%s'\n", key, ssTable.id, val);
                    return val;
                }
            }

            System.out.printf("[Read Path] Key '%s' NOT FOUND\n", key);
            return null;
        }

        private void flushMemTableToSSTable() {
            sstableCounter++;
            SSTable newSSTable = new SSTable(sstableCounter, memTable);
            ssTables.add(newSSTable);
            System.out.printf(">>> [FLUSH] MemTable exceeded threshold. Flushed SSTable #%d to Disk (Keys: %s) <<<\n",
                    sstableCounter, newSSTable.sortedData.keySet());
            memTable = new ConcurrentSkipListMap<>();
        }

        // Compaction Simulation (Merge Sort overlapping SSTables)
        public synchronized void compact() {
            if (ssTables.size() < 2) return;
            System.out.println("\n>>> [COMPACTION] Starting Merge-Sort Compaction across SSTables <<<");
            Map<String, String> mergedMap = new TreeMap<>();
            for (SSTable ssTable : ssTables) {
                mergedMap.putAll(ssTable.sortedData);
            }
            ssTables.clear();
            sstableCounter++;
            SSTable compactedSSTable = new SSTable(sstableCounter, mergedMap);
            ssTables.add(compactedSSTable);
            System.out.printf("Compaction Complete -> Created Merged SSTable #%d (Keys: %s)\n",
                    compactedSSTable.id, compactedSSTable.sortedData.keySet());
        }
    }

    public static void main(String[] args) {
        System.out.println("=== LSM-Tree Storage Engine Simulation ===");

        // MemTable flushes when size reaches 3 items
        LSMEngine engine = new LSMEngine(3);

        engine.put("user:1", "Alice");
        engine.put("user:2", "Bob");
        engine.put("user:3", "Charlie"); // Triggers Flush #1

        engine.put("user:4", "David");
        engine.put("user:1", "Alice_Updated"); // Overwrite key
        engine.put("user:5", "Edward"); // Triggers Flush #2

        // Read requests
        System.out.println("\n--- Performing Reads ---");
        engine.get("user:1"); // Returns Alice_Updated from MemTable/SSTable
        engine.get("user:2"); // Returns Bob from SSTable #1
        engine.get("user:99"); // Returns NOT FOUND (Bloom Filter skip)

        // Trigger Compaction
        engine.compact();
    }
}
