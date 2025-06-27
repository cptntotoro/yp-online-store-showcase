FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app

COPY pom.xml .
COPY payment-service/pom.xml ./payment-service/
COPY payment-service/src ./payment-service/src/

# Устанавливаем родительский POM в локальный репозиторий Maven
RUN mvn install -N -DskipTests

RUN mvn -f payment-service/pom.xml clean package -DskipTests

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=builder /app/payment-service/target/payment-service.jar app.jar

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
