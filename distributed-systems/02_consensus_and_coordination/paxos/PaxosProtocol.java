package paxos;

import java.util.*;

/**
 * Executable Java simulation of the Basic Paxos Protocol.
 * Demonstrates Proposer, Acceptor, and Learner interactions, Ballot numbers, and Quorum consensus.
 */
public class PaxosProtocol {

    public static class Promise {
        public final boolean granted;
        public final int highestAcceptedProposal;
        public final String highestAcceptedValue;

        public Promise(boolean granted, int highestAcceptedProposal, String highestAcceptedValue) {
            this.granted = granted;
            this.highestAcceptedProposal = highestAcceptedProposal;
            this.highestAcceptedValue = highestAcceptedValue;
        }
    }

    public static class AcceptorNode {
        public final int nodeId;
        private int highestPromisedProposal = -1;
        private int highestAcceptedProposal = -1;
        private String highestAcceptedValue = null;

        public AcceptorNode(int nodeId) {
            this.nodeId = nodeId;
        }

        // Phase 1b: Prepare Handler
        public synchronized Promise handlePrepare(int proposalNumber) {
            if (proposalNumber > highestPromisedProposal) {
                highestPromisedProposal = proposalNumber;
                System.out.printf("Acceptor %d: Promised Proposal #%d (LastAccepted: #%d = '%s')\n",
                        nodeId, proposalNumber, highestAcceptedProposal, highestAcceptedValue);
                return new Promise(true, highestAcceptedProposal, highestAcceptedValue);
            }
            System.out.printf("Acceptor %d: REJECTED Prepare #%d (Promised higher #%d)\n",
                    nodeId, proposalNumber, highestPromisedProposal);
            return new Promise(false, -1, null);
        }

        // Phase 2b: Accept Handler
        public synchronized boolean handleAccept(int proposalNumber, String value) {
            if (proposalNumber >= highestPromisedProposal) {
                highestPromisedProposal = proposalNumber;
                highestAcceptedProposal = proposalNumber;
                highestAcceptedValue = value;
                System.out.printf("Acceptor %d: ACCEPTED Proposal #%d with Value '%s'\n", nodeId, proposalNumber, value);
                return true;
            }
            System.out.printf("Acceptor %d: REJECTED Accept #%d (Promised higher #%d)\n",
                    nodeId, proposalNumber, highestPromisedProposal);
            return false;
        }
    }

    public static class ProposerNode {
        public final int proposerId;
        private final List<AcceptorNode> acceptors;

        public ProposerNode(int proposerId, List<AcceptorNode> acceptors) {
            this.proposerId = proposerId;
            this.acceptors = acceptors;
        }

        public boolean propose(int proposalNumber, String proposedValue) {
            System.out.printf("\n--- Proposer %d starting Paxos Proposal #%d for Value: '%s' ---\n",
                    proposerId, proposalNumber, proposedValue);

            // Phase 1: Prepare
            int promisesCount = 0;
            int maxSeenProposal = -1;
            String chosenValue = proposedValue;

            for (AcceptorNode acceptor : acceptors) {
                Promise p = acceptor.handlePrepare(proposalNumber);
                if (p.granted) {
                    promisesCount++;
                    if (p.highestAcceptedProposal > maxSeenProposal && p.highestAcceptedValue != null) {
                        maxSeenProposal = p.highestAcceptedProposal;
                        chosenValue = p.highestAcceptedValue;
                    }
                }
            }

            int majority = (acceptors.size() / 2) + 1;
            if (promisesCount < majority) {
                System.out.printf("Proposer %d: FAILED Phase 1 (Promises: %d/%d)\n", proposerId, promisesCount, acceptors.size());
                return false;
            }

            System.out.printf("Proposer %d: PASSED Phase 1. Selected Value to Accept: '%s'\n", proposerId, chosenValue);

            // Phase 2: Accept
            int acceptCount = 0;
            for (AcceptorNode acceptor : acceptors) {
                boolean accepted = acceptor.handleAccept(proposalNumber, chosenValue);
                if (accepted) acceptCount++;
            }

            if (acceptCount >= majority) {
                System.out.printf(">>> SUCCESS: Value '%s' COMMITTED with Proposal #%d (Accepted: %d/%d) <<<\n",
                        chosenValue, proposalNumber, acceptCount, acceptors.size());
                return true;
            } else {
                System.out.printf("Proposer %d: FAILED Phase 2 (Accepts: %d/%d)\n", proposerId, acceptCount, acceptors.size());
                return false;
            }
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Basic Paxos Protocol Simulation ===");

        List<AcceptorNode> acceptors = Arrays.asList(
                new AcceptorNode(1),
                new AcceptorNode(2),
                new AcceptorNode(3),
                new AcceptorNode(4),
                new AcceptorNode(5)
        );

        ProposerNode p1 = new ProposerNode(101, acceptors);
        ProposerNode p2 = new ProposerNode(102, acceptors);

        // Scenario 1: Proposer 1 succeeds
        p1.propose(10, "TX_100_DEPOSIT");

        // Scenario 2: Proposer 2 tries higher proposal number
        p2.propose(20, "TX_200_WITHDRAW");
    }
}
