# Chapter 2: Entity Lifecycle & Persistence Context

The **Persistence Context** acts as an in-memory staging area (First-Level L1 Cache) between application code and the database, managing entity state transitions and automatic SQL generation.

---

## 1. The 4 Entity Lifecycle States

```
                 new Entity()
                      |
                      v
               [ TRANSIENT ]  (No DB Identity, Not Managed)
                      |
        persist() /   |
        save()        | merge()
                      v
               [ PERSISTENT ] (Has DB Identity, Managed in L1 Cache)
                /     |
       detach() /     | remove()
       clear()        v
               [ REMOVED ]    (Scheduled for SQL DELETE)
                      |
               [ DETACHED ]   (Has DB Identity, Session Closed)
```

| State | Associated with Session? | Has DB Primary Key? | Dirty Checking Active? |
| :--- | :--- | :--- | :--- |
| **`Transient`** | **No** | No (`null`) | No |
| **`Persistent`** | **Yes** | **Yes** | **Yes** (Automated SQL `UPDATE` on commit) |
| **`Detached`** | **No** | **Yes** | No |
| **`Removed`** | **Yes** | **Yes** | Scheduled for `DELETE` |

---

## 2. State Transition APIs: `persist()` vs `merge()`

### 2.1 `persist(entity)`
* Moves a `Transient` instance into the `Persistent` state.
* Does **NOT** immediately fire an SQL `INSERT` statement unless the ID generation strategy is `IDENTITY`.
* Modifying fields on a `persist()`ed entity *after* calling `persist()` will automatically include those updates in the eventual SQL `INSERT`/`UPDATE`.

### 2.2 `merge(entity)`
* Copies the field state of a `Detached` entity onto a **new Managed Persistent entity instance** inside the current session.
* Returns the new Managed instance. The original `Detached` object passed as an argument **remains Detached**!

```java
// MERGE WARNING: Always use the returned instance!
User detachedUser = getDetachedUser();
User managedUser = session.merge(detachedUser); // Correct!

detachedUser.setName("Jane"); // WRONG: Modifies Detached object (ignored by Hibernate!)
managedUser.setName("Jane");  // CORRECT: Modifies Managed object (triggers UPDATE)
```

---

## 3. Persistence Context Mechanics: L1 Cache & Dirty Checking

```
+-------------------------------------------------------------------+
| Persistence Context (StatefulPersistenceContext)                  |
|                                                                   |
| [ Identity Map ]  --> Map<EntityKey, Object>                      |
|                       Guarantees: userA == userB in same Session  |
|                                                                   |
| [ Entity Snapshots ] -> Loaded Field State Snapshots [Val1, Val2] |
|                                                                   |
| [ ActionQueue ]   --> Deterministic DML Execution Queue           |
+-------------------------------------------------------------------+
```

### 3.1 Automatic Dirty Checking
During `session.flush()`, Hibernate iterates through all managed entities in the Identity Map, comparing current field values against the initial loading snapshot:

```java
// Automatic Dirty Checking Example
User user = session.find(User.class, 1L); // Entity loaded into L1 Cache & Snapshot saved
user.setEmail("newemail@example.com");    // Field modified in RAM

// NO session.update() or repository.save() IS NEEDED!
transaction.commit(); // Triggers flush() -> Snapshot diff -> SQL UPDATE automatically generated!
```

### 3.2 `ActionQueue` Deterministic Execution Order
To prevent Foreign Key constraint violations, Hibernate's `ActionQueue` executes queued SQL statements in strict deterministic order during `flush()`, regardless of the order Java code was executed:

1. `OrphanRemovalAction`
2. `EntityInsertAction`
3. `EntityUpdateAction`
4. `CollectionRemoveAction` / `CollectionUpdateAction`
5. `EntityDeleteAction`

---

## 4. Staff Engineer Entity Lifecycle Rules
1. **Always Use the Return Value of `merge()`:** Never mutate the detached object instance passed into `session.merge()`. Always use the returned managed instance reference.
2. **Clear Session During Large Bulk Iterations:** When reading 50,000 entities in a loop, invoke `session.flush()` and `session.clear()` periodically (e.g. every 100 entities) to prevent L1 Cache memory bloat and `OutOfMemoryError`.
3. **Do Not Call `save()` on Managed Entities:** Calling `save()` or `update()` on an already `Persistent` managed entity creates unnecessary processing overhead. Allow automatic dirty checking to handle updates.
