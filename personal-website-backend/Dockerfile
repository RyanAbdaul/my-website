# syntax=docker/dockerfile:1

# ---------- build stage ----------
# Matches the Maven wrapper's bundled Maven (3.9.16) and the project's Java 17 target.git
FROM maven:3.9.16-eclipse-temurin-17 AS builder
WORKDIR /build

# Layer the POM first so dependency resolution is cached across rebuilds.
COPY pom.xml ./
RUN mvn dependency:go-offline -B

# Source code last, so a code change does not invalidate the dependency cache.
COPY src ./src
RUN mvn package -DskipTests -B

# ---------- runtime stage ----------
FROM eclipse-temurin:17-jre AS runner
WORKDIR /app

# Non-root user for security.
RUN groupadd -r appuser && useradd -r -g appuser appuser

# Copy the Spring Boot fat JAR (the *.jar.original file is not matched by *.jar).
COPY --from=builder /build/target/*.jar /app/app.jar

# Writable uploads directory (mounted as a volume at runtime).
RUN mkdir -p /app/uploads && chown -R appuser:appuser /app

USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]