FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /app
COPY pom.xml .
RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*
COPY . .
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8001
ENTRYPOINT ["java", "-XX:+ExitOnOutOfMemoryError", "-XX:+UseContainerSupport", "-jar", "/app/app.jar"]