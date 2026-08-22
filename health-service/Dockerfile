# Build stage
FROM eclipse-temurin:17-jdk-jammy as builder
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*
RUN echo "Starting Maven build..." && mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=builder /build/target/health-service-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-XX:+ExitOnOutOfMemoryError", "-XX:+UseContainerSupport", "-jar", "/app/app.jar"]
