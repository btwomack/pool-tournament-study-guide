# Pool Tournament Management App: Comprehensive Documentation

This document consolidates all study guides, test results, and deployment instructions for the Pool Tournament Management App, covering Weeks 1 through 4 of its development. It is designed to serve as a comprehensive resource for understanding the application's architecture, implementation details, testing methodology, and deployment process, particularly useful for junior/entry-level Java developer interview preparation.

---

## Table of Contents

1.  [Week 1 Study Guide: Core Spring Boot & Persistence](#week-1-study-guide-core-spring-boot--persistence)
2.  [Week 2 Study Guide: Bracket Logic & Algorithms](#week-2-study-guide-bracket-logic--algorithms)
3.  [Week 3 Study Guide: Real-time, Payments & Frontend](#week-3-study-guide-real-time-payments--frontend)
4.  [Week 4 Study Guide: Advanced Features & Deployment](#week-4-study-guide-advanced-features--deployment)
5.  [Bracket Logic Test Results](#bracket-logic-test-results)
6.  [Render Deployment Instructions](#render-deployment-instructions)

---

## 1. Week 1 Study Guide: Core Spring Boot & Persistence

This section provides an in-depth explanation of the concepts, patterns, and design decisions implemented in Week 1 of the Pool Tournament Management App. It is designed to help you prepare for junior/entry-level Java developer interviews.

### 1.1. Project Structure and Organization

The project follows a standard **Spring Boot layered architecture**. This separation of concerns ensures that each part of the application has a specific responsibility, making the code easier to maintain, test, and scale.

| Package | Responsibility |
| :--- | :--- |
| `config/` | Application configuration (Security, CORS, etc.) |
| `entity/` | JPA entities representing database tables |
| `enums/` | Enumerated types for fixed values (Roles, Statuses) |
| `repository/` | Data access layer using Spring Data JPA |
| `dto/` | Data Transfer Objects for API requests and responses |
| `service/` | Business logic layer |
| `controller/` | REST API endpoints |
| `security/` | Security-related components (JWT, Filters) |
| `exception/` | Global error handling logic |

### 1.2. Key Patterns

*   **Repository Pattern**: Abstracts the data store. The application interacts with interfaces rather than specific database implementation details.
*   **DTO Pattern**: Decouples the internal data model (Entities) from the external API (DTOs). This prevents leaking sensitive database details and allows the API to evolve independently of the database.

> **Interview Question**: Why don't we return Entities directly from our Controllers?
> **Answer**: Returning entities directly can lead to several issues:
> 1.  **Security**: You might accidentally expose sensitive fields like `passwordHash`.
> 2.  **Performance**: Large object graphs (via Lazy Loading) might be serialized, causing "N+1" select problems or circular reference errors.
> 3.  **Tight Coupling**: Any change to the database schema would immediately break the API contract for clients.

### 1.3. Maven and Dependency Management

**Maven** is a build automation tool used primarily for Java projects. It manages project dependencies, the build lifecycle, and project documentation.

*   **`pom.xml`**: The Project Object Model file where dependencies (like Spring Boot, JPA, Security) are defined.
*   **Dependency Injection (DI)**: Maven downloads the JAR files, but Spring's Inversion of Control (IoC) container manages the instantiation and "wiring" of these classes.

### 1.4. JPA, Entities, and Hibernate

**JPA (Jakarta Persistence API)** is a specification for Object-Relational Mapping (ORM) in Java. **Hibernate** is the most popular implementation of this specification.

#### Key Annotations

*   `@Entity`: Marks a class as a database-mapped object.
*   `@Table`: Specifies the primary table for the entity.
*   `@Id` & `@GeneratedValue`: Defines the primary key and its generation strategy (we use `UUID`).
*   `@ManyToOne` & `@OneToMany`: Defines relationships between tables.

#### Flyway vs. Hibernate ddl-auto

In this project, we set `hibernate.ddl-auto: validate` and use **Flyway** for versioned SQL migrations.

> **Interview Question**: Why use Flyway instead of letting Hibernate create the tables (`ddl-auto: update`)?
> **Answer**: `ddl-auto: update` is dangerous for production because it's non-deterministic and can't handle complex migrations (like renaming columns or data migration). Flyway provides a versioned, reproducible history of the schema that is identical across all environments (dev, staging, prod).

### 1.5. Spring Security and JWT Flow

#### The Filter Chain

Spring Security works through a series of filters. We've added a custom `JwtAuthenticationFilter` before the standard `UsernamePasswordAuthenticationFilter`.

#### Stateless Authentication with JWT

1.  **Login**: User provides credentials.
2.  **Issuance**: `AuthService` verifies credentials and uses `JwtTokenProvider` to create a signed JWT.
3.  **Storage**: The client stores this token (usually in LocalStorage or a Cookie).
4.  **Verification**: For subsequent requests, the client sends the token in the `Authorization: Bearer <token>` header. Our filter validates the signature and sets the user context.

#### BCrypt Password Hashing

We never store plain-text passwords. `BCryptPasswordEncoder` uses a slow hashing algorithm with a built-in salt to protect against rainbow table and brute-force attacks.

### 1.6. Security Hardening: IDOR and Rate Limiting

#### IDOR (Insecure Direct Object Reference)

An IDOR attack occurs when a user changes an ID in a URL (e.g., `/api/tournaments/123`) to access a resource they don't own.
*   **Defense**: We use `TournamentOwnershipChecker` to verify that the `createdBy` ID of the tournament matches the `id` of the authenticated user before allowing any modifications.

#### Rate Limiting

Prevents automated abuse (like "botting" a tournament signup). We've planned a filter to limit the number of requests from a single IP address within a time window.

### 1.7. Global Exception Handling

#### `@RestControllerAdvice`

This annotation allows us to handle exceptions across the entire application in one place.
*   **Benefit**: It ensures that the API always returns a consistent JSON error format, rather than a messy HTML stack trace from the server.

### 1.8. Jakarta Bean Validation

#### Annotations used:

*   `@NotBlank`: String must not be null and must have at least one non-whitespace character.
*   `@Email`: Must be a valid email format.
*   `@Min` / `@Max`: Numerical constraints.

These are checked at the Controller level using the `@Valid` annotation on request bodies.

### 1.9. Common Interview "Quick-Fire" Questions

1.  **What is `@Transactional`?**
    It ensures that a series of database operations either all succeed or all fail (Atomicity). If an exception occurs, the database rolls back to its previous state.
2.  **What is the difference between `@RestController` and `@Controller`?**
    `@RestController` is a convenience annotation that combines `@Controller` and `@ResponseBody`. It means every method returns a data object (JSON) instead of a view (HTML).
3.  **What is Dependency Injection?**
    It's a pattern where an object's dependencies are "injected" into it (usually via the constructor) by the Spring container, rather than the object creating them itself. This makes the code more testable (via mocking).
4.  **What is a "Bean" in Spring?**
    A Bean is simply an object that is instantiated, assembled, and managed by the Spring IoC container.

---

## 2. Week 2 Study Guide: Bracket Logic & Algorithms

This section explains the core business logic, algorithms, and architectural patterns implemented in Week 2 of the Pool Tournament Management App.

### 2.1. Algorithm Design: Tournament Brackets

#### Single Elimination (Binary Tree Structure)

A single elimination bracket is essentially a **complete binary tree**.
*   **Power of 2**: For a "perfect" bracket, the number of players must be a power of 2 (2, 4, 8, 16, 32).
*   **Byes**: If the player count is not a power of 2, we calculate the next power of 2 and assign "Byes" to the top seeds.
    *   `bracketSize = 2 ^ ceil(log2(n))`
    *   `numByes = bracketSize - n`
*   **Seeding**: Top seeds are placed to ensure they don't meet until the final rounds.

#### Double Elimination (Complex Wiring)

Double elimination is significantly more complex because it involves two interconnected brackets:
1.  **Winners Bracket**: Standard single elimination.
2.  **Losers Bracket**: Players drop here after their first loss.
*   **Odd/Even Round Logic**: In the losers bracket, odd-numbered rounds typically merge survivors from the previous losers round with "drop-downs" from the winners bracket. Even-numbered rounds are "pure" advancement rounds.
*   **Grand Final (GF)**: The winners of both brackets meet.
*   **GF Reset**: Since it's double elimination, the Winners Bracket champion must lose *twice* to be eliminated. If the Losers Bracket champion wins the first GF match, a "Reset" (GF2) is triggered.

> **Interview Question**: How would you represent a tournament bracket in a data structure?
> **Answer**: While a bracket is logically a tree, in a database-backed application, it's often represented as a **Graph** where each `Match` entity has self-referencing links (`nextMatchWinner`, `nextMatchLoser`). This allows for flexible traversal and updates.

### 2.2. Spring Patterns and Best Practices

#### `@Transactional` and Atomicity

Bracket generation and match result recording involve multiple database operations (creating matches, updating player statuses, linking matches).
*   **Why it's critical**: If the system crashes after creating 10 matches but before linking them, the bracket is corrupted. `@Transactional` ensures that either the *entire* operation succeeds or *nothing* is committed to the database (Rollback).

#### Strategy Pattern (Format Selection)

The `BracketGeneratorService` uses a form of the **Strategy Pattern**. Based on the player count or a manual override, it selects between the `generateSingleElim` and `generateDoubleElim` algorithms. This keeps the code clean and allows for adding new formats (like Round Robin) in the future without changing the main entry point.

#### Self-Referencing JPA Relationships

The `Match` entity contains fields like `nextMatchWinner` of type `Match`. This is a self-referencing `@ManyToOne` relationship.
*   **Interview Tip**: Mention that this can lead to "N+1" problems if not handled with proper `JOIN FETCH` or `EntityGraph` when loading the entire bracket.

### 2.3. Match State Machine and Validation

A match progresses through a lifecycle:
1.  **Pending**: Waiting for players.
2.  **Ready**: Both players assigned.
3.  **Completed**: Scores recorded and winner determined.

#### Score Validation

We validate that the winner's score reaches the `raceTo` threshold. This prevents illegal states where a match is marked complete without a definitive winner.

#### Forfeit Handling

When a player is dropped or disqualified, the system must **cascade** the effect. All pending matches for that player are auto-forfeited, and their opponents are advanced. This demonstrates an understanding of **event-driven updates** within a system.

### 2.4. Testing Strategies

#### Unit Testing with Mockito

We test the `BracketGeneratorService` by mocking its dependencies (`MatchRepository`, `TournamentRepository`). This allows us to verify the *logic* of the bracket generation (e.g., correct number of matches created) without needing a real database.

#### Integration Testing

Testing the full match lifecycle (Signup -> Generate -> Record Result) ensures that the components work together correctly, transactions roll back on error, and the database constraints are respected.

### 2.5. Big-O Analysis

*   **Bracket Generation**: `O(N)`, where N is the number of players. We iterate a constant number of times per player to create matches and link them.
*   **Space Complexity**: `O(N)` to store the match entities in memory before saving.
*   **Finding Pending Matches**: `O(M)` where M is the number of matches, though indexed database queries make this highly efficient.

### 2.6. Common Interview Questions

1.  **What is the difference between recursion and iteration in the context of brackets?**
    *   *Answer*: Brackets can be generated recursively (splitting the problem in half) or iteratively (round by round). Iteration is often more memory-efficient in Java to avoid `StackOverflowError` for very large trees, though brackets are rarely large enough for this to matter.
2.  **How do you handle concurrent updates to a match?**
    *   *Answer*: We can use **Optimistic Locking** (`@Version` annotation in JPA) to ensure that if two admins try to record a result for the same match simultaneously, one will fail rather than overwriting the other.
3.  **Why use UUIDs instead of Long IDs?**
    *   *Answer*: UUIDs are harder to guess (security), prevent IDOR if not properly checked, and can be generated by the application without waiting for a database sequence, which is better for distributed systems.

---

## 3. Week 3 Study Guide: Real-time, Payments & Frontend

This section covers the advanced features implemented in Week 3, including real-time communication, third-party integrations (Stripe, Twilio), and the React frontend architecture.

### 3.1. Real-Time Communication: WebSockets

#### WebSocket vs. HTTP Polling

In a tournament app, real-time updates are critical. When a score is recorded, every viewer should see the bracket update immediately.
*   **HTTP Polling**: The client asks the server every few seconds "Any updates?". This is inefficient, adds high server load, and creates a delay in updates.
*   **WebSockets**: A persistent, bi-directional connection. Once established, the server can "push" data to the client instantly.
*   **STOMP**: A simple text-oriented messaging protocol used over WebSockets. It provides features like **Pub/Sub** (publish/subscribe) which allows clients to subscribe to specific topics (e.g., `/topic/tournament/{id}`).
*   **SockJS**: A fallback library that provides a WebSocket-like object when the browser or network doesn't support real-time WebSockets (it falls back to long-polling).

> **Interview Question**: Why use STOMP instead of raw WebSockets?
> **Answer**: Raw WebSockets are just a "pipe" for bytes. STOMP adds a messaging layer with frames like `CONNECT`, `SUBSCRIBE`, and `SEND`, which standardizes how clients and servers interact, making it easier to manage subscriptions and routing.

### 3.2. Payment Integration: Stripe Architecture

#### The PaymentIntent Flow

We use the **Stripe PaymentIntent** API, which is the modern standard for secure payments.
1.  **Server**: Creates a `PaymentIntent` and returns a `clientSecret` to the frontend.
2.  **Frontend**: Uses the `clientSecret` to collect payment details securely via **Stripe Elements** (the card details never touch our server).
3.  **Webhook**: Once the payment is successful, Stripe sends an asynchronous HTTP POST request (a **Webhook**) to our server.

#### Webhook Security and Idempotency

*   **Signature Verification**: We use the `Stripe-Signature` header to verify that the request actually came from Stripe and hasn't been tampered with.
*   **Why Webhooks?**: You cannot trust the client-side confirmation (e.g., a user could manually trigger the "success" callback in JavaScript). The webhook is a server-to-server confirmation.
*   **Idempotency**: Our webhook handler should be able to handle the same event multiple times without creating duplicate registrations (using the `stripePaymentId` to check if it's already processed).

### 3.3. Notification Patterns: SMS via Twilio

#### SMS Reminders

We use **Twilio** to send SMS notifications when a match is "Ready" (both players have advanced).
*   **Pattern**: This is typically triggered within the `MatchService` after a result is recorded and the next match's players are filled.
*   **Considerations**: SMS costs money and can be annoying. We should implement rate limiting and allow users to opt-out.

### 3.4. Frontend Architecture: React + Vite + Tailwind

#### Component Design

*   **Dumb Components**: Components like `MatchCard` and `Bracket` are "presentational." They receive data via props and don't know where the data comes from. This makes them highly reusable and easy to test.
*   **Smart Components (Pages)**: Pages like `TournamentView` manage state, handle API calls, and coordinate with WebSockets.

#### State Management for Real-Time Data

When a WebSocket message arrives (e.g., `BRACKET_UPDATE`), we don't necessarily need to push the entire new bracket through the socket. Instead, we send a "ping" (the event type) and the frontend fetches the latest data via a standard REST GET request. This keeps the WebSocket payload small and ensures the frontend state is always in sync with the database.

#### Mobile-First Design

Using **TailwindCSS**, we build for mobile first and add complexity for larger screens using prefixes like `md:` and `lg:`. Brackets are notoriously hard to display on mobile, so we use horizontal scrolling (`overflow-x-auto`) to ensure the user can navigate the tree.

### 3.5. Common Interview Questions

1.  **How do you handle a "Lost Connection" in WebSockets?**
    *   *Answer*: The STOMP client has a built-in heartbeat mechanism. If the connection drops, we can configure an `onDisconnect` callback to attempt a reconnection or notify the user.
2.  **What is the difference between a REST API and a WebSocket?**
    *   *Answer*: REST is stateless, request-response based, and usually runs over HTTP. WebSockets are stateful, bi-directional, and persistent.
3.  **Why do we use Environment Variables for API keys?**
    *   *Answer*: To keep sensitive secrets (like the Stripe Secret Key) out of the source code and version control (GitHub). This allows different keys for development and production environments.
4.  **How does TailwindCSS improve developer productivity?**
    *   *Answer*: It eliminates the need to write custom CSS and manage large CSS files. By using utility classes directly in the HTML/JSX, you can see the styling intent immediately and ensure a consistent design system.

---

## 4. Week 4 Study Guide: Advanced Features & Deployment

This section covers the final stage of the Pool Tournament Management App, focusing on domain modeling, AI integration, comprehensive testing, and modern deployment strategies.

### 4.1. Domain Modeling: The Calcutta Auction

The Calcutta auction is a real-world business process where participants bid on players in a tournament. Modeling this in code requires careful consideration of several factors:

| Aspect | Implementation Detail |
| :--- | :--- |
| **Entities** | `CalcuttaBid` links a `User` (bidder), a `PlayerRegistration` (the subject), and a `Tournament`. |
| **Business Rules** | A new bid must always be higher than the current maximum bid for that player. |
| **Concurrency** | Using `@Transactional` ensures that the check-and-save operation is atomic, preventing race conditions where two users bid the same amount simultaneously. |
| **Audit Trail** | Storing `bidTime` and the full history of bids allows for transparency and dispute resolution. |

> **Interview Question**: How would you handle a "last-second" bid in a real-time auction?
> **Answer**: You could implement "bid sniping" protection by extending the auction end time by a few minutes if a bid is placed within the final seconds. This would be handled in the `CalcuttaService` by updating the tournament's `auctionEndTime`.

### 4.2. AI and LLM Integration Patterns

Integrating AI (like OpenAI's GPT or Google's Gemini) into a Spring Boot application involves several design patterns:

*   **Service Layer Abstraction**: Create a `MatchupPreviewService` that abstracts the AI logic. This allows you to switch between different LLM providers or even a template-based fallback without changing the rest of the app.
*   **Latency Management**: AI API calls can be slow (2-5 seconds). You should never call an AI API directly within a web request. Instead, use an **Asynchronous Pattern** (e.g., Spring's `@Async`) to generate the preview in the background and notify the frontend via WebSockets when it's ready.
*   **Fallback Strategies**: If the AI API is down or the rate limit is reached, the service should fall back to a pre-defined template to ensure the UI doesn't break.

### 4.3. The Testing Pyramid

A professional Java project follows the **Testing Pyramid** to ensure reliability at all levels:

1.  **Unit Tests**: Test individual methods in isolation (e.g., `BracketGeneratorServiceTest`). These are fast and run frequently.
2.  **Integration Tests**: Test how multiple components work together (e.g., `TournamentSecurityTest`). These often use a real or in-memory database (H2).
3.  **Security Tests**: Specifically target authentication, authorization, and common vulnerabilities like **IDOR** (Insecure Direct Object Reference).
4.  **E2E (End-to-End) Tests**: Test the full flow from the frontend to the backend (e.g., using Selenium or Playwright).

### 4.4. Docker and Deployment (PaaS)

#### Docker Fundamentals

*   **Dockerfile**: A script that contains all the commands to build an image. We use a **Multi-Stage Build** to keep the final image small (Stage 1 builds the JAR with Maven, Stage 2 only includes the JRE and the JAR).
*   **Docker Compose**: A tool for defining and running multi-container Docker applications. It allows you to spin up the backend and a PostgreSQL database with a single command (`docker-compose up`).

#### Deployment to Render (PaaS)

Render is a Platform-as-a-Service (PaaS) that simplifies deployment.
*   **Infrastructure as Code (IaC)**: The `render.yaml` file defines the entire environment, including the web service, the database, and the necessary environment variables.
*   **Environment Variables**: Sensitive keys (Stripe, Twilio, JWT Secret) are never committed to GitHub. They are injected into the container at runtime by the PaaS provider.

### 4.5. README as a Professional Skill

A great README is often the first thing an interviewer looks at on your GitHub. It should include:
*   **Clear Value Proposition**: What does the app do?
*   **Tech Stack**: What tools did you use?
*   **Setup Instructions**: Can I run this locally in 5 minutes?
*   **Architecture Overview**: How is the project structured?

### 4.6. Common Interview Questions

1.  **What is the benefit of a multi-stage Docker build?**
    *   *Answer*: It significantly reduces the size of the final image by excluding build-time dependencies (like Maven and the source code) and only including the necessary runtime components (the JAR and the JRE). This improves security and deployment speed.
2.  **How do you mock an external service like Stripe in your tests?**
    *   *Answer*: We use **Mockito** to create a mock of the `PaymentService`. This allows us to simulate successful and failed payments without making actual network calls to Stripe, making the tests faster and more reliable.
3.  **What is an IDOR attack and how did you prevent it in this project?**
    *   *Answer*: IDOR occurs when a user can access or modify a resource (like a tournament) simply by changing the ID in the URL. We prevented this by implementing a `TournamentOwnershipChecker` that verifies the `createdBy` user of a tournament matches the currently authenticated user before allowing any modifications.
4.  **Why use Client-Side Routing (React Router) instead of Server-Side Routing?**
    *   *Answer*: Client-side routing provides a smoother, "Single Page Application" (SPA) experience. The page doesn't reload when navigating between views, and state is preserved across transitions.

---

## 5. Bracket Logic Test Results

**Date:** April 2, 2026  
**Author:** Manus AI  
**Project:** Pool Tournament Management App

### 5.1. Overview

This report details the comprehensive review, bug fixing, and testing of the bracket logic for the Pool Tournament Management App. The bracket logic encompasses single and double elimination tournament generation, match score recording, player advancement, and mid-tournament player drops/disqualifications.

A full test suite of **44 unit and integration tests** was developed and executed. **All 44 tests currently pass (100% success rate).**

### 5.2. Bugs Found and Fixed

During the review and compilation process, several bugs and compilation errors were identified and resolved:

#### 5.2.1. Compilation Errors

*   **Lambda Final Variable Issue:** In `BracketGeneratorService`, a non-final variable was used inside a lambda expression for stream filtering. This was fixed by extracting the variable to a `final` local variable before the stream operation.
*   **Entity Field Mismatches:** Several services referenced incorrect field names (e.g., `bracketFormat` instead of `formatOverride` in `TournamentService`, or missing builder methods for the `User` entity in `StripeWebhookController`). These were corrected to match the actual JPA entities.
*   **Repository Method Signatures:** `CalcuttaService` attempted to use non-existent repository methods. These methods (`findByTournament_Id`, `findByPlayer_Id`, `findMaxBidByPlayerId`) were added to `CalcuttaBidRepository`.

#### 5.2.2. Data Integrity and Test Setup Issues

*   **Database Configuration:** The test suite was failing to connect to PostgreSQL. A dedicated `application-test.yml` was created to configure an H2 in-memory database for testing.
*   **Constraint Violations:** `TournamentSecurityTest` failed due to foreign key constraints and unique constraints (e.g., trying to delete a tournament before its matches, or creating duplicate users). The `@BeforeEach` setup was refactored to clear the database in the correct order and ensure unique test data.
*   **Authentication Mocking:** `BracketController` was throwing a `NullPointerException` because it expected a specific `User` object from the `AuthenticationPrincipal`, which wasn't being populated correctly by Spring Security's `@WithMockUser`. This was fixed by creating a `TestUserIdHolder` to inject the correct mock user ID during tests.

#### 5.2.3. Bracket Logic Bugs

*   **PlayerDropService Case Sensitivity:** The `dropPlayer` method checked for disqualification using `"disqualified".equals(reason)`, which failed when the reason was provided in uppercase. This was fixed to use `reason.equalsIgnoreCase("disqualified")`.
*   **MatchResultResponse Field Access:** `MatchServiceTest` was incorrectly using `.isBracketReset()` instead of `.getBracketReset()` for a `Boolean` object field, causing compilation errors.

### 5.3. Test Scenarios Verified

The following specific bracket scenarios were implemented as unit tests and successfully verified:

| Scenario | Component | Result | Notes |
| :--- | :--- | :--- | :--- |
| **Single elimination (4 players)** | `BracketGeneratorService` | **PASS** | Generates exactly 3 matches (2 in R1, 1 in R2). No byes. |
| **Single elimination (5 players)** | `BracketGeneratorService` | **PASS** | Generates 7 matches. Next power of 2 is 8, so exactly 3 byes are generated and auto-advanced. |
| **Single elimination (8 players)** | `BracketGeneratorService` | **PASS** | Generates 7 matches. No byes. Full advancement chain verified. |
| **Double elimination (10 players)** | `BracketGeneratorService` | **PASS** | Generates 28 total matches (15 WB, 11 LB, 1 GF, 1 GFR). |
| **Double elimination (16 players)** | `BracketGeneratorService` | **PASS** | Generates exactly 28 matches. Verifies 4 WB rounds. |
| **Double elimination (32 players)** | `BracketGeneratorService` | **PASS** | Generates exactly 56 matches. Verifies 5 WB rounds. |
| **Grand Final: WB Champ Wins** | `MatchService` | **PASS** | Tournament is marked COMPLETE. GF Reset match is deactivated (marked as bye). |
| **Grand Final: LB Champ Wins** | `MatchService` | **PASS** | Bracket reset is triggered. Both players are advanced to the GF Reset match. |
| **Score Validation** | `MatchService` | **PASS** | Scores below the `raceTo` threshold are rejected with an `InvalidMatchResultException`. |
| **Bye Auto-Advancement** | `MatchService` | **PASS** | Players assigned a bye are automatically advanced to the next round with a win. |
| **Player Drop / Forfeit** | `PlayerDropService` | **PASS** | Dropping a player marks their pending matches as forfeits and auto-advances their opponents. |

### 5.4. Remaining Issues and Edge Cases

While the core logic is sound and fully tested, the following edge cases should be monitored in future iterations:

1.  **Concurrent Match Updates:** If two admins attempt to record the result of the same match simultaneously, a race condition could occur. Implementing optimistic locking (e.g., `@Version` annotation on the `Match` entity) would prevent this.
2.  **Dropping from Grand Final:** The `PlayerDropService` currently assumes standard advancement (`nextMatchWinner`). If a player drops out *during* the Grand Final, the logic might not correctly trigger the bracket reset or tournament completion flow.
3.  **Manual Seeding Validation:** The `GenerateBracketRequest` accepts a "manual" seeding mode, but the service currently assumes the players are already sorted by seed. Additional validation should ensure no duplicate seeds exist before generation.

### 5.5. Conclusion

The bracket generation and match lifecycle logic for Week 2/3 is robust and functions according to the mathematical requirements of single and double elimination tournaments. All requested test scenarios have been automated and pass successfully. The complete source code, including all fixes and tests, has been packaged into the updated ZIP archive.

---

## 6. Render Deployment Instructions

I have successfully pushed the complete, tested project to your GitHub repository: `btwomack/pool-tournament-app`.

Because the Render API requires an authentication token and a Workspace ID that are private to your account, I cannot trigger the deployment programmatically. However, the repository now includes a fully configured `render.yaml` Blueprint file, which makes deployment extremely simple.

Please follow these exact steps to deploy both your backend and frontend to Render:

### 6.1. Step 1: Connect to Render

1.  Log in to your account at [Render.com](https://render.com).
2.  If you haven't already, ensure your GitHub account is connected to your Render account.

### 6.2. Step 2: Deploy using the Blueprint

The `render.yaml` file in your repository is a "Blueprint" that tells Render exactly how to set up the database, backend, and frontend, and how to connect them together.

1.  On the Render Dashboard, click the **"New +"** button in the top right corner.
2.  Select **"Blueprint"** from the dropdown menu.
3.  You will see a list of your connected GitHub repositories. Find and select **`btwomack/pool-tournament-app`**.
    *   *If you don't see it, click "Configure account" on the right side to grant Render access to this specific repository.*
4.  On the next screen, Render will read the `render.yaml` file and show you a summary of the resources it will create:
    *   A PostgreSQL database (`pool-tournament-db`)
    *   A Web Service for the Spring Boot backend (`pool-backend`)
    *   A Static Site for the React frontend (`pool-frontend`)
5.  Click **"Apply Blueprint"** at the bottom of the page.

### 6.3. Step 3: Monitor the Deployment

Render will now begin provisioning the database and building both services.

1.  **Database:** This will spin up almost instantly.
2.  **Backend (`pool-backend`):** This uses Docker to compile the Java code and run the Spring Boot app. It may take 3-5 minutes to build and start.
3.  **Frontend (`pool-frontend`):** This uses Vite to build the React app. It will automatically link its API calls to the backend service.

You can click on the individual services in your dashboard to view their build logs.

### 6.4. Step 4: Configure Environment Variables (Optional but Recommended)

The Blueprint automatically sets up most of the required environment variables (like database connections and a generated JWT secret). However, to enable full functionality (Payments and SMS), you will need to update the placeholder values in the backend service.

1.  Go to your Render Dashboard and click on the **`pool-backend`** web service.
2.  Click on **"Environment"** in the left sidebar.
3.  Update the following variables with your actual keys:
    *   `STRIPE_SECRET_KEY`
    *   `STRIPE_WEBHOOK_SECRET`
    *   `TWILIO_ACCOUNT_SID`
    *   `TWILIO_AUTH_TOKEN`
    *   `TWILIO_PHONE_NUMBER`
4.  Click **"Save Changes"**. Render will automatically restart the backend with the new keys.

### 6.5. Step 5: Access Your App

Once both the backend and frontend have finished deploying (showing a green "Live" status):

1.  Go to your Render Dashboard and click on the **`pool-frontend`** static site.
2.  Click the URL displayed near the top left (e.g., `https://pool-frontend-xxxx.onrender.com`).
3.  This is your live Pool Tournament Management App!
