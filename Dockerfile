# Build Stage
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests -B

# Production Runtime Stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN addgroup -S klink && adduser -S klink -G klink
USER klink:klink

COPY --from=builder /app/target/*.jar app.jar

ENV SPRING_PROFILES_ACTIVE=prod \
    PORT=8080

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
