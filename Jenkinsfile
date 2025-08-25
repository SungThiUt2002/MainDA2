pipeline {
    agent any
    
    tools {
        maven 'Maven'
        jdk 'Java21'
    }
    
    environment {
        GIT_COMMIT_SHORT = sh(script: "git rev-parse --short HEAD", returnStdout: true).trim()
        DOCKER_REGISTRY = 'your-registry'
    }
    
    stages {
        stage('Verify Environment') {
            steps {
                echo 'Verifying build environment...'
                sh '''
                    echo "=== Java Environment ==="
                    java -version
                    echo "JAVA_HOME: $JAVA_HOME"
                    
                    echo "=== Maven Environment ==="
                    mvn -version
                    
                    echo "=== Project Structure ==="
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
                                    sh 'mvn clean compile test'
                                }
                            } else if (fileExists('account-service')) {
                                dir('account-service') {
                                    echo 'Building Account Service...'
                                    sh 'mvn clean compile test'
                                }
                            } else {
                                echo 'Account Service not found, skipping...'
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
                                    echo "No test results for Account Service"
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
                                    sh 'mvn clean compile test'
                                }
                            } else if (fileExists('cart-service')) {
                                dir('cart-service') {
                                    echo 'Building Cart Service...'
                                    sh 'mvn clean compile test'
                                }
                            } else {
                                echo 'Cart Service not found, skipping...'
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
                                    echo "No test results for Cart Service"
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
                                    sh 'mvn clean compile test'
                                }
                            } else if (fileExists('config-server')) {
                                dir('config-server') {
                                    echo 'Building Config Server...'
                                    sh 'mvn clean compile test'
                                }
                            } else {
                                echo 'Config Server not found, skipping...'
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
                                    sh 'mvn clean compile test'
                                }
                            } else if (fileExists('discoveryservice')) {
                                dir('discoveryservice') {
                                    echo 'Building Discovery Service...'
                                    sh 'mvn clean compile test'
                                }
                            } else {
                                echo 'Discovery Service not found, skipping...'
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
                                    sh 'mvn clean compile test'
                                }
                            } else if (fileExists('product-service')) {
                                dir('product-service') {
                                    echo 'Building Product Service...'
                                    sh 'mvn clean compile test'
                                }
                            } else {
                                echo 'Product Service not found, skipping...'
                            }
                        }
                    }
                }
                
                stage('Other Services') {
                    steps {
                        script {
                            def serviceNames = ['Inventory-Service', 'inventory-service', 'Order-Service', 'order-service', 'Shop-Service', 'shop']
                            
                            serviceNames.each { serviceName ->
                                if (fileExists(serviceName)) {
                                    dir(serviceName) {
                                        echo "Building ${serviceName}..."
                                        sh 'mvn clean compile test'
                                    }
                                }
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
                                def imageName = dir.toLowerCase().replaceAll('[^a-z0-9-]', '-')
                                sh """
                                    docker build -t ${imageName}:${GIT_COMMIT_SHORT} .
                                    docker tag ${imageName}:${GIT_COMMIT_SHORT} ${imageName}:latest
                                    echo "Built: ${imageName}:latest"
                                """
                            }
                        } else {
                            echo "Skipping ${dir} - no Dockerfile found"
                        }
                    }
                }
            }
        }
        
        stage('Deploy Services') {
            when {
                anyOf {
                    fileExists 'docker-compose.yml'
                    fileExists 'docker-compose.yaml'
                }
            }
            steps {
                echo 'Deploying microservices...'
                sh '''
                    echo "Stopping existing containers..."
                    docker-compose down || docker compose down || true
                    
                    echo "Starting new containers..."
                    docker-compose up -d || docker compose up -d
                    
                    echo "Waiting for services to start..."
                    sleep 30
                    
                    echo "Checking running containers:"
                    docker ps
                '''
            }
        }
    }
    
    post {
        always {
            echo 'Cleaning up workspace...'
            script {
                try {
                    archiveArtifacts artifacts: '**/target/*.jar', allowEmptyArchive: true, fingerprint: true
                } catch (Exception e) {
                    echo "No artifacts to archive"
                }
                
                try {
                    junit testResults: '**/target/surefire-reports/*.xml', allowEmptyResults: true
                } catch (Exception e) {
                    echo "No test results to publish"
                }
            }
        }
        success {
            echo 'Pipeline completed successfully!'
        }
        failure {
            echo 'Pipeline failed!'
            sh '''
                echo "=== Container Status ==="
                docker ps -a || echo "Docker not available"
                
                echo "=== Recent Logs ==="
                docker-compose logs --tail=20 || docker compose logs --tail=20 || echo "No compose logs"
            '''
        }
        cleanup {
            cleanWs()
        }
    }
}
