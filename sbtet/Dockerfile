FROM maven:3.9.6-eclipse-temurin-17 AS builder
# Builder stage
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app

# Copy pom and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy source and build jar
COPY src ./src
RUN mvn clean package -Dmaven.test.skip=true

# Run stage
FROM eclipse-temurin:17-jre-focal
WORKDIR /app

# Copy built jar
COPY --from=builder /app/target/*.jar app.jar

# Expose port Spring Boot uses
EXPOSE 8080

# Start the app
ENTRYPOINT ["java", "-jar", "app.jar"]