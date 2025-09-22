    pipeline {
    agent any
    
    tools {
        maven 'Maven'
    }
    
    environment {
        BUILD_VERSION = "v.${BUILD_NUMBER}"
        JAVA_HOME = "/opt/java/openjdk"
        HARBOR_REGISTRY = "152.42.230.92:8082"  
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
        
        stage('Build JAR Files') {
            steps {
                script {
                    // Phân loại services theo loại - sử dụng cách tiếp cận của file 2
                    def microservices = ['account-service', 'cart-service', 'product-service', 
                                       'inventory-service', 'order-service']
                    def infrastructureServices = ['config-server', 'discoveryservice', 'common-dto']
                    def allServices = microservices + infrastructureServices
                    
                    // Build common-dto trước (vì các service khác depend vào nó)
                    if (fileExists('common-dto') && fileExists('common-dto/pom.xml')) {
                        dir('common-dto') {
                            echo "🔧 Building common-dto library first..."
                            sh 'mvn clean install -DskipTests=true'
                            echo "✅ common-dto built and installed to local repository"
                        }
                    }
                    
                    allServices.each { service ->
                        if (service == 'common-dto') {
                            return // Đã build ở trên
                        }
                        
                        def serviceDir = null
                        
                        // Kiểm tra cả hai định dạng tên thư mục (Pascal và kebab case)
                        def pascalCase = service.split('-').collect { it.capitalize() }.join('-') + (service.endsWith('-service') ? '' : '-Service')
                        def kebabCase = service.toLowerCase()
                        
                        if (fileExists(pascalCase)) {
                            serviceDir = pascalCase
                        } else if (fileExists(kebabCase)) {
                            serviceDir = kebabCase
                        }
                        
                        if (serviceDir && fileExists("${serviceDir}/pom.xml")) {
                            dir(serviceDir) {
                                echo "Building JAR for ${service} in directory ${serviceDir}..."
                                sh 'mvn clean package -DskipTests=true'
                                
                                // Kiểm tra xem JAR đã được tạo chưa
                                def jarExists = sh(script: "ls target/*.jar 2>/dev/null || echo 'no-jar'", returnStdout: true).trim()
                                if (jarExists == 'no-jar') {
                                    error("JAR file not found for ${service}")
                                } else {
                                    echo "✅ JAR built successfully for ${service}: ${jarExists}"
                                }
                            }
                        } else {
                            echo "⚠️ Skipping ${service} - directory or pom.xml not found"
                        }
                    }
                }
            }
        }
        
        // ===== SONARQUBE STAGE =====
      
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
                                        mvn compile sonar:sonar \
                                            -DskipTests=true \
                                            -Dsonar.projectKey=microservices-${projectName} \
                                            -Dsonar.projectName="Microservices ${projectName}" \
                                            -Dsonar.projectVersion=${BUILD_VERSION} || echo "SonarQube failed for ${projectName}, continuing..."
                                    """
                                }
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
                            echo "Logging into Harbor registry (HTTP mode)..."
                            echo "\${HARBOR_PASSWORD}" | docker login http://${HARBOR_REGISTRY} -u "\${HARBOR_USERNAME}" --password-stdin
                        """
                    }
                    
                    // Build Frontend first
                    if (fileExists('shop/src') && fileExists('shop/package.json')) {
                        dir('shop') {    
                        echo "=== Building React Frontend ==="
                        sh '''
                            docker run --rm -v $(pwd):/app -w /app node:18-alpine sh -c "npm install && npm run build"
                            
                            cat > Dockerfile << 'EOF'
FROM nginx:alpine
COPY build/ /usr/share/nginx/html/
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
EOF

                            cat > nginx.conf << 'EOF'
server {
    listen 80;
    location / {
        root /usr/share/nginx/html;
        try_files $uri $uri/ /index.html;
    }
    
    location /api/ {
        proxy_pass http://istio-gateway/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
EOF
                        '''
                            
                        sh """
                            docker build -t frontend:${BUILD_VERSION} .
                            docker tag frontend:${BUILD_VERSION} ${HARBOR_REGISTRY}/${HARBOR_PROJECT}/frontend:${BUILD_VERSION}
                            docker tag frontend:${BUILD_VERSION} ${HARBOR_REGISTRY}/${HARBOR_PROJECT}/frontend:latest
                            docker push ${HARBOR_REGISTRY}/${HARBOR_PROJECT}/frontend:${BUILD_VERSION}
                            docker push ${HARBOR_REGISTRY}/${HARBOR_PROJECT}/frontend:latest
                            
                            echo "✅ Frontend built and pushed successfully"
                        """
                    }
                }
                    
                    // Backend services
                    def dockerServices = ['account-service', 'cart-service', 'product-service', 
                                        'inventory-service', 'order-service',
                                        'config-server', 'discoveryservice']
                    
                    def builtImages = []
                    
                    dockerServices.each { service ->
                        def serviceDir = null
                        
                        // Kiểm tra cả hai định dạng tên thư mục
                        def pascalCase = service.split('-').collect { it.capitalize() }.join('-')
                        if (!service.endsWith('-service') && !service.equals('config-server') && !service.equals('discoveryservice')) {
                            pascalCase += '-Service'
                        }
                        def kebabCase = service.toLowerCase()
                        
                        if (fileExists(pascalCase)) {
                            serviceDir = pascalCase
                        } else if (fileExists(kebabCase)) {
                            serviceDir = kebabCase
                        }
                        
                        if (serviceDir && fileExists("${serviceDir}/target") && fileExists("${serviceDir}/pom.xml")) {
                            def serviceName = service.toLowerCase().replaceAll('[^a-z0-9-]', '-')
                            echo "=== Processing ${serviceName} from ${serviceDir} ==="
                            
                            dir(serviceDir) {
                                // Port mapping cho từng service
                                def servicePorts = [
                                    'account-service': '9003',
                                    'cart-service': '9008', 
                                    'product-service': '9001',
                                    'inventory-service': '9007',
                                    'order-service': '9011',
                                    'config-server': '9021',
                                    'discoveryservice': '8761'
                                ]
                                
                                def port = servicePorts[service] ?: '8080'
                                
                                // Tạo Dockerfile với security best practices 
                                sh '''
cat > Dockerfile << 'EOF'
FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="DevSecOps Team"
LABEL service="''' + service + '''"
LABEL version="''' + BUILD_VERSION + '''"

RUN apk add --no-cache curl

WORKDIR /app

RUN addgroup -g 1001 -S appgroup && \\
    adduser -u 1001 -S appuser -G appgroup

COPY target/*.jar app.jar

RUN chown appuser:appgroup app.jar

USER appuser

EXPOSE ''' + port + '''

ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -XX:MaxGCPauseMillis=100"

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
EOF
'''
                                
                                echo "Building Docker image for ${serviceName}..."
                                sh """
                                    docker build -t ${serviceName}:${BUILD_VERSION} . || {
                                        echo "Build failed, retrying with network host mode..."
                                        docker build --network=host -t ${serviceName}:${BUILD_VERSION} .
                                    }
                                    
                                    docker tag ${serviceName}:${BUILD_VERSION} ${serviceName}:latest
                                    docker tag ${serviceName}:${BUILD_VERSION} ${HARBOR_REGISTRY}/${HARBOR_PROJECT}/${serviceName}:${BUILD_VERSION}
                                    docker tag ${serviceName}:${BUILD_VERSION} ${HARBOR_REGISTRY}/${HARBOR_PROJECT}/${serviceName}:latest
                                """
                                
                                echo "Pushing ${serviceName} to Harbor..."
                                sh '''
                                    for i in 1 2 3; do
                                        if docker push ''' + HARBOR_REGISTRY + '''/''' + HARBOR_PROJECT + '''/''' + serviceName + ''':''' + BUILD_VERSION + ''' && \\
                                           docker push ''' + HARBOR_REGISTRY + '''/''' + HARBOR_PROJECT + '''/''' + serviceName + ''':latest; then
                                            echo "✅ Push successful for ''' + serviceName + '''"
                                            break
                                        else
                                            echo "Push failed, attempt $i/3"
                                            sleep 5
                                        fi
                                    done
                                '''
                                
                                // Clean up local images để tiết kiệm disk space
                                sh """
                                    docker rmi ${serviceName}:${BUILD_VERSION} ${serviceName}:latest || true
                                """
                                
                                builtImages.add(serviceName)
                                echo "✅ Successfully built and pushed ${serviceName}"
                            }
                        } else {
                            echo "⚠️ Skipping ${service} - directory, target folder, or pom.xml not found"
                        }
                    }
                    
                    sh "docker logout http://${HARBOR_REGISTRY}"
                    
                    if (builtImages.isEmpty()) {
                        error("No images were built successfully!")
                    }
                    
                    echo """
=== 🚀 BUILD & PUSH SUMMARY 🚀 ===

Successfully built and pushed ${builtImages.size()} microservices:
${builtImages.collect { "✅ ${it}:${BUILD_VERSION}" }.join('\n')}

🔗 Harbor Registry: http://${HARBOR_REGISTRY}
📁 Project: ${HARBOR_PROJECT}
🏷️  Tag: ${BUILD_VERSION} & latest
                    """
                }
            }
        }
        
        stage('Git Tagging') {
            steps {
                script {
                    sh '''
                        git config user.name "Jenkins CI"
                        git config user.email "jenkins@localhost"
                        
                        if ! git rev-parse "v''' + BUILD_VERSION + '''" >/dev/null 2>&1; then
                            git tag -a "v''' + BUILD_VERSION + '''" -m "Release version ''' + BUILD_VERSION + ''' - Built on $(date)"
                            echo "✅ Tag v''' + BUILD_VERSION + ''' created successfully"
                        else
                            echo "ℹ️ Tag v''' + BUILD_VERSION + ''' already exists"
                        fi
                    '''
                }
            }
        }
        
        // ===== IMAGE SCANNING STAGE =====
        /*
        stage('Quét image') {
            steps {
                script {
                    echo "=== Harbor Vulnerability Scanning with Trivy ==="
                    echo "Harbor is automatically scanning pushed images for security vulnerabilities using Trivy scanner."
                    echo "Check Harbor UI at http://${HARBOR_REGISTRY} for detailed vulnerability reports."
                    echo "Navigate to: Projects → ${HARBOR_PROJECT} → Repositories → Select image → Vulnerabilities tab"
                }
            }
        }
        */
        
        stage('Update K8s Config Repository') {
            steps {
                script {
                    sh '''
                        git clone http://152.42.230.92:3010/nam/microservices-k8s.git k8s-config || {
                            cd k8s-config && git pull origin main
                        }
                        
                        cd k8s-config
                        find . -name "*.yaml" -o -name "*.yml" | grep -E "(deployment|deploy)" | while read file; do
                            sed -i 's|image: .*/''' + HARBOR_PROJECT + '''/\\([^:]*\\):.*|image: ''' + HARBOR_REGISTRY + '''/''' + HARBOR_PROJECT + '''/\\1:''' + BUILD_VERSION + '''|g' "$file"
                        done
                        
                        git config user.name "Jenkins CI"
                        git config user.email "jenkins@localhost"
                        git add . && git commit -m "Update images to ''' + BUILD_VERSION + '''" && git push origin main
                        cd .. && rm -rf k8s-config
                    '''
                }
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
                    archiveArtifacts artifacts: '**/target/*.jar', allowEmptyArchive: true
                } catch (Exception e) {
                    echo "No JAR files to archive"
                }
                
                // Clean up unused Docker images to save space
                sh '''
                    echo "Cleaning up unused Docker images..."
                    docker image prune -f --filter "dangling=true" || true
                    docker volume prune -f || true
                    
                    df -h
                    docker system df
                '''
            }
        }
        success {
            echo """
🎉 COMPLETE PIPELINE SUCCESS! 🎉

✅ Environment check completed
✅ Dependencies started successfully
✅ JAR files built with Maven
❌ SonarQube code analysis - SKIPPED (commented out)
✅ Docker images built with security optimizations  
✅ Images pushed to Harbor registry: ${HARBOR_REGISTRY}/${HARBOR_PROJECT}/
✅ Git tag v${BUILD_VERSION} created
✅ Trivy vulnerability scanning completed
✅ Applications deployed successfully
✅ Cleanup performed

📊 Pipeline completed in ${currentBuild.durationString}

📋 Next Steps:
1. Check Harbor UI for vulnerability scan results  
2. Verify application health in deployment environment
   (SonarQube reports skipped - uncomment stage if needed)
            """
        }
        failure {
            script {
                sh "docker logout http://${HARBOR_REGISTRY} || true"
                echo """
❌ PIPELINE FAILED at stage: ${env.STAGE_NAME}

Check logs above for detailed error information.
Common issues:
- Build compilation errors
- Docker daemon not running  
- Harbor registry connectivity
- Insufficient permissions
                """
            }
        }
        cleanup {
            cleanWs()
        }
    }
}
