FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /app
RUN apt-get update && apt-get install -y maven && apt-get clean && rm -rf /var/lib/apt/lists/*
COPY pom.xml ./
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests -B

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8001
ENTRYPOINT ["java", "-XX:+ExitOnOutOfMemoryError", "-XX:+UseContainerSupport", "-jar", "/app/app.jar"]