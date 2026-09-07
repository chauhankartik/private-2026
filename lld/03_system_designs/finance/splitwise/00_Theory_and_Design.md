# Expense Sharing System (Splitwise) — LLD Deep Dive (Interview Edition)

> **Interview Difficulty:** Google / Amazon L5–L6  
> **Core Patterns Used:** Strategy, Factory Method, Singleton, Facade, Composite, Observer  
> **SOLID Coverage:** All 5 principles applied  
> **Key Technical Challenge:** Expense validation across 3 split types (Equal, Exact, Percentage) and **Graph Debt Simplification (Min Cash Flow Algorithm)**.

---

## 1. Problem Statement (What the Interviewer Gives You)

> "Design an Expense Sharing System like Splitwise. It should support:
> - User & Group Management (Create groups for Trips, Housemates, Events).
> - Adding Expenses with multiple split strategies:
>   1. **Equal Split** — Amount is divided equally among participants.
>   2. **Exact Split** — Exact custom amount specified per participant (Sum must equal total).
>   3. **Percentage Split** — Custom percentage assigned per participant (Sum of % must equal 100%).
> - Tracking individual & group balances (Who owes whom and how much).
> - **Debt Simplification Algorithm (Min Cash Flow)** — Minimizes the total number of monetary transactions needed to settle all debts in a group.
> - Settlement workflow (User A pays User B to clear balance)."

---

## 2. Clarifying Questions (Ask These First!)

| Question | Why It Matters |
|---|---|
| Can an expense be **paid by multiple users** simultaneously? | Single payer simplifies model (`paidBy: User`); multi-payer requires `Map<User, Double> paidBy`. |
| Is **Debt Simplification** automatic or optional per group? | Debt simplification reduces $N \times (N-1)$ transactions to at most $N-1$ transactions. |
| How do we handle **precision/rounding issues** in Equal/Percentage splits? | E.g. $100 / 3 = 33.333...$; first participant absorbs remainder cents ($33.34 + 33.33 + 33.33 = 100.00$). |
| Should we track **historical transactions** and balance updates? | Immutability for expenses vs mutable balance sheet. |

---

## 3. Debt Simplification Algorithm (Min Cash Flow Graph Algorithm)

```
                            BEFORE SIMPLIFICATION (6 Transactions)
                            
                 Alice ──────────── owes $50 ───────────► Bob
                   │                                      │
               owes $30                                owes $40
                   │                                      │
                   ▼                                      ▼
                Charlie ─────────── owes $20 ───────────► Dave
                
                                         │
                                         ▼ Calculate Net Balances
                          Alice: -$80   Bob: +$10   Charlie: +$10   Dave: +$60
                                         │
                                         ▼ Min Cash Flow Greedy Optimization
                            
                            AFTER SIMPLIFICATION (2 Transactions)
                            
                 Alice ───────────────── pays $60 ───────────────► Dave
                 Alice ───────────────── pays $10 ───────────────► Bob
                 Alice ───────────────── pays $10 ───────────────► Charlie
```

### Min Cash Flow Logic:
1. Compute **Net Balance** for each user ($Net = TotalPaid - TotalOwed$).
2. Separate users into **Debtors** ($Net < 0$) and **Creditors** ($Net > 0$).
3. Greedily match max debtor with max creditor: $Settlement = \min(|MaxDebtor|, |MaxCreditor|)$.
4. Repeat until all net balances become $0$. Reduces transactions to maximum $N-1$.

---

## 4. Class Diagram (UML Architecture)

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                           SplitwiseService (Facade/Singleton)                   │
│  - users: Map<String, User>                                                     │
│  - groups: Map<String, Group>                                                   │
│  - debtSimplifier: DebtSimplifier                                              │
│  + addExpense(paidBy, amount, splits, splitType): Expense                       │
│  + simplifyGroupDebts(groupId): List<Transaction>                              │
│  + settleDebt(fromUser, toUser, amount): void                                  │
└─────────────────────────────────────────────────────────────────────────────────┘
         │                                       │
         ▼ has-many                              ▼ uses
┌────────────────────────────────┐      ┌────────────────────────────────┐
│             Expense            │      │         ISplitStrategy         │
│  - expenseId: String           │      │          <<interface>>         │
│  - amount: double              │      │  + validateAndCompute(...):    │
│  - paidBy: User                │      │     List<Split>                │
│  - splits: List<Split>         │      └────────────────────────────────┘
└────────────────────────────────┘                       ▲
                 │                     ┌─────────────────┼─────────────────┐
                 ▼ contains            │                 │                 │
┌────────────────────────────────┐  EqualStrategy  ExactStrategy  PercentageStrategy
│         Split (Abstract)       │
│  - user: User                  │
│  - amount: double              │
└────────────────────────────────┘
        ▲           ▲           ▲
        │           │           │
   EqualSplit  ExactSplit  PercentageSplit
```

---

## 5. Design Patterns Applied

| Pattern | Component | Why It Was Chosen |
|---|---|---|
| **Strategy** | `ISplitStrategy` | Encapsulates dynamic split logic (`Equal`, `Exact`, `Percentage`) and validation rules. |
| **Facade / Singleton** | `SplitwiseService` | Provides a unified entry point for users, groups, expenses, and settlements. |
| **Factory Method** | `SplitStrategyFactory` | Returns the correct `ISplitStrategy` based on `SplitType`. |
| **Composite** | `Group` & `Expense` | Allows treating individual expenses and group-aggregated expenses uniformly. |
