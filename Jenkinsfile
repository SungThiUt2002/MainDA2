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
                    // Phân loại services theo loại
                    def microservices = ['account-service', 'cart-service', 'product-service', 
                                       'inventory-service', 'order-service']
                    def infrastructureServices = ['config-server', 'discoveryservice', 'common-dto']
                    def allServices = microservices + infrastructureServices
                    
                    // Build common-dto trước
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
                        
                        // Kiểm tra cả hai định dạng tên thư mục
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
                    if (fileExists('shop') && fileExists('shop/package.json')) {
                        dir('shop') {    
                            echo "=== Building React Frontend ==="
                            
                            // Xác nhận cấu trúc thư mục
                            sh '''
                                echo "=== Verifying shop directory structure ==="
                                pwd
                                ls -la
                                echo "=== Checking package.json content ==="
                                head -10 package.json
                            '''
                            
                            // Build React app với error handling tốt hơn
                            sh '''
                                echo "=== Building React Application ==="
                                
                                # Tạo Dockerfile tạm để build
                                cat > Dockerfile.build << 'EOF'
FROM node:18-alpine
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build
EOF

                                # Build React app
                                docker build -f Dockerfile.build -t temp-react-builder .
                                
                                # Extract build artifacts
                                docker run --rm -v "$(pwd)/build:/output" temp-react-builder sh -c "cp -r /app/build/* /output/"
                                
                                # Cleanup temp builder image
                                docker rmi temp-react-builder
                                
                                echo "=== Build completed, checking output ==="
                                ls -la build/ || echo "Build directory not found"
                            '''
                            
                            // Tạo production Dockerfile
                            sh '''
                                cat > Dockerfile << 'EOF'
FROM nginx:alpine

# Install curl for health check
RUN apk add --no-cache curl

# Copy built React app
COPY build/ /usr/share/nginx/html/

# Copy custom nginx configuration
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Create non-root user for security
RUN addgroup -g 1001 -S nginx-group && \\
    adduser -u 1001 -S nginx-user -G nginx-group

# Set proper permissions
RUN chown -R nginx-user:nginx-group /usr/share/nginx/html

EXPOSE 80

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \\
    CMD curl -f http://localhost/ || exit 1

# Labels for better management
LABEL maintainer="DevSecOps Team"
LABEL service="frontend"
LABEL version="''' + BUILD_VERSION + '''"

CMD ["nginx", "-g", "daemon off;"]
EOF
                            '''

                            // Tạo nginx configuration
                            sh '''
                                cat > nginx.conf << 'EOF'
server {
    listen 80;
    server_name _;
    
    root /usr/share/nginx/html;
    index index.html;
    
    # Security headers
    add_header X-Frame-Options "SAMEORIGIN" always;
    add_header X-XSS-Protection "1; mode=block" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header Referrer-Policy "no-referrer-when-downgrade" always;
    add_header Content-Security-Policy "default-src 'self' http: https: data: blob: 'unsafe-inline'" always;
    
    # Handle React Router (SPA)
    location / {
        try_files $uri $uri/ /index.html;
    }
    
    # API proxy to backend services
    location /api/ {
        proxy_pass http://api-gateway:8080/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # CORS headers
        add_header Access-Control-Allow-Origin * always;
        add_header Access-Control-Allow-Methods "GET, POST, PUT, DELETE, OPTIONS" always;
        add_header Access-Control-Allow-Headers "DNT,User-Agent,X-Requested-With,If-Modified-Since,Cache-Control,Content-Type,Range,Authorization" always;
        
        # Handle preflight requests
        if ($request_method = 'OPTIONS') {
            add_header Access-Control-Allow-Origin * always;
            add_header Access-Control-Allow-Methods "GET, POST, PUT, DELETE, OPTIONS" always;
            add_header Access-Control-Allow-Headers "DNT,User-Agent,X-Requested-With,If-Modified-Since,Cache-Control,Content-Type,Range,Authorization" always;
            add_header Access-Control-Max-Age 1728000;
            add_header Content-Type 'text/plain; charset=utf-8';
            add_header Content-Length 0;
            return 204;
        }
    }
    
    # Static assets caching
    location ~* \\.(js|css|png|jpg|jpeg|gif|ico|svg)$ {
        expires 1y;
        add_header Cache-Control "public, immutable";
    }
    
    # Gzip compression
    gzip on;
    gzip_vary on;
    gzip_min_length 1024;
    gzip_proxied any;
    gzip_comp_level 6;
    gzip_types
        text/plain
        text/css
        text/xml
        text/javascript
        application/javascript
        application/xml+rss
        application/json;
}
EOF
                            '''
                                
                            // Build Docker image và push
                            sh """
                                echo "=== Building Docker image for frontend ==="
                                docker build -t frontend:${BUILD_VERSION} . || {
                                    echo "Docker build failed, checking for common issues..."
                                    ls -la
                                    ls -la build/ || echo "Build directory missing"
                                    exit 1
                                }
                                
                                # Tag images
                                docker tag frontend:${BUILD_VERSION} ${HARBOR_REGISTRY}/${HARBOR_PROJECT}/frontend:${BUILD_VERSION}
                                docker tag frontend:${BUILD_VERSION} ${HARBOR_REGISTRY}/${HARBOR_PROJECT}/frontend:latest
                                
                                echo "=== Pushing frontend images to Harbor ==="
                                docker push ${HARBOR_REGISTRY}/${HARBOR_PROJECT}/frontend:${BUILD_VERSION}
                                docker push ${HARBOR_REGISTRY}/${HARBOR_PROJECT}/frontend:latest
                                
                                echo "✅ Frontend built and pushed successfully"
                                
                                # Cleanup local images to save space
                                docker rmi frontend:${BUILD_VERSION} || true
                            """
                        }
                    } else {
                        echo "⚠️ Frontend build skipped - shop directory or package.json not found"
                        
                        // Debug information
                        sh '''
                            echo "=== Current directory structure ==="
                            pwd
                            ls -la
                            
                            echo "=== Looking for shop-related directories ==="
                            find . -name "*shop*" -type d 2>/dev/null || echo "No shop directories found"
                            
                            echo "=== Looking for package.json files ==="
                            find . -name "package.json" 2>/dev/null || echo "No package.json files found"
                        '''
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
✅ SonarQube code analysis completed
✅ Docker images built with security optimizations  
✅ Images pushed to Harbor registry: ${HARBOR_REGISTRY}/${HARBOR_PROJECT}/
✅ Git tag v${BUILD_VERSION} created
✅ Applications deployed successfully
✅ Cleanup performed

📊 Pipeline completed in ${currentBuild.durationString}

📋 Next Steps:
1. Check Harbor UI for vulnerability scan results  
2. Verify application health in deployment environment
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
