package splitwise;

import splitwise.CoreModels.*;
import splitwise.Enums.SplitType;

import java.util.List;

public class SplitStrategy {

    public interface ISplitStrategy {
        boolean validateSplits(double totalAmount, List<Split> splits);
        void computeSplits(double totalAmount, List<Split> splits);
    }

    public static class EqualSplitStrategy implements ISplitStrategy {
        @Override
        public boolean validateSplits(double totalAmount, List<Split> splits) {
            return splits != null && !splits.isEmpty() && totalAmount > 0;
        }

        @Override
        public void computeSplits(double totalAmount, List<Split> splits) {
            int count = splits.size();
            double equalShare = Math.floor((totalAmount / count) * 100.0) / 100.0;
            double remainder = Math.round((totalAmount - (equalShare * count)) * 100.0) / 100.0;

            for (int i = 0; i < count; i++) {
                double share = equalShare + (i == 0 ? remainder : 0.0);
                splits.get(i).setAmount(share);
            }
        }
    }

    public static class ExactSplitStrategy implements ISplitStrategy {
        @Override
        public boolean validateSplits(double totalAmount, List<Split> splits) {
            if (splits == null || splits.isEmpty() || totalAmount <= 0) return false;
            double sum = 0.0;
            for (Split s : splits) {
                sum += s.getAmount();
            }
            return Math.abs(sum - totalAmount) < 0.01;
        }

        @Override
        public void computeSplits(double totalAmount, List<Split> splits) {
            // Amounts are already specified in ExactSplit
        }
    }

    public static class PercentageSplitStrategy implements ISplitStrategy {
        @Override
        public boolean validateSplits(double totalAmount, List<Split> splits) {
            if (splits == null || splits.isEmpty() || totalAmount <= 0) return false;
            double totalPercentage = 0.0;
            for (Split s : splits) {
                if (s instanceof PercentageSplit) {
                    totalPercentage += ((PercentageSplit) s).getPercentage();
                }
            }
            return Math.abs(totalPercentage - 100.0) < 0.01;
        }

        @Override
        public void computeSplits(double totalAmount, List<Split> splits) {
            for (Split s : splits) {
                if (s instanceof PercentageSplit) {
                    PercentageSplit ps = (PercentageSplit) s;
                    double amount = Math.round((totalAmount * ps.getPercentage() / 100.0) * 100.0) / 100.0;
                    ps.setAmount(amount);
                }
            }
        }
    }

    public static class SplitStrategyFactory {
        public static ISplitStrategy getStrategy(SplitType type) {
            switch (type) {
                case EQUAL:
                    return new EqualSplitStrategy();
                case EXACT:
                    return new ExactSplitStrategy();
                case PERCENTAGE:
                    return new PercentageSplitStrategy();
                default:
                    return new EqualSplitStrategy();
            }
        }
    }
}
