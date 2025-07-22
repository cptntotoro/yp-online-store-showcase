FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app

COPY pom.xml .
COPY auth-service/pom.xml ./auth-service/
COPY auth-service/src ./auth-service/src/

# Устанавливаем родительский POM в локальный репозиторий Maven
RUN mvn install -N -DskipTests

RUN mvn -f auth-service/pom.xml clean package -DskipTests

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=builder /app/auth-service/target/auth-service.jar app.jar

EXPOSE 9000
ENTRYPOINT ["java", "-jar", "app.jar"]
