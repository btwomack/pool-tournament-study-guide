# Pool Tournament App: Week 2 Study Guide (Interview Prep)

This guide explains the core business logic, algorithms, and architectural patterns implemented in Week 2 of the Pool Tournament Management App.

---

## 1. Algorithm Design: Tournament Brackets

### Single Elimination (Binary Tree Structure)
A single elimination bracket is essentially a **complete binary tree**.
*   **Power of 2**: For a "perfect" bracket, the number of players must be a power of 2 (2, 4, 8, 16, 32).
*   **Byes**: If the player count is not a power of 2, we calculate the next power of 2 and assign "Byes" to the top seeds.
    *   `bracketSize = 2 ^ ceil(log2(n))`
    *   `numByes = bracketSize - n`
*   **Seeding**: Top seeds are placed to ensure they don't meet until the final rounds.

### Double Elimination (Complex Wiring)
Double elimination is significantly more complex because it involves two interconnected brackets:
1.  **Winners Bracket**: Standard single elimination.
2.  **Losers Bracket**: Players drop here after their first loss.
*   **Odd/Even Round Logic**: In the losers bracket, odd-numbered rounds typically merge survivors from the previous losers round with "drop-downs" from the winners bracket. Even-numbered rounds are "pure" advancement rounds.
*   **Grand Final (GF)**: The winners of both brackets meet.
*   **GF Reset**: Since it's double elimination, the Winners Bracket champion must lose *twice* to be eliminated. If the Losers Bracket champion wins the first GF match, a "Reset" (GF2) is triggered.

> **Interview Question**: How would you represent a tournament bracket in a data structure?
> **Answer**: While a bracket is logically a tree, in a database-backed application, it's often represented as a **Graph** where each `Match` entity has self-referencing links (`nextMatchWinner`, `nextMatchLoser`). This allows for flexible traversal and updates.

---

## 2. Spring Patterns and Best Practices

### `@Transactional` and Atomicity
Bracket generation and match result recording involve multiple database operations (creating matches, updating player statuses, linking matches).
*   **Why it's critical**: If the system crashes after creating 10 matches but before linking them, the bracket is corrupted. `@Transactional` ensures that either the *entire* operation succeeds or *nothing* is committed to the database (Rollback).

### Strategy Pattern (Format Selection)
The `BracketGeneratorService` uses a form of the **Strategy Pattern**. Based on the player count or a manual override, it selects between the `generateSingleElim` and `generateDoubleElim` algorithms. This keeps the code clean and allows for adding new formats (like Round Robin) in the future without changing the main entry point.

### Self-Referencing JPA Relationships
The `Match` entity contains fields like `nextMatchWinner` of type `Match`. This is a self-referencing `@ManyToOne` relationship.
*   **Interview Tip**: Mention that this can lead to "N+1" problems if not handled with proper `JOIN FETCH` or `EntityGraph` when loading the entire bracket.

---

## 3. Match State Machine and Validation

A match progresses through a lifecycle:
1.  **Pending**: Waiting for players.
2.  **Ready**: Both players assigned.
3.  **Completed**: Scores recorded and winner determined.

### Score Validation
We validate that the winner's score reaches the `raceTo` threshold. This prevents illegal states where a match is marked complete without a definitive winner.

### Forfeit Handling
When a player is dropped or disqualified, the system must **cascade** the effect. All pending matches for that player are auto-forfeited, and their opponents are advanced. This demonstrates an understanding of **event-driven updates** within a system.

---

## 4. Testing Strategies

### Unit Testing with Mockito
We test the `BracketGeneratorService` by mocking its dependencies (`MatchRepository`, `TournamentRepository`). This allows us to verify the *logic* of the bracket generation (e.g., correct number of matches created) without needing a real database.

### Integration Testing
Testing the full match lifecycle (Signup -> Generate -> Record Result) ensures that the components work together correctly, transactions roll back on error, and the database constraints are respected.

---

## 5. Big-O Analysis

*   **Bracket Generation**: `O(N)`, where N is the number of players. We iterate a constant number of times per player to create matches and link them.
*   **Space Complexity**: `O(N)` to store the match entities in memory before saving.
*   **Finding Pending Matches**: `O(M)` where M is the number of matches, though indexed database queries make this highly efficient.

---

## Common Interview Questions

1.  **What is the difference between recursion and iteration in the context of brackets?**
    *   *Answer*: Brackets can be generated recursively (splitting the problem in half) or iteratively (round by round). Iteration is often more memory-efficient in Java to avoid `StackOverflowError` for very large trees, though brackets are rarely large enough for this to matter.
2.  **How do you handle concurrent updates to a match?**
    *   *Answer*: We can use **Optimistic Locking** (`@Version` annotation in JPA) to ensure that if two admins try to record a result for the same match simultaneously, one will fail rather than overwriting the other.
3.  **Why use UUIDs instead of Long IDs?**
    *   *Answer*: UUIDs are harder to guess (security), prevent IDOR if not properly checked, and can be generated by the application without waiting for a database sequence, which is better for distributed systems.
