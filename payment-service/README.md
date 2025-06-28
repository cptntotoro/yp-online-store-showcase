# Сервис оплаты

Подключен к БД приложения. 

Позволяет получить баланс счета пользователя, обработать оплату заказа и получить возврат средств за заказ.

## 🚀 Как запускать

### В Docker

#### Только тесты
```
docker-compose --profile test up --build --abort-on-container-exit payment-test
```
Если ответ 0, все тесты прошли успешно

#### Только продакшен
```
docker-compose --profile prod up -d payment-service
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

2. Соберите сервис (компиляция + тесты):
```
mvn clean install -pl payment-service
```

3. Запустите сервис:
```
java -jar payment-service/target/payment-service.jar --spring.profiles.active=dev
```

Сервис будет доступен по адресу: http://localhost:8081.
