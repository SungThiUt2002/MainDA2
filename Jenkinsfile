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
        GIT_REPO_URL = "http://152.42.230.92:3010"
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
        
        stage('Detect Changed Services') {
            steps {
                script {
                    echo "=== Detecting changes by comparing with the previous commit ==="

                    // Sửa đổi: Sử dụng lệnh git diff HEAD~1 HEAD
                    def changedFiles = sh(script: "git diff --name-only HEAD~1 HEAD", returnStdout: true).trim().split('\n')

                    echo "Changed files: ${changedFiles.join(', ')}"

                    def backendServices = ['account-service', 'cart-service', 'product-service', 'inventory-service', 'order-service', 'config-server', 'discoveryservice', 'common-dto']
                    def changedBackendServices = []

                    backendServices.each { svc ->
                        if (changedFiles.any { it.startsWith("${svc}/") || it == "${svc}/pom.xml" }) {
                            changedBackendServices.add(svc)
                        }
                    }

                    def frontendChanged = changedFiles.any { it.startsWith("shop/") || it == "shop/package.json" }

                    env.CHANGED_SERVICES = changedBackendServices.join(',')
                    env.FRONTEND_CHANGED = frontendChanged.toString()

                    echo "Detected changed backend services: ${env.CHANGED_SERVICES ?: 'None'}"
                    echo "Frontend changed? ${env.FRONTEND_CHANGED}"
                }
            }
        }

        stage('Start Dependencies') {
            when {
                anyOf {
                    expression { return env.CHANGED_SERVICES?.trim() }
                    expression { return env.FRONTEND_CHANGED == 'true' }
                }
            }
            steps {
                script {
                    echo "=== Starting Infrastructure via Docker Compose ==="
                    sh 'docker-compose up -d || true'
                    echo "Waiting for dependencies to start..."
                    sleep 30
                }
            }
        }

        stage('Build JAR Files') {
            when {
                expression { return env.CHANGED_SERVICES?.trim() }
            }
            steps {
                script {
                    def services = env.CHANGED_SERVICES.split(',')

                    if (services.contains('common-dto')) {
                        echo "🔧 Building common-dto library first..."
                        dir('common-dto') {
                            sh 'mvn clean install -DskipTests=true'
                        }
                    }

                    def servicesToBuild = services.findAll { it != 'common-dto' }
                    servicesToBuild.each { service ->
                        echo "Building JAR for ${service}..."
                        if (fileExists("${service}/pom.xml")) {
                            dir(service) {
                                sh 'mvn clean package -DskipTests=true'
                                def jarExists = sh(script: "ls target/*.jar 2>/dev/null || echo 'no-jar'", returnStdout: true).trim()
                                if (jarExists == 'no-jar') {
                                    error("JAR file not found for ${service}")
                                } else {
                                    echo "✅ JAR built successfully for ${service}: ${jarExists}"
                                }
                            }
                        }
                    }
                }
            }
        }

        stage('Quét mã nguồn') {
            when {
                expression { return env.CHANGED_SERVICES?.trim() }
            }
            steps {
                script {
                    def pomDirs = env.CHANGED_SERVICES.split(',')
                    pomDirs.each { pomDir ->
                        if (pomDir && pomDir != 'common-dto') {
                            dir(pomDir) {
                                withSonarQubeEnv('SonarQube') {
                                    def projectName = pomDir.replaceAll('^\\./', '')
                                    sh """
                                        echo "=== Running SonarQube Analysis for ${projectName} ==="
                                        mvn compile sonar:sonar \\
                                            -DskipTests=true \\
                                            -Dsonar.projectKey=microservices-${projectName} \\
                                            -Dsonar.projectName="Microservices ${projectName}" \\
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
            when {
                anyOf {
                    expression { return env.CHANGED_SERVICES?.trim() }
                    expression { return env.FRONTEND_CHANGED == 'true' }
                }
            }
            steps {
                script {
                    withCredentials([usernamePassword(credentialsId: 'harbor-login-robot',
                                                    passwordVariable: 'HARBOR_PASSWORD',
                                                    usernameVariable: 'HARBOR_USERNAME')]) {
                        sh "echo \"\${HARBOR_PASSWORD}\" | docker login http://${HARBOR_REGISTRY} -u \"\${HARBOR_USERNAME}\" --password-stdin"
                    }

                    if (env.FRONTEND_CHANGED == 'true') {
                        echo "=== Building and pushing Frontend ==="
                        dir('shop') {
                            sh 'npm ci && npm run build'
                            sh """
                                cat > Dockerfile <<EOF
FROM nginx:alpine
COPY build/ /usr/share/nginx/html/
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
EOF
                                docker build -t frontend:${BUILD_VERSION} .
                                docker tag frontend:${BUILD_VERSION} ${HARBOR_REGISTRY}/${HARBOR_PROJECT}/frontend:${BUILD_VERSION}
                                docker tag frontend:${BUILD_VERSION} ${HARBOR_REGISTRY}/${HARBOR_PROJECT}/frontend:latest
                                docker push ${HARBOR_REGISTRY}/${HARBOR_PROJECT}/frontend:${BUILD_VERSION}
                                docker push ${HARBOR_REGISTRY}/${HARBOR_PROJECT}/frontend:latest
                            """
                        }
                    }

                    if (env.CHANGED_SERVICES?.trim()) {
                        def servicesToBuild = env.CHANGED_SERVICES.split(',')
                        servicesToBuild.each { service ->
                            if (service != 'common-dto' && fileExists("${service}/target")) {
                                echo "=== Building and pushing Docker image for ${service} ==="
                                dir(service) {
                                    def servicePorts = [
                                        'account-service': '9003', 'cart-service': '9008', 'product-service': '9001',
                                        'inventory-service': '9007', 'order-service': '9011', 'config-server': '9021',
                                        'discoveryservice': '8761'
                                    ]
                                    def port = servicePorts[service] ?: '8080'

                                    sh """
                                        cat > Dockerfile <<EOF
FROM eclipse-temurin:21-jre-alpine
COPY target/*.jar /app.jar
EXPOSE ${port}
ENTRYPOINT ["java", "-jar", "/app.jar"]
EOF
                                        docker build -t ${service}:${BUILD_VERSION} .
                                        docker tag ${service}:${BUILD_VERSION} ${HARBOR_REGISTRY}/${HARBOR_PROJECT}/${service}:${BUILD_VERSION}
                                        docker tag ${service}:${BUILD_VERSION} ${HARBOR_REGISTRY}/${HARBOR_PROJECT}/${service}:latest
                                        docker push ${HARBOR_REGISTRY}/${HARBOR_PROJECT}/${service}:${BUILD_VERSION}
                                        docker push ${HARBOR_REGISTRY}/${HARBOR_PROJECT}/${service}:latest
                                    """
                                }
                            }
                        }
                    }

                    sh "docker logout http://${HARBOR_REGISTRY}"
                }
            }
        }

        stage('Git Tagging') {
            when {
                anyOf {
                    expression { return env.CHANGED_SERVICES?.trim() }
                    expression { return env.FRONTEND_CHANGED == 'true' }
                }
            }
            steps {
                script {
                    withCredentials([usernamePassword(credentialsId: 'gitea-credentials',
                                                    passwordVariable: 'GIT_PASSWORD',
                                                    usernameVariable: 'GIT_USERNAME')]) {
                        sh '''
                            echo "=== Simple Git Tagging ==="
                            
                            # Thiết lập git config
                            git config user.name "Jenkins CI"
                            git config user.email "jenkins@localhost"
                            
                            # Lấy URL gốc của repository và thêm credentials
                            REPO_URL=$(git config --get remote.origin.url)
                            
                            # Loại bỏ http:// hoặc https:// nếu có
                            CLEAN_URL=$(echo $REPO_URL | sed 's|^https\\?://||')
                            
                            # Tạo URL với credentials
                            AUTHENTICATED_URL="http://${GIT_USERNAME}:${GIT_PASSWORD}@${CLEAN_URL}"
                            
                            # Thiết lập remote với credentials
                            git remote set-url origin "$AUTHENTICATED_URL"
                            
                            # Kiểm tra xem tag đã tồn tại chưa
                            if ! git rev-parse "${BUILD_VERSION}" >/dev/null 2>&1; then
                                echo "Creating tag ${BUILD_VERSION}..."
                                git tag -a "${BUILD_VERSION}" -m "Jenkins Build #${BUILD_NUMBER}"
                                
                                # Push tag
                                echo "Pushing tag to repository..."
                                git push origin "${BUILD_VERSION}" || {
                                    echo "Failed to push tag, trying to fetch and retry..."
                                    git fetch origin
                                    git push origin "${BUILD_VERSION}"
                                }
                                
                                echo "✅ Tag pushed successfully"
                            else
                                echo "Tag ${BUILD_VERSION} already exists"
                            fi
                            
                            # Reset lại URL gốc để bảo mật
                            git remote set-url origin "$REPO_URL"
                        '''
                    }
                }
            }
        }

        stage('Update K8s Config Repository') {
            when {
                anyOf {
                    expression { return env.CHANGED_SERVICES?.trim() }
                    expression { return env.FRONTEND_CHANGED == 'true' }
                }
            }
            steps {
                script {
                    withCredentials([usernamePassword(credentialsId: 'gitea-credentials',
                                                    passwordVariable: 'GIT_PASSWORD',
                                                    usernameVariable: 'GIT_USERNAME')]) {
                        sh '''
                            echo "=== Updating K8s Configuration Repository ==="
                            
                            # Xóa thư mục k8s-config nếu tồn tại
                            rm -rf k8s-config
                            
                            # Clone repository với credentials
                            K8S_REPO_URL="http://${GIT_USERNAME}:${GIT_PASSWORD}@152.42.230.92:3010/nam/microservices-k8s.git"
                            git clone "$K8S_REPO_URL" k8s-config
                            
                            cd k8s-config
                            
                            # Cập nhật images cho các services đã thay đổi
                            for service in ${CHANGED_SERVICES//,/ }; do
                                if [ "$service" != "common-dto" ]; then
                                    echo "Updating images for service: $service"
                                    find . -name "*.yaml" | grep -i "$service" | while read file; do
                                        if [ -f "$file" ]; then
                                            sed -i "s|image: .*/''' + HARBOR_PROJECT + '''/\\([^:]*\\):.*|image: ''' + HARBOR_REGISTRY + '''/''' + HARBOR_PROJECT + '''/\\1:''' + BUILD_VERSION + '''|g" "$file"
                                            echo "Updated $file"
                                        fi
                                    done
                                fi
                            done
                            
                            # Cập nhật frontend nếu có thay đổi
                            if [ "${FRONTEND_CHANGED}" = "true" ]; then
                                echo "Updating frontend images"
                                find . -name "*.yaml" | grep -i "frontend" | while read file; do
                                    if [ -f "$file" ]; then
                                        sed -i "s|image: .*/''' + HARBOR_PROJECT + '''/frontend:.*|image: ''' + HARBOR_REGISTRY + '''/''' + HARBOR_PROJECT + '''/frontend:''' + BUILD_VERSION + '''|g" "$file"
                                        echo "Updated frontend in $file"
                                    fi
                                done
                            fi
                            
                            # Commit và push thay đổi
                            git config user.name "Jenkins CI"
                            git config user.email "jenkins@localhost"
                            
                            # Kiểm tra xem có thay đổi nào không
                            if git diff --quiet; then
                                echo "No changes to commit"
                            else
                                git add .
                                git commit -m "Update images to ''' + BUILD_VERSION + '''"
                                git push origin main
                                echo "✅ K8s configuration updated successfully"
                            fi
                            
                            # Cleanup
                            cd ..
                            rm -rf k8s-config
                        '''
                    }
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
                '''
            }
        }
        
        failure {
            echo "❌ Pipeline failed. Kiểm tra logs để xác định nguyên nhân."
        }
        
        success {
            echo "✅ Pipeline completed successfully!"
        }
    }
}
