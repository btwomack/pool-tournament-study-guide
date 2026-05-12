# Pool Tournament App: Week 3 Study Guide (Interview Prep)

This guide covers the advanced features implemented in Week 3, including real-time communication, third-party integrations (Stripe, Twilio), and the React frontend architecture.

---

## 1. Real-Time Communication: WebSockets

### WebSocket vs. HTTP Polling
In a tournament app, real-time updates are critical. When a score is recorded, every viewer should see the bracket update immediately.
*   **HTTP Polling**: The client asks the server every few seconds "Any updates?". This is inefficient, adds high server load, and creates a delay in updates.
*   **WebSockets**: A persistent, bi-directional connection. Once established, the server can "push" data to the client instantly.
*   **STOMP**: A simple text-oriented messaging protocol used over WebSockets. It provides features like **Pub/Sub** (publish/subscribe) which allows clients to subscribe to specific topics (e.g., `/topic/tournament/{id}`).
*   **SockJS**: A fallback library that provides a WebSocket-like object when the browser or network doesn't support real-time WebSockets (it falls back to long-polling).

> **Interview Question**: Why use STOMP instead of raw WebSockets?
> **Answer**: Raw WebSockets are just a "pipe" for bytes. STOMP adds a messaging layer with frames like `CONNECT`, `SUBSCRIBE`, and `SEND`, which standardizes how clients and servers interact, making it easier to manage subscriptions and routing.

---

## 2. Payment Integration: Stripe Architecture

### The PaymentIntent Flow
We use the **Stripe PaymentIntent** API, which is the modern standard for secure payments.
1.  **Server**: Creates a `PaymentIntent` and returns a `clientSecret` to the frontend.
2.  **Frontend**: Uses the `clientSecret` to collect payment details securely via **Stripe Elements** (the card details never touch our server).
3.  **Webhook**: Once the payment is successful, Stripe sends an asynchronous HTTP POST request (a **Webhook**) to our server.

### Webhook Security and Idempotency
*   **Signature Verification**: We use the `Stripe-Signature` header to verify that the request actually came from Stripe and hasn't been tampered with.
*   **Why Webhooks?**: You cannot trust the client-side confirmation (e.g., a user could manually trigger the "success" callback in JavaScript). The webhook is a server-to-server confirmation.
*   **Idempotency**: Our webhook handler should be able to handle the same event multiple times without creating duplicate registrations (using the `stripePaymentId` to check if it's already processed).

---

## 3. Notification Patterns: SMS via Twilio

### SMS Reminders
We use **Twilio** to send SMS notifications when a match is "Ready" (both players have advanced).
*   **Pattern**: This is typically triggered within the `MatchService` after a result is recorded and the next match's players are filled.
*   **Considerations**: SMS costs money and can be annoying. We should implement rate limiting and allow users to opt-out.

---

## 4. Frontend Architecture: React + Vite + Tailwind

### Component Design
*   **Dumb Components**: Components like `MatchCard` and `Bracket` are "presentational." They receive data via props and don't know where the data comes from. This makes them highly reusable and easy to test.
*   **Smart Components (Pages)**: Pages like `TournamentView` manage state, handle API calls, and coordinate with WebSockets.

### State Management for Real-Time Data
When a WebSocket message arrives (e.g., `BRACKET_UPDATE`), we don't necessarily need to push the entire new bracket through the socket. Instead, we send a "ping" (the event type) and the frontend fetches the latest data via a standard REST GET request. This keeps the WebSocket payload small and ensures the frontend state is always in sync with the database.

### Mobile-First Design
Using **TailwindCSS**, we build for mobile first and add complexity for larger screens using prefixes like `md:` and `lg:`. Brackets are notoriously hard to display on mobile, so we use horizontal scrolling (`overflow-x-auto`) to ensure the user can navigate the tree.

---

## Common Interview Questions

1.  **How do you handle a "Lost Connection" in WebSockets?**
    *   *Answer*: The STOMP client has a built-in heartbeat mechanism. If the connection drops, we can configure an `onDisconnect` callback to attempt a reconnection or notify the user.
2.  **What is the difference between a REST API and a WebSocket?**
    *   *Answer*: REST is stateless, request-response based, and usually runs over HTTP. WebSockets are stateful, bi-directional, and persistent.
3.  **Why do we use Environment Variables for API keys?**
    *   *Answer*: To keep sensitive secrets (like the Stripe Secret Key) out of the source code and version control (GitHub). This allows different keys for development and production environments.
4.  **How does TailwindCSS improve developer productivity?**
    *   *Answer*: It eliminates the need to write custom CSS and manage large CSS files. By using utility classes directly in the HTML/JSX, you can see the styling intent immediately and ensure a consistent design system.
