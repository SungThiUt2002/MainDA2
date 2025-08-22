pipeline {
    agent any
    
    tools {
        maven 'Maven'
        jdk 'Java17'
    }
    
    environment {
        DOCKER_REGISTRY = 'your-registry'
        GIT_COMMIT_SHORT = sh(script: "git rev-parse --short HEAD", returnStdout: true).trim()
    }
    
    stages {
        stage('Checkout') {
            steps {
                echo 'Checking out source code...'
                checkout scm
            }
        }
        
        stage('Build & Test Microservices') {
            parallel {
                stage('Account Service') {
                    steps {
                        dir('account-service') {
                            echo 'Building Account Service...'
                            sh 'mvn clean compile test -DskipTests=false'
                        }
                    }
                    post {
                        always {
                            publishTestResults testResultsPattern: 'account-service/target/surefire-reports/*.xml'
                        }
                    }
                }
                
                stage('Cart Service') {
                    steps {
                        dir('cart-service') {
                            echo 'Building Cart Service...'
                            sh 'mvn clean compile test -DskipTests=false'
                        }
                    }
                    post {
                        always {
                            publishTestResults testResultsPattern: 'cart-service/target/surefire-reports/*.xml'
                        }
                    }
                }
                
                stage('Config Server') {
                    steps {
                        dir('config-server') {
                            echo 'Building Config Server...'
                            sh 'mvn clean compile test -DskipTests=false'
                        }
                    }
                }
                
                stage('Discovery Service') {
                    steps {
                        dir('discoveryservice') {
                            echo 'Building Discovery Service...'
                            sh 'mvn clean compile test -DskipTests=false'
                        }
                    }
                }
                
                stage('Inventory Service') {
                    steps {
                        dir('inventory-service') {
                            echo 'Building Inventory Service...'
                            sh 'mvn clean compile test -DskipTests=false'
                        }
                    }
                }
                
                stage('Order Service') {
                    steps {
                        dir('order-service') {
                            echo 'Building Order Service...'
                            sh 'mvn clean compile test -DskipTests=false'
                        }
                    }
                }
                
                stage('Product Service') {
                    steps {
                        dir('product-service') {
                            echo 'Building Product Service...'
                            sh 'mvn clean compile test -DskipTests=false'
                        }
                    }
                }
                
                stage('Shop Service') {
                    steps {
                        dir('shop') {
                            echo 'Building Shop Service...'
                            sh 'mvn clean compile test -DskipTests=false'
                        }
                    }
                }
            }
        }
        
        stage('Package Applications') {
            steps {
                script {
                    def services = ['account-service', 'cart-service', 'config-server', 
                                  'discoveryservice', 'inventory-service', 'order-service', 
                                  'product-service', 'shop']
                    
                    services.each { service ->
                        dir(service) {
                            sh 'mvn package -DskipTests=true'
                        }
                    }
                }
            }
        }
        
        stage('Build Docker Images') {
            steps {
                script {
                    def services = ['account-service', 'cart-service', 'config-server', 
                                  'discoveryservice', 'inventory-service', 'order-service', 
                                  'product-service', 'shop']
                    
                    services.each { service ->
                        dir(service) {
                            sh "docker build -t ${service}:${GIT_COMMIT_SHORT} ."
                            sh "docker tag ${service}:${GIT_COMMIT_SHORT} ${service}:latest"
                        }
                    }
                }
            }
        }
        
        stage('Deploy to Development') {
            steps {
                echo 'Stopping existing containers...'
                sh 'docker-compose down || true'
                
                echo 'Starting new containers...'
                sh 'docker-compose up -d'
                
                // Health checks
                script {
                    sleep(30) // Wait for services to start
                    
                    def healthChecks = [
                        'config-server': 8888,
                        'discoveryservice': 8761
                    ]
                    
                    healthChecks.each { service, port ->
                        echo "Health check for ${service}..."
                        timeout(time: 5, unit: 'MINUTES') {
                            waitUntil {
                                script {
                                    def result = sh(script: "curl -f http://localhost:${port}/actuator/health || exit 1", returnStatus: true)
                                    return result == 0
                                }
                            }
                        }
                        echo "${service} is healthy!"
                    }
                }
            }
        }
        
        stage('Integration Tests') {
            steps {
                echo 'Running integration tests...'
                script {
                    // Add your integration test commands here
                    sh '''
                        echo "Testing API endpoints..."
                        # curl tests, postman, etc.
                    '''
                }
            }
        }
    }
    
    post {
        always {
            echo 'Cleaning up...'
            // Clean workspace
            cleanWs()
            
            // Archive artifacts
            archiveArtifacts artifacts: '**/target/*.jar', fingerprint: true, allowEmptyArchive: true
            
            // Publish test reports
            publishTestResults testResultsPattern: '**/target/surefire-reports/*.xml'
        }
        success {
            echo '🎉 Pipeline completed successfully!'
            // Send success notifications
            // slackSend channel: '#devops', message: "✅ Microservices pipeline succeeded!"
        }
        failure {
            echo '❌ Pipeline failed!'
            // Send failure notifications
            // slackSend channel: '#devops', message: "❌ Microservices pipeline failed!"
        }
        unstable {
            echo '⚠️ Pipeline unstable (some tests failed)'
        }
    }
}
