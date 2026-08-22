# syntax=docker/dockerfile:1.7

# === Stage 1: Build with Maven ===
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace

# Copy pom.xml and download dependencies (cached separately from source)
COPY pom.xml ./
RUN --mount=type=cache,target=/root/.m2 mvn -q -B dependency:go-offline

# Copy source code and build the JAR
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -q -B -DskipTests package \
    && cp target/*.jar /workspace/app.jar

# === Stage 2: Runtime with JRE ===
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/app.jar app.jar

EXPOSE 8082
ENTRYPOINT ["java","-jar","/app/app.jar"]
