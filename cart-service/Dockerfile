#FROM openjdk:21-jdk-slim
#
## Tạo user không phải root
#RUN groupadd -r appuser && useradd -r -g appuser appuser
#
## Copy application
#COPY target/cart-service-*.jar app.jar
#
## Set permissions
#RUN chown appuser:appuser app.jar
#
## Switch to non-root user
#USER appuser
#
#EXPOSE 8081
#ENTRYPOINT ["java", "-jar", "/app.jar"]