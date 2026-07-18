# RapidSD

RapidSD is a rapid-fire system design practice app detached into a local monorepo:

- `apps/web`: Next.js App Router frontend
- `apps/api`: Spring Boot REST API
- PostgreSQL with Flyway migrations
- Spring-owned JWT auth with BCrypt password hashing
- OpenAI-backed answer grading

## Prerequisites

- Node.js 22+
- npm 10+
- Java 21+
- Maven 3.9+
- PostgreSQL 15+

## Configuration

Create a PostgreSQL database and set API environment variables as needed:

```bash
DATABASE_URL=jdbc:postgresql://localhost:5432/rapidsd
DATABASE_USERNAME=rapidsd
DATABASE_PASSWORD=rapidsd
JWT_SECRET=replace-with-a-long-random-secret
OPENAI_API_KEY=sk-...
OPENAI_MODEL=gpt-4.1-mini
FRONTEND_URL=http://localhost:3000
```

The web app uses `API_BASE_URL` for server-side route handlers:

```bash
API_BASE_URL=http://localhost:8080
```

## Development

Install frontend dependencies:

```bash
npm install
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

## Verification

```bash
npm run build:web
npm run build:api
```

The API runs Flyway on startup and seeds the RapidSD question catalog.
