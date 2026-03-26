# Backend

Minimal Spring Boot backend for the invite app.

## Stack

- Java 17
- Gradle
- Spring Boot
- Spring Data JPA
- H2

## Run

1. Make sure Java 17+ is available.
2. From the `backend` directory, run:

```bash
./gradlew bootRun --args='--spring.profiles.active=dev'
```

The API starts on `http://localhost:8080`.

## Endpoints

- `POST /api/events`
- `GET /api/events/owner/{ownerId}`

## H2 Console

Available at `http://localhost:8080/h2-console`

Use these defaults:

- JDBC URL: `jdbc:h2:mem:invite-db`
- Username: `sa`
- Password: `password`

## Test

Run the test suite with:

```bash
./gradlew test
```

## Docker

This machine has Apple `container` CLI installed, so Docker is not required for local image builds.

### Apple Container CLI

Build the application image:

```bash
container build -t invite-orch-service:local .
```

Run the container:

```bash
container run --rm -p 8080:8080 invite-orch-service:local
```

### Docker Alternative

If you are using another environment with Docker available, you can build the same image with:

```bash
docker build -t invite-orch-service:local .
```

Run the container:

```bash
docker run --rm -p 8080:8080 -e SPRING_PROFILES_ACTIVE=dev invite-orch-service:local
```

## Production Container Notes

The container image is hardened for a minimal production baseline:

- multi-stage build so the runtime image does not include Gradle or source code
- non-root runtime user
- runtime jar copied via wildcard, so the image is not tied to a hardcoded jar filename
- conservative JVM container options via `JAVA_TOOL_OPTIONS`

Override runtime settings as needed:

```bash
container run --rm -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=dev \
  -e JAVA_TOOL_OPTIONS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError" \
  invite-orch-service:local
```

## Jenkins Manual Learning Path

Before adding a `Jenkinsfile`, follow the manual Jenkins UI guide in:

`docs/manual-jenkins-ui-setup.md`

That guide covers:

- running Jenkins locally in Docker
- unlocking Jenkins and installing plugins
- creating a manual backend job
- wiring GitHub checkout
- running `./gradlew test`
- building the backend image
- preparing for Artifactory push
