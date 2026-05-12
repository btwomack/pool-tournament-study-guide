# Pool Tournament App: Week 1 Study Guide (Interview Prep)

This guide provides an in-depth explanation of the concepts, patterns, and design decisions implemented in Week 1 of the Pool Tournament Management App. It is designed to help you prepare for junior/entry-level Java developer interviews.

---

## 1. Project Structure and Organization

### What it is and why it exists
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

### Key Patterns
*   **Repository Pattern**: Abstracts the data store. The application interacts with interfaces rather than specific database implementation details.
*   **DTO Pattern**: Decouples the internal data model (Entities) from the external API (DTOs). This prevents leaking sensitive database details and allows the API to evolve independently of the database.

> **Interview Question**: Why don't we return Entities directly from our Controllers?
> **Answer**: Returning entities directly can lead to several issues:
> 1. **Security**: You might accidentally expose sensitive fields like `passwordHash`.
> 2. **Performance**: Large object graphs (via Lazy Loading) might be serialized, causing "N+1" select problems or circular reference errors.
> 3. **Tight Coupling**: Any change to the database schema would immediately break the API contract for clients.

---

## 2. Maven and Dependency Management

### What it is
**Maven** is a build automation tool used primarily for Java projects. It manages project dependencies, the build lifecycle, and project documentation.

### Key Concepts
*   **`pom.xml`**: The Project Object Model file where dependencies (like Spring Boot, JPA, Security) are defined.
*   **Dependency Injection (DI)**: Maven downloads the JAR files, but Spring's Inversion of Control (IoC) container manages the instantiation and "wiring" of these classes.

---

## 3. JPA, Entities, and Hibernate

### What it is
**JPA (Jakarta Persistence API)** is a specification for Object-Relational Mapping (ORM) in Java. **Hibernate** is the most popular implementation of this specification.

### Key Annotations
*   `@Entity`: Marks a class as a database-mapped object.
*   `@Table`: Specifies the primary table for the entity.
*   `@Id` & `@GeneratedValue`: Defines the primary key and its generation strategy (we use `UUID`).
*   `@ManyToOne` & `@OneToMany`: Defines relationships between tables.

### Flyway vs. Hibernate ddl-auto
In this project, we set `hibernate.ddl-auto: validate` and use **Flyway** for versioned SQL migrations.

> **Interview Question**: Why use Flyway instead of letting Hibernate create the tables (`ddl-auto: update`)?
> **Answer**: `ddl-auto: update` is dangerous for production because it's non-deterministic and can't handle complex migrations (like renaming columns or data migration). Flyway provides a versioned, reproducible history of the schema that is identical across all environments (dev, staging, prod).

---

## 4. Spring Security and JWT Flow

### The Filter Chain
Spring Security works through a series of filters. We've added a custom `JwtAuthenticationFilter` before the standard `UsernamePasswordAuthenticationFilter`.

### Stateless Authentication with JWT
1.  **Login**: User provides credentials.
2.  **Issuance**: `AuthService` verifies credentials and uses `JwtTokenProvider` to create a signed JWT.
3.  **Storage**: The client stores this token (usually in LocalStorage or a Cookie).
4.  **Verification**: For subsequent requests, the client sends the token in the `Authorization: Bearer <token>` header. Our filter validates the signature and sets the user context.

### BCrypt Password Hashing
We never store plain-text passwords. `BCryptPasswordEncoder` uses a slow hashing algorithm with a built-in salt to protect against rainbow table and brute-force attacks.

---

## 5. Security Hardening: IDOR and Rate Limiting

### IDOR (Insecure Direct Object Reference)
An IDOR attack occurs when a user changes an ID in a URL (e.g., `/api/tournaments/123`) to access a resource they don't own.
*   **Defense**: We use `TournamentOwnershipChecker` to verify that the `createdBy` ID of the tournament matches the `id` of the authenticated user before allowing any modifications.

### Rate Limiting
Prevents automated abuse (like "botting" a tournament signup). We've planned a filter to limit the number of requests from a single IP address within a time window.

---

## 6. Global Exception Handling

### `@RestControllerAdvice`
This annotation allows us to handle exceptions across the entire application in one place.
*   **Benefit**: It ensures that the API always returns a consistent JSON error format, rather than a messy HTML stack trace from the server.

---

## 7. Jakarta Bean Validation

### Annotations used:
*   `@NotBlank`: String must not be null and must have at least one non-whitespace character.
*   `@Email`: Must be a valid email format.
*   `@Min` / `@Max`: Numerical constraints.

These are checked at the Controller level using the `@Valid` annotation on request bodies.

---

## Common Interview "Quick-Fire" Questions

1.  **What is `@Transactional`?**
    It ensures that a series of database operations either all succeed or all fail (Atomicity). If an exception occurs, the database rolls back to its previous state.
2.  **What is the difference between `@RestController` and `@Controller`?**
    `@RestController` is a convenience annotation that combines `@Controller` and `@ResponseBody`. It means every method returns a data object (JSON) instead of a view (HTML).
3.  **What is Dependency Injection?**
    It's a pattern where an object's dependencies are "injected" into it (usually via the constructor) by the Spring container, rather than the object creating them itself. This makes the code more testable (via mocking).
4.  **What is a "Bean" in Spring?**
    A Bean is simply an object that is instantiated, assembled, and managed by the Spring IoC container.
