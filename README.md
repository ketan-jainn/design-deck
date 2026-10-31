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

Terraform in `terraform/vercel` manages the Vercel project; GitHub Actions builds and deploys it when
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
2. From the repository root, run `npm run tf:vercel -- init`, then
   `npm run tf:vercel -- plan -out=production.tfplan`. Review the plan and run
   `npm run tf:vercel -- apply production.tfplan` to create the project. The `tf:vercel`
   script loads `.env` before starting Terraform; already-set shell environment
   variables take precedence. `npm run tf -- ...` remains an alias for Vercel.
   A plan only previews changes; it does not create the project. Keep the
   Terraform state private and backed up. Do not commit state or saved plans.
3. Run `npm run tf:vercel -- output` and add these GitHub repository Actions secrets:
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

If upgrading an older checkout that stored Vercel state at
`terraform/terraform.tfstate`, back it up privately and move it to
`terraform/vercel/terraform.tfstate` before using the new commands. Do not
overwrite a state file already present in the destination. Initialize the new
directory and review a fresh plan. Saved plans from the old directory must not
be reused.

## Manage the existing Neon project with Terraform

The Neon configuration in `terraform/neon` has its own local state, separate
from Vercel. It uses the community-maintained `kislerdm/neon` provider and
imports an existing project; it does not migrate Docker data or run Flyway.
Neon supplies both direct and pooled connection endpoints for the same project.
The configuration sets the Neon Free history window to six hours (21,600
seconds); review the plan if the existing project's window is different.

1. In Neon, select the existing project and copy its project ID. Create a Neon
   **management API key** with access to the project; database credentials and
   object-storage keys are not Neon management API keys.
2. In the root `.env`, set `NEON_API_KEY` and `TF_VAR_neon_project_id` (see
   `.env.example`). Keep these values private. The `tf:neon` command loads this
   file; variables already set in the shell take precedence.
3. From the repository root, run `npm run tf:neon -- init`, then
   `npm run tf:neon -- plan -out=neon-import.tfplan`. Review the full plan:
   it should import the existing project, with no unexpected updates,
   replacements, or deletions. If it proposes unexpected changes, stop and
   investigate rather than applying.
4. When the plan is correct, run `npm run tf:neon -- apply neon-import.tfplan`
   to record the import. `npm run tf:neon -- plan` should then show no changes.
   Terraform blocks destruction of this managed project with `prevent_destroy`.
   An apply can import a resource into state before a later update fails. If an
   apply fails, check `npm run tf:neon -- state list`, correct the issue, and
   create a fresh plan; don't apply the old saved plan again.

The provider can write database credentials into Terraform state, even when
they are marked sensitive. Keep `terraform/neon/terraform.tfstate`, saved plan
files, and backups private and out of Git. Store a secure backup of the state;
for shared or automated use, configure an encrypted, access-controlled remote
backend with locking before collaborating. The provider lock file should be
committed. Continue configuring pooled `DATABASE_URL` and direct
`SPRING_FLYWAY_URL` on the API separately, and manage schema changes with Flyway.

## Deploy the backend to Render

For production Terraform commands, fill in the root `.env.prod` file and use
`npm run tf:vercel:prod -- plan`, `npm run tf:neon:prod -- plan`, or
`npm run tf:render:prod -- plan`. Run the corresponding command with `init`
first when needed. These production scripts load `.env.prod`; existing shell
environment variables take precedence. The other `tf:*` scripts load `.env`.

`terraform/render` manages a free Render web service separately from Neon and
Vercel. Render builds `apps/api/Dockerfile` from `main`; GitHub Actions triggers
later deployments through the Render API. Render automatic deployment is
disabled. The workflow runs when a push to `main` changes `apps/api/**` and
requests the pushed commit. It reports that Render accepted the request, not
that the build or deployment succeeded; check the Render dashboard for the
result. It does not wait for the separate Java CI workflow to pass.

1. Make the repository accessible to Render and push the Dockerfile and this
   configuration to `main`. Render supports public repository URLs without
   auto-deploy; its documentation recommends connecting your Git provider for
   repositories you own. A private repository needs Git credentials configured
   in Render. GitHub Actions still triggers the deployments in either case.
2. Create a Render API key and find your workspace owner ID in Render's
   settings. Put them in the root `.env` as `RENDER_API_KEY` and
   `RENDER_OWNER_ID`; do not commit them. See `.env.example` for the remaining
   `TF_VAR_render_*` values. Set the region near Neon. Use a JDBC URL with the
   Neon pooled hostname for the application and the direct hostname for Flyway,
   both with `sslmode=require`; provide the database username and password
   separately. Set the frontend origin and a random JWT secret of at least 32
   characters. Terraform creates a free Render Key Value instance in the same
   region and passes its internal connection URL to the API; no Redis URL is
   needed in `.env`. The local Docker Redis cannot be reached from Render.
   Set the BazaarLink key if
   that feature is needed. Set `TF_VAR_render_bazaarlink_model` to choose the
   grading model; it defaults to `qwen/qwen3.7-flash:free`. Variables already
   set in the shell take precedence.
3. Run `npm run tf:render -- init` and
   `npm run tf:render -- plan -out=render.tfplan`. Review the entire plan,
   especially the `free` plan and resource actions, then run
   `npm run tf:render -- apply render.tfplan`. Creation can trigger an initial
   deployment before GitHub Actions is configured. If the service already
   exists, import it instead of creating a duplicate.
4. In GitHub repository Settings > Secrets and variables > Actions, add
   `RENDER_API_KEY` containing a Render API key and `RENDER_SERVICE_ID`
   containing the service ID from `npm run tf:render -- output -raw backend_service_id`.
   The API key grants account access; keep it private. After these secrets are set,
   push a commit that changes `apps/api/` to `main` and check both GitHub
   Actions and Render deploy logs. A matching push before these secrets exist
   fails the workflow; push another API change after configuring them, or trigger
   a deployment from the Render dashboard.
5. Set `API_BASE_URL` in Vercel Production to the backend URL shown by
   `npm run tf:render -- output backend_url`, then redeploy the frontend.
   Spring Boot runs Flyway on startup against the direct database endpoint.

Terraform state and saved plans contain backend secrets even when values are
marked sensitive. Keep them out of Git, restrict access and back up state
securely; use encrypted remote state with locking for shared access. Commit the
provider lock file. Render's free service sleeps after inactivity and has usage
limits; review the current [free tier limits](https://render.com/docs/free).
Provider v1.9.1 has a reported [free-service update issue](https://github.com/render-oss/terraform-provider-render/issues/80)
involving maintenance mode. If an update fails, inspect the issue and state;
do not replace the service blindly. Configuration changes do not automatically
deploy with this setup: trigger a deployment after applying them.

The Render health check uses `/actuator/health/liveness`. It exposes only the
application's liveness status and does not check Neon or Key Value availability.
Other actuator paths, including metrics and detailed health, are blocked.
The Render Terraform inputs require TLS for both Neon JDBC URLs. Render Key
Value uses its internal URL on Render's same-region private network; this URL
normally uses `redis://` and does not provide TLS. External access is disabled.
`sslmode=require` encrypts the Neon connection; stronger certificate
verification requires the JDBC driver to be configured with an appropriate CA.

Free Render Key Value does not persist data and loses its contents on restart.
This includes the JWT logout blocklist: a previously logged-out token can become
valid again until it expires. The `noeviction` policy prevents memory pressure
from silently evicting blocklist entries, but writes fail when memory is full.
Only one free Key Value instance is allowed per workspace. If one already
exists, import it rather than creating a duplicate; review the plan carefully.
