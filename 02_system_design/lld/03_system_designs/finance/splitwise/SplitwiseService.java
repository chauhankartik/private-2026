package splitwise;

import splitwise.CoreModels.*;
import splitwise.Enums.GroupType;
import splitwise.Enums.SplitType;
import splitwise.SplitStrategy.ISplitStrategy;
import splitwise.SplitStrategy.SplitStrategyFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SplitwiseService {
    private static volatile SplitwiseService instance;

    private final Map<String, User> users = new ConcurrentHashMap<>();
    private final Map<String, Group> groups = new ConcurrentHashMap<>();
    private final DebtSimplifier debtSimplifier = new DebtSimplifier();

    private SplitwiseService() {}

    public static SplitwiseService getInstance() {
        if (instance == null) {
            synchronized (SplitwiseService.class) {
                if (instance == null) {
                    instance = new SplitwiseService();
                }
            }
        }
        return instance;
    }

    public User createUser(String userId, String name, String email) {
        User user = new User(userId, name, email);
        users.put(userId, user);
        return user;
    }

    public Group createGroup(String groupId, String name, GroupType groupType) {
        Group group = new Group(groupId, name, groupType);
        groups.put(groupId, group);
        return group;
    }

    public void addUserToGroup(String groupId, String userId) {
        Group group = groups.get(groupId);
        User user = users.get(userId);
        if (group != null && user != null) {
            group.addMember(user);
        }
    }

    public Expense addExpense(String groupId, String title, double amount, String paidByUserId,
                              List<Split> splits, SplitType splitType) {
        User paidBy = users.get(paidByUserId);
        if (paidBy == null) {
            System.out.println("❌ Invalid payer user ID: " + paidByUserId);
            return null;
        }

        ISplitStrategy strategy = SplitStrategyFactory.getStrategy(splitType);
        if (!strategy.validateSplits(amount, splits)) {
            System.out.println("❌ Invalid splits sum or configuration for expense: " + title);
            return null;
        }

        strategy.computeSplits(amount, splits);

        String expenseId = "EXP_" + UUID.randomUUID().toString().substring(0, 8);
        Expense expense = new Expense(expenseId, title, amount, paidBy, splits, splitType);

        Group group = groups.get(groupId);
        if (group != null) {
            group.addExpense(expense);
        }

        System.out.printf("💸 Added Expense [%s] in Group '%s' | Total: $%.2f | Paid By: %s%n",
                title, (group != null ? group.getName() : "Individual"), amount, paidBy.getName());

        return expense;
    }

    public Map<User, Double> calculateGroupNetBalances(String groupId) {
        Group group = groups.get(groupId);
        if (group == null) return Collections.emptyMap();

        Map<User, Double> netBalances = new HashMap<>();
        for (User member : group.getMembers()) {
            netBalances.put(member, 0.0);
        }

        for (Expense expense : group.getExpenses()) {
            User paidBy = expense.getPaidBy();
            netBalances.put(paidBy, netBalances.getOrDefault(paidBy, 0.0) + expense.getAmount());

            for (Split split : expense.getSplits()) {
                User u = split.getUser();
                netBalances.put(u, netBalances.getOrDefault(u, 0.0) - split.getAmount());
            }
        }
        return netBalances;
    }

    public List<Transaction> simplifyGroupDebts(String groupId) {
        Group group = groups.get(groupId);
        if (group == null) {
            System.out.println("❌ Group not found: " + groupId);
            return Collections.emptyList();
        }

        Map<User, Double> netBalances = calculateGroupNetBalances(groupId);
        List<Transaction> simplified = debtSimplifier.simplifyDebts(netBalances);

        System.out.printf("%n✨ --- DEBT SIMPLIFICATION FOR GROUP '%s' ---%n", group.getName());
        if (simplified.isEmpty()) {
            System.out.println("🎉 All debts settled up! No transactions needed.");
        } else {
            for (Transaction t : simplified) {
                System.out.println("  " + t);
            }
        }
        return simplified;
    }

    public void printGroupBalanceSheet(String groupId) {
        Group group = groups.get(groupId);
        if (group == null) return;

        Map<User, Double> netBalances = calculateGroupNetBalances(groupId);

        System.out.printf("%n📊 --- BALANCE SHEET FOR GROUP '%s' ---%n", group.getName());
        for (Map.Entry<User, Double> entry : netBalances.entrySet()) {
            double bal = Math.round(entry.getValue() * 100.0) / 100.0;
            if (bal > 0) {
                System.out.printf("  🟢 %-15s gets back $%.2f%n", entry.getKey().getName(), bal);
            } else if (bal < 0) {
                System.out.printf("  🔴 %-15s owes      $%.2f%n", entry.getKey().getName(), -bal);
            } else {
                System.out.printf("  ⚪ %-15s is settled (Balance: $0.00)%n", entry.getKey().getName());
            }
        }
    }

    public User getUser(String userId) {
        return users.get(userId);
    }
}
