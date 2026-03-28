pipeline {
    agent any

    options {
        timestamps()
        disableConcurrentBuilds()
    }

    parameters {
        string(
            name: 'REGISTRY_URL',
            defaultValue: 'localhost:5001',
            description: 'Container registry host to push the image to.'
        )
        string(
            name: 'IMAGE_NAME',
            defaultValue: 'invite-orch-service',
            description: 'Repository/image name to build and push.'
        )
        string(
            name: 'REGISTRY_CREDENTIALS_ID',
            defaultValue: 'local-registry-creds',
            description: 'Jenkins username/password credential used for registry login.'
        )
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Test') {
            steps {
                sh './gradlew test'
            }
        }

        stage('Prepare Metadata') {
            steps {
                script {
                    def defaultRegistryUrl = 'localhost:5001'
                    def defaultImageName = 'invite-orch-service'
                    def defaultCredentialsId = 'local-registry-creds'
                    def resolvedRegistryUrl = params.REGISTRY_URL?.trim() ? params.REGISTRY_URL.trim() : defaultRegistryUrl
                    def resolvedImageName = params.IMAGE_NAME?.trim() ? params.IMAGE_NAME.trim() : defaultImageName
                    def resolvedCredentialsId = params.REGISTRY_CREDENTIALS_ID?.trim() ? params.REGISTRY_CREDENTIALS_ID.trim() : defaultCredentialsId
                    def shortSha = sh(
                        script: 'git rev-parse --short=7 HEAD',
                        returnStdout: true
                    ).trim()
                    def imageTag = "build-${env.BUILD_NUMBER}-${shortSha}"
                    def localImage = "${resolvedImageName}:${imageTag}"
                    def remoteImage = "${resolvedRegistryUrl}/${resolvedImageName}:${imageTag}"

                    env.REGISTRY_URL = resolvedRegistryUrl
                    env.IMAGE_NAME = resolvedImageName
                    env.REGISTRY_CREDENTIALS_ID = resolvedCredentialsId
                    env.GIT_SHORT_SHA = shortSha
                    env.IMAGE_TAG = imageTag
                    env.LOCAL_IMAGE = localImage
                    env.REMOTE_IMAGE = remoteImage

                    echo "Resolved REGISTRY_URL=${env.REGISTRY_URL}"
                    echo "Resolved IMAGE_NAME=${env.IMAGE_NAME}"
                    echo "Resolved IMAGE_TAG=${env.IMAGE_TAG}"
                    echo "Resolved LOCAL_IMAGE=${env.LOCAL_IMAGE}"
                    echo "Resolved REMOTE_IMAGE=${env.REMOTE_IMAGE}"
                }
            }
        }

        stage('Build Image') {
            steps {
                sh 'docker build -t "${LOCAL_IMAGE}" .'
            }
        }

        stage('Login To Registry') {
            steps {
                withCredentials([
                    usernamePassword(
                        credentialsId: env.REGISTRY_CREDENTIALS_ID,
                        usernameVariable: 'REGISTRY_USERNAME',
                        passwordVariable: 'REGISTRY_PASSWORD'
                    )
                ]) {
                    sh '''
                        printf '%s' "$REGISTRY_PASSWORD" | docker login "$REGISTRY_URL" \
                          --username "$REGISTRY_USERNAME" \
                          --password-stdin
                    '''
                }
            }
        }

        stage('Tag And Push Image') {
            steps {
                sh '''
                    docker tag "$LOCAL_IMAGE" "$REMOTE_IMAGE"
                    docker push "$REMOTE_IMAGE"
                '''
            }
        }
    }

    post {
        always {
            sh 'docker logout "$REGISTRY_URL" || true'
        }
        success {
            echo "Pushed image: ${env.REMOTE_IMAGE}"
        }
    }
}
