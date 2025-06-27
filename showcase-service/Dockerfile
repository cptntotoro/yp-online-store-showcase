FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app

COPY pom.xml .
COPY showcase-service/pom.xml ./showcase-service/
COPY showcase-service/src ./showcase-service/src/

# Устанавливаем родительский POM в локальный репозиторий Maven
RUN mvn install -N -DskipTests

RUN mvn -f showcase-service/pom.xml clean package -DskipTests

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=builder /app/showcase-service/target/showcase-service.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]