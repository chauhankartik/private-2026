package two_phase_commit_2pc;

import java.util.*;

/**
 * Executable Java simulation of the Two-Phase Commit (2PC) Protocol.
 * Demonstrates Prepare Phase, Voting, Decision Phase, and Rollback on Participant Rejection.
 */
public class TwoPhaseCommitCoordinator {

    public enum Vote { YES, NO }

    public static class ParticipantNode {
        public final int nodeId;
        private boolean locked = false;

        public ParticipantNode(int nodeId) {
            this.nodeId = nodeId;
        }

        // Phase 1: Prepare
        public synchronized Vote prepare(String txId) {
            // Simulate 90% success rate
            if (Math.random() > 0.1) {
                locked = true;
                System.out.printf("Participant %d: Voted YES for Tx '%s' (Row Locks Acquired)\n", nodeId, txId);
                return Vote.YES;
            } else {
                System.out.printf("Participant %d: Voted NO for Tx '%s' (Resource Contention / Lock Failed)\n", nodeId, txId);
                return Vote.NO;
            }
        }

        // Phase 2: Commit
        public synchronized void commit(String txId) {
            if (locked) {
                locked = false;
                System.out.printf("Participant %d: COMMITTED Tx '%s' (Locks Released)\n", nodeId, txId);
            }
        }

        // Phase 2: Rollback
        public synchronized void rollback(String txId) {
            if (locked) {
                locked = false;
                System.out.printf("Participant %d: ROLLED BACK Tx '%s' (Locks Released)\n", nodeId, txId);
            }
        }
    }

    public static class TransactionCoordinator {
        private final List<ParticipantNode> participants;

        public TransactionCoordinator(List<ParticipantNode> participants) {
            this.participants = participants;
        }

        public boolean executeTransaction(String txId) {
            System.out.printf("\n================ Starting 2PC Transaction '%s' ================\n", txId);

            // Phase 1: Prepare & Vote Collection
            System.out.println("--- PHASE 1: PREPARE & VOTE ---");
            boolean allVotedYes = true;

            for (ParticipantNode p : participants) {
                Vote v = p.prepare(txId);
                if (v == Vote.NO) {
                    allVotedYes = false;
                }
            }

            // Phase 2: Decision & Execution
            System.out.println("--- PHASE 2: DECISION EXECUTION ---");
            if (allVotedYes) {
                System.out.printf(">>> Coordinator Decision for '%s': GLOBAL COMMIT <<<\n", txId);
                for (ParticipantNode p : participants) {
                    p.commit(txId);
                }
                return true;
            } else {
                System.out.printf(">>> Coordinator Decision for '%s': GLOBAL ABORT / ROLLBACK <<<\n", txId);
                for (ParticipantNode p : participants) {
                    p.rollback(txId);
                }
                return false;
            }
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Two-Phase Commit (2PC) Protocol Simulation ===");

        List<ParticipantNode> participants = Arrays.asList(
                new ParticipantNode(1),
                new ParticipantNode(2),
                new ParticipantNode(3)
        );

        TransactionCoordinator coordinator = new TransactionCoordinator(participants);

        // Execute transactions
        coordinator.executeTransaction("TX_1001_BANK_TRANSFER");
        coordinator.executeTransaction("TX_1002_ORDER_CHECKOUT");
    }
}
