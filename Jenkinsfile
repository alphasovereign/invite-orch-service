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

    environment {
        LOCAL_IMAGE = ''
        REMOTE_IMAGE = ''
        IMAGE_TAG = ''
        GIT_SHORT_SHA = ''
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
                    env.GIT_SHORT_SHA = sh(
                        script: 'git rev-parse --short=7 HEAD',
                        returnStdout: true
                    ).trim()
                    env.IMAGE_TAG = "build-${env.BUILD_NUMBER}-${env.GIT_SHORT_SHA}"
                    env.LOCAL_IMAGE = "${params.IMAGE_NAME}:${env.IMAGE_TAG}"
                    env.REMOTE_IMAGE = "${params.REGISTRY_URL}/${params.IMAGE_NAME}:${env.IMAGE_TAG}"
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
                        credentialsId: params.REGISTRY_CREDENTIALS_ID,
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
