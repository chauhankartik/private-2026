package splitwise;

import splitwise.Enums.GroupType;
import splitwise.Enums.SplitType;

import java.util.*;

public class CoreModels {

    public static class User {
        private final String userId;
        private final String name;
        private final String email;

        public User(String userId, String name, String email) {
            this.userId = userId;
            this.name = name;
            this.email = email;
        }

        public String getUserId() { return userId; }
        public String getName() { return name; }
        public String getEmail() { return email; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            User user = (User) o;
            return Objects.equals(userId, user.userId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(userId);
        }

        @Override
        public String toString() {
            return name + " (" + userId + ")";
        }
    }

    public static abstract class Split {
        private final User user;
        protected double amount;

        public Split(User user) {
            this.user = user;
        }

        public User getUser() { return user; }
        public double getAmount() { return amount; }
        public void setAmount(double amount) { this.amount = amount; }
    }

    public static class EqualSplit extends Split {
        public EqualSplit(User user) {
            super(user);
        }
    }

    public static class ExactSplit extends Split {
        public ExactSplit(User user, double amount) {
            super(user);
            this.amount = amount;
        }
    }

    public static class PercentageSplit extends Split {
        private final double percentage;

        public PercentageSplit(User user, double percentage) {
            super(user);
            this.percentage = percentage;
        }

        public double getPercentage() { return percentage; }
    }

    public static class Expense {
        private final String expenseId;
        private final String title;
        private final double amount;
        private final User paidBy;
        private final List<Split> splits;
        private final SplitType splitType;

        public Expense(String expenseId, String title, double amount, User paidBy, List<Split> splits, SplitType splitType) {
            this.expenseId = expenseId;
            this.title = title;
            this.amount = amount;
            this.paidBy = paidBy;
            this.splits = splits;
            this.splitType = splitType;
        }

        public String getExpenseId() { return expenseId; }
        public String getTitle() { return title; }
        public double getAmount() { return amount; }
        public User getPaidBy() { return paidBy; }
        public List<Split> getSplits() { return splits; }
        public SplitType getSplitType() { return splitType; }
    }

    public static class Group {
        private final String groupId;
        private final String name;
        private final GroupType groupType;
        private final List<User> members = new ArrayList<>();
        private final List<Expense> expenses = new ArrayList<>();

        public Group(String groupId, String name, GroupType groupType) {
            this.groupId = groupId;
            this.name = name;
            this.groupType = groupType;
        }

        public String getGroupId() { return groupId; }
        public String getName() { return name; }
        public GroupType getGroupType() { return groupType; }
        public List<User> getMembers() { return members; }
        public List<Expense> getExpenses() { return expenses; }

        public void addMember(User user) {
            if (!members.contains(user)) {
                members.add(user);
            }
        }

        public void addExpense(Expense expense) {
            expenses.add(expense);
        }
    }

    public static class Transaction {
        private final User from;
        private final User to;
        private final double amount;

        public Transaction(User from, User to, double amount) {
            this.from = from;
            this.to = to;
            this.amount = Math.round(amount * 100.0) / 100.0;
        }

        public User getFrom() { return from; }
        public User getTo() { return to; }
        public double getAmount() { return amount; }

        @Override
        public String toString() {
            return String.format("💸 %s pays %s -> $%.2f", from.getName(), to.getName(), amount);
        }
    }
}
