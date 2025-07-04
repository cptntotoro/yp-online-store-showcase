[![Java CI with Maven](https://github.com/cptntotoro/yp-online-store-showcase/actions/workflows/maven.yml/badge.svg)](https://github.com/cptntotoro/yp-online-store-showcase/actions/workflows/maven.yml) [![Coverage Status](https://coveralls.io/repos/github/cptntotoro/yp-online-store-showcase/badge.svg)](https://coveralls.io/github/cptntotoro/yp-online-store-showcase)

# Приложение "Витрина онлайн-магазина"
Приложение на реактивном стеке: Java 21, Spring Boot (WebFlux, R2DBC), PostgreSQL, Redis, Lombok, Mapstruct, Maven, Docker, Thymeleaf, HTML, CSS, JavaScript

## О проекте
Веб-приложение представляет собой витрину товаров.
Пользователь может положить товар в корзину, оформить, оплатить и отменить заказ.
Также пользователь может наполнить витрину товаров новыми товарами.

Предоставление актуальных данных для разных пользователей реализовано с помощью управления идентификатором пользователя через куки с помощью WebFilter.

Для отображения актуальной стоимости корзины и реализации UI элементов согласно её состоянию применен @ControllerAdvice. 

Для уменьшения нагрузки на приложение состояние корзины и список товаров кешируются с помощью Redis.  

## Демонстрация

![](demo.gif)

## 🚀 Как запускать приложение 

### В Docker

#### Только тесты
```
docker-compose --profile test up --build --abort-on-container-exit
```
Если ответ 0, все тесты прошли успешно

#### Только продакшен
```
docker-compose --profile prod up -d
```
Приложение будет доступно по адресу: http://localhost:8080/products.

### Остановка контейнеров

Чтобы остановить все запущенные контейнеры, выполните:

```
docker-compose down
```

Если вы хотите также удалить тома (volumes), используйте:

```
docker-compose down -v
```

### Локально

1. Убедитесь, что у вас установлен PostgreSQL на порту 5432

2. Убедитесь, что у вас устновлен Redis на порту 6379

3. Убедитесь, что у вас запущен Docker Desktop и соберите проект:
```
mvn clean package
```
4. Запустите сервис витрины товаров:
```
java -jar showcase-service/target/showcase-service.jar --spring.profiles.active=prod
```
5. (Опционально) запустите сервис оплаты:
```
java -jar payment-service/target/payment-service.jar --spring.profiles.active=prod
```

Приложение будет доступно по адресу: http://localhost:8080/products.
