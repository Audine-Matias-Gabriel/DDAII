# --- Build stage ---
# Compila el jar dentro de la imagen: no hace falta correr "mvn package" antes.
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src ./src
RUN mvn -q -B package -DskipTests

# --- Run stage ---
# openjdk:17-slim ya no existe en Docker Hub; eclipse-temurin es el reemplazo.
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /app/target/backend-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
