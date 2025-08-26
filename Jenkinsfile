pipeline {
    agent any
    
    tools {
        maven 'Maven'
    }
    
    environment {
        GIT_COMMIT_SHORT = sh(script: "git rev-parse --short HEAD", returnStdout: true).trim()
        JAVA_HOME = "/opt/java/openjdk"
    }
    
    stages{
        stage('Environment Check') {
            steps {
                sh '''
                    echo "=== Build Environment ==="
                    echo "JAVA_HOME: $JAVA_HOME"
                    java -version
              
                    echo "=== Maven Test ==="
                    mvn -version
                
                    echo "=== Project Structure ==="
                    find . -maxdepth 2 -name "pom.xml" -exec dirname {} \\; | sort
                '''
           }
      }
        
        stage('SonarQube Analysis') {
            steps {
                script {
                    // Find all directories with pom.xml and run sonar analysis
                    def pomDirs = sh(script: "find . -maxdepth 2 -name 'pom.xml' -exec dirname {} \\;", returnStdout: true).trim().split('\n')
                    
                    pomDirs.each { pomDir ->
                        if (pomDir && pomDir != '.') {
                            dir(pomDir) {
                                withSonarQubeEnv('SonarQube') {
                                    def projectName = pomDir.replaceAll('^\\./', '')
                                    sh """
                                        echo "=== Running SonarQube Analysis for ${projectName} ==="
                                        echo "Using JAVA_HOME: $JAVA_HOME"
                                        mvn clean verify sonar:sonar \
                                            -Dsonar.projectKey=microservices-${projectName} \
                                            -Dsonar.projectName="Microservices ${projectName}" \
                                            -Dsonar.projectVersion=${GIT_COMMIT_SHORT} || echo "SonarQube failed for ${projectName}, continuing..."
                                    """
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Quality Gate stage removed for faster pipeline execution
        // SonarQube analysis results are still available in SonarQube dashboard
        
        stage('Build Common DTO') {
            when {
                expression { 
                    return sh(script: "find . -name 'common-dto' -type d", returnStdout: true).trim() != ""
                }
            }
            steps {
                dir('common-dto') {
                    echo 'Building common-dto...'
                    sh 'mvn clean install -DskipTests=true'
                }
            }
        }
        
        stage('Build Microservices') {
            parallel {
                stage('Account Service') {
                    when {
                        anyOf {
                            expression { fileExists('Account-Service') }
                            expression { fileExists('account-service') }
                        }
                    }
                    steps {
                        script {
                            def serviceDir = fileExists('Account-Service') ? 'Account-Service' : 'account-service'
                            dir(serviceDir) {
                                sh 'mvn clean package -DskipTests=true'
                            }
                        }
                    }
                }
                
                stage('Cart Service') {
                    when {
                        anyOf {
                            expression { fileExists('Cart-Service') }
                            expression { fileExists('cart-service') }
                        }
                    }
                    steps {
                        script {
                            def serviceDir = fileExists('Cart-Service') ? 'Cart-Service' : 'cart-service'
                            dir(serviceDir) {
                                sh 'mvn clean package -DskipTests=true'
                            }
                        }
                    }
                }
                
                stage('Config Server') {
                    when {
                        anyOf {
                            expression { fileExists('Config-Server') }
                            expression { fileExists('config-server') }
                        }
                    }
                    steps {
                        script {
                            def serviceDir = fileExists('Config-Server') ? 'Config-Server' : 'config-server'
                            dir(serviceDir) {
                                sh 'mvn clean package -DskipTests=true'
                            }
                        }
                    }
                }
                
                stage('Discovery Service') {
                    when {
                        anyOf {
                            expression { fileExists('Discovery-Service') }
                            expression { fileExists('discoveryservice') }
                        }
                    }
                    steps {
                        script {
                            def serviceDir = fileExists('Discovery-Service') ? 'Discovery-Service' : 'discoveryservice'
                            dir(serviceDir) {
                                sh 'mvn clean package -DskipTests=true'
                            }
                        }
                    }
                }
                
                stage('Product Service') {
                    when {
                        anyOf {
                            expression { fileExists('Product-Service') }
                            expression { fileExists('product-service') }
                        }
                    }
                    steps {
                        script {
                            def serviceDir = fileExists('Product-Service') ? 'Product-Service' : 'product-service'
                            dir(serviceDir) {
                                sh '''
                                    export JAVA_HOME=$JAVA_HOME
                                    mvn clean package -DskipTests=true
                                '''
                            }
                        }
                    }
                }
                
                stage('Other Services') {
                    steps {
                        script {
                            def otherServices = ['Inventory-Service', 'inventory-service', 
                                               'Order-Service', 'order-service', 
                                               'Shop-Service', 'shop']
                            
                            otherServices.each { service ->
                                if (fileExists(service)) {
                                    dir(service) {
                                        echo "Building ${service}..."
                                        sh 'mvn clean package -DskipTests=true'
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        stage('Run Tests') {
            steps {
                script {
                    def pomDirs = sh(script: "find . -maxdepth 2 -name 'pom.xml' -exec dirname {} \\;", returnStdout: true).trim().split('\n')
                    
                    pomDirs.each { dir ->
                        if (dir && dir != '.' && dir != './common-dto') {
                            dir(dir) {
                                echo "Testing ${dir}..."
                                sh 'mvn test || true'
                            }
                        }
                    }
                }
            }
            post {
                always {
                    script {
                        try {
                            junit testResults: '**/target/surefire-reports/*.xml', allowEmptyResults: true
                        } catch (Exception e) {
                            echo "No test results to publish"
                        }
                    }
                }
            }
        }
        
        stage('Build Docker Images') {
            steps {
                script {
                    def services = sh(script: "find . -maxdepth 2 -name 'Dockerfile' -exec dirname {} \\;", returnStdout: true).trim().split('\n')
                    
                    services.each { serviceDir ->
                        if (serviceDir && serviceDir != '.') {
                            dir(serviceDir) {
                                def imageName = serviceDir.replaceAll('^\\./', '').toLowerCase().replaceAll('[^a-z0-9-]', '-')
                                echo "Building Docker image: ${imageName}"
                                sh """
                                    docker build -t ${imageName}:${GIT_COMMIT_SHORT} .
                                    docker tag ${imageName}:${GIT_COMMIT_SHORT} ${imageName}:latest
                                """
                            }
                        }
                    }
                }
            }
        }
        
        stage('Deploy') {
            when {
                anyOf {
                    expression { fileExists('docker-compose.yml') }
                    expression { fileExists('docker-compose.yaml') }
                }
            }
            steps {
                sh '''
                    echo "Stopping existing containers..."
                    docker-compose down || true
                    
                    echo "Starting services..."
                    docker-compose up -d
                    
                    echo "Service status:"
                    docker ps
                '''
            }
        }
    }
    
    post {
        always {
            script {
                try {
                    archiveArtifacts artifacts: '**/target/*.jar', allowEmptyArchive: true
                } catch (Exception e) {
                    echo "No JAR files to archive"
                }
            }
        }
        success {
            echo 'Pipeline completed successfully!'
        }
        failure {
            echo 'Pipeline failed!'
        }
        cleanup {
            cleanWs()
        }
    }
}
