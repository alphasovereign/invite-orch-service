# Manual Jenkins UI Setup

This guide helps you learn Jenkins manually before converting the flow into pipeline-as-code.

## Goal

Use a local Jenkins instance in Docker, then create a manual job in the Jenkins UI that:

1. clones this backend repo
2. runs `./gradlew test`
3. builds the backend image from `Dockerfile`
4. later pushes the image to Artifactory

## Before You Start

You need:

- Docker available locally
- GitHub access to `invite-orch-service.git`
- Artifactory registry URL and credentials for the push step later

This backend repo already contains:

- Gradle wrapper
- tests
- Dockerfile

## Step 1: Start Jenkins Locally In Docker

Create a persistent Jenkins home volume:

```bash
docker volume create jenkins_home
```

Run Jenkins:

```bash
docker run --name jenkins \
  --restart unless-stopped \
  -p 8081:8080 \
  -p 50000:50000 \
  -v jenkins_home:/var/jenkins_home \
  -v /var/run/docker.sock:/var/run/docker.sock \
  jenkins/jenkins:lts-jdk17
```

Why this shape:

- `8081:8080` exposes Jenkins UI on `http://localhost:8081`
- `50000:50000` exposes the inbound agent port if you need it later
- `jenkins_home` persists Jenkins config and jobs
- `/var/run/docker.sock` allows Jenkins to talk to the host Docker daemon for image builds

## Step 2: Unlock Jenkins

Open:

`http://localhost:8081`

Get the initial admin password:

```bash
docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword
```

Paste that password into the Jenkins UI.

## Step 3: Install Plugins

Choose:

- `Install suggested plugins`

After the basic setup, make sure these plugins are present:

- Git
- Pipeline
- Credentials Binding
- Docker Pipeline
- Workspace Cleanup

For the first manual learning pass, you do not need to create a `Jenkinsfile` yet.

## Step 4: Create A Manual Job

Create a new item:

- name: `invite-orch-service-manual`
- type: `Freestyle project`

## Step 5: Configure Source Code

In the job configuration:

- enable `Git` under Source Code Management
- repository URL:
  - `https://github.com/alphasovereign/invite-orch-service.git`
- branch to build:
  - `*/main`

If the repo is private, add GitHub credentials in Jenkins first.

## Step 6: Configure The Build Step

Add a build step:

- `Execute shell`

Use:

```bash
cd "$WORKSPACE"
./gradlew test
```

This verifies the backend compiles and tests pass inside Jenkins.

## Step 7: Add Manual Image Build Step

Add another `Execute shell` step:

```bash
cd "$WORKSPACE"
docker build -t invite-orch-service:manual-test .
```

This uses the repo `Dockerfile` and produces a local image tag for learning.

## Step 8: Add Artifactory Push Later

Once build and test are working, add Jenkins credentials for Artifactory:

- username/password or token

Then add another shell step like:

```bash
cd "$WORKSPACE"
docker login your-artifactory.example.com -u "$ARTIFACTORY_USERNAME" -p "$ARTIFACTORY_PASSWORD"
docker tag invite-orch-service:manual-test your-artifactory.example.com/docker/invite-orch-service:manual-test
docker push your-artifactory.example.com/docker/invite-orch-service:manual-test
```

Do not hardcode secrets in the job. Store them in Jenkins Credentials and expose them through the build environment.

## Step 9: What Success Looks Like

You are ready to move to pipeline-as-code when:

- Jenkins can clone the repo
- `./gradlew test` passes in Jenkins
- Docker image build succeeds in Jenkins
- Artifactory login succeeds
- image push succeeds and the image appears in Artifactory

## Step 10: Then Convert To `Jenkinsfile`

After the manual job works, convert the exact same flow into a committed `Jenkinsfile`:

1. checkout
2. test
3. image build
4. registry login
5. image push

That gives you the learning benefit of manual setup first, then the maintainability of pipeline-as-code.
