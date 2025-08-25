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
                    def output = sh(
                        script: "find . -maxdepth 2 -name 'pom.xml' -exec dirname {} \\; | grep -v '^\\.$' | sort",
                        returnStdout: true
                    ).trim()
                    
                    if (!output) {
                        echo "No Maven projects found"
                        return
                    }
                    
                    def services = output.split('\n').findAll { it.trim() }
                    echo "Found services: ${services}"
                    
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
                    def output = sh(
                        script: "find . -maxdepth 2 -name 'pom.xml' -exec dirname {} \\; | grep -v '^\\.$'",
                        returnStdout: true
                    ).trim()
                    
                    if (!output) {
                        echo "No Maven projects to package"
                        return
                    }
                    
                    def services = output.split('\n').findAll { it.trim() }
                    services.each { service ->
                        dir(service) {
                            echo "Packaging ${service.replaceAll('./','')}"
                            sh 'mvn package -DskipTests=true'
                        }
                    }
                }
            }
        }
        
        stage('Build Images') {
            steps {
                script {
                    def output = sh(
                        script: "find . -maxdepth 2 -name 'Dockerfile' -exec dirname {} \\;",
                        returnStdout: true
                    ).trim()
                    
                    if (!output) {
                        echo "No Dockerfiles found, skipping image build"
                        return
                    }
                    
                    def services = output.split('\n').findAll { it.trim() }
                    services.each { service ->
                        def serviceName = service.replaceAll('./','').toLowerCase()
                        dir(service) {
                            echo "Building Docker image for ${serviceName}"
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
                    echo "Stopping existing containers..."
                    docker-compose down || true
                    echo "Starting new containers..."
                    docker-compose up -d
                    sleep 30
                    echo "Deployment completed!"
                '''
            }
        }
    }
    
    post {
        always {
            script {
                if (sh(script: "find . -name '*.xml' -path '*/surefire-reports/*'", returnStatus: true) == 0) {
                    publishTestResults testResultsPattern: '**/target/surefire-reports/*.xml'
                }
                if (sh(script: "find . -name '*.jar' -path '*/target/*'", returnStatus: true) == 0) {
                    archiveArtifacts artifacts: '**/target/*.jar', allowEmptyArchive: true
                }
            }
        }
        success { echo 'Pipeline succeeded!' }
        failure { echo 'Pipeline failed!' }
    }
}
