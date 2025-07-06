[![Java CI with Maven](https://github.com/cptntotoro/yp-online-store-showcase/actions/workflows/maven.yml/badge.svg)](https://github.com/cptntotoro/yp-online-store-showcase/actions/workflows/maven.yml) [![Coverage Status](https://coveralls.io/repos/github/cptntotoro/yp-online-store-showcase/badge.svg)](https://coveralls.io/github/cptntotoro/yp-online-store-showcase)

# Приложение "Витрина онлайн-магазина"
Приложение на реактивном стеке: Java 21, Spring Boot (WebFlux, R2DBC), PostgreSQL, Redis, Lombok, Mapstruct, Maven, Docker, Thymeleaf, HTML, CSS, JavaScript

## О проекте
Веб-приложение представляет собой витрину товаров. 
Пользователь может положить товар в корзину, оформить, оплатить и отменить заказ. 
Также пользователь может наполнить витрину товаров новыми товарами.

По умолчанию баланс счета пользователя равен 15 000 рублей (payment.default-balance).
Если пользователь отменяет оплаченный заказ, стоимость заказа возвращается на счет пользователя. 
Если средств на балансе счета пользователя недостаточно, оплата не совершается.

### Технические особенности
- Предоставление актуальных данных для разных пользователей реализовано с помощью управления идентификатором пользователя через куки с помощью WebFilter. 
- Для отображения актуальной стоимости корзины и реализации UI элементов согласно её состоянию применен @ControllerAdvice. 
- Для уменьшения нагрузки на приложение состояние корзин пользователей и список товаров кешируются с помощью Redis.
- Для исполнения @Testcontainers тестов сервиса витрины товаров (showcase-service) с помощью Docker реализована установка Docker в тестовый Docker-контейнер.

### UI особенности

- Если сервис оплаты недоступен, а пользователь хочет оплатить или отменить оплаченный(!) заказ, кнопки на соответствующих страницах блокируются и отображается сообщение о проблеме
- Если недостаточно средств на счете для оплаты заказа, пользователь получает сообщение об ошибке.

## Демонстрация

![](demo.gif)

## 🚀 Как запускать приложение 

### В Docker

#### Только тесты для обоих сервисов
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

3. Убедитесь, что у вас запущен Docker Desktop и соберите оба сервиса (+тесты):
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
