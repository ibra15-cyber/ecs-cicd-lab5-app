# STAGE 1: Build the application with Gradle
# We use a base image with both Gradle and JDK 21 to build the application.
FROM gradle:8.4.0-jdk21-jammy AS builder

# Set the working directory inside the container
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
# Use a lightweight JRE 21 base image from Eclipse Temurin.
# This image only contains the Java Runtime Environment, not the full JDK.
FROM eclipse-temurin:21-jre-jammy

# Set the working directory
WORKDIR /app

# Copy the executable JAR from the 'builder' stage into this new, minimal image.
# Note: Gradle places the JAR file in the 'build/libs' directory.
COPY --from=builder /app/build/libs/*.jar app.jar

# Expose the port the application listens on
EXPOSE 8080

# Define the command to run the application when the container starts
CMD ["java", "-jar", "app.jar"]
