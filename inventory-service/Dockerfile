## Giai đoạn build với Java 21
#FROM eclipse-temurin:21-jdk AS build
#
#WORKDIR /app
#
## Copy Maven files
#COPY pom.xml .
#COPY .mvn .mvn
#COPY mvnw .
#
## Download dependencies
#RUN ./mvnw dependency:go-offline -B
#
## Copy source code
#COPY src src
#
## Build the application
#RUN ./mvnw clean package -DskipTests
#
## Giai đoạn runtime với Java 21 JRE
#FROM eclipse-temurin:21-jre
#
#WORKDIR /app
#
## Copy the built jar
#COPY --from=build /app/target/*.jar app.jar
#
## Expose port
#EXPOSE 9007
#
## Health check
#HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
#  CMD curl -f http://localhost:9007/actuator/health || exit 1
#
## Run the application
#ENTRYPOINT ["java", "-jar", "app.jar"]
