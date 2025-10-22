# --- AŞAMA 1: PROJEYİ DERLEME (AŞÇI) ---
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app

# 1. Önce SADECE ana pom.xml'i kopyala
COPY pom.xml .

# 2. Tüm modüllerin pom.xml'lerini kopyala
COPY api-gateway/pom.xml ./api-gateway/
COPY customer-service/pom.xml ./customer-service/
COPY subscription-service/pom.xml ./subscription-service/
COPY payment-service/pom.xml ./payment-service/
COPY notification-service/pom.xml ./notification-service/
COPY common-dto/pom.xml ./common-dto/
# BENİM HATAM BURADAYDI, DÜZELTİLDİ ("discovery-server" -> "discovery-service")
COPY discovery-service/pom.xml ./discovery-service/

# 3. Sadece bağımlılıkları indir
RUN mvn dependency:go-offline

# 4. Şimdi projenin geri kalan tüm kaynak kodlarını kopyala
COPY . .

# 5. Projeyi derle
RUN mvn clean package -DskipTests

# --- AŞAMA 2: PROJEYİ ÇALIŞTIRMA (GARSON) ---
FROM eclipse-temurin:17-jre-focal
WORKDIR /app

ARG JAR_FILE
COPY --from=builder /app/${JAR_FILE} app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]