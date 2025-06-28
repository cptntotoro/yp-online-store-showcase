# Сервис витрины товаров

Содержит контроллеры для отображения и управления UI приложения, а также позволяет управлять пользователями, товарами, корзиной товаров и заказами. 

## 🚀 Как запускать

### В Docker

#### Только тесты
```
docker-compose --profile test up --build --abort-on-container-exit showcase-test
```
Если ответ 0, все тесты прошли успешно

#### Только продакшен
```
docker-compose --profile prod up -d showcase-service
```
Сервис будет доступен по адресу: http://localhost:8081.

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
java -jar target/payment-service.jar --spring.profiles.active=dev
```

Приложение будет доступно по адресу: http://localhost:8080/products.
