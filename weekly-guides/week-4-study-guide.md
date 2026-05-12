# Pool Tournament App: Week 4 Study Guide (Interview Prep)

This guide covers the final stage of the Pool Tournament Management App, focusing on domain modeling, AI integration, comprehensive testing, and modern deployment strategies.

---

## 1. Domain Modeling: The Calcutta Auction

The Calcutta auction is a real-world business process where participants bid on players in a tournament. Modeling this in code requires careful consideration of several factors:

| Aspect | Implementation Detail |
| :--- | :--- |
| **Entities** | `CalcuttaBid` links a `User` (bidder), a `PlayerRegistration` (the subject), and a `Tournament`. |
| **Business Rules** | A new bid must always be higher than the current maximum bid for that player. |
| **Concurrency** | Using `@Transactional` ensures that the check-and-save operation is atomic, preventing race conditions where two users bid the same amount simultaneously. |
| **Audit Trail** | Storing `bidTime` and the full history of bids allows for transparency and dispute resolution. |

> **Interview Question**: How would you handle a "last-second" bid in a real-time auction?
> **Answer**: You could implement "bid sniping" protection by extending the auction end time by a few minutes if a bid is placed within the final seconds. This would be handled in the `CalcuttaService` by updating the tournament's `auctionEndTime`.

---

## 2. AI and LLM Integration Patterns

Integrating AI (like OpenAI's GPT or Google's Gemini) into a Spring Boot application involves several design patterns:

*   **Service Layer Abstraction**: Create a `MatchupPreviewService` that abstracts the AI logic. This allows you to switch between different LLM providers or even a template-based fallback without changing the rest of the app.
*   **Latency Management**: AI API calls can be slow (2-5 seconds). You should never call an AI API directly within a web request. Instead, use an **Asynchronous Pattern** (e.g., Spring's `@Async`) to generate the preview in the background and notify the frontend via WebSockets when it's ready.
*   **Fallback Strategies**: If the AI API is down or the rate limit is reached, the service should fall back to a pre-defined template to ensure the UI doesn't break.

---

## 3. The Testing Pyramid

A professional Java project follows the **Testing Pyramid** to ensure reliability at all levels:

1.  **Unit Tests**: Test individual methods in isolation (e.g., `BracketGeneratorServiceTest`). These are fast and run frequently.
2.  **Integration Tests**: Test how multiple components work together (e.g., `TournamentSecurityTest`). These often use a real or in-memory database (H2).
3.  **Security Tests**: Specifically target authentication, authorization, and common vulnerabilities like **IDOR** (Insecure Direct Object Reference).
4.  **E2E (End-to-End) Tests**: Test the full flow from the frontend to the backend (e.g., using Selenium or Playwright).

---

## 4. Docker and Deployment (PaaS)

### Docker Fundamentals
*   **Dockerfile**: A script that contains all the commands to build an image. We use a **Multi-Stage Build** to keep the final image small (Stage 1 builds the JAR with Maven, Stage 2 only includes the JRE and the JAR).
*   **Docker Compose**: A tool for defining and running multi-container Docker applications. It allows you to spin up the backend and a PostgreSQL database with a single command (`docker-compose up`).

### Deployment to Render (PaaS)
Render is a Platform-as-a-Service (PaaS) that simplifies deployment.
*   **Infrastructure as Code (IaC)**: The `render.yaml` file defines the entire environment, including the web service, the database, and the necessary environment variables.
*   **Environment Variables**: Sensitive keys (Stripe, Twilio, JWT Secret) are never committed to GitHub. They are injected into the container at runtime by the PaaS provider.

---

## 5. README as a Professional Skill

A great README is often the first thing an interviewer looks at on your GitHub. It should include:
*   **Clear Value Proposition**: What does the app do?
*   **Tech Stack**: What tools did you use?
*   **Setup Instructions**: Can I run this locally in 5 minutes?
*   **Architecture Overview**: How is the project structured?

---

## Common Interview Questions

1.  **What is the benefit of a multi-stage Docker build?**
    *   *Answer*: It significantly reduces the size of the final image by excluding build-time dependencies (like Maven and the source code) and only including the necessary runtime components (the JAR and the JRE). This improves security and deployment speed.
2.  **How do you mock an external service like Stripe in your tests?**
    *   *Answer*: We use **Mockito** to create a mock of the `PaymentService`. This allows us to simulate successful and failed payments without making actual network calls to Stripe, making the tests faster and more reliable.
3.  **What is an IDOR attack and how did you prevent it in this project?**
    *   *Answer*: IDOR occurs when a user can access or modify a resource (like a tournament) simply by changing the ID in the URL. We prevented this by implementing a `TournamentOwnershipChecker` that verifies the `createdBy` user of a tournament matches the currently authenticated user before allowing any modifications.
4.  **Why use Client-Side Routing (React Router) instead of Server-Side Routing?**
    *   *Answer*: Client-side routing provides a smoother, "Single Page Application" (SPA) experience. The page doesn't reload when navigating between views, and state is preserved across transitions.
