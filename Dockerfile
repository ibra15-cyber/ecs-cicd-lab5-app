# STAGE 1: Build the application with Gradle
FROM gradle:8.4.0-jdk21-jammy AS builder

WORKDIR /app

# Copy the Gradle wrapper and its associated files
COPY gradlew .
COPY gradle ./gradle

# Copy the build script and the source code
COPY build.gradle settings.gradle ./
COPY src ./src

# Make the Gradle wrapper script executable
RUN chmod +x ./gradlew

# Build the application. The `build` command will create the executable JAR file.
RUN ./gradlew build --no-daemon

# STAGE 2: Create the final production image
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Install PostgreSQL client for database health checks and migrations
RUN apt-get update && \
    apt-get install -y postgresql-client curl && \
    rm -rf /var/lib/apt/lists/*

# Copy the executable JAR from the 'builder' stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Copy migration scripts from source
COPY src/main/resources/db/migration/ /app/db/migration/

# Copy migration and utility scripts
COPY migrate.sh ./
COPY wait-for-db.sh ./
RUN chmod +x migrate.sh wait-for-db.sh

# Create directory for static resources
RUN mkdir -p /app/static

# Expose the port the application listens on
EXPOSE 8080

# Define the command to run the application when the container starts
CMD ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]