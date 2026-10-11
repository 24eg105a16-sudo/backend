# ==============================================================================
# Multi-stage Dockerfile for Personal Expense Tracker Backend (Spring Boot 3.3.4, Java 21)
# ==============================================================================

# ------------------------------------------------------------------------------
# Stage 1: Build the Application
# ------------------------------------------------------------------------------
FROM maven:3.9-eclipse-temurin-21-alpine AS builder

WORKDIR /app

# Cache Maven dependencies layer
COPY pom.xml .
RUN mvn dependency:go-offline -B || true

# Copy application source code
COPY src ./src

# Package Spring Boot executable JAR (skipping unit tests during image build)
RUN mvn clean package -DskipTests -B

# ------------------------------------------------------------------------------
# Stage 2: Minimal Runtime Environment
# ------------------------------------------------------------------------------
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create non-root user and group for security hardening
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy compiled JAR artifact from builder stage
COPY --from=builder /app/target/*.jar app.jar

# Set correct ownership for application directory
RUN chown -R appuser:appgroup /app

# Switch to unprivileged user
USER appuser

# Expose Spring Boot server port (matches SERVER_PORT default 8080)
EXPOSE 8080

# Configure health check targeting public /api/health endpoint
HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
  CMD wget -q --spider http://localhost:8080/api/health || exit 1

# Container-aware JVM options and application entrypoint
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
