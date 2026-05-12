# Pool Tournament Management App

A comprehensive, real-time tournament management system built with Spring Boot 3.3, Java 21, and React.

## Features
- **Bracket Engine**: Support for Single and Double Elimination brackets.
- **Real-time Updates**: Live bracket viewing and score updates via WebSockets.
- **Calcutta Auction**: Bidding system for players in the tournament.
- **AI Previews**: AI-generated matchup previews and post-match recaps.
- **Payments**: Secure tournament entry fees using Stripe.
- **Notifications**: SMS match reminders via Twilio.
- **Security**: JWT-based authentication and role-based access control.

## Tech Stack
- **Backend**: Spring Boot, Spring Security, JPA/Hibernate, Flyway, PostgreSQL.
- **Frontend**: React, TypeScript, TailwindCSS, Vite.
- **Communication**: STOMP over WebSockets.
- **Third-Party**: Stripe, Twilio, OpenAI (Matchup Previews).

## Setup Instructions

### Local Development (Docker)
1.  Clone the repository.
2.  Ensure Docker and Docker Compose are installed.
3.  Run `docker-compose -f deployment/docker-compose.yml up --build`.
4.  The backend will be available at `http://localhost:8080`.

### Local Development (Manual)
1.  **Backend**:
    -   Configure a PostgreSQL database.
    -   Set environment variables (see `.env.example`).
    -   Run `./mvnw spring-boot:run`.
2.  **Frontend**:
    -   Navigate to `frontend/`.
    -   Run `npm install` and `npm run dev`.
    -   The frontend will be available at `http://localhost:3000`.

## Deployment
This project is configured for deployment on **Render** using the provided `render.yaml` file. Simply connect your GitHub repository to Render, and it will automatically build and deploy the application.

## Testing
- Run backend tests: `./mvnw test`
- Security tests are located in `src/test/java/com/pooltournament/security/`.
