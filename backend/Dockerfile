# ============================================================
# Backend Dockerfile - Multi-stage build
# ============================================================
# This Dockerfile uses a multi-stage build to create a small,
# optimized Docker image for our Spring Boot application.
#
# Stage 1 (build): Uses Maven to compile and package the app
# Stage 2 (run): Uses a slim JRE image to run the JAR file
#
# Multi-stage builds keep the final image small because the
# build tools (Maven, JDK) aren't included in the final image.
# ============================================================

# --- Stage 1: Build the application ---
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy the POM file first to cache dependencies
# Docker caches layers, so if pom.xml hasn't changed,
# it won't re-download all dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn package -DskipTests -B

# --- Stage 2: Run the application ---
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy the JAR file from the build stage
COPY --from=build /app/target/*.jar app.jar

# Expose port 8080
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
