// pipeline {
//     agent any
//
//     tools {
//         maven 'Maven'
//     }
//
//     environment {
//         BUILD_VERSION = "v.${BUILD_NUMBER}"
//         JAVA_HOME = "/opt/java/openjdk"
//         HARBOR_REGISTRY = "152.42.230.92:8082"
//         HARBOR_PROJECT = "doan_devsecops"
//     }
//
//     stages {
//         stage('Environment Check') {
//             steps {
//                 sh '''
//                     echo "=== Build Environment ==="
//                     echo "JAVA_HOME: $JAVA_HOME"
//                     java -version
//
//                     echo "=== Maven Version ==="
//                     mvn -version
//
//                     echo "=== Project Structure ==="
//                     find . -maxdepth 2 -name "pom.xml" -exec dirname {} \\; | sort
//
//                     echo "=== Harbor Registry Config ==="
//                     echo "HARBOR_REGISTRY: $HARBOR_REGISTRY"
//                     echo "HARBOR_PROJECT: $HARBOR_PROJECT"
//                 '''
//             }
//         }
//
//         stage('Detect Changed Services') {
//             steps {
//                 script {
//                     echo "=== Detecting changes by comparing with the previous commit ==="
//
//                     def changedFiles = sh(script: "git diff --name-only HEAD~1 HEAD", returnStdout: true).trim().split('\n')
//
//                     echo "Changed files: ${changedFiles.join(', ')}"
//
//                     def backendServices = ['account-service', 'cart-service', 'product-service', 'inventory-service', 'order-service', 'config-server', 'discoveryservice', 'common-dto']
//                     def changedBackendServices = []
//
//                     backendServices.each { svc ->
//                         if (changedFiles.any { it.startsWith("${svc}/") || it == "${svc}/pom.xml" }) {
//                             changedBackendServices.add(svc)
//                         }
//                     }
//
//                     def frontendChanged = changedFiles.any { it.startsWith("shop/") || it == "shop/package.json" }
//                     def jenkinsFileChanged = changedFiles.any { it == "Jenkinsfile" }
//
//                     env.CHANGED_SERVICES = changedBackendServices.join(',')
//                     env.FRONTEND_CHANGED = frontendChanged.toString()
//                     env.JENKINSFILE_CHANGED = jenkinsFileChanged.toString()
//
//                     echo "Detected changed backend services: ${env.CHANGED_SERVICES ?: 'None'}"
//                     echo "Frontend changed? ${env.FRONTEND_CHANGED}"
//                     echo "Jenkinsfile changed? ${env.JENKINSFILE_CHANGED}"
//                 }
//             }
//         }
//
//         stage('Start Dependencies') {
//             when {
//                 anyOf {
//                     expression { return env.CHANGED_SERVICES?.trim() }
//                     expression { return env.FRONTEND_CHANGED == 'true' }
//                     expression { return env.JENKINSFILE_CHANGED == 'true' }
//                 }
//             }
//             steps {
//                 script {
//                     echo "=== Starting Infrastructure via Docker Compose ==="
//                     sh 'docker-compose up -d || true'
//                     echo "Waiting for dependencies to start..."
//                     sleep 30
//                 }
//             }
//         }
//
//         stage('Build JAR Files') {
//             when {
//                 expression { return env.CHANGED_SERVICES?.trim() }
//             }
//             steps {
//                 script {
//                     def services = env.CHANGED_SERVICES.split(',')
//
//                     if (services.contains('common-dto')) {
//                         echo "🔧 Building common-dto library first..."
//                         dir('common-dto') {
//                             sh 'mvn clean install -DskipTests=true'
//                         }
//                     }
//
//                     def servicesToBuild = services.findAll { it != 'common-dto' }
//                     servicesToBuild.each { service ->
//                         echo "Building JAR for ${service}..."
//                         if (fileExists("${service}/pom.xml")) {
//                             dir(service) {
//                                 sh 'mvn clean package -DskipTests=true'
//                                 def jarExists = sh(script: "ls target/*.jar 2>/dev/null || echo 'no-jar'", returnStdout: true).trim()
//                                 if (jarExists == 'no-jar') {
//                                     error("JAR file not found for ${service}")
//                                 } else {
//                                     echo "✅ JAR built successfully for ${service}: ${jarExists}"
//                                 }
//                             }
//                         }
//                     }
//                 }
//             }
//         }
//
//         stage('Quét mã nguồn') {
//             when {
//                 expression { return env.CHANGED_SERVICES?.trim() }
//             }
//             steps {
//                 script {
//                     def pomDirs = env.CHANGED_SERVICES.split(',')
//                     pomDirs.each { pomDir ->
//                         if (pomDir && pomDir != 'common-dto') {
//                             dir(pomDir) {
//                                 withSonarQubeEnv('SonarQube') {
//                                     def projectName = pomDir.replaceAll('^\\./', '')
//                                     sh """
//                                         echo "=== Running SonarQube Analysis for ${projectName} ==="
//                                         mvn compile sonar:sonar \\
//                                             -DskipTests=true \\
//                                             -Dsonar.projectKey=microservices-${projectName} \\
//                                             -Dsonar.projectName="Microservices ${projectName}" \\
//                                             -Dsonar.projectVersion=${BUILD_VERSION} || echo "SonarQube failed for ${projectName}, continuing..."
//                                     """
//                                 }
//                             }
//                         }
//                     }
//                 }
//             }
//         }
//
//         stage('Build và Push Docker Images') {
//             when {
//                 anyOf {
//                     expression { return env.CHANGED_SERVICES?.trim() }
//                     expression { return env.FRONTEND_CHANGED == 'true' }
//                     expression { return env.JENKINSFILE_CHANGED == 'true' }
//                 }
//             }
//             steps {
//                 script {
//                     withCredentials([usernamePassword(credentialsId: 'harbor-login-robot',
//                                                     passwordVariable: 'HARBOR_PASSWORD',
//                                                     usernameVariable: 'HARBOR_USERNAME')]) {
//                         sh "echo \"\${HARBOR_PASSWORD}\" | docker login http://${HARBOR_REGISTRY} -u \"\${HARBOR_USERNAME}\" --password-stdin"
//                     }
//
//                     if (env.FRONTEND_CHANGED == 'true') {
//                         echo "=== Building and pushing Frontend ==="
//                         dir('shop') {
//                             sh 'npm ci && npm run build'
//                             sh """
//                                 cat > Dockerfile <<EOF
// FROM nginx:alpine
// COPY build/ /usr/share/nginx/html/
// EXPOSE 80
// CMD ["nginx", "-g", "daemon off;"]
// EOF
//                                 docker build -t frontend:${BUILD_VERSION} .
//                                 docker tag frontend:${BUILD_VERSION} ${HARBOR_REGISTRY}/${HARBOR_PROJECT}/frontend:${BUILD_VERSION}
//                                 docker tag frontend:${BUILD_VERSION} ${HARBOR_REGISTRY}/${HARBOR_PROJECT}/frontend:latest
//                                 docker push ${HARBOR_REGISTRY}/${HARBOR_PROJECT}/frontend:${BUILD_VERSION}
//                                 docker push ${HARBOR_REGISTRY}/${HARBOR_PROJECT}/frontend:latest
//                             """
//                         }
//                     }
//
//                     if (env.CHANGED_SERVICES?.trim()) {
//                         def servicesToBuild = env.CHANGED_SERVICES.split(',')
//                         servicesToBuild.each { service ->
//                             if (service != 'common-dto' && fileExists("${service}/target")) {
//                                 echo "=== Building and pushing Docker image for ${service} ==="
//                                 dir(service) {
//                                     def servicePorts = [
//                                         'account-service': '9003', 'cart-service': '9008', 'product-service': '9001',
//                                         'inventory-service': '9007', 'order-service': '9011', 'config-server': '9021',
//                                         'discoveryservice': '8761'
//                                     ]
//                                     def port = servicePorts[service] ?: '8080'
//
//                                     sh """
//                                         cat > Dockerfile <<EOF
// FROM eclipse-temurin:21-jre-alpine
// COPY target/*.jar /app.jar
// EXPOSE ${port}
// ENTRYPOINT ["java", "-jar", "/app.jar"]
// EOF
//                                         docker build -t ${service}:${BUILD_VERSION} .
//                                         docker tag ${service}:${BUILD_VERSION} ${HARBOR_REGISTRY}/${HARBOR_PROJECT}/${service}:${BUILD_VERSION}
//                                         docker tag ${service}:${BUILD_VERSION} ${HARBOR_REGISTRY}/${HARBOR_PROJECT}/${service}:latest
//                                         docker push ${HARBOR_REGISTRY}/${HARBOR_PROJECT}/${service}:${BUILD_VERSION}
//                                         docker push ${HARBOR_REGISTRY}/${HARBOR_PROJECT}/${service}:latest
//                                     """
//                                 }
//                             }
//                         }
//                     }
//
//                     sh "docker logout http://${HARBOR_REGISTRY}"
//                 }
//             }
//         }
//
//         stage('Git Tagging') {
//             steps {
//                 script {
//                     withCredentials([string(credentialsId: 'gitea-token', variable: 'GIT_TOKEN')]) {
//                         sh '''
//                             git config user.name "Jenkins CI"
//                             git config user.email "jenkins@localhost"
//
//                             TAG_NAME="update_''' + BUILD_VERSION + '''"
//
//                             if ! git rev-parse "$TAG_NAME" >/dev/null 2>&1; then
//                                 git tag -a "$TAG_NAME" -m "Build #''' + BUILD_NUMBER + '''"
//                                 git push https://nam:${GIT_TOKEN}@github.com/SungThiUt2002/MainDA2.git "$TAG_NAME"
//                             fi
//                         '''
//                     }
//                 }
//             }
//         }
//
//         stage('Update K8s Config Repository') {
//             when {
//                 anyOf {
//                     expression { return env.CHANGED_SERVICES?.trim() }
//                     expression { return env.FRONTEND_CHANGED == 'true' }
//                     expression { return env.JENKINSFILE_CHANGED == 'true' }
//                 }
//             }
//             steps {
//                 script {
//                     sh """
//                         rm -rf k8s-config
//                         git clone http://152.42.230.92:3010/nam/microservices-k8s.git k8s-config
//                         cd k8s-config
//
//                         SERVICES=\$(echo "\${CHANGED_SERVICES}" | tr ',' ' ')
//
//                         for service in \$SERVICES; do
//                             if [ "\$service" != "common-dto" ]; then
//                                 find . -name "*.yaml" | grep -i "\$service" | while read file; do
//                                     sed -i "s|image: .*/''' + HARBOR_PROJECT + '''/\\([^:]*\\):.*|image: ''' + HARBOR_REGISTRY + '''/''' + HARBOR_PROJECT + '''/\\1:''' + BUILD_VERSION + '''|g" "\$file"
//                                 done
//                             fi
//                         done
//
//                         if [ "\${FRONTEND_CHANGED}" = "true" ]; then
//                             find . -name "*.yaml" | grep -i "frontend" | while read file; do
//                                 sed -i "s|image: .*/''' + HARBOR_PROJECT + '''/frontend:.*|image: ''' + HARBOR_REGISTRY + '''/''' + HARBOR_PROJECT + '''/frontend:''' + BUILD_VERSION + '''|g" "\$file"
//                             done
//                         fi
//
//                         git config user.name "Jenkins CI"
//                         git config user.email "jenkins@localhost"
//                         git add . && git commit -m "Update images to ''' + BUILD_VERSION + '''" && git push origin main
//                         cd .. && rm -rf k8s-config
//                     """
//                 }
//             }
//         }
//     }
//
//     post {
//         always {
//             script {
//                 echo "=== Pipeline Cleanup ==="
//                 sh 'pkill -f "discoveryservice-.*.jar" || true'
//                 sh 'pkill -f "config-server-.*.jar" || true'
//                 try {
//                     archiveArtifacts artifacts: '**/target/*.jar', allowEmptyArchive: true
//                 } catch (Exception e) {
//                     echo "No JAR files to archive"
//                 }
//                 sh '''
//                     echo "Cleaning up unused Docker images..."
//                     docker image prune -f --filter "dangling=true" || true
//                     docker volume prune -f || true
//                 '''
//             }
//         }
//     }
// }


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
        ISTIO_INGRESS_IP = "167.172.88.205"   // External IP của istio-ingressgateway
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
                    def jenkinsFileChanged = changedFiles.any { it == "Jenkinsfile" }

                    env.CHANGED_SERVICES = changedBackendServices.join(',')
                    env.FRONTEND_CHANGED = frontendChanged.toString()
                    env.JENKINSFILE_CHANGED = jenkinsFileChanged.toString()

                    echo "Detected changed backend services: ${env.CHANGED_SERVICES ?: 'None'}"
                    echo "Frontend changed? ${env.FRONTEND_CHANGED}"
                    echo "Jenkinsfile changed? ${env.JENKINSFILE_CHANGED}"
                }
            }
        }

        stage('Start Dependencies') {
            when {
                anyOf {
                    expression { return env.CHANGED_SERVICES?.trim() }
                    expression { return env.FRONTEND_CHANGED == 'true' }
                    expression { return env.JENKINSFILE_CHANGED == 'true' }
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
                    expression { return env.JENKINSFILE_CHANGED == 'true' }
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
                                # Tạo Dockerfile kèm default.conf
                                cat > Dockerfile <<EOF
FROM nginx:alpine
COPY build/ /usr/share/nginx/html/
COPY default.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
EOF

                                # Viết default.conf cho Nginx
                                cat > default.conf <<EOCONF
server {
    listen 80;

    location / {
        root   /usr/share/nginx/html;
        index  index.html;
        try_files \$uri /index.html;
    }

    # Proxy API call tới Istio Ingress Gateway
    location /api/ {
        proxy_pass http://${ISTIO_INGRESS_IP};
    }
}
EOCONF

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
            steps {
                script {
                    withCredentials([string(credentialsId: 'gitea-token', variable: 'GIT_TOKEN')]) {
                        sh '''
                            git config user.name "Jenkins CI"
                            git config user.email "jenkins@localhost"

                            TAG_NAME="update_''' + BUILD_VERSION + '''"

                            if ! git rev-parse "$TAG_NAME" >/dev/null 2>&1; then
                                git tag -a "$TAG_NAME" -m "Build #''' + BUILD_NUMBER + '''"
                                git push https://nam:${GIT_TOKEN}@github.com/SungThiUt2002/MainDA2.git "$TAG_NAME"
                            fi
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
                    expression { return env.JENKINSFILE_CHANGED == 'true' }
                }
            }
            steps {
                script {
                    sh """
                        rm -rf k8s-config
                        git clone http://152.42.230.92:3010/nam/microservices-k8s.git k8s-config
                        cd k8s-config

                        SERVICES=\$(echo "\${CHANGED_SERVICES}" | tr ',' ' ')

                        for service in \$SERVICES; do
                            if [ "\$service" != "common-dto" ]; then
                                find . -name "*.yaml" | grep -i "\$service" | while read file; do
                                    sed -i "s|image: .*/''' + HARBOR_PROJECT + '''/\\([^:]*\\):.*|image: ''' + HARBOR_REGISTRY + '''/''' + HARBOR_PROJECT + '''/\\1:''' + BUILD_VERSION + '''|g" "\$file"
                                done
                            fi
                        done

                        if [ "\${FRONTEND_CHANGED}" = "true" ]; then
                            find . -name "*.yaml" | grep -i "frontend" | while read file; do
                                sed -i "s|image: .*/''' + HARBOR_PROJECT + '''/frontend:.*|image: ''' + HARBOR_REGISTRY + '''/''' + HARBOR_PROJECT + '''/frontend:''' + BUILD_VERSION + '''|g" "\$file"
                            done
                        fi

                        git config user.name "Jenkins CI"
                        git config user.email "jenkins@localhost"
                        git add . && git commit -m "Update images to ''' + BUILD_VERSION + '''" && git push origin main
                        cd .. && rm -rf k8s-config
                    """
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
    }
}
