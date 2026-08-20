FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY target/plant-service-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-XX:+ExitOnOutOfMemoryError", "-XX:+UseContainerSupport", "-jar", "/app/app.jar"]