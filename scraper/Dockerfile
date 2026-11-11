FROM openjdk:17-slim
WORKDIR /app
COPY target/scraper-1.0.0.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
