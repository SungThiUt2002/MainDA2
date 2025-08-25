pipeline {
    agent any
    
    environment {
        GIT_COMMIT_SHORT = sh(script: "git rev-parse --short HEAD", returnStdout: true).trim()
        // Use system Java instead of Jenkins tool
        JAVA_HOME = sh(script: "readlink -f /usr/bin/java | sed 's:/bin/java::'", returnStdout: true).trim()
    }
    
    stages {
        stage('Verify Environment') {
            steps {
                echo 'Verifying system environment...'
                sh '''
                    echo "=== System Java ==="
                    which java
                    java -version
                    echo "JAVA_HOME: $JAVA_HOME"
                    
                    echo "=== Maven Version ==="
                    mvn -version
                    
                    echo "=== Available Tools ==="
                    which mvn
                    which docker || echo "Docker not in PATH"
                '''
            }
        }
        
        stage('Checkout') {
            steps {
                echo 'Checking out source code...'
                checkout scm
                
                sh '''
                    echo "=== Repository Structure ==="
                    ls -la
                    echo "=== Maven Projects Found ==="
                    find . -maxdepth 2 -name "pom.xml" -exec dirname {} \\; | sort
                '''
            }
        }
        
        stage('Build & Test Microservices') {
            parallel {
                stage('Account Service') {
                    steps {
                        script {
                            if (fileExists('Account-Service')) {
                                dir('Account-Service') {
                                    echo 'Building Account Service...'
                                    sh 'mvn clean compile test -DskipTests=false'
                                }
                            } else if (fileExists('account-service')) {
                                dir('account-service') {
                                    echo 'Building Account Service...'
                                    sh 'mvn clean compile test -DskipTests=false'
                                }
                            } else {
                                echo 'Account Service directory not found, skipping...'
                            }
                        }
                    }
                    post {
                        always {
                            script {
                                try {
                                    if (fileExists('Account-Service/target/surefire-reports/*.xml')) {
                                        junit testResults: 'Account-Service/target/surefire-reports/*.xml', allowEmptyResults: true
                                    } else if (fileExists('account-service/target/surefire-reports/*.xml')) {
                                        junit testResults: 'account-service/target/surefire-reports/*.xml', allowEmptyResults: true
                                    }
                                } catch (Exception e) {
                                    echo "No test results for Account Service: ${e.getMessage()}"
                                }
                            }
                        }
                    }
                }
                
                stage('Cart Service') {
                    steps {
                        script {
                            if (fileExists('Cart-Service')) {
                                dir('Cart-Service') {
                                    echo 'Building Cart Service...'
                                    sh 'mvn clean compile test -DskipTests=false'
                                }
                            } else if (fileExists('cart-service')) {
                                dir('cart-service') {
                                    echo 'Building Cart Service...'
                                    sh 'mvn clean compile test -DskipTests=false'
                                }
                            } else {
                                echo 'Cart Service directory not found, skipping...'
                            }
                        }
                    }
                    post {
                        always {
                            script {
                                try {
                                    if (fileExists('Cart-Service/target/surefire-reports/*.xml')) {
                                        junit testResults: 'Cart-Service/target/surefire-reports/*.xml', allowEmptyResults: true
                                    } else if (fileExists('cart-service/target/surefire-reports/*.xml')) {
                                        junit testResults: 'cart-service/target/surefire-reports/*.xml', allowEmptyResults: true
                                    }
                                } catch (Exception e) {
                                    echo "No test results for Cart Service: ${e.getMessage()}"
                                }
                            }
                        }
                    }
                }
                
                stage('Config Server') {
                    steps {
                        script {
                            if (fileExists('Config-Server')) {
                                dir('Config-Server') {
                                    echo 'Building Config Server...'
                                    sh 'mvn clean compile test -DskipTests=false'
                                }
                            } else if (fileExists('config-server')) {
                                dir('config-server') {
                                    echo 'Building Config Server...'
                                    sh 'mvn clean compile test -DskipTests=false'
                                }
                            } else {
                                echo 'Config Server directory not found, skipping...'
                            }
                        }
                    }
                }
                
                stage('Discovery Service') {
                    steps {
                        script {
                            if (fileExists('Discovery-Service')) {
                                dir('Discovery-Service') {
                                    echo 'Building Discovery Service...'
                                    sh 'mvn clean compile test -DskipTests=false'
                                }
                            } else if (fileExists('discoveryservice')) {
                                dir('discoveryservice') {
                                    echo 'Building Discovery Service...'
                                    sh 'mvn clean compile test -DskipTests=false'
                                }
                            } else {
                                echo 'Discovery Service directory not found, skipping...'
                            }
                        }
                    }
                }
                
                stage('Inventory Service') {
                    steps {
                        script {
                            if (fileExists('Inventory-Service')) {
                                dir('Inventory-Service') {
                                    echo 'Building Inventory Service...'
                                    sh 'mvn clean compile test -DskipTests=false'
                                }
                            } else if (fileExists('inventory-service')) {
                                dir('inventory-service') {
                                    echo 'Building Inventory Service...'
                                    sh 'mvn clean compile test -DskipTests=false'
                                }
                            } else {
                                echo 'Inventory Service directory not found, skipping...'
                            }
                        }
                    }
                }
                
                stage('Order Service') {
                    steps {
                        script {
                            if (fileExists('Order-Service')) {
                                dir('Order-Service') {
                                    echo 'Building Order Service...'
                                    sh 'mvn clean compile test -DskipTests=false'
                                }
                            } else if (fileExists('order-service')) {
                                dir('order-service') {
                                    echo 'Building Order Service...'
                                    sh 'mvn clean compile test -DskipTests=false'
                                }
                            } else {
                                echo 'Order Service directory not found, skipping...'
                            }
                        }
                    }
                }
                
                stage('Product Service') {
                    steps {
                        script {
                            if (fileExists('Product-Service')) {
                                dir('Product-Service') {
                                    echo 'Building Product Service...'
                                    sh 'mvn clean compile test -DskipTests=false'
                                }
                            } else if (fileExists('product-service')) {
                                dir('product-service') {
                                    echo 'Building Product Service...'
                                    sh 'mvn clean compile test -DskipTests=false'
                                }
                            } else {
                                echo 'Product Service directory not found, skipping...'
                            }
                        }
                    }
                }
                
                stage('Shop Service') {
                    steps {
                        script {
                            if (fileExists('Shop-Service')) {
                                dir('Shop-Service') {
                                    echo 'Building Shop Service...'
                                    sh 'mvn clean compile test -DskipTests=false'
                                }
                            } else if (fileExists('shop')) {
                                dir('shop') {
                                    echo 'Building Shop Service...'
                                    sh 'mvn clean compile test -DskipTests=false'
                                }
                            } else {
                                echo 'Shop Service directory not found, skipping...'
                            }
                        }
                    }
                }
            }
        }
        
        stage('Package Applications') {
            steps {
                script {
                    def pomDirs = sh(script: "find . -maxdepth 2 -name 'pom.xml' -exec dirname {} \\;", returnStdout: true).trim().split('\n')
                    
                    pomDirs.each { dir ->
                        if (dir && dir != '.') {
                            dir(dir) {
                                echo "Packaging ${dir}..."
                                sh 'mvn package -DskipTests=true'
                            }
                        }
                    }
                }
            }
        }
        
        stage('Build Docker Images') {
            steps {
                script {
                    def pomDirs = sh(script: "find . -maxdepth 2 -name 'pom.xml' -exec dirname {} \\;", returnStdout: true).trim().split('\n')
                    
                    pomDirs.each { dir ->
                        if (dir && dir != '.' && fileExists("${dir}/Dockerfile")) {
                            dir(dir) {
                                echo "Building Docker image for ${dir}..."
                                sh """
                                    docker build -t ${dir.toLowerCase().replaceAll('[^a-z0-9-]', '-')}:${GIT_COMMIT_SHORT} .
                                    docker tag ${dir.toLowerCase().replaceAll('[^a-z0-9-]', '-')}:${GIT_COMMIT_SHORT} ${dir.toLowerCase().replaceAll('[^a-z0-9-]', '-')}:latest
                                """
                            }
                        } else {
                            echo "Skipping ${dir} - no Dockerfile found"
                        }
                    }
                }
            }
        }
    }
    
    post {
        always {
            echo 'Cleaning up...'
            script {
                try {
                    archiveArtifacts artifacts: '**/target/*.jar', fingerprint: true, allowEmptyArchive: true
                } catch (Exception e) {
                    echo "No JAR files to archive"
                }
                
                try {
                    junit testResults: '**/target/surefire-reports/*.xml', allowEmptyResults: true
                } catch (Exception e) {
                    echo "No test results to publish"
                }
            }
        }
        success {
            echo '🎉 Pipeline completed successfully!'
        }
        failure {
            echo '❌ Pipeline failed!'
        }
    }
}
