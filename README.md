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

## Deploy the frontend to Vercel

Terraform creates the Vercel project; GitHub Actions builds and deploys it when
commits are pushed to `main`. This does not use Vercel's GitHub integration app
and does not deploy pull requests or other branches.

Use your existing **Hobby** team in Vercel's team switcher. Vercel uses the word
"team" for this free account context; no paid team or trial is needed. Check
that the selected team is on Hobby before applying: Terraform does not verify or
change the team's plan. This configuration does not enable paid Vercel add-ons.
Hobby is intended for personal, noncommercial projects, and usage is limited;
Vercel may pause service when the free allowance is exhausted.

1. Create a Vercel API token and locate the ID of your existing Hobby team in
   its General settings. Add `VERCEL_API_TOKEN` and `TF_VAR_vercel_team_id` to
   the root `.env` file using `.env.example` as a guide. Do not commit `.env`
   or put the token in a `.tf` file. Run `npm install` if needed.
2. From the repository root, run `npm run tf -- init`, then
   `npm run tf -- plan -out=production.tfplan`. Review the plan and run
   `npm run tf -- apply production.tfplan` to create the project. The `tf`
   script loads `.env` before starting Terraform; already-set shell environment
   variables take precedence. A plan only
   previews changes; it does not create the project. Keep the Terraform state
   private and backed up. Do not commit state or saved plans.
3. Run `npm run tf -- output` and add these GitHub repository Actions secrets:
   `VERCEL_PROJECT_ID` from `vercel_project_id`, `VERCEL_ORG_ID` from
   `vercel_team_id`, and `VERCEL_TOKEN` containing a Vercel API token with
   access to that team and project. The local `.env` is not uploaded to GitHub
   Actions. Keep the token out of Terraform state.
4. In the Vercel project's **Production** environment settings, set
   `API_BASE_URL` to the deployed backend's HTTPS base URL and
   `COOKIE_SECURE` to `true`. The frontend needs the backend to provide its API;
   deploying the frontend alone does not create the backend or database.
5. Commit the Terraform configuration, lock file, and deployment workflow,
   then push to `main`. The workflow checks out that commit, builds the npm
   workspace and deploys it to production. Check the Actions run and Vercel
   deployment logs for the result. Re-run the workflow by pushing a new commit
   to `main` if the first run occurred before secrets were configured.

Monitor Vercel usage in its dashboard. GitHub Actions usage is billed separately:
standard hosted runners are free for public repositories, while private
repositories have an allowance. If the repository is private, set a GitHub
Actions budget with "Stop usage when budget limit is reached" to prevent
overage charges. Backend services have their own plans and limits.

If a project with this name already exists in the selected Vercel team, import
it into Terraform state instead of attempting to create a duplicate. Check that
the existing project is not connected to Vercel's GitHub integration; disconnect
that integration if it would otherwise create deployments for other branches.
