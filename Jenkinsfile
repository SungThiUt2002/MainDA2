pipeline {
    agent any  // Thay vì dùng Docker agent
    
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
        stage('Start Dependencies') {
            steps {
                script {
                    echo "=== Starting Infrastructure via Docker Compose ==="
                    sh 'docker compose up -d'

                    echo "=== Building and Starting Core Services ==="
                    dir('discoveryservice') { 
                        sh 'mvn clean package -DskipTests=true' 
                    }
                    dir('config-server') { 
                        sh 'mvn clean package -DskipTests=true' 
                    }
                    
                    dir('discoveryservice') { 
                        sh 'nohup java -jar target/discoveryservice-*.jar &' 
                    }
                    dir('config-server') { 
                        sh 'nohup java -jar target/config-server-*.jar &' 
                    }
                    
                    echo "Waiting for all dependencies to start up..."
                    sleep 30
                }
            }
        }
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
        
        stage('SonarQube Analysis') {
            steps {
                script {
                    def pomDirs = sh(script: "find . -maxdepth 2 -name 'pom.xml' -exec dirname {} \\;", returnStdout: true).trim().split('\n')
                    
                    pomDirs.each { pomDir ->
                        if (pomDir && pomDir != '.') {
                            dir(pomDir) {
                                withSonarQubeEnv('SonarQube') {
                                    def projectName = pomDir.replaceAll('^\\./', '')
                                    sh """
                                        echo "=== Running SonarQube Analysis for ${projectName} ==="
                                        echo "Using JAVA_HOME: $JAVA_HOME"
                                        mvn clean compile sonar:sonar \
                                            -DskipTests=true \
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
                                sh 'mvn clean package -DskipTests=true'
                            }
                        }
                    }
                }
                
                stage('Other Services') {
                    steps {
                        script {
                            // Chỉ build backend services (Maven projects)
                            def backendServices = ['Inventory-Service', 'inventory-service', 
                                                 'Order-Service', 'order-service', 
                                                 'Shop-Service']
                            
                            backendServices.each { service ->
                                if (fileExists(service) && fileExists("${service}/pom.xml")) {
                                    dir(service) {
                                        echo "Building backend service: ${service}..."
                                        sh 'mvn clean package -DskipTests=true'
                                    }
                                }
                            }
                            
                            // Skip frontend - để riêng pipeline khác
                            if (fileExists('shop') && !fileExists('shop/pom.xml')) {
                                echo "Found frontend directory 'shop' - skipping (not a Maven project)"
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
                    
                    pomDirs.each { dirPath ->
                        if (dirPath && dirPath != '.' && dirPath != './common-dto' && fileExists("${dirPath}/pom.xml")) {
                            dir(dirPath) {
                                echo "Testing ${dirPath}..."
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
                    docker compose down || true
                    
                    echo "Starting services..."
                    docker compose up -d
                    
                    echo "Service status:"
                    docker ps
                '''
            }
        }
    }
    
    post {
        always {
            node('') {  // Wrap trong node context
                script {
                    echo "=== Cleaning up environment ==="
                    
                    sh 'pkill -f "discoveryservice-.*.jar" || true'
                    sh 'pkill -f "config-server-.*.jar" || true'
                    sh 'docker compose down -v || true'
                    
                    try {
                        archiveArtifacts artifacts: '**/target/*.jar', allowEmptyArchive: true
                    } catch (Exception e) {
                        echo "No JAR files to archive"
                    }
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
            node('') {  // Wrap cleanWs trong node context
                cleanWs()
            }
        }
    }
}
