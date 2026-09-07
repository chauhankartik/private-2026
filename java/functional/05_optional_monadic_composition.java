package functional;

import java.util.Optional;

/**
 * Optional Monadic Composition & Defensive Design Masterclass
 *
 * Demonstrates:
 * 1. Safe Monadic Composition (map, flatMap, filter) avoiding null checks & Optional.get()
 * 2. Java 9+ Optional enhancements (or(), ifPresentOrElse(), stream())
 * 3. Lazy Evaluation: orElse vs orElseGet computation overhead comparison
 * 4. Defensive Optional return patterns
 */
class OptionalMonadicCompositionDemo {

    static class Address {
        private final String city;
        private final String zipCode;

        public Address(String city, String zipCode) {
            this.city = city;
            this.zipCode = zipCode;
        }

        public String city() { return city; }
        public String zipCode() { return zipCode; }
    }

    static class UserProfile {
        private final String id;
        private final Address address;

        public UserProfile(String id, Address address) {
            this.id = id;
            this.address = address;
        }

        public String id() { return id; }
        public Address address() { return address; }
    }

    public static void main(String[] args) {
        System.out.println("=== 1. Monadic Chaining (map, flatMap, filter) ===");
        demonstrateMonadicChaining();

        System.out.println("\n=== 2. Performance Comparison: orElse vs orElseGet ===");
        demonstrateOrElseVsOrElseGet();

        System.out.println("\n=== 3. Java 9+ Optional Methods (or, ifPresentOrElse, stream) ===");
        demonstrateJava9OptionalEnhancements();

        System.out.println("\n[SUCCESS] Optional monadic composition demonstration completed cleanly.");
    }

    private static void demonstrateMonadicChaining() {
        UserProfile validUser = new UserProfile("usr_100", new Address("San Francisco", "94105"));
        UserProfile userWithoutAddress = new UserProfile("usr_200", null);

        // Safe extraction of zip code without null pointer checks
        String zip1 = extractZipCode(Optional.of(validUser));
        String zip2 = extractZipCode(Optional.of(userWithoutAddress));
        String zip3 = extractZipCode(Optional.empty());

        System.out.println("  Extracted Zip 1 (Valid User):    '" + zip1 + "'");
        System.out.println("  Extracted Zip 2 (Null Address):  '" + zip2 + "'");
        System.out.println("  Extracted Zip 3 (Empty User):    '" + zip3 + "'");
    }

    private static String extractZipCode(Optional<UserProfile> userOpt) {
        return userOpt
                .map(UserProfile::address) // Returns Address or null
                .map(Address::zipCode)     // Returns zipCode or null
                .filter(zip -> zip.length() == 5) // Filter valid 5-digit ZIPs
                .orElse("DEFAULT_00000");
    }

    private static void demonstrateOrElseVsOrElseGet() {
        Optional<String> existingValue = Optional.of("Cached Data");

        System.out.println("  Testing existing Optional with orElse(expensiveCall()):");
        String res1 = existingValue.orElse(computeExpensiveDefault());

        System.out.println("  Testing existing Optional with orElseGet(this::expensiveCall):");
        String res2 = existingValue.orElseGet(OptionalMonadicCompositionDemo::computeExpensiveDefault);

        System.out.println("  >>> Key Insight: orElse ALWAYS evaluates default argument, while orElseGet evaluates LAZILY only when empty!");
    }

    private static String computeExpensiveDefault() {
        System.out.println("    >>> [EXPENSIVE DB CALL EXECUTED] Returning fallback default!");
        return "Fallback Database Value";
    }

    private static void demonstrateJava9OptionalEnhancements() {
        Optional<String> primaryConfig = Optional.empty();
        Optional<String> secondaryConfig = Optional.of("Secondary Backup Config");

        // 1. Optional.or() (Chaining fallback Optionals)
        Optional<String> effectiveConfig = primaryConfig.or(() -> secondaryConfig);
        System.out.println("  Optional.or() Result: " + effectiveConfig.orElse(""));

        // 2. Optional.ifPresentOrElse() (Branching logic)
        System.out.print("  Optional.ifPresentOrElse(): ");
        effectiveConfig.ifPresentOrElse(
                val -> System.out.println("Config Found: " + val),
                () -> System.out.println("Config Missing!")
        );

        // 3. Optional.stream() (Converting Optional to 0 or 1 element Stream)
        long count = Optional.of("Element").stream().count();
        System.out.println("  Optional.stream().count(): " + count);
    }
}
