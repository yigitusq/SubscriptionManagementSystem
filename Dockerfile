FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app

COPY pom.xml .

COPY api-gateway/pom.xml ./api-gateway/
COPY customer-service/pom.xml ./customer-service/
COPY subscription-service/pom.xml ./subscription-service/
COPY payment-service/pom.xml ./payment-service/
COPY notification-service/pom.xml ./notification-service/
COPY discovery-service/pom.xml ./discovery-service/

RUN mvn dependency:go-offline

COPY . .

# 5. Projeyi derle
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-focal
WORKDIR /app

ARG JAR_FILE
COPY --from=builder /app/customer-service/target/*.jar /app/app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]