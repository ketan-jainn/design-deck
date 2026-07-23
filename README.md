# RapidSD

RapidSD is a rapid-fire system design practice app in a local monorepo:

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
DATABASE_URL=jdbc:postgresql://localhost:5432/rapidsd
DATABASE_USERNAME=rapidsd
DATABASE_PASSWORD=rapidsd
JWT_SECRET=replace-with-a-long-random-secret
GEMINI_API_KEY=your-key-from-aistudio
GEMINI_MODEL=gemini-flash-latest
FRONTEND_URL=http://localhost:3000
API_BASE_URL=http://localhost:8080
```

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

This starts PostgreSQL, the API on `:8080`, and the web app on `:3000`.

Stop containers:

```bash
npm run docker:down
```

## Verification

```bash
npm run build:web
npm run build:api
```

The API runs Flyway on startup and seeds the RapidSD question catalog.
