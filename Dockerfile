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

# Build the application
RUN ./gradlew build --no-daemon -x test

# STAGE 2: Create the final production image
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Copy the executable JAR from the 'builder' stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Copy migration scripts (Flyway will auto-run these on startup)
COPY src/main/resources/db/migration/ /app/db/migration/

# Create directory for static resources
RUN mkdir -p /app/static

# Expose the port the application listens on
EXPOSE 8080

# Flyway will automatically run migrations when the app starts
CMD ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]