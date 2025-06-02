# Use Eclipse Temurin JDK 21 base image (Alpine version for minimal size)
FROM eclipse-temurin:21.0.7_6-jdk

# Create a directory for the app
WORKDIR /app

# Copy the JAR file into the container
ARG JAR_FILE=target/campaign-manager.jar
COPY ${JAR_FILE} app.jar

# Expose port 8080 (Spring Boot default)
EXPOSE 8080

# Run the JAR
ENTRYPOINT ["java", "-jar", "/app/app.jar"]