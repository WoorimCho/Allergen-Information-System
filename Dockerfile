# ── Stage 1: Build & Cache Dependencies ────────────────────────
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app

# Install Maven
RUN apk add --no-cache maven

# Copy pom.xml first (for better layer caching)
COPY pom.xml ./

# Download dependencies (this layer is cached unless pom.xml changes)
RUN mvn dependency:go-offline -B || true

# Copy source code
COPY src ./src

# Build the application (skip tests for faster builds)
RUN mvn clean package -DskipTests

# ── Stage 2: Create Minimal Runtime Image ──────────────────────
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Create non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy only the JAR from builder stage
COPY --from=builder /app/target/*.jar app.jar

# Expose application port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]

