# Use OpenJDK 21 lightweight base image
FROM openjdk:21-jdk-slim

# Set working directory
WORKDIR /app

# Copy built jar from target/ (Maven) or build/libs/ (Gradle)
COPY target/myapp-0.0.1-SNAPSHOT.jar app.jar

# Expose Spring Boot default port
EXPOSE 8080

# Run the app
ENTRYPOINT ["java", "-jar", "app.jar"]