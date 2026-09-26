package splitwise;

import splitwise.CoreModels.Transaction;
import splitwise.CoreModels.User;

import java.util.*;

public class DebtSimplifier {

    private static class BalanceNode {
        final User user;
        double amount;

        BalanceNode(User user, double amount) {
            this.user = user;
            this.amount = amount;
        }
    }

    /**
     * Min Cash Flow Algorithm: Minimizes the number of transactions to settle debts.
     * Takes net balances map (Positive = Creditor/Owed, Negative = Debtor/Owes).
     */
    public List<Transaction> simplifyDebts(Map<User, Double> netBalances) {
        List<Transaction> simplifiedTransactions = new ArrayList<>();

        // Max-Heap for Creditors (Net balance > 0)
        PriorityQueue<BalanceNode> creditors = new PriorityQueue<>((a, b) -> Double.compare(b.amount, a.amount));

        // Max-Heap for Debtors (Net balance < 0, stored as positive values for comparison)
        PriorityQueue<BalanceNode> debtors = new PriorityQueue<>((a, b) -> Double.compare(b.amount, a.amount));

        for (Map.Entry<User, Double> entry : netBalances.entrySet()) {
            double bal = Math.round(entry.getValue() * 100.0) / 100.0;
            if (bal > 0.01) {
                creditors.add(new BalanceNode(entry.getKey(), bal));
            } else if (bal < -0.01) {
                debtors.add(new BalanceNode(entry.getKey(), -bal));
            }
        }

        while (!creditors.isEmpty() && !debtors.isEmpty()) {
            BalanceNode creditor = creditors.poll();
            BalanceNode debtor = debtors.poll();

            double settleAmount = Math.min(creditor.amount, debtor.amount);
            settleAmount = Math.round(settleAmount * 100.0) / 100.0;

            if (settleAmount > 0) {
                simplifiedTransactions.add(new Transaction(debtor.user, creditor.user, settleAmount));
            }

            creditor.amount -= settleAmount;
            debtor.amount -= settleAmount;

            if (creditor.amount > 0.01) {
                creditors.add(creditor);
            }
            if (debtor.amount > 0.01) {
                debtors.add(debtor);
            }
        }

        return simplifiedTransactions;
    }
}
