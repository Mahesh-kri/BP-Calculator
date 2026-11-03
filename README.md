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
2. **security** – OWASP dependency-check (fails on CVSS >= 9), SARIF uploaded to GitHub code scanning.
3. **build-and-push** – builds and pushes the image to Docker Hub, then scans it with Trivy (fails on HIGH/CRITICAL) and uploads SARIF.
4. **deploy** – only on `main`, gated by the `production` GitHub Environment, deploys a zero-traffic revision, smoke tests it, then blue-green cuts traffic over.

Set these repository/environment secrets (Settings -> Secrets and variables -> Actions):

| Secret | Description |
|---|---|
| `DOCKERHUB_USERNAME` | Docker Hub username |
| `DOCKERHUB_TOKEN` | Docker Hub access token (not your password) |
| `SONAR_TOKEN` | SonarCloud token with "Execute Analysis" permission |
| `SONAR_ORGANIZATION` | SonarCloud organization key |
| `SONAR_PROJECT_KEY` | SonarCloud project key |
| `NVD_API_KEY` | NVD API key for dependency-check (recommended; without it the NVD sync is slow) |
| `AZURE_WEBAPP_NAME` | Web App name (e.g. `bpcalculator-abcd`) |
| `AZURE_WEBAPP_PUBLISH_PROFILE` | Publish profile XML of the **staging** deployment slot |

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

**Dependencies (OWASP dependency-check)** — runs in the `security` job under the `security` Maven profile and fails on CVSS >= 9. Reports are written to `target/dependency-check/` (HTML, JSON, SARIF) and the SARIF is uploaded to GitHub code scanning.

```sh
mvn -B -Psecurity org.owasp:dependency-check-maven:check -DnvdApiKey=<key>
```

**Container image (Trivy)** — after the image is pushed, Trivy scans it by digest and fails on `HIGH`/`CRITICAL` (fixed CVEs only). Results are uploaded as SARIF.

**Dependabot** — `.github/dependabot.yml` opens weekly PRs for Maven, GitHub Actions, and Docker base-image updates.

## Deploy to Azure Web App for Containers

The app is stateless (no database; theme persists in browser `localStorage`), so a staging slot can run alongside production without sticky sessions.

The deploy job uses **publish profile** authentication (no Entra ID / service principal needed) and App Service **Auto swap** for blue-green:

1. `azure/webapps-deploy` deploys the immutable `sha-<commit>` image to the **staging** slot using the slot's publish profile.
2. Auto swap (configured once on the staging slot) warms up staging and swaps it into production.
3. The job then polls `https://<app>.azurewebsites.net/actuator/health` until production is healthy.
4. If deployment fails, production is untouched; if the swap fails, App Service keeps the previous production slot.

### One-time setup (Azure portal, no Entra ID)

1. **Create a Web App for Containers** (Linux). Image: `youruser/bpcalculator`, tag `latest` (CI overwrites the tag per deploy). App Service pulls from Docker Hub.
2. **Configuration → General settings**: set **Health check path** to `/actuator/health`. Add application settings `WEBSITES_PORT=5000` and `SERVER_PORT=5000`.
3. **Deployment slots → Add slot**, name it `staging` (requires **Standard S1 or higher**; slots are not available on Free/Basic).
4. Open the **staging** slot → **Deployment Center → Manage publish profile → Download**. Paste the XML into the `AZURE_WEBAPP_PUBLISH_PROFILE` secret.
5. Open the **staging** slot → **Configuration → General settings → Auto swap** → target slot **production** → Save.
6. Add `AZURE_WEBAPP_NAME` (Settings → Secrets → Actions).

> Auto swap is configured on the staging slot, so every successful deployment to staging is swapped into production automatically. No ARM/Entra call is made by CI.

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
