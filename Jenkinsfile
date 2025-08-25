pipeline {
    agent any
    
    tools {
        maven 'Maven'
        jdk 'Java21'
    }
    

    environment {
        DOCKER_REGISTRY = 'your-registry'
        GIT_COMMIT_SHORT = sh(script: "git rev-parse --short HEAD", returnStdout: true).trim()
        // Ensure JAVA_HOME is set correctly
        JAVA_HOME = tool('Java21')
        MAVEN_HOME = tool('Maven')
        PATH = "${env.MAVEN_HOME}/bin:${env.JAVA_HOME}/bin:${env.PATH}"
    }
    stages {
        stage('Checkout') {
            steps {
                echo 'Checking out source code...'
                checkout scm
                
                // Verify Java and Maven installation
                sh '''
                    echo "Java Version:"
                    java -version
                    echo "JAVA_HOME: $JAVA_HOME"
                    echo "Maven Version:"
                    mvn -version
                '''
            }
        }
        
        stage('Build & Test Microservices') {
            parallel {
                stage('Account Service') {
                    steps {
                        script {
                            // Check if directory exists
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
                                if (fileExists('Account-Service/target/surefire-reports/*.xml')) {
                                    publishTestResults testResultsPattern: 'Account-Service/target/surefire-reports/*.xml'
                                } else if (fileExists('account-service/target/surefire-reports/*.xml')) {
                                    publishTestResults testResultsPattern: 'account-service/target/surefire-reports/*.xml'
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
                                if (fileExists('Cart-Service/target/surefire-reports/*.xml')) {
                                    publishTestResults testResultsPattern: 'Cart-Service/target/surefire-reports/*.xml'
                                } else if (fileExists('cart-service/target/surefire-reports/*.xml')) {
                                    publishTestResults testResultsPattern: 'cart-service/target/surefire-reports/*.xml'
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
                    // Get actual directories that exist
                    def possibleServices = [
                        'Account-Service', 'account-service',
                        'Cart-Service', 'cart-service', 
                        'Config-Server', 'config-server',
                        'Discovery-Service', 'discoveryservice', 
                        'Inventory-Service', 'inventory-service', 
                        'Order-Service', 'order-service',
                        'Product-Service', 'product-service', 
                        'Shop-Service', 'shop'
                    ]
                    
                    def existingServices = []
                    possibleServices.each { service ->
                        if (fileExists(service)) {
                            existingServices.add(service)
                        }
                    }
                    
                    echo "Found services: ${existingServices}"
                    
                    existingServices.each { service ->
                        dir(service) {
                            echo "Packaging ${service}..."
                            sh 'mvn package -DskipTests=true'
                        }
                    }
                }
            }
        }
        
        stage('Build Docker Images') {
            steps {
                script {
                    // Get actual directories that exist and have Dockerfile
                    def possibleServices = [
                        'Account-Service', 'account-service',
                        'Cart-Service', 'cart-service', 
                        'Config-Server', 'config-server',
                        'Discovery-Service', 'discoveryservice', 
                        'Inventory-Service', 'inventory-service', 
                        'Order-Service', 'order-service',
                        'Product-Service', 'product-service', 
                        'Shop-Service', 'shop'
                    ]
                    
                    possibleServices.each { service ->
                        if (fileExists(service) && fileExists("${service}/Dockerfile")) {
                            dir(service) {
                                echo "Building Docker image for ${service}..."
                                sh """
                                    docker build -t ${service.toLowerCase()}:${GIT_COMMIT_SHORT} .
                                    docker tag ${service.toLowerCase()}:${GIT_COMMIT_SHORT} ${service.toLowerCase()}:latest
                                """
                            }
                        } else {
                            echo "Skipping ${service} - directory or Dockerfile not found"
                        }
                    }
                }
            }
        }
        
        stage('Deploy to Development') {
            when {
                expression { fileExists('docker-compose.yml') }
            }
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
                        timeout(time: 3, unit: 'MINUTES') {
                            waitUntil {
                                script {
                                    try {
                                        def result = sh(script: "curl -f http://localhost:${port}/actuator/health || exit 1", returnStatus: true)
                                        return result == 0
                                    } catch (Exception e) {
                                        echo "Health check failed: ${e.getMessage()}"
                                        return false
                                    }
                                }
                            }
                        }
                        echo "${service} is healthy!"
                    }
                }
            }
        }
        
        stage('Integration Tests') {
            when {
                expression { fileExists('docker-compose.yml') }
            }
            steps {
                echo 'Running integration tests...'
                script {
                    // Basic API tests
                    sh '''
                        echo "Testing basic endpoints..."
                        # Add your integration test commands here
                        # Example: newman run postman_collection.json
                    '''
                }
            }
        }
    }
    
    post {
        always {
            echo 'Cleaning up...'
            
            // Archive artifacts if they exist
            script {
                if (sh(script: "find . -name '*.jar' -path '*/target/*' | head -1", returnStatus: true) == 0) {
                    archiveArtifacts artifacts: '**/target/*.jar', fingerprint: true, allowEmptyArchive: true
                }
                
                // Publish test reports if they exist  
                if (sh(script: "find . -name '*.xml' -path '*/surefire-reports/*' | head -1", returnStatus: true) == 0) {
                    publishTestResults testResultsPattern: '**/target/surefire-reports/*.xml'
                }
            }
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
            
            // Show recent logs for debugging
            sh '''
                echo "=== Recent logs for debugging ==="
                docker-compose logs --tail=50 || echo "No docker-compose logs available"
            '''
        }
        unstable {
            echo '⚠️ Pipeline unstable (some tests failed)'
        }
    }
}
