[![Java CI with Maven](https://github.com/cptntotoro/yp-online-store-showcase/actions/workflows/maven.yml/badge.svg)](https://github.com/cptntotoro/yp-online-store-showcase/actions/workflows/maven.yml) [![Coverage Status](https://coveralls.io/repos/github/cptntotoro/yp-online-store-showcase/badge.svg)](https://coveralls.io/github/cptntotoro/yp-online-store-showcase)

# Приложение "Витрина онлайн-магазина"
Приложение на реактивном стеке: Java 21, Spring Boot (WebFlux, R2DBC), PostgreSQL, Lombok, Mapstruct, Maven, Docker, Thymeleaf, HTML, CSS, JavaScript

## О проекте
Веб-приложение представляет собой витрину товаров.
Пользователь может положить товар в корзину, оформить, оплатить и отменить заказ.
Также пользователь может наполнить витрину товаров новыми товарами.

Предоставление актуальных данных для разных пользователей реализовано с помощью управления идентификатором пользователя через куки в OncePerRequestFilter в Spring Web.

Для отображения актуальной стоимости корзины и реализации UI элементов согласно её состоянию применен @ControllerAdvice. 

Для уменьшения нагрузки на приложение состояние корзины кешируется с помощью Spring Cache.  

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

1. Разверните БД согласно application.properties и application-test.properties в отдельном приложении или среде разработки

2. Соберите проект:
```
mvn clean package
```

3. Запустите приложение:
```
java -jar target/yp-online-store.jar --spring.profiles.active=prod
```

Приложение будет доступно по адресу: http://localhost:8080/products.
