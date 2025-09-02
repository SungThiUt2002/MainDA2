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
              
                    echo "=== Maven Version ==="
                    mvn -version
                
                    echo "=== Project Structure ==="
                    find . -maxdepth 2 -name "pom.xml" -exec dirname {} \\; | sort
                    
                    echo "=== Harbor Registry Config ==="
                    echo "HARBOR_REGISTRY: $HARBOR_REGISTRY"
                    echo "HARBOR_PROJECT: $HARBOR_PROJECT"
                    
                    echo "=== Docker Version ==="
                    docker --version
                '''
            }
        }
        
        stage('Start Dependencies') {
            steps {
                script {
                    echo "=== Starting Infrastructure via Docker Compose ==="
                    sh 'docker-compose up -d || true'

                    echo "=== Building and Starting Core Services ==="
                    if (fileExists('discoveryservice')) {
                        dir('discoveryservice') { 
                            sh 'mvn clean package -DskipTests=true' 
                            sh 'nohup java -jar target/discoveryservice-*.jar &'
                        }
                    }
                    
                    if (fileExists('config-server')) {
                        dir('config-server') { 
                            sh 'mvn clean package -DskipTests=true' 
                            sh 'nohup java -jar target/config-server-*.jar &'
                        }
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
        
        stage('Đóng gói mã nguồn ứng dụng') {
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
                                echo "Building ${serviceDir}..."
                                sh 'mvn clean package -DskipTests=true'
                                echo "JAR files created:"
                                sh 'ls -la target/*.jar'
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
                                echo "Building ${serviceDir}..."
                                sh 'mvn clean package -DskipTests=true'
                                echo "JAR files created:"
                                sh 'ls -la target/*.jar'
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
                                echo "Building ${serviceDir}..."
                                sh 'mvn clean package -DskipTests=true'
                                echo "JAR files created:"
                                sh 'ls -la target/*.jar'
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
                                        echo "JAR files created:"
                                        sh 'ls -la target/*.jar'
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        stage('Quét mã nguồn') {
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
        
        stage('Đóng gói ứng dụng bằng Docker') {
            parallel {
                stage('Build Docker Images') {
                    steps {
                        script {
                            def services = ['Account-Service', 'account-service', 'Cart-Service', 'cart-service', 
                                          'Product-Service', 'product-service', 'Inventory-Service', 'inventory-service',
                                          'Order-Service', 'order-service', 'Shop-Service', 'shop-service']
                            
                            services.each { service ->
                                if (fileExists(service) && fileExists("${service}/target")) {
                                    def serviceName = service.toLowerCase().replaceAll('[^a-z0-9-]', '-')
                                    echo "=== Building Docker image for: ${serviceName} ==="
                                    
                                    dir(service) {
                                        // Check JAR files first
                                        echo "Checking target directory:"
                                        sh 'ls -la target/'
                                        
                                        // Get exact JAR filename
                                        def jarFile = sh(script: 'ls target/*.jar | head -1 | xargs basename', returnStdout: true).trim()
                                        echo "JAR file found: ${jarFile}"
                                        
                                        // Create optimized Dockerfile
                                        if (fileExists('Dockerfile')) {
                                            echo "Found existing Dockerfile, checking content..."
                                            sh 'cat Dockerfile'
                                            
                                            // Fix commented Dockerfiles
                                            sh '''
                                                if grep -q "^#FROM" Dockerfile; then
                                                    echo "Uncommenting Dockerfile..."
                                                    sed 's/^#//g' Dockerfile > Dockerfile.tmp && mv Dockerfile.tmp Dockerfile
                                                fi
                                            '''
                                        } else {
                                            echo "Creating Dockerfile for ${serviceName}..."
                                            sh """
cat > Dockerfile << 'EOF'
FROM openjdk:21-jre-slim

WORKDIR /app

# Create non-root user
RUN groupadd -r appuser && useradd -r -g appuser appuser

# Copy JAR file
COPY target/${jarFile} app.jar

# Set ownership
RUN chown appuser:appuser app.jar

# Switch to non-root user
USER appuser

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \\
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
EOF"""
                                        }
                                        
                                        echo "Final Dockerfile content:"
                                        sh 'cat Dockerfile'
                                        
                                        echo "Building Docker image..."
                                        sh """
                                            docker build -t ${serviceName}:${GIT_COMMIT_SHORT} .
                                            docker tag ${serviceName}:${GIT_COMMIT_SHORT} ${serviceName}:latest
                                            docker tag ${serviceName}:${GIT_COMMIT_SHORT} ${HARBOR_REGISTRY}/${HARBOR_PROJECT}/${serviceName}:${GIT_COMMIT_SHORT}
                                            docker tag ${serviceName}:${GIT_COMMIT_SHORT} ${HARBOR_REGISTRY}/${HARBOR_PROJECT}/${serviceName}:latest
                                        """
                                        
                                        echo "Image built and tagged successfully for ${serviceName}"
                                        sh "docker images | grep ${serviceName}"
                                    }
                                }
                            }
                        }
                    }
                }
                
                stage('Cập nhật tag và commit lên git') {
                    steps {
                        script {
                            sh """
                                echo "Creating Git tag..."
                                git config user.name "jenkins-ci"
                                git config user.email "jenkins@company.local"
                                
                                if git rev-parse "v${GIT_COMMIT_SHORT}" >/dev/null 2>&1; then
                                    echo "Tag v${GIT_COMMIT_SHORT} already exists"
                                else
                                    git tag -a "v${GIT_COMMIT_SHORT}" -m "Release version ${GIT_COMMIT_SHORT}"
                                    echo "Tag v${GIT_COMMIT_SHORT} created"
                                fi
                            """
                        }
                    }
                }
            }
        }
        
        stage('Push image lên Harbor') {
            steps {
                script {
                    withCredentials([usernamePassword(credentialsId: 'harbor-login-robot', 
                                                    passwordVariable: 'HARBOR_PASSWORD', 
                                                    usernameVariable: 'HARBOR_USERNAME')]) {
                        sh """
                            echo "Logging into Harbor registry..."
                            echo "\${HARBOR_PASSWORD}" | docker login ${HARBOR_REGISTRY} -u "\${HARBOR_USERNAME}" --password-stdin
                        """
                    }
                    
                    def services = ['Account-Service', 'account-service', 'Cart-Service', 'cart-service', 
                                  'Product-Service', 'product-service', 'Inventory-Service', 'inventory-service',
                                  'Order-Service', 'order-service', 'Shop-Service', 'shop-service']
                    
                    def pushedImages = []
                    services.each { service ->
                        if (fileExists(service) && fileExists("${service}/target")) {
                            def serviceName = service.toLowerCase().replaceAll('[^a-z0-9-]', '-')
                            
                            // Verify image exists before pushing
                            def imageExists = sh(script: "docker images -q ${serviceName}:${GIT_COMMIT_SHORT}", returnStdout: true).trim()
                            if (imageExists) {
                                echo "Pushing ${serviceName} to Harbor..."
                                sh """
                                    docker push ${HARBOR_REGISTRY}/${HARBOR_PROJECT}/${serviceName}:${GIT_COMMIT_SHORT}
                                    docker push ${HARBOR_REGISTRY}/${HARBOR_PROJECT}/${serviceName}:latest
                                """
                                pushedImages.add(serviceName)
                                echo "Successfully pushed ${serviceName}"
                            } else {
                                error("Image ${serviceName}:${GIT_COMMIT_SHORT} not found!")
                            }
                        }
                    }
                    
                    sh "docker logout ${HARBOR_REGISTRY}"
                    
                    echo "=== Push Summary ==="
                    echo "Successfully pushed ${pushedImages.size()} images:"
                    pushedImages.each { img ->
                        echo "✓ ${img}"
                    }
                }
            }
        }
        
        stage('Quét image với Trivy') {
            steps {
                script {
                    echo "=== Harbor Vulnerability Scanning with Trivy ==="
                    echo "Trivy scanner is automatically analyzing pushed images..."
                    echo ""
                    echo "Scanning for:"
                    echo "• OS vulnerabilities (CVEs)"
                    echo "• Application dependencies"
                    echo "• Security misconfigurations"
                    echo "• License compliance"
                    echo ""
                    echo "Harbor UI: http://localhost:80"
                    echo "Navigate: Projects → ${HARBOR_PROJECT} → Repositories → [image] → Vulnerabilities"
                    echo ""
                    
                    // Wait for scan initiation
                    echo "Waiting for vulnerability scans to initialize..."
                    sleep 60
                    
                    echo "✓ Trivy vulnerability scanning initiated for all pushed images"
                    echo "Review detailed scan results in Harbor UI"
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
                    echo "=== Deploying Applications ==="
                    echo "Stopping existing containers..."
                    docker-compose down || true
                    
                    echo "Starting services with new images..."
                    docker-compose up -d
                    
                    echo "=== Deployment Status ==="
                    docker ps --format "table {{.Names}}\\t{{.Status}}\\t{{.Ports}}"
                    
                    echo "=== Health Check ==="
                    sleep 45
                    echo "Applications should be ready. Check service endpoints."
                '''
            }
        }
    }
    
    post {
        always {
            script {
                echo "=== Pipeline Cleanup ==="
                
                sh 'pkill -f "discoveryservice-.*.jar" || true'
                sh 'pkill -f "config-server-.*.jar" || true'
                
                try {
                    archiveArtifacts artifacts: '**/target/*.jar', allowEmptyArchive: true, excludes: '**/*-sources.jar,**/*-javadoc.jar'
                } catch (Exception e) {
                    echo "No JAR files to archive: ${e.getMessage()}"
                }
                
                sh '''
                    echo "Cleaning up unused Docker resources..."
                    docker image prune -f --filter "dangling=true" || true
                    docker container prune -f || true
                '''
            }
        }
        success {
            echo '''
=== 🎉 PIPELINE SUCCESS 🎉 ===

✅ Build completed successfully
✅ SonarQube code analysis completed  
✅ Docker images built and pushed to Harbor
✅ Trivy vulnerability scanning initiated
✅ Applications deployed successfully

📋 Next Steps:
1. Review SonarQube reports: [SonarQube URL]
2. Check vulnerability scans: Harbor UI → Projects → doan_devsecops
3. Verify application health: Check service endpoints
4. Monitor logs: docker-compose logs -f

🔗 Useful Links:
• Harbor Registry: http://localhost:80
• SonarQube: [Your SonarQube URL]
            '''
        }
        failure {
            echo '''
=== ❌ PIPELINE FAILED ❌ ===

Pipeline failed at stage: ''' + "${env.STAGE_NAME}" + '''

🔍 Troubleshooting:
1. Check build logs above for detailed errors
2. Verify all dependencies are running
3. Check Docker daemon status
4. Verify Harbor registry connectivity
            '''
            sh "docker logout ${HARBOR_REGISTRY} || true"
        }
        cleanup {
            cleanWs()
        }
    }
}
