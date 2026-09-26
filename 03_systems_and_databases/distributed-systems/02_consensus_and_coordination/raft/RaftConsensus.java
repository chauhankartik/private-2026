package raft;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Executable Java simulation of the Raft Consensus State Machine.
 * Demonstrates Leader Election, Term Management, and Log Entry Replication across a cluster.
 */
public class RaftConsensus {

    public enum Role { FOLLOWER, CANDIDATE, LEADER }

    public static class LogEntry {
        public final int term;
        public final int index;
        public final String command;

        public LogEntry(int term, int index, String command) {
            this.term = term;
            this.index = index;
            this.command = command;
        }

        @Override
        public String toString() {
            return String.format("[Term:%d, idx:%d, cmd:'%s']", term, index, command);
        }
    }

    public static class RaftNode {
        public final int nodeId;
        public Role currentRole = Role.FOLLOWER;
        public int currentTerm = 0;
        public Integer votedFor = null;
        public final List<LogEntry> log = new ArrayList<>();
        public int commitIndex = 0;
        public int lastApplied = 0;

        private final List<RaftNode> cluster = new ArrayList<>();

        public RaftNode(int nodeId) {
            this.nodeId = nodeId;
        }

        public void setCluster(List<RaftNode> cluster) {
            this.cluster.clear();
            this.cluster.addAll(cluster);
        }

        // --- Election Phase ---
        public synchronized void startElection() {
            currentRole = Role.CANDIDATE;
            currentTerm++;
            votedFor = nodeId;
            int votes = 1;
            System.out.printf("Node %d started Election for Term %d\n", nodeId, currentTerm);

            int lastLogIndex = log.size();
            int lastLogTerm = log.isEmpty() ? 0 : log.get(log.size() - 1).term;

            for (RaftNode peer : cluster) {
                if (peer.nodeId != this.nodeId) {
                    boolean granted = peer.requestVote(currentTerm, nodeId, lastLogIndex, lastLogTerm);
                    if (granted) votes++;
                }
            }

            int majority = (cluster.size() / 2) + 1;
            if (votes >= majority) {
                currentRole = Role.LEADER;
                System.out.printf(">>> Node %d won election and became LEADER for Term %d (Votes: %d/%d) <<<\n",
                        nodeId, currentTerm, votes, cluster.size());
            } else {
                currentRole = Role.FOLLOWER;
                votedFor = null;
            }
        }

        public synchronized boolean requestVote(int term, int candidateId, int lastLogIndex, int lastLogTerm) {
            if (term > currentTerm) {
                currentTerm = term;
                currentRole = Role.FOLLOWER;
                votedFor = null;
            }

            int myLastLogIndex = log.size();
            int myLastLogTerm = log.isEmpty() ? 0 : log.get(log.size() - 1).term;

            boolean logIsUpToDate = lastLogTerm > myLastLogTerm ||
                    (lastLogTerm == myLastLogTerm && lastLogIndex >= myLastLogIndex);

            if (term == currentTerm && (votedFor == null || votedFor == candidateId) && logIsUpToDate) {
                votedFor = candidateId;
                System.out.printf("Node %d voted YES for Candidate %d in Term %d\n", nodeId, candidateId, term);
                return true;
            }
            return false;
        }

        // --- Client Write & Replication Phase ---
        public synchronized boolean submitCommand(String command) {
            if (currentRole != Role.LEADER) {
                System.out.printf("Node %d REJECTED command (Not Leader)\n", nodeId);
                return false;
            }

            int newIndex = log.size() + 1;
            LogEntry entry = new LogEntry(currentTerm, newIndex, command);
            log.add(entry);
            System.out.printf("Leader Node %d appended command to log: %s\n", nodeId, entry);

            int ackCount = 1;
            for (RaftNode peer : cluster) {
                if (peer.nodeId != nodeId) {
                    boolean success = peer.appendEntries(currentTerm, nodeId, newIndex - 1,
                            newIndex > 1 ? log.get(newIndex - 2).term : 0, Collections.singletonList(entry), commitIndex);
                    if (success) ackCount++;
                }
            }

            int majority = (cluster.size() / 2) + 1;
            if (ackCount >= majority) {
                commitIndex = newIndex;
                System.out.printf("Leader Node %d COMMITTED entry at index %d (Acks: %d/%d)\n",
                        nodeId, commitIndex, ackCount, cluster.size());
                return true;
            }
            return false;
        }

        public synchronized boolean appendEntries(int term, int leaderId, int prevLogIndex,
                                                 int prevLogTerm, List<LogEntry> entries, int leaderCommit) {
            if (term < currentTerm) return false;

            if (term > currentTerm) {
                currentTerm = term;
                currentRole = Role.FOLLOWER;
                votedFor = null;
            }

            // Append entries
            for (LogEntry e : entries) {
                if (e.index <= log.size()) {
                    log.set(e.index - 1, e);
                } else {
                    log.add(e);
                }
            }

            if (leaderCommit > commitIndex) {
                commitIndex = Math.min(leaderCommit, log.size());
            }
            return true;
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Raft Consensus Protocol Simulation ===");

        List<RaftNode> cluster = Arrays.asList(
                new RaftNode(1),
                new RaftNode(2),
                new RaftNode(3),
                new RaftNode(4),
                new RaftNode(5)
        );

        for (RaftNode node : cluster) {
            node.setCluster(cluster);
        }

        // Node 1 triggers election
        cluster.get(0).startElection();

        // Submit client writes
        cluster.get(0).submitCommand("SET user:1 = 'Alice'");
        cluster.get(0).submitCommand("TRANSFER $100 -> $200");

        // Verify committed logs across nodes
        System.out.println("\n--- Cluster Log Status ---");
        for (RaftNode node : cluster) {
            System.out.printf("Node %d [%s] (Term: %d, CommitIdx: %d) Log: %s\n",
                    node.nodeId, node.currentRole, node.currentTerm, node.commitIndex, node.log);
        }
    }
}
