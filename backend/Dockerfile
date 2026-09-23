# ========================================================
# Production Multi-Stage Dockerfile for Spring Boot Backend
# ========================================================
FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /app

# Copy Maven wrapper and POM first for layer caching
COPY pom.xml mvnw ./
COPY .mvn .mvn
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B || true

# Copy source code and build jar
COPY src ./src
RUN ./mvnw clean package -DskipTests -B

# Minimal runtime image
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

COPY --from=build /app/target/criminal-network-intelligence-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080
ENV PORT=8080

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
