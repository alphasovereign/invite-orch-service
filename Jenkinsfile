pipeline {
    agent any

    options {
        timestamps()
        disableConcurrentBuilds()
    }

    parameters {
        choice(
            name: 'TARGET_REGISTRY',
            choices: ['local', 'ecr'],
            description: 'Choose whether to push to the local registry or AWS ECR.'
        )
        string(
            name: 'REGISTRY_URL',
            defaultValue: 'localhost:5001',
            description: 'Local container registry host to push the image to.'
        )
        string(
            name: 'IMAGE_NAME',
            defaultValue: 'invite-orch-service',
            description: 'Repository/image name to build and push.'
        )
        string(
            name: 'REGISTRY_CREDENTIALS_ID',
            defaultValue: 'local-registry-creds',
            description: 'Jenkins username/password credential used for local registry login.'
        )
        string(
            name: 'AWS_REGION',
            defaultValue: 'us-east-1',
            description: 'AWS region for ECR authentication.'
        )
        string(
            name: 'AWS_CREDENTIALS_ID',
            defaultValue: 'aws-ecr-creds',
            description: 'Jenkins username/password credential where username is AWS access key ID and password is AWS secret access key.'
        )
        string(
            name: 'ECR_REGISTRY',
            defaultValue: '586631184178.dkr.ecr.us-east-1.amazonaws.com',
            description: 'AWS ECR registry hostname.'
        )
        string(
            name: 'ECR_REPOSITORY',
            defaultValue: 'invite/artifactory',
            description: 'AWS ECR repository path.'
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
                    def defaultTargetRegistry = 'local'
                    def defaultRegistryUrl = 'localhost:5001'
                    def defaultImageName = 'invite-orch-service'
                    def defaultCredentialsId = 'local-registry-creds'
                    def defaultAwsRegion = 'us-east-1'
                    def defaultAwsCredentialsId = 'aws-ecr-creds'
                    def defaultEcrRegistry = '586631184178.dkr.ecr.us-east-1.amazonaws.com'
                    def defaultEcrRepository = 'invite/artifactory'
                    def resolvedTargetRegistry = params.TARGET_REGISTRY?.trim() ? params.TARGET_REGISTRY.trim() : defaultTargetRegistry
                    def resolvedRegistryUrl = params.REGISTRY_URL?.trim() ? params.REGISTRY_URL.trim() : defaultRegistryUrl
                    def resolvedImageName = params.IMAGE_NAME?.trim() ? params.IMAGE_NAME.trim() : defaultImageName
                    def resolvedCredentialsId = params.REGISTRY_CREDENTIALS_ID?.trim() ? params.REGISTRY_CREDENTIALS_ID.trim() : defaultCredentialsId
                    def resolvedAwsRegion = params.AWS_REGION?.trim() ? params.AWS_REGION.trim() : defaultAwsRegion
                    def resolvedAwsCredentialsId = params.AWS_CREDENTIALS_ID?.trim() ? params.AWS_CREDENTIALS_ID.trim() : defaultAwsCredentialsId
                    def resolvedEcrRegistry = params.ECR_REGISTRY?.trim() ? params.ECR_REGISTRY.trim() : defaultEcrRegistry
                    def resolvedEcrRepository = params.ECR_REPOSITORY?.trim() ? params.ECR_REPOSITORY.trim() : defaultEcrRepository
                    def shortSha = sh(
                        script: 'git rev-parse --short=7 HEAD',
                        returnStdout: true
                    ).trim()
                    def imageTag = "build-${env.BUILD_NUMBER}-${shortSha}"
                    def localImage = "${resolvedImageName}:${imageTag}"
                    def remoteImage
                    def loginRegistry
                    def authMode
                    def loginCredentialsId

                    if (resolvedTargetRegistry == 'ecr') {
                        remoteImage = "${resolvedEcrRegistry}/${resolvedEcrRepository}:${imageTag}"
                        loginRegistry = resolvedEcrRegistry
                        authMode = 'ecr'
                        loginCredentialsId = resolvedAwsCredentialsId
                    } else {
                        remoteImage = "${resolvedRegistryUrl}/${resolvedImageName}:${imageTag}"
                        loginRegistry = resolvedRegistryUrl
                        authMode = 'local'
                        loginCredentialsId = resolvedCredentialsId
                    }

                    env.TARGET_REGISTRY = resolvedTargetRegistry
                    env.REGISTRY_URL = resolvedRegistryUrl
                    env.IMAGE_NAME = resolvedImageName
                    env.REGISTRY_CREDENTIALS_ID = resolvedCredentialsId
                    env.AWS_REGION = resolvedAwsRegion
                    env.AWS_CREDENTIALS_ID = resolvedAwsCredentialsId
                    env.ECR_REGISTRY = resolvedEcrRegistry
                    env.ECR_REPOSITORY = resolvedEcrRepository
                    env.GIT_SHORT_SHA = shortSha
                    env.IMAGE_TAG = imageTag
                    env.LOCAL_IMAGE = localImage
                    env.REMOTE_IMAGE = remoteImage
                    env.LOGIN_REGISTRY = loginRegistry
                    env.AUTH_MODE = authMode
                    env.LOGIN_CREDENTIALS_ID = loginCredentialsId

                    echo "Resolved TARGET_REGISTRY=${env.TARGET_REGISTRY}"
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
                script {
                    if (env.AUTH_MODE == 'ecr') {
                        withCredentials([
                            usernamePassword(
                                credentialsId: env.AWS_CREDENTIALS_ID,
                                usernameVariable: 'AWS_ACCESS_KEY_ID',
                                passwordVariable: 'AWS_SECRET_ACCESS_KEY'
                            )
                        ]) {
                            sh '''
                                aws ecr get-login-password --region "$AWS_REGION" | docker login "$ECR_REGISTRY" \
                                  --username AWS \
                                  --password-stdin
                            '''
                        }
                    } else {
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
            sh '''
                if [ -n "$LOGIN_REGISTRY" ]; then
                  docker logout "$LOGIN_REGISTRY" || true
                fi
            '''
        }
        success {
            echo "Pushed image: ${env.REMOTE_IMAGE}"
        }
    }
}
