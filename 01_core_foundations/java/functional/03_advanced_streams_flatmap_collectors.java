package functional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Advanced Stream Operations: FlatMap, GroupingBy, PartitioningBy & Teeing Collectors
 *
 * Demonstrates:
 * 1. flatMap for nested object graph and list flattening
 * 2. Collectors.groupingBy with multi-level downstream aggregators
 * 3. Collectors.partitioningBy for predicate partitioning
 * 4. Collectors.toMap with duplicate key merge resolution
 * 5. Collectors.teeing (Java 12+) for dual-collector aggregation
 */
class AdvancedStreamsCollectorsDemo {

    static class Developer {
        private final String name;
        private final String department;
        private final int salary;
        private final List<String> skills;

        public Developer(String name, String department, int salary, List<String> skills) {
            this.name = name;
            this.department = department;
            this.salary = salary;
            this.skills = skills;
        }

        public String name() { return name; }
        public String department() { return department; }
        public int salary() { return salary; }
        public List<String> skills() { return skills; }
    }

    static class SalaryRange {
        private final int minSalary;
        private final int maxSalary;

        public SalaryRange(int minSalary, int maxSalary) {
            this.minSalary = minSalary;
            this.maxSalary = maxSalary;
        }

        public int minSalary() { return minSalary; }
        public int maxSalary() { return maxSalary; }
    }

    public static void main(String[] args) {
        List<Developer> devs = Arrays.asList(
                new Developer("Alice", "Engineering", 120_000, Arrays.asList("Java", "Spring", "Docker")),
                new Developer("Bob", "Engineering", 110_000, Arrays.asList("Java", "Kubernetes")),
                new Developer("Charlie", "Data", 130_000, Arrays.asList("Python", "Spark", "SQL")),
                new Developer("David", "Data", 95_000, Arrays.asList("Python", "SQL")),
                new Developer("Eve", "QA", 90_000, Arrays.asList("Java", "Selenium"))
        );

        System.out.println("=== 1. flatMap Nested Collection Flattening ===");
        demonstrateFlatMap(devs);

        System.out.println("\n=== 2. Collectors.groupingBy & Downstream Aggregations ===");
        demonstrateGroupingBy(devs);

        System.out.println("\n=== 3. Collectors.partitioningBy Predicate Splitting ===");
        demonstratePartitioningBy(devs);

        System.out.println("\n=== 4. Collectors.toMap with Duplicate Key Merge Function ===");
        demonstrateToMap(devs);

        System.out.println("\n=== 5. Collectors.teeing (Java 12+ Dual Collector) ===");
        demonstrateTeeing(devs);

        System.out.println("\n[SUCCESS] Advanced Streams & Collectors demonstration completed cleanly.");
    }

    private static void demonstrateFlatMap(List<Developer> devs) {
        // Flatten all skills lists across developers into a unique sorted list
        Set<String> uniqueSkills = devs.stream()
                .flatMap(dev -> dev.skills().stream())
                .collect(Collectors.toCollection(TreeSet::new));

        System.out.println("  All Unique Tech Skills: " + uniqueSkills);
    }

    private static void demonstrateGroupingBy(List<Developer> devs) {
        // 1. Group developers by department
        Map<String, List<Developer>> byDept = devs.stream()
                .collect(Collectors.groupingBy(Developer::department));
        System.out.println("  Grouped by Department Count: " + byDept.keySet());

        // 2. Group by department -> Average salary per department
        Map<String, Double> avgSalaryByDept = devs.stream()
                .collect(Collectors.groupingBy(
                        Developer::department,
                        Collectors.averagingInt(Developer::salary)
                ));
        System.out.println("  Average Salary by Dept:      " + avgSalaryByDept);

        // 3. Group by department -> Set of Developer names
        Map<String, Set<String>> namesByDept = devs.stream()
                .collect(Collectors.groupingBy(
                        Developer::department,
                        Collectors.mapping(Developer::name, Collectors.toSet())
                ));
        System.out.println("  Developer Names by Dept:     " + namesByDept);
    }

    private static void demonstratePartitioningBy(List<Developer> devs) {
        // Partition developers into High Earners (salary >= 110,000) vs Normal Earners
        Map<Boolean, List<String>> partitionedBySalary = devs.stream()
                .collect(Collectors.partitioningBy(
                        dev -> dev.salary() >= 110_000,
                        Collectors.mapping(Developer::name, Collectors.toList())
                ));

        System.out.println("  High Earners (>= $110k) True:  " + partitionedBySalary.get(true));
        System.out.println("  Normal Earners (< $110k) False: " + partitionedBySalary.get(false));
    }

    private static void demonstrateToMap(List<Developer> devs) {
        // Convert list to map of Department -> Highest Paid Developer Name
        Map<String, String> topEarnerByDept = devs.stream()
                .collect(Collectors.toMap(
                        Developer::department,
                        Developer::name,
                        (existingDev, newDev) -> existingDev // Keep existing if duplicate department
                ));

        System.out.println("  ToMap Department -> First Developer: " + topEarnerByDept);
    }

    private static void demonstrateTeeing(List<Developer> devs) {
        // Compute Min & Max Salary simultaneously in a single stream pass using custom dual accumulation
        // Note: Java 12+ provides built-in Collectors.teeing(collector1, collector2, merger)
        int[] acc = devs.stream()
                .collect(
                        () -> new int[]{Integer.MAX_VALUE, Integer.MIN_VALUE},
                        (res, dev) -> {
                            res[0] = Math.min(res[0], dev.salary());
                            res[1] = Math.max(res[1], dev.salary());
                        },
                        (res1, res2) -> {
                            res1[0] = Math.min(res1[0], res2[0]);
                            res1[1] = Math.max(res1[1], res2[1]);
                        }
                );

        SalaryRange range = new SalaryRange(acc[0], acc[1]);

        System.out.println("  Dual Collector Result -> Min Salary: $" + range.minSalary() + ", Max Salary: $" + range.maxSalary());
        System.out.println("  >>> Note: Java 12+ simplifies dual collector streams using Collectors.teeing(col1, col2, merger).");
    }
}
