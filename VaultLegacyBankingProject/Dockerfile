# Build stage
FROM eclipse-temurin:21-jdk as builder

WORKDIR /build

# Copy the jar file
COPY target/vault-legacy-banking-1.0-SNAPSHOT.jar application.jar

# Run stage
FROM gcr.io/distroless/java21-debian12:nonroot

LABEL maintainer="Vault Legacy"
LABEL application="Vault Legacy Banking"
LABEL description="Spring Boot application for Vault Legacy Banking Platform"

WORKDIR /app

# Copy the jar file from builder stage
COPY --from=builder /build/application.jar ./app.jar

EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]