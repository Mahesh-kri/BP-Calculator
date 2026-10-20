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

## CI: publish to Docker Hub

`.github/workflows/build-and-push.yml` runs tests, builds the image, and pushes it to Docker Hub on every push to `master`/`main` and on `v*` tags.

Set these repository secrets (Settings -> Secrets and variables -> Actions):

| Secret | Description |
|---|---|
| `DOCKERHUB_USERNAME` | Docker Hub username |
| `DOCKERHUB_TOKEN` | Docker Hub access token (not your password) |

Image tags produced: `latest` (default branch), branch name, `sha-<commit>`, and `v1.2.3` / `1.2` for git tags. Pin deploys to an immutable tag (`sha-...` or `vX.Y.Z`) rather than `latest`.

## Deploy to Azure (App Service - Web App for Containers)

The app is stateless (no database; theme persists in browser `localStorage`), so any number of instances/slots can run without sticky sessions.

1. Build and push the image (via CI above) — e.g. `youruser/bpcalculator:v1.0.0`.
2. In Azure, create the Web App for Containers and point it at that image and port `5000`.
3. Add the Docker Hub credentials as the app's container registry settings so it can pull from the registry.
4. Set the health check path to `/actuator/health`.
5. Runtime config is env-driven: the app listens on `SERVER_PORT` (default `5000`). App Service sets `WEBSITES_PORT`.

Azure always routes to the container's port 80/8080 by convention if you don't set it, so explicitly set the app setting:

```sh
az webapp config appsettings set \
  --resource-group <rg> --name <app> \
  --settings WEBSITES_PORT=5000 SERVER_PORT=5000
```

### Blue-green with deployment slots

Web App for Containers provides **deployment slots** (e.g. `staging`). Create a staging slot, deploy the new image there, warm it up, then swap into production — traffic switches with no downtime, and swapping back rolls back.

```sh
# one-time: create a staging slot
az webapp deployment slot create \
  --resource-group <rg> --name <app> --slot staging \
  --configuration-source <app>

# deploy the new image to staging
az webapp config container set \
  --resource-group <rg> --name <app> --slot staging \
  --docker-custom-image-name youruser/bpcalculator:v1.0.1 \
  --docker-registry-server-url https://index.docker.io

# warm up + verify, then swap staging -> production
az webapp deployment slot swap \
  --resource-group <rg> --name <app> --slot staging --target-slot production
```

Keep image tags immutable and configuration in env vars (which are "sticky"/slot-specific as needed) so the same image can serve either color.


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
