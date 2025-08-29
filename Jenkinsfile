pipeline {
    agent any
    
    tools {
        maven 'Maven'
    }
    
    environment {
        GIT_COMMIT_SHORT = sh(script: "git rev-parse --short HEAD", returnStdout: true).trim()
        JAVA_HOME = "/opt/java/openjdk"
        HARBOR_REGISTRY = "localhost:80"
        HARBOR_PROJECT = "doan_devsecops"
    }
    
    stages {
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
                    
                    echo "=== Harbor Registry Config ==="
                    echo "HARBOR_REGISTRY: $HARBOR_REGISTRY"
                    echo "HARBOR_PROJECT: $HARBOR_PROJECT"
                '''
            }
        }
        
        stage('Start Dependencies') {
            steps {
                script {
                    echo "=== Starting Infrastructure via Docker Compose ==="
                    sh 'docker-compose up -d || true'

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
                    
                    echo "Waiting for dependencies to start..."
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
                            def backendServices = ['Inventory-Service', 'inventory-service', 
                                                 'Order-Service', 'order-service', 
                                                 'Shop-Service', 'shop-service']
                            
                            backendServices.each { service ->
                                if (fileExists(service) && fileExists("${service}/pom.xml")) {
                                    dir(service) {
                                        echo "Building backend service: ${service}..."
                                        sh 'mvn clean package -DskipTests=true'
                                    }
                                }
                            }
                            
                            if (fileExists('shop') && !fileExists('shop/pom.xml')) {
                                echo "Found frontend directory 'shop' - skipping (not a Maven project)"
                            }
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
                                    docker tag ${imageName}:${GIT_COMMIT_SHORT} ${HARBOR_REGISTRY}/${HARBOR_PROJECT}/${imageName}:${GIT_COMMIT_SHORT}
                                    docker tag ${imageName}:${GIT_COMMIT_SHORT} ${HARBOR_REGISTRY}/${HARBOR_PROJECT}/${imageName}:latest
                                """
                            }
                        }
                    }
                }
            }
        }
        
        stage('Push to Harbor & Update Git') {
            parallel {
                stage('Push to Harbor Registry') {
                    steps {
                        script {
                            // Login to Harbor
                            withCredentials([usernamePassword(credentialsId: 'harbor-login-robot', 
                                                            passwordVariable: 'HARBOR_PASSWORD', 
                                                            usernameVariable: 'HARBOR_USERNAME')]) {
                                sh """
                                    echo "Logging into Harbor registry..."
                                    echo '${HARBOR_PASSWORD}' | docker login ${HARBOR_REGISTRY} -u '${HARBOR_USERNAME}' --password-stdin
                                """
                            }
                            
                            // Push images to Harbor
                            def services = sh(script: "find . -maxdepth 2 -name 'Dockerfile' -exec dirname {} \\;", returnStdout: true).trim().split('\n')
                            
                            services.each { serviceDir ->
                                if (serviceDir && serviceDir != '.') {
                                    def imageName = serviceDir.replaceAll('^\\./', '').toLowerCase().replaceAll('[^a-z0-9-]', '-')
                                    echo "Pushing ${imageName} to Harbor Registry..."
                                    
                                    sh """
                                        docker push ${HARBOR_REGISTRY}/${HARBOR_PROJECT}/${imageName}:${GIT_COMMIT_SHORT}
                                        docker push ${HARBOR_REGISTRY}/${HARBOR_PROJECT}/${imageName}:latest
                                    """
                                }
                            }
                            
                            // Logout from Harbor
                            sh "docker logout ${HARBOR_REGISTRY}"
                        }
                    }
                }
                
                stage('Update Git with Tag') {
                    steps {
                        script {
                            withCredentials([usernamePassword(credentialsId: 'gitea-admin', 
                                                            passwordVariable: 'GIT_PASSWORD', 
                                                            usernameVariable: 'GIT_USERNAME')]) {
                                sh """
                                    echo "Creating and pushing new tag..."
                                    git config user.name "${GIT_USERNAME}"
                                    git config user.email "${GIT_USERNAME}@company.local"
                                    
                                    # Check if tag already exists
                                    if git rev-parse "v${GIT_COMMIT_SHORT}" >/dev/null 2>&1; then
                                        echo "Tag v${GIT_COMMIT_SHORT} already exists, skipping..."
                                    else
                                        echo "Creating new tag v${GIT_COMMIT_SHORT}"
                                        git tag -a "v${GIT_COMMIT_SHORT}" -m "Release version ${GIT_COMMIT_SHORT} - Docker images built and pushed"
                                        
                                        # Push with current remote URL
                                        git push origin "v${GIT_COMMIT_SHORT}" || echo "Failed to push tag, continuing..."
                                    fi
                                """
                            }
                        }
                    }
                }
            }
        }
        
        stage('Check Vulnerability Scan Results') {
            steps {
                script {
                    echo "=== Vulnerability Scan Results ==="
                    echo "Harbor is automatically scanning pushed images for security vulnerabilities."
                    echo "Check Harbor UI at http://localhost:80 for detailed vulnerability reports."
                    echo "Images with high/critical vulnerabilities may be blocked based on project policy."
                    
                    // Wait for vulnerability scan to complete
                    echo "Waiting for Harbor vulnerability scanning to complete..."
                    sleep 30
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
                    
                    echo "Starting services with new images..."
                    docker-compose up -d
                    
                    echo "Service status:"
                    docker ps --format "table {{.Names}}\\t{{.Status}}\\t{{.Ports}}"
                '''
            }
        }
    }
    
    post {
        always {
            script {
                echo "=== Cleaning up environment ==="
                
                sh 'pkill -f "discoveryservice-.*.jar" || true'
                sh 'pkill -f "config-server-.*.jar" || true'
                sh 'docker-compose down -v || true'
                
                try {
                    archiveArtifacts artifacts: '**/target/*.jar', allowEmptyArchive: true
                } catch (Exception e) {
                    echo "No JAR files to archive"
                }
                
                // Clean up Docker images to save space
                sh '''
                    echo "Cleaning up local Docker images..."
                    docker image prune -f || true
                '''
            }
        }
        success {
            echo 'Pipeline completed successfully!'
            echo "Images pushed to Harbor: ${HARBOR_REGISTRY}/${HARBOR_PROJECT}/"
            echo "Harbor will automatically scan images for vulnerabilities"
            echo "Check Harbor UI for vulnerability reports and deployment approval"
        }
        failure {
            echo 'Pipeline failed!'
            sh "docker logout ${HARBOR_REGISTRY} || true"
        }
        cleanup {
            cleanWs()
        }
    }
}
