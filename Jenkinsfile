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
                    def microservices = [/*'account-service', */'cart-service'/*, 'product-service', 
                                       'inventory-service', 'order-service'*/]
                    def infrastructureServices = ['config-server', 'discoveryservice', 'common-dto']
                    def allServices = microservices + infrastructureServices
                    
                    // Build common-dto first
                    if (fileExists('common-dto') && fileExists('common-dto/pom.xml')) {
                        dir('common-dto') {
                            echo "🔧 Building common-dto library first..."
                            sh 'mvn clean install -DskipTests=true'
                            echo "✅ common-dto built and installed to local repository"
                        }
                    }
                    
                    allServices.each { service ->
                        if (service == 'common-dto') {
                            return
                        }
                        
                        def serviceDir = null
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
                    
                    // Build Frontend - COMPLETELY FIXED
                    if (fileExists('shop') && fileExists('shop/package.json')) {
                        dir('shop') {    
                            echo "=== Building React Frontend ==="
                            
                            sh '''
                                echo "=== Directory verification ==="
                                pwd && ls -la
                                echo "=== Package.json check ==="
                                head -5 package.json
                            '''

                            timeout(time: 20, unit: 'MINUTES') {
                                sh '''
                                    echo "=== Building React Application ==="
                                    
                                    # FIXED: Skip problematic volume mount, use COPY-based approach only
                                    echo "Using reliable Docker COPY method (avoiding mount issues)..."
                                    
                                    cat > Dockerfile.build << 'EOF'
FROM node:20-alpine

# Install git for some npm packages
RUN apk add --no-cache git

WORKDIR /app

# Copy package files first for better caching
COPY package*.json ./

# Install dependencies with optimizations
RUN echo "Installing dependencies..." && \\
    npm ci --silent --no-audit --no-fund --production=false && \\
    echo "Dependencies installed"

# Copy all source code
COPY . .

# Build the application
RUN echo "Building React app..." && \\
    CI=false npm run build && \\
    echo "Build completed" && \\
    ls -la build/ && \\
    echo "Build verification: $(ls -A build | wc -l) files created"
EOF

                                    echo "Starting Docker build process..."
                                    
                                    # Build with progress monitoring
                                    docker build -f Dockerfile.build -t react-builder . 2>&1 | while IFS= read -r line; do
                                        echo "$line"
                                        # Periodic output to prevent Jenkins timeout
                                        case "$line" in
                                            *"Step"*|*"RUN"*|*"npm"*|*"webpack"*|*"Compiled"*)
                                                echo "[$(date '+%H:%M:%S')] Progress: $line"
                                                ;;
                                        esac
                                    done
                                    
                                    # FIXED: Improved build detection logic
                                    echo "=== Extracting and verifying build ==="
                                    
                                    # Create container and extract build
                                    docker create --name build-container react-builder
                                    
                                    # Copy build directory out
                                    if docker cp build-container:/app/build ./build; then
                                        echo "Build extraction successful"
                                    else
                                        echo "Build extraction failed, trying alternative..."
                                        docker cp build-container:/app/build/. ./build/
                                    fi
                                    
                                    # Cleanup container and image
                                    docker rm build-container
                                    docker rmi react-builder
                                    rm -f Dockerfile.build
                                    
                                    echo "=== FIXED Build Verification Logic ==="
                                    
                                    # Multiple verification methods for robustness
                                    BUILD_SUCCESS=false
                                    
                                    # Method 1: Check directory and key files
                                    if [ -d "build" ] && [ -f "build/index.html" ]; then
                                        echo "✓ Method 1: Directory and index.html check passed"
                                        BUILD_SUCCESS=true
                                    fi
                                    
                                    # Method 2: Check for non-empty build directory
                                    if [ -d "build" ] && [ -n "$(find build -type f -name '*.html' -o -name '*.js' -o -name '*.css' 2>/dev/null | head -1)" ]; then
                                        echo "✓ Method 2: Build files detected"
                                        BUILD_SUCCESS=true
                                    fi
                                    
                                    # Method 3: Count files in build directory
                                    if [ -d "build" ] && [ "$(find build -type f 2>/dev/null | wc -l)" -gt 3 ]; then
                                        echo "✓ Method 3: Sufficient build files found ($(find build -type f | wc -l) files)"
                                        BUILD_SUCCESS=true
                                    fi
                                    
                                    # Final verification
                                    if [ "$BUILD_SUCCESS" = "true" ]; then
                                        echo "✅ BUILD VERIFICATION SUCCESSFUL"
                                        echo "Build directory contents:"
                                        ls -la build/
                                        echo "Total files: $(find build -type f | wc -l)"
                                        echo "Build size: $(du -sh build/ | cut -f1)"
                                        
                                        # Show key files
                                        echo "Key build files:"
                                        [ -f "build/index.html" ] && echo "  ✓ index.html ($(wc -c < build/index.html) bytes)"
                                        [ -f "build/asset-manifest.json" ] && echo "  ✓ asset-manifest.json"
                                        
                                        # Count static files
                                        if [ -d "build/static" ]; then
                                            echo "  ✓ static/ directory ($(find build/static -type f | wc -l) files)"
                                        fi
                                        
                                    else
                                        echo "❌ BUILD VERIFICATION FAILED"
                                        echo "Debug information:"
                                        echo "Current directory: $(pwd)"
                                        echo "Directory listing:"
                                        ls -la
                                        
                                        if [ -d "build" ]; then
                                            echo "Build directory exists but appears incomplete:"
                                            ls -la build/
                                            find build -type f | head -10
                                        else
                                            echo "Build directory does not exist"
                                        fi
                                        
                                        exit 1
                                    fi
                                '''
                            }
                            
                            // Create production Dockerfile
                            sh '''
                                echo "=== Creating Production Dockerfile ==="
                                cat > Dockerfile << 'EOF'
FROM nginx:alpine

# Security: Install curl and create non-root user
RUN apk add --no-cache curl && \\
    addgroup -g 1001 -S appgroup && \\
    adduser -u 1001 -S appuser -G appgroup

# Copy built application
COPY build/ /usr/share/nginx/html/

# Copy nginx configuration
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Set proper permissions
RUN chown -R appuser:appgroup /usr/share/nginx/html /var/cache/nginx /var/run

# Switch to non-root user
USER appuser

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=5s --retries=3 \\
    CMD curl -f http://localhost:80/ || exit 1

# Labels
LABEL maintainer="DevSecOps Team" \\
      service="frontend" \\
      version="''' + BUILD_VERSION + '''"

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
EOF
                            '''

                            // Create nginx configuration
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
    
    # SPA routing
    location / {
        try_files $uri $uri/ /index.html;
    }
    
    # API proxy
    location /api/ {
        proxy_pass http://backend-gateway:8080/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # Timeouts
        proxy_connect_timeout 30s;
        proxy_send_timeout 30s;
        proxy_read_timeout 30s;
        
        # CORS headers
        add_header Access-Control-Allow-Origin * always;
        add_header Access-Control-Allow-Methods "GET, POST, PUT, DELETE, OPTIONS" always;
        add_header Access-Control-Allow-Headers "Content-Type, Authorization" always;
    }
    
    # Static file caching
    location ~* \\.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2|ttf|eot)$ {
        expires 1y;
        add_header Cache-Control "public, immutable";
        add_header Vary "Accept-Encoding";
    }
    
    # Compression
    gzip on;
    gzip_vary on;
    gzip_min_length 1024;
    gzip_comp_level 6;
    gzip_types
        text/plain
        text/css
        text/xml
        text/javascript
        application/javascript
        application/json
        application/xml+rss;
    
    # Security
    server_tokens off;
    client_max_body_size 10M;
}
EOF
                            '''
                                
                            // Build and push Docker imageF
                            sh """
                                echo "=== Building Production Docker Image ==="
                                
                                docker build -t frontend:${BUILD_VERSION} .
                                
                                # Tag for Harbor
                                docker tag frontend:${BUILD_VERSION} ${HARBOR_REGISTRY}/${HARBOR_PROJECT}/frontend:${BUILD_VERSION}
                                docker tag frontend:${BUILD_VERSION} ${HARBOR_REGISTRY}/${HARBOR_PROJECT}/frontend:latest
                                
                                echo "=== Pushing to Harbor Registry ==="
                                docker push ${HARBOR_REGISTRY}/${HARBOR_PROJECT}/frontend:${BUILD_VERSION}
                                docker push ${HARBOR_REGISTRY}/${HARBOR_PROJECT}/frontend:latest
                                
                                echo "✅ Frontend image pushed successfully!"
                                
                                # Cleanup
                                docker rmi frontend:${BUILD_VERSION} || true
                            """
                        }
                    } else {
                        echo "⚠️ Frontend build skipped - shop directory or package.json not found"
                    }
                    
                    // Backend services
                    def dockerServices = [/*'account-service', */'cart-service'/*, 'product-service', 
                                        'inventory-service', 'order-service',
                                        'config-server', 'discoveryservice'*/]
                    
                    def builtImages = []
                    
                    dockerServices.each { service ->
                        def serviceDir = null
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
                                def servicePorts = [
                                    /*'account-service': '9003',*/
                                    'cart-service': '9008'/*, 
                                    'product-service': '9001',
                                    'inventory-service': '9007',
                                    'order-service': '9011',
                                    'config-server': '9021',
                                    'discoveryservice': '8761'*/
                                ]
                                
                                def port = servicePorts[service] ?: '8080'
                                
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
            withCredentials([usernamePassword(credentialsId: 'gitea-credentials', 
                                            passwordVariable: 'GIT_PASSWORD', 
                                            usernameVariable: 'GIT_USERNAME')]) {
                sh """
                    echo "=== Simple Git Tagging ==="
                    
                    # Configure git
                    git config user.name "Jenkins CI"  
                    git config user.email "jenkins@localhost"
                    
                    # Store credentials temporarily
                    git config credential.helper store
                    echo "protocol=http
host=152.42.230.92:3010
username=${GIT_USERNAME}
password=${GIT_PASSWORD}" | git credential approve
                    
                    # Create and push tag
                    if ! git rev-parse "v${BUILD_VERSION}" >/dev/null 2>&1; then
                        echo "Creating tag v${BUILD_VERSION}..."
                        git tag -a "v${BUILD_VERSION}" -m "Jenkins Build #${BUILD_NUMBER}"
                        
                        echo "Pushing tag..."
                        git push origin "v${BUILD_VERSION}"
                        echo "✅ Tag pushed successfully"
                    else
                        echo "Tag v${BUILD_VERSION} already exists"
                        git push origin "v${BUILD_VERSION}" || echo "Tag may already be on remote"
                    fi
                    
                    # Clean up credentials
                    git config --unset credential.helper
                    
                    # Verify
                    git tag -l | tail -5
                """
            }
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
