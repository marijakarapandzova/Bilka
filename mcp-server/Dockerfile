FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY target/mcp-server-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8085
ENTRYPOINT ["java", "-XX:+ExitOnOutOfMemoryError", "-XX:+UseContainerSupport", "-jar", "/app/app.jar"]
