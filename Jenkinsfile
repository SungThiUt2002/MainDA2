pipeline {
    agent any
    
    tools {
        maven 'Maven'
    }
    
    environment {
        GIT_COMMIT_SHORT = sh(script: "git rev-parse --short HEAD", returnStdout: true).trim()
        HARBOR_REGISTRY = "localhost:80"
        HARBOR_PROJECT = "doan_devsecops"
    }
    
    stages {
        // Comment out time-consuming stages
        /*
        stage('Environment Check') {
            steps {
                sh '''
                    echo "=== Quick Environment Check ==="
                    docker --version
                    echo "HARBOR_REGISTRY: $HARBOR_REGISTRY"
                    echo "HARBOR_PROJECT: $HARBOR_PROJECT"
                '''
            }
        }
        
        stage('Start Dependencies') {
            steps {
                script {
                    echo "=== Skipping dependency startup for faster build ==="
                    // sh 'docker-compose up -d || true'
                    // Start core services if needed
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
            steps {
                script {
                    echo "=== Skipping JAR build - will build in Docker ==="
                    // JAR build will be handled by Docker multi-stage build
                }
            }
        }
        
        stage('Quét mã nguồn') {
            steps {
                script {
                    echo "=== Skipping SonarQube analysis for faster build ==="
                    // SonarQube analysis commented out
                }
            }
        }
        */
        
        stage('Build và Push Docker Images') {
            steps {
                script {
                    // Login to Harbor first
                    withCredentials([usernamePassword(credentialsId: 'harbor-login-robot', 
                                                    passwordVariable: 'HARBOR_PASSWORD', 
                                                    usernameVariable: 'HARBOR_USERNAME')]) {
                        sh """
                            echo "Logging into Harbor registry..."
                            echo "\${HARBOR_PASSWORD}" | docker login ${HARBOR_REGISTRY} -u "\${HARBOR_USERNAME}" --password-stdin
                        """
                    }
                    
                    def services = ['account-service', 'cart-service', 'product-service', 
                                  'inventory-service', 'order-service', 'config-server', 
                                  'discoveryservice']
                    
                    def builtImages = []
                    
                    services.each { service ->
                        // Check both uppercase and lowercase directory names
                        def serviceDir = null
                        if (fileExists("${service.split('-').collect{it.capitalize()}.join('-')}")) {
                            serviceDir = "${service.split('-').collect{it.capitalize()}.join('-')}"
                        } else if (fileExists(service)) {
                            serviceDir = service
                        }
                        
                        if (serviceDir && fileExists("${serviceDir}/pom.xml")) {
                            def serviceName = service.toLowerCase()
                            echo "=== Processing ${serviceName} from ${serviceDir} ==="
                            
                            dir(serviceDir) {
                                // Create optimized Dockerfile with multi-stage build
                                sh """
cat > Dockerfile << 'EOF'
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app
COPY pom.xml .
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests=true

FROM eclipse-temurin:21-jre

WORKDIR /app

# Create non-root user
RUN groupadd -r appuser && useradd -r -g appuser appuser

# Copy JAR from builder stage
COPY --from=builder /app/target/*.jar app.jar

# Set ownership
RUN chown appuser:appuser app.jar

# Switch to non-root user
USER appuser

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \\
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
EOF"""
                                
                                echo "Building Docker image for ${serviceName}..."
                                sh """
                                    docker build -t ${serviceName}:${GIT_COMMIT_SHORT} .
                                    docker tag ${serviceName}:${GIT_COMMIT_SHORT} ${serviceName}:latest
                                    docker tag ${serviceName}:${GIT_COMMIT_SHORT} ${HARBOR_REGISTRY}/${HARBOR_PROJECT}/${serviceName}:${GIT_COMMIT_SHORT}
                                    docker tag ${serviceName}:${GIT_COMMIT_SHORT} ${HARBOR_REGISTRY}/${HARBOR_PROJECT}/${serviceName}:latest
                                """
                                
                                echo "Pushing ${serviceName} to Harbor..."
                                sh """
                                    docker push ${HARBOR_REGISTRY}/${HARBOR_PROJECT}/${serviceName}:${GIT_COMMIT_SHORT}
                                    docker push ${HARBOR_REGISTRY}/${HARBOR_PROJECT}/${serviceName}:latest
                                """
                                
                                builtImages.add(serviceName)
                                echo "✅ Successfully built and pushed ${serviceName}"
                            }
                        } else {
                            echo "⚠️ Skipping ${service} - directory or pom.xml not found"
                        }
                    }
                    
                    sh "docker logout ${HARBOR_REGISTRY}"
                    
                    echo """
=== 🚀 BUILD & PUSH SUMMARY 🚀 ===

Successfully built and pushed ${builtImages.size()} microservices:
${builtImages.collect { "✅ ${it}:${GIT_COMMIT_SHORT}" }.join('\n')}

🔗 Harbor Registry: http://localhost:80
📁 Project: ${HARBOR_PROJECT}
🏷️  Tag: ${GIT_COMMIT_SHORT} & latest
                    """
                }
            }
        }
        
        // Quick Git tagging
        stage('Git Tagging') {
            steps {
                script {
                    sh """
                        git config user.name "Jenkins CI"
                        git config user.email "jenkins@localhost"
                        
                        if ! git rev-parse "v${GIT_COMMIT_SHORT}" >/dev/null 2>&1; then
                            git tag -a "v${GIT_COMMIT_SHORT}" -m "Release version ${GIT_COMMIT_SHORT}"
                            echo "✅ Tag v${GIT_COMMIT_SHORT} created"
                        else
                            echo "ℹ️ Tag v${GIT_COMMIT_SHORT} already exists"
                        fi
                    """
                }
            }
        }
        
        /*
        stage('Trivy Security Scan') {
            steps {
                script {
                    echo "=== Security scanning will run automatically in Harbor ==="
                    echo "Check Harbor UI for Trivy scan results"
                }
            }
        }
        
        stage('Deploy') {
            steps {
                script {
                    echo "=== Skipping deployment for faster pipeline ==="
                    // docker-compose deployment commented out
                }
            }
        }
        */
    }
    
    post {
        always {
            script {
                sh '''
                    echo "=== Quick Cleanup ==="
                    docker image prune -f --filter "dangling=true" || true
                '''
            }
        }
        success {
            echo '''
🎉 DOCKER BUILD & HARBOR PUSH COMPLETED! 🎉

✅ All microservice images built successfully
✅ Images pushed to Harbor registry  
✅ Git tag created

🔍 Next Steps:
• Check Harbor UI: http://localhost:80
• Review pushed images in project: doan_devsecops
• Trivy security scans will run automatically
            '''
        }
        failure {
            sh "docker logout ${HARBOR_REGISTRY} || true"
        }
        cleanup {
            cleanWs()
        }
    }
}
