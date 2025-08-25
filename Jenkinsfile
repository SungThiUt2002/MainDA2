pipeline {
    agent any
    
    tools {
        maven 'Maven'
        jdk 'Java21'
    }
    
    environment {
        GIT_COMMIT_SHORT = sh(script: "git rev-parse --short HEAD", returnStdout: true).trim()
        JAVA_HOME = tool('Java21')
        PATH = "${env.JAVA_HOME}/bin:${env.PATH}"
    }
    
    stages {
        stage('Checkout') {
            steps {
                echo 'Checking out source code...'
                checkout scm
                sh 'java -version && mvn -version'
            }
        }
        
        stage('Build & Test') {
            steps {
                script {
                    // Find all directories with pom.xml
                    def services = sh(
                        script: "find . -maxdepth 2 -name 'pom.xml' -exec dirname {} \\; | grep -v '^\\.$' | sort",
                        returnStdout: true
                    ).trim().split('\n')
                    
                    echo "Found services: ${services}"
                    
                    // Build all services in parallel
                    def parallelStages = [:]
                    services.each { service ->
                        def serviceName = service.replaceAll('./','')
                        parallelStages[serviceName] = {
                            dir(service) {
                                echo "Building ${serviceName}..."
                                sh 'mvn clean compile test -DskipTests=false'
                            }
                        }
                    }
                    parallel parallelStages
                }
            }
        }
        
        stage('Package') {
            steps {
                script {
                    def services = sh(
                        script: "find . -maxdepth 2 -name 'pom.xml' -exec dirname {} \\; | grep -v '^\\.$'",
                        returnStdout: true
                    ).trim().split('\n')
                    
                    services.each { service ->
                        dir(service) {
                            sh 'mvn package -DskipTests=true'
                        }
                    }
                }
            }
        }
        
        stage('Build Images') {
            steps {
                script {
                    def services = sh(
                        script: "find . -maxdepth 2 -name 'Dockerfile' -exec dirname {} \\;",
                        returnStdout: true
                    ).trim().split('\n')
                    
                    services.each { service ->
                        def serviceName = service.replaceAll('./','').toLowerCase()
                        dir(service) {
                            sh """
                                docker build -t ${serviceName}:${GIT_COMMIT_SHORT} .
                                docker tag ${serviceName}:${GIT_COMMIT_SHORT} ${serviceName}:latest
                            """
                        }
                    }
                }
            }
        }
        
        stage('Deploy') {
            when { fileExists('docker-compose.yml') }
            steps {
                sh '''
                    docker-compose down || true
                    docker-compose up -d
                    sleep 30
                    echo "Deployment completed!"
                '''
            }
        }
    }
    
    post {
        always {
            publishTestResults testResultsPattern: '**/target/surefire-reports/*.xml'
            archiveArtifacts artifacts: '**/target/*.jar', allowEmptyArchive: true
        }
        success { echo '✅ Pipeline succeeded!' }
        failure { echo '❌ Pipeline failed!' }
    }
}
