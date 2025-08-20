pipeline {
    agent any
    
    stages {
        stage('Checkout') {
            steps {
                echo 'Checking out code from Gitea...'
            }
        }
        
        stage('Build Microservices') {
            parallel {
                stage('Account Service') {
                    steps {
                        dir('account-service') {
                            echo 'Building account-service...'
                        }
                    }
                }
                stage('Cart Service') {
                    steps {
                        dir('cart-service') {
                            echo 'Building cart-service...'
                        }
                    }
                }
                stage('Config Server') {
                    steps {
                        dir('config-server') {
                            echo 'Building config-server...'
                        }
                    }
                }
            }
        }
        
        stage('Test') {
            steps {
                echo 'Running tests...'
            }
        }
        
        stage('Deploy') {
            steps {
                echo 'Deploying microservices...'
            }
        }
    }
    
    post {
        success {
            echo 'Pipeline completed successfully!'
        }
        failure {
            echo 'Pipeline failed!'
        }
    }
}  
