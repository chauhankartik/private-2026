package saga_pattern;

import java.util.*;

/**
 * Executable Java simulation of the Saga Orchestration Pattern with Compensating Rollbacks.
 * Demonstrates local step execution, failure handling, and backward compensation execution.
 */
public class SagaOrchestrator {

    public interface SagaStep {
        String getName();
        boolean execute();
        void compensate();
    }

    public static class OrderServiceStep implements SagaStep {
        @Override
        public String getName() { return "OrderService:CreateOrder"; }

        @Override
        public boolean execute() {
            System.out.println("[Step 1] OrderService: Order PENDING created in OrderDB");
            return true;
        }

        @Override
        public void compensate() {
            System.out.println("[Compensate 1] OrderService: Order CANCELLED in OrderDB");
        }
    }

    public static class PaymentServiceStep implements SagaStep {
        private final boolean shouldFail;

        public PaymentServiceStep(boolean shouldFail) {
            this.shouldFail = shouldFail;
        }

        @Override
        public String getName() { return "PaymentService:ChargeCard"; }

        @Override
        public boolean execute() {
            if (shouldFail) {
                System.out.println("[Step 2] PaymentService: Charge Card FAILED (Insufficient Funds)");
                return false;
            }
            System.out.println("[Step 2] PaymentService: Card Charged $150 successfully");
            return true;
        }

        @Override
        public void compensate() {
            System.out.println("[Compensate 2] PaymentService: Refunded $150 to Card");
        }
    }

    public static class InventoryServiceStep implements SagaStep {
        @Override
        public String getName() { return "InventoryService:ReserveStock"; }

        @Override
        public boolean execute() {
            System.out.println("[Step 3] InventoryService: Stock Reserved in InventoryDB");
            return true;
        }

        @Override
        public void compensate() {
            System.out.println("[Compensate 3] InventoryService: Stock Released in InventoryDB");
        }
    }

    public static class Orchestrator {
        public boolean runSaga(String sagaId, List<SagaStep> steps) {
            System.out.printf("\n================ Executing Saga Orchestration '%s' ================\n", sagaId);
            Stack<SagaStep> executedSteps = new Stack<>();

            for (SagaStep step : steps) {
                System.out.printf("Executing: %s...\n", step.getName());
                boolean success = step.execute();

                if (success) {
                    executedSteps.push(step);
                } else {
                    System.out.printf("\n>>> SAGA FAILURE at '%s'! Triggering Backward Compensations <<<\n", step.getName());
                    rollback(executedSteps);
                    return false;
                }
            }

            System.out.printf(">>> SAGA SUCCESS: All steps for '%s' completed successfully <<<\n", sagaId);
            return true;
        }

        private void rollback(Stack<SagaStep> executedSteps) {
            while (!executedSteps.isEmpty()) {
                SagaStep step = executedSteps.pop();
                System.out.printf("Compensating: %s...\n", step.getName());
                step.compensate();
            }
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Saga Orchestration Pattern Simulation ===");

        Orchestrator orchestrator = new Orchestrator();

        // Workflow 1: Successful Saga
        List<SagaStep> successfulWorkflow = Arrays.asList(
                new OrderServiceStep(),
                new PaymentServiceStep(false), // Payment succeeds
                new InventoryServiceStep()
        );
        orchestrator.runSaga("SAGA_ORDER_1001", successfulWorkflow);

        // Workflow 2: Failed Saga at Payment Step -> Triggers Rollback of Order Step
        List<SagaStep> failedWorkflow = Arrays.asList(
                new OrderServiceStep(),
                new PaymentServiceStep(true), // Payment fails!
                new InventoryServiceStep()
        );
        orchestrator.runSaga("SAGA_ORDER_1002", failedWorkflow);
    }
}
