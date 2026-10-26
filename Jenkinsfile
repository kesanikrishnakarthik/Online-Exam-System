pipeline {
    agent any

    stages {
        stage('Checkout from GitHub') {
            steps {
                checkout scm
            }
        }

        stage('Check Java') {
            steps {
                bat 'java -version'
            }
        }

        stage('Check Maven') {
            steps {
                bat 'mvn -version'
            }
        }

        stage('Build Application') {
            steps {
                bat 'mvn clean package -DskipTests'
            }
        }

        stage('Check Docker') {
            steps {
                bat 'docker --version'
            }
        }

        stage('Build Docker Image') {
            steps {
                bat 'docker build -t online-exam:1.0 .'
            }
        }

        stage('Deploy Application') {
            steps {
                bat '''
                    docker rm -f online-exam-container 2>NUL
                    docker run -d --name online-exam-container -p 8081:8081 online-exam:1.0
                '''
            }
        }
    }
}

