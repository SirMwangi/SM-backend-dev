# Build stage
FROM eclipse-temurin:25-jdk AS build
WORKDIR /app

# Copy Maven wrapper files and pom.xml
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Ensure wrapper script is executable and download dependencies
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Copy project source code and build jar
COPY src ./src
RUN ./mvnw clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:25-jre
WORKDIR /app

# Run as non-root user for security
RUN groupadd -r appgroup && useradd -r -g appgroup appuser

# Copy jar from build stage
COPY --from=build --chown=appuser:appgroup /app/target/*.jar app.jar

USER appuser

# Expose port (default 8080)
EXPOSE 8080

ENV PORT=8080

ENTRYPOINT ["java", "-jar", "app.jar"]
