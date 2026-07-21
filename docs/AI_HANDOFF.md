# AI Handoff — RapidSD

Give this document to any AI agent before making changes. It describes the project structure, conventions, API contract, and current refactor status so work can start without re-exploring the codebase.

## Project purpose

RapidSD is a rapid-fire system design practice app. Users browse questions by topic, run spaced-repetition practice sessions, get AI grading, and track progress.

## Quick start commands

```bash
# Local dev (Postgres must be running)
cp .env.example .env
npm install
npm run dev:api    # Spring Boot on :8080
npm run dev:web    # Next.js on :3000

# Docker (full stack)
npm run docker:up

# Build verification
npm run build:web
npm run build:api
```

## Directory map

```
apps/
  api/src/main/java/dev/designdeck/api/
    RapidSdApiApplication.java    # Entry point, PasswordEncoder + ObjectMapper beans
    controller/                     # One controller per REST resource
    service/                        # Service interfaces
    service/impl/                   # Service implementations (@Service)
    repository/                     # JpaRepository interfaces only
    entity/                         # JPA entities + enums + composite key classes
    dto/
      auth/                         # SignupRequest, LoginRequest, AuthResponse, ...
      profile/                      # ProfileDto, UpdateProfileRequest
      catalog/                      # CategoryDto, QuestionDto, AnswerKeyDto, ...
      practice/                     # SessionRequest, AttemptRequest, ProgressSummary, ...
      grading/                      # GradeRequest, GradeDto
    mapper/                         # AuthMapper, CatalogMapper, PracticeMapper
    security/                       # SecurityConfig, JwtFilter, JwtService
    exception/                      # ApiException, ApiErrors (@RestControllerAdvice)
  api/src/main/resources/
    application.yml
    db/migration/V1__initial_schema_and_seed.sql
  web/src/
    app/                            # Next.js App Router pages + BFF proxy
    lib/api.ts                      # Client-side API client (all endpoints)
    lib/server-api.ts               # Server-side proxy + cookie auth
    lib/types.ts                    # Frontend DTO types
docs/
  ARCHITECTURE.md                   # Human-readable system design
  AI_HANDOFF.md                     # This file
docker-compose.yml                  # db + api + web
docker-compose.dev.yml              # db only
.env.example                        # Environment variable template
```

## Layer conventions

### Controllers
- Thin: validate input, call service, return DTO.
- Use `SecurityUtils.currentUserId()` for authenticated endpoints.
- One controller per resource domain.

### Services
- Interface in `service/`, implementation in `service/impl/`.
- Controllers inject interfaces only.
- Business logic (SRS algorithm, streak updates) lives in services, not repositories.

### Repositories
- Extend `JpaRepository<Entity, Id>`.
- Custom queries via `@Query` (JPQL or native when needed).
- No `repository/impl/` package — no JDBC.

### Entities vs DTOs
- Entities map 1:1 to Flyway tables.
- DTOs are Java records used at API boundaries.
- Mappers convert entity → DTO; never expose entities from controllers.

### Flyway
- Never edit `V1__*` after it has been applied in any environment.
- Add new migrations as `V2__description.sql`.

## API contract (stable — do not break without updating frontend)

Base URL: `/api` (proxied by Next.js BFF)

| Method | Path | Auth | Request body | Response |
|--------|------|------|--------------|----------|
| POST | `/auth/signup` | Public | `{ email, password, displayName? }` | `{ accessToken, refreshToken, profile }` |
| POST | `/auth/login` | Public | `{ email, password }` | `{ accessToken, refreshToken, profile }` |
| POST | `/auth/logout` | Public | — | `{ ok: true }` |
| POST | `/auth/forgot-password` | Public | `{ email }` | `{ ok: true }` |
| POST | `/auth/reset-password` | Public | `{ token, password }` | `{ ok: true }` |
| GET | `/me` | JWT | — | `ProfileDto` |
| PATCH | `/me` | JWT | `{ displayName, dailyGoal }` | `ProfileDto` |
| GET | `/categories` | Public | — | `CategoryDto[]` |
| GET | `/questions?topic=&q=` | Public | — | `QuestionDto[]` |
| GET | `/questions/{id}` | Public | — | `QuestionDto` |
| POST | `/sessions` | JWT | `{ size }` | `{ questions: QuestionDto[] }` |
| POST | `/attempts` | JWT | `{ questionId, selfRating?, userAnswer?, aiScore?, aiFeedback? }` | `{ ok: true }` |
| GET | `/progress/summary` | JWT | — | `ProgressSummary` |
| POST | `/grade` | JWT | `{ questionId, userAnswer }` | `GradeDto` |

### DTO field reference

**ProfileDto:** `{ email, displayName, dailyGoal, streak }`

**CategoryDto:** `{ id, name, slug, color, sortOrder }`

**QuestionDto:** `{ id, prompt, qtype, difficulty, companies[], sources[], category?, answerKey? }`

**ProgressSummary:** `{ totalAnswered, accuracy, streak, dailyGoal, todayCount, dueCount, weakest[], strongest[] }`

**GradeDto:** `{ score, missing[], wrong[], improvements[], summary }`

## Environment variables

| Variable | Used by | Default |
|----------|---------|---------|
| `DATABASE_URL` | API | `jdbc:postgresql://localhost:5432/rapidsd` |
| `DATABASE_USERNAME` | API | `rapidsd` |
| `DATABASE_PASSWORD` | API | `rapidsd` |
| `JWT_SECRET` | API | dev placeholder in application.yml |
| `OPENAI_API_KEY` | API | empty |
| `OPENAI_MODEL` | API | `gpt-4.1-mini` |
| `FRONTEND_URL` | API | `http://localhost:3000` |
| `API_BASE_URL` | Web (server proxy) | `http://localhost:8080` |

In Docker Compose, web uses `API_BASE_URL=http://api:8080`.

## Security

- Stateless JWT auth via `JwtFilter`.
- Public paths: `/api/auth/**`, `/api/categories`, `/api/questions/**`.
- All other `/api/*` requires valid JWT in `Authorization: Bearer` header.
- Next.js BFF stores tokens in httpOnly cookies (`rsd_access`, `rsd_refresh`).

## Frontend integration points

When changing the API, update these files:

1. `apps/web/src/lib/api.ts` — paths and methods
2. `apps/web/src/lib/types.ts` — TypeScript types
3. `apps/web/src/lib/server-api.ts` — proxy, cookie handling, token field names
4. `apps/web/src/middleware.ts` — route guards (if auth model changes)

## Refactor status checklist

- [x] JPA entities for all 9 tables
- [x] JpaRepository replaces JDBC repository/impl
- [x] DTOs split by domain package
- [x] Service interfaces + impl/
- [x] Controllers split by resource
- [x] JDBC removed (JdbcClient, JdbcTemplate)
- [x] ApiErrors uses @RestControllerAdvice
- [x] Docker Compose (db + api + web)
- [x] Frontend types aligned with API contract
- [ ] Integration tests (`@DataJpaTest`, `@WebMvcTest`)
- [ ] Favorites feature (table exists, no API yet)
- [ ] Logout token blocklist (currently client-side only)
- [ ] Email service for password reset (currently stdout)
- [ ] OpenAPI/Swagger documentation

## Known gaps / backlog

1. **Favorites** — `favorites` table has entity + repository but no controller/service/endpoints.
2. **Password reset** — reset link printed to stdout; needs email integration.
3. **Logout** — no server-side token invalidation.
4. **Tests** — no automated tests yet; use manual smoke checklist below.

## Manual smoke test checklist

After backend or frontend changes:

1. Signup → lands on `/home`
2. Browse categories and search questions
3. Start session → submit attempt → AI grade (requires `OPENAI_API_KEY`)
4. Progress dashboard loads stats
5. Settings: update profile, logout works

## Development guidelines for AI agents

1. Read this file and `docs/ARCHITECTURE.md` first.
2. Match existing naming and package structure.
3. Keep API contract stable unless explicitly asked to version it.
4. Do not edit applied Flyway migrations.
5. Do not reintroduce JDBC — use JPA repositories.
6. One class per concern; no monolithic DTO or controller files.
7. Run `mvn -f apps/api/pom.xml test` and `npm run build:web` before finishing.

## Entity ↔ table mapping

| Entity | Table | PK |
|--------|-------|-----|
| AppUser | app_users | id (UUID) |
| Profile | profiles | user_id (UUID) |
| PasswordResetToken | password_reset_tokens | token (String) |
| Category | categories | id (UUID) |
| Question | questions | id (UUID) |
| AnswerKey | answer_keys | question_id (UUID) |
| UserCardState | user_card_state | (user_id, question_id) |
| Attempt | attempts | id (UUID) |
| Favorite | favorites | (user_id, question_id) |

## Controller map

| Controller | Base path |
|------------|-----------|
| AuthController | `/api/auth` |
| ProfileController | `/api/me` |
| CategoryController | `/api/categories` |
| QuestionController | `/api/questions` |
| SessionController | `/api/sessions` |
| AttemptController | `/api/attempts` |
| ProgressController | `/api/progress` |
| GradeController | `/api/grade` |
