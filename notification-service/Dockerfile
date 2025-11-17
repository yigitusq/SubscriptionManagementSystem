FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app
COPY notification-service/. .
RUN mvn clean package -DskipTests -Dmaven.wagon.http.retryHandler.count=3 -Dmaven.wagon.http.retryHandler.requestSentEnabled=true

FROM eclipse-temurin:17-jre-focal
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]
