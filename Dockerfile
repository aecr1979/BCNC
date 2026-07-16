# syntax=docker/dockerfile:1

############################
# Stage 1: Build with Maven
############################
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

# Download dependencies first to leverage Docker layer caching
COPY pom.xml ./
COPY mvnw ./mvnw
COPY .mvn/ ./.mvn/
RUN chmod +x ./mvnw \
    && ./mvnw -B -e dependency:go-offline || ./mvnw -B -e dependency:resolve

# Copy the rest of the sources and build the runnable jar
COPY src ./src
RUN ./mvnw -B -e clean package -DskipTests

############################
# Stage 2: Runtime with JRE
############################
FROM eclipse-temurin:21-jre
WORKDIR /app

# Run as a non-root user for better security
RUN groupadd -r spring && useradd -r -g spring spring

# Copy the built artifact from the build stage
COPY --from=build /workspace/target/*.jar app.jar

USER spring

# Spring Boot 4 / Actuator-friendly: expose the app port
EXPOSE 8080

# Use container-aware defaults (reads application.yaml from classpath)
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
