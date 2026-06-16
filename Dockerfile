# ── Stage 1: Build & Cache Dependencies ────────────────────────
# Java version to use
FROM eclipse-temurin:17-jdk-alpine AS builder
# Standard, always use (main dir)
WORKDIR /app

COPY pom.xml .
COPY src ./src
COPY mvnw mvnw.cmd ./
COPY .mvn .mvn/
#COPY pom.xml ./

# Compile and package the application, skipping tests for speed
RUN ./mvnw clean package -DskipTests

# Stage 2: Create the final lightweight runtime image
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy only the compiled JAR from the builder stage
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]


##move pom/mvnw/mvn.xml to /app
#COPY mvnw mvnw.cmd ./
#COPY .mvn .mvn/
#COPY pom.xml ./
##Change file permissions to execute
#RUN chmod +x mvnw && ./mvnw dependency:go-offline -B
#
## ── Stage 2: Package and Extract Layers ────────────────────────
## move src
#COPY src ./src
## Package the maven
#RUN ./mvnw package -DskipTests -B
#
## Extract the jar into distinct layers
#RUN java -Djarmode=layertools -jar target/*.jar extract
#
## ── Stage 3: Minimal Production Runtime ─────────────────────────
#FROM eclipse-temurin:17-jre-alpine AS runtime
#
## Security: Maintain your non-root user setup
#RUN addgroup -S spring && adduser -S spring -G spring
#USER spring:spring
#WORKDIR /app
#
## Copy layers individually (Dependencies rarely change, making builds instant)
##COPY --from=builder /app/dependencies/ ./
##COPY --from=builder /app/spring-boot-loader/ ./
##COPY --from=builder /app/snapshot-dependencies/ ./
##COPY --from=builder /app/application/ ./
#
## Port number to use
#EXPOSE 8080
#
## Production-safe settings from your original file
#ENV SPRING_DOCKER_COMPOSE_ENABLED=false
#
## Use the official launcher instead of a direct fat jar execution
##ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
##ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher"]
#ENTRYPOINT ["java, "-jar", ]