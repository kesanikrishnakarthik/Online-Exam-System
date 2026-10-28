pipeline {
    agent any

    environment {
        APP_NAME = 'online-exam'
        APP_VERSION = '1.0'
        CONTAINER_NAME = 'online-exam-container'
        SERVER_PORT = '8083'
    }

    stages {
        stage('Checkout from GitHub') {
            steps {
                echo 'Checking out source code from Git repository...'
                checkout scm
            }
        }

        stage('Environment Verification') {
            steps {
                echo 'Verifying Java, Maven, and Docker environments...'
                bat 'java -version'
                bat 'mvn -version'
                bat 'docker --version'
            }
        }

        stage('Run Automated Tests') {
            steps {
                echo 'Executing domain, attempt limit, and security unit tests...'
                bat 'mvn test'
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Build Application') {
            steps {
                echo 'Packaging Spring Boot executable jar with production assets...'
                bat 'mvn clean package -DskipTests'
            }
        }

        stage('Build Docker Image') {
            steps {
                echo "Building Docker image: ${APP_NAME}:${APP_VERSION}..."
                bat "docker build -t ${APP_NAME}:${APP_VERSION} ."
            }
        }

        stage('Deploy Application') {
            steps {
                echo "Deploying containerized application on port ${SERVER_PORT}..."
                bat """
                    docker rm -f ${CONTAINER_NAME} 2>NUL
                    docker run -d --name ${CONTAINER_NAME} -p ${SERVER_PORT}:8081 ${APP_NAME}:${APP_VERSION}
                """
            }
        }

        stage('Health Check & Verification') {
            steps {
                echo 'Verifying application deployment and container availability...'
                bat "docker ps --filter name=${CONTAINER_NAME}"
            }
        }
    }

    post {
        success {
            echo 'CI/CD Pipeline executed successfully! ExamSphere is running at http://localhost:8083'
        }
        failure {
            echo 'CI/CD Pipeline failed. Please check test results and build logs.'
        }
    }
}

