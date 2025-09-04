pipeline {
    agent any
    
    tools {
        maven 'Maven'
    }
    
    environment {
        GIT_COMMIT_SHORT = sh(script: "git rev-parse --short HEAD", returnStdout: true).trim()
        HARBOR_REGISTRY = "host.docker.internal:80"
        HARBOR_PROJECT = "doan_devsecops"
    }
    
    stages {
        stage('Build JAR Files First') {
            steps {
                script {
                    def services = ['account-service', 'cart-service', 'product-service', 
                                  'inventory-service', 'order-service', 'config-server', 
                                  'discoveryservice']
                    
                    services.each { service ->
                        def serviceDir = null
                        if (fileExists("${service.split('-').collect{it.capitalize()}.join('-')}")) {
                            serviceDir = "${service.split('-').collect{it.capitalize()}.join('-')}"
                        } else if (fileExists(service)) {
                            serviceDir = service
                        }
                        
                        if (serviceDir && fileExists("${serviceDir}/pom.xml")) {
                            dir(serviceDir) {
                                echo "Building JAR for ${service}..."
                                sh 'mvn clean package -DskipTests=true'
                            }
                        }
                    }
                }
            }
        }
        
        stage('Build và Push Docker Images') {
            steps {
                script {
                    // Login to Harbor
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
                        def serviceDir = null
                        if (fileExists("${service.split('-').collect{it.capitalize()}.join('-')}")) {
                            serviceDir = "${service.split('-').collect{it.capitalize()}.join('-')}"
                        } else if (fileExists(service)) {
                            serviceDir = service
                        }
                        
                        if (serviceDir && fileExists("${serviceDir}/target") && fileExists("${serviceDir}/pom.xml")) {
                            def serviceName = service.toLowerCase()
                            echo "=== Processing ${serviceName} from ${serviceDir} ==="
                            
                            dir(serviceDir) {
                                // Simple single-stage Dockerfile using local JAR
                                sh """
cat > Dockerfile << 'EOF'
FROM eclipse-temurin:21-jre

WORKDIR /app

# Create non-root user
RUN groupadd -r appuser && useradd -r -g appuser appuser

# Copy JAR file from local build
COPY target/*.jar app.jar

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
                                    # Simple build using existing JAR
                                    docker build -t ${serviceName}:${GIT_COMMIT_SHORT} . || {
                                        echo "Build failed, trying with network host mode..."
                                        docker build --network=host -t ${serviceName}:${GIT_COMMIT_SHORT} .
                                    }
                                    
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
                            echo "⚠️ Skipping ${service} - directory, target folder, or pom.xml not found"
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

✅ JAR files built with Maven
✅ Docker images built successfully  
✅ Images pushed to Harbor registry
✅ Git tag created
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
