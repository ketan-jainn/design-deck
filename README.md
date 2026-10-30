# Design Deck

Design Deck is a rapid-fire system design practice app in a local monorepo:

- `apps/web`: Next.js App Router frontend
- `apps/api`: Spring Boot REST API (JPA + Spring Data)
- PostgreSQL with Flyway migrations
- Spring-owned JWT auth with BCrypt password hashing
- Gemini-backed answer grading

See [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) for system design and [docs/AI_HANDOFF.md](docs/AI_HANDOFF.md) for AI/developer onboarding.

## Prerequisites

- Node.js 22+
- npm 10+
- Java 21+
- Maven 3.9+
- PostgreSQL 15+ (or Docker)

## Configuration

Copy the example env file and edit as needed:

```bash
cp .env.example .env
```

Key variables:

```bash
DATABASE_URL=jdbc:postgresql://localhost:5432/designdeck
DATABASE_USERNAME=designdeck
DATABASE_PASSWORD=designdeck
JWT_SECRET=replace-with-a-long-random-secret
BAZAARLINK_API_KEY=your-bazaarlink-api-key
BAZAARLINK_MODEL=qwen/qwen3.7-flash:free
FRONTEND_URL=http://localhost:3000
API_BASE_URL=http://localhost:8080
```

For password reset emails, set `RESEND_API_KEY` and set `MAIL_FROM` to an address on a domain verified with Resend. The fallback `noreply@designdeck.dev` is a placeholder and requires domain verification before it can send email. Without a Resend API key, reset links are logged locally.

## Development

Install frontend dependencies:

```bash
npm install
```

Run PostgreSQL only (Docker):

```bash
npm run docker:db
```

Run the API:

```bash
npm run dev:api
```

Run the web app:

```bash
npm run dev:web
```

Open `http://localhost:3000`.

## Docker (full stack)

```bash
cp .env.example .env
npm run docker:up
```

This starts PostgreSQL, Redis, the API on `:8080`, and the web app on `:3000`.

Both Compose configurations use the `designdeck` project and its database volume. A fresh volume is initialized with the `designdeck` database and development credentials. If you have an existing `.env`, update its database values to match `.env.example`. Stop any old containers using ports 5432, 6379, 8080, or 3000 before starting the new setup. Previous database volumes are left untouched; their data is not migrated into the new database. Existing sessions require signing in again.

Stop containers:

```bash
npm run docker:down
```

## Verification

```bash
npm run build:web
npm run build:api
```

The API runs Flyway on startup and seeds the Design Deck question catalog.
