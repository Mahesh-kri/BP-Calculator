# bp
Blood Pressure Calculator
Spring Boot (Java 17) + Thymeleaf

## Build

```sh
mvn clean package
```

## Run

```sh
mvn spring-boot:run
```

The application starts on http://localhost:5000

## Test

```sh
mvn test
```

## Docker

Build the image:

```sh
docker build -t bpcalculator .
```

Run the container:

```sh
docker run --rm -p 5000:5000 bpcalculator
```

Or with Compose:

```sh
docker compose up --build
```

The app is then available at http://localhost:5000

## CI/CD pipeline

`.github/workflows/build-and-push.yml` runs on every push to `master`/`main` and on `v*` tags:

1. **test** – unit + integration + BDD tests, JaCoCo coverage gate, SpotBugs, SonarCloud quality gate.
2. **security** – OWASP dependency-check (CVSS >= 9), SARIF uploaded to GitHub code scanning. Runs after `test` (reuses the Maven cache), is **advisory/non-blocking**, and is capped at 30 minutes so a slow NVD sync never holds up a release. The blocking CVE gate is Trivy in `build-and-push`.
3. **build-and-push** – builds and pushes the image to Docker Hub, then scans it with Trivy (fails on HIGH/CRITICAL) and uploads SARIF.
4. **deploy** – only on `main`, gated by the `production` GitHub Environment, deploys the new image and smoke tests `/actuator/health`.

Set these repository/environment secrets (Settings -> Secrets and variables -> Actions):

| Secret | Description |
|---|---|
| `DOCKERHUB_USERNAME` | Docker Hub username |
| `DOCKERHUB_TOKEN` | Docker Hub access token (not your password) |
| `SONAR_TOKEN` | SonarCloud token with "Execute Analysis" permission |
| `SONAR_ORGANIZATION` | SonarCloud organization key |
| `SONAR_PROJECT_KEY` | SonarCloud project key |
| `NVD_API_KEY` | NVD API key for dependency-check (recommended; without it the NVD sync is slow) |
| `AZURE_WEBAPP_NAME` | Web App name (e.g. `bpcalculator`) |
| `AZURE_WEBAPP_URL` | Base URL, e.g. `https://bpcalculator-ggbkhjemapd4d0hd.austriaeast-01.azurewebsites.net` |
| `AZURE_WEBAPP_PUBLISH_PROFILE` | Publish profile XML (production slot, or the staging slot when using slots) |

Image tags produced: `latest` (default branch), branch name, `sha-<commit>`, and `v1.2.3` / `1.2` for git tags. Pin deploys to an immutable tag (`sha-...` or `vX.Y.Z`) rather than `latest`.

## Static analysis: SonarQube / SonarCloud

The pipeline runs a SonarCloud analysis after tests and **fails the build if the quality gate fails** (`sonar.qualitygate.wait=true`). Coverage is fed from the JaCoCo XML report (`target/site/jacoco/jacoco.xml`).

One-time setup:

1. Sign in to <https://sonarcloud.io> with GitHub and **import** the `BP-Calculator` repository.
2. Copy the **organization key** and **project key** shown by SonarCloud (Project -> Information).
3. Generate a token: **My Account -> Security -> Generate Tokens** (type "Project Analysis").
4. Add `SONAR_TOKEN`, `SONAR_ORGANIZATION`, and `SONAR_PROJECT_KEY` as GitHub secrets.

Run the same analysis locally (requires a token):

```sh
mvn -B clean verify
mvn -B org.sonarsource.scanner.maven:sonar-maven-plugin:sonar \
  -Dsonar.host.url=https://sonarcloud.io \
  -Dsonar.organization=<org> \
  -Dsonar.projectKey=<project> \
  -Dsonar.token=<token>
```

To use a **self-hosted SonarQube** instead, point `sonar.host.url` at your server (e.g. `http://localhost:9000`) and use a token generated there; no SonarCloud organization/project-key secrets are needed.

## Security scanning

**Dependencies (OWASP dependency-check)** — runs in the `security` job under the `security` Maven profile and flags CVSS >= 9. Reports are written to `target/dependency-check/` (HTML, JSON, SARIF) and the SARIF is uploaded to GitHub code scanning. This job is advisory (`continue-on-error`), capped at 30 minutes, and is not a dependency of the build/deploy, so NVD sync time cannot block a release. The first run downloads the NVD database (a few minutes with `NVD_API_KEY`); the `~/.m2/repository/org/owasp/dependency-check-data` cache makes later runs fast.

```sh
mvn -B -Psecurity org.owasp:dependency-check-maven:check -DnvdApiKey=<key>
```

**Container image (Trivy)** — after the image is pushed, Trivy scans it by digest and fails on `HIGH`/`CRITICAL` (fixed CVEs only). Results are uploaded as SARIF.

**Dependabot** — `.github/dependabot.yml` opens weekly PRs for Maven, GitHub Actions, and Docker base-image updates.

## Deploy to Azure Web App for Containers

The app is stateless (no database; theme persists in browser `localStorage`), so a staging slot can run alongside production without sticky sessions.

The deploy job uses **publish profile** authentication (no Entra ID / service principal needed). It deploys the immutable `sha-<commit>` image with `azure/webapps-deploy`, then polls `AZURE_WEBAPP_URL/actuator/health`.

### Free (F1) plan — direct deploy

Free/Basic tiers have no deployment slots, so the image is deployed straight to production. The app restarts on the new image. Set the `AZURE_WEBAPP_PUBLISH_PROFILE` secret to the **production** publish profile, and leave the `DEPLOY_SLOT` repository variable unset.

### Standard S1+ — slot-based blue-green

On Standard S1 or higher, use the `staging` slot + Auto swap:

1. Create the **staging** slot (clone settings from production).
2. Download the **staging slot's** publish profile into `AZURE_WEBAPP_PUBLISH_PROFILE`.
3. On the staging slot: **Configuration → General settings → Auto swap → On**, target **production**.
4. Add a repository **variable** `DEPLOY_SLOT=staging` (Settings → Secrets and variables → Actions → Variables).

Flow: `webapps-deploy` pushes the image to staging → App Service warms up and auto-swaps into production → the job smoke-tests production. No ARM/Entra call is made by CI.

### One-time setup (Azure portal, no Entra ID)

1. Use an existing **Web App for Containers** (Linux) or create one. It can pull `youruser/bpcalculator:latest` from Docker Hub.
2. **Configuration → Application settings**: add `WEBSITES_PORT=5000` and `SERVER_PORT=5000`.
3. **Configuration → General settings → Health check path**: `/actuator/health`.
4. Download the publish profile from the target (app for F1, staging slot for blue-green) into the secret.
5. Add `AZURE_WEBAPP_NAME` and `AZURE_WEBAPP_URL` secrets (URL is the region-specific default domain, e.g. `https://bpcalculator-ggbkhjemapd4d0hd.austriaeast-01.azurewebsites.net`).

### Approval / authorization gate

The `deploy` job uses `environment: production`. In **Settings -> Environments -> production**, add **Required reviewers** (and optionally a wait timer or deployment branch rule limited to `main`). A deploy then pauses until a reviewer approves it, which is the authorization gate for production.

Keep image tags immutable (`sha-...`/`vX.Y.Z`) so each deployment is reproducible.


## Features

- Calculates the blood pressure category (Low / Ideal / Pre-High / High) from systolic and diastolic readings
- Server-side validation with user-friendly error messages
- Responsive UI with a colored result panel and reference ranges
- Dark mode (persisted in `localStorage`)
- Health guidance per category, with a "speak to a doctor" prompt and NHS links for high readings
- Health and info endpoints via Spring Boot Actuator

## Endpoints

| Endpoint | Description |
|---|---|
| `GET /` | Calculator page |
| `POST /` | Submit `systolic` and `diastolic` |
| `GET /privacy` | Privacy policy |
| `GET /actuator/health` | Health check |
| `GET /actuator/info` | Application info |
