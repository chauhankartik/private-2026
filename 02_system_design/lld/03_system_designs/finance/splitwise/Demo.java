package splitwise;

import splitwise.CoreModels.*;
import splitwise.Enums.GroupType;
import splitwise.Enums.SplitType;

import java.util.*;

public class Demo {

    public static void main(String[] args) {
        System.out.println("======================================================================");
        System.out.println(" 💰 EXPENSE SHARING SYSTEM (SPLITWISE) — LLD INTERVIEW DEMO");
        System.out.println("======================================================================\n");

        SplitwiseService service = SplitwiseService.getInstance();

        // -------------------------------------------------------------------------
        // SCENARIO 1: USER & GROUP SETUP
        // -------------------------------------------------------------------------
        System.out.println("🔹 SCENARIO 1: Creating Users & Group 'Goa Vacation 2026'...");

        User alice = service.createUser("U1", "Alice", "alice@gmail.com");
        User bob = service.createUser("U2", "Bob", "bob@gmail.com");
        User charlie = service.createUser("U3", "Charlie", "charlie@gmail.com");
        User dave = service.createUser("U4", "Dave", "dave@gmail.com");

        Group group = service.createGroup("G1", "Goa Vacation 2026", GroupType.TRIP);
        service.addUserToGroup("G1", "U1");
        service.addUserToGroup("G1", "U2");
        service.addUserToGroup("G1", "U3");
        service.addUserToGroup("G1", "U4");

        System.out.println("✅ Group created with members: Alice, Bob, Charlie, Dave.\n");

        // -------------------------------------------------------------------------
        // SCENARIO 2: EQUAL SPLIT EXPENSE
        // -------------------------------------------------------------------------
        System.out.println("🔹 SCENARIO 2: Alice pays $200 for Seafood Dinner (Split Equally)...");

        List<Split> dinnerSplits = Arrays.asList(
                new CoreModels.EqualSplit(alice),
                new CoreModels.EqualSplit(bob),
                new CoreModels.EqualSplit(charlie),
                new CoreModels.EqualSplit(dave)
        );

        service.addExpense("G1", "Seafood Dinner", 200.0, "U1", dinnerSplits, SplitType.EQUAL);

        // -------------------------------------------------------------------------
        // SCENARIO 3: EXACT SPLIT EXPENSE
        // -------------------------------------------------------------------------
        System.out.println("\n🔹 SCENARIO 3: Bob pays $150 for Beach Cab (Exact Split)...");

        List<Split> cabSplits = Arrays.asList(
                new CoreModels.ExactSplit(alice, 50.0),
                new CoreModels.ExactSplit(bob, 30.0),
                new CoreModels.ExactSplit(charlie, 70.0)
        );

        service.addExpense("G1", "Beach Cab", 150.0, "U2", cabSplits, SplitType.EXACT);

        // -------------------------------------------------------------------------
        // SCENARIO 4: PERCENTAGE SPLIT EXPENSE
        // -------------------------------------------------------------------------
        System.out.println("\n🔹 SCENARIO 4: Charlie pays $400 for Villa Rental (Percentage Split)...");

        List<Split> villaSplits = Arrays.asList(
                new CoreModels.PercentageSplit(alice, 40.0), // $160
                new CoreModels.PercentageSplit(bob, 30.0),  // $120
                new CoreModels.PercentageSplit(charlie, 20.0), // $80
                new CoreModels.PercentageSplit(dave, 10.0)   // $40
        );

        service.addExpense("G1", "Villa Rental", 400.0, "U3", villaSplits, SplitType.PERCENTAGE);

        // -------------------------------------------------------------------------
        // SCENARIO 5: BALANCE SHEET INSPECTION
        // -------------------------------------------------------------------------
        System.out.println("\n🔹 SCENARIO 5: Generating Balance Sheet for 'Goa Vacation 2026'...");
        service.printGroupBalanceSheet("G1");

        // -------------------------------------------------------------------------
        // SCENARIO 6: DEBT SIMPLIFICATION ALGORITHM (Min Cash Flow Graph Algorithm)
        // -------------------------------------------------------------------------
        System.out.println("\n🔹 SCENARIO 6: Running Graph Debt Simplification (Min Cash Flow Algorithm)...");
        List<Transaction> transactions = service.simplifyGroupDebts("G1");

        // -------------------------------------------------------------------------
        // SCENARIO 7: SETTLEMENT WORKFLOW
        // -------------------------------------------------------------------------
        System.out.println("\n🔹 SCENARIO 7: Executing Debt Settlement...");
        if (!transactions.isEmpty()) {
            Transaction firstTxn = transactions.get(0);
            System.out.printf("💳 %s settles debt by paying $%.2f to %s...%n",
                    firstTxn.getFrom().getName(), firstTxn.getAmount(), firstTxn.getTo().getName());
            
            // Add settlement expense
            List<Split> settlementSplits = Collections.singletonList(
                    new CoreModels.ExactSplit(firstTxn.getTo(), firstTxn.getAmount())
            );
            service.addExpense("G1", "Settlement Payment", firstTxn.getAmount(),
                    firstTxn.getFrom().getUserId(), settlementSplits, SplitType.EXACT);

            System.out.println("\n📊 Re-checking Balance Sheet after settlement:");
            service.printGroupBalanceSheet("G1");
        }

        System.out.println("\n======================================================================");
        System.out.println(" 💰 EXPENSE SHARING SYSTEM DEMO COMPLETED SUCCESSFULLY");
        System.out.println("======================================================================");
    }
}
