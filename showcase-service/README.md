# Сервис витрины товаров

Содержит контроллеры для данных и управления UI приложения, а также позволяет управлять пользователями, товарами, корзиной товаров и заказами.
Сообщается с сервисом оплаты с помощью PaymentServiceClient. В случае отсутствия соединения отображает соответствующие уведомления в UI приложения.

Содержит кеш товаров и корзин в Redis.

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
Приложение сервиса будет доступен по адресу: http://localhost:8080/products.

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

3. Убедитесь, что у вас запущен Docker Desktop и соберите сервис (компиляция + тесты):
```
mvn clean install -pl showcase-service
```

1. Запустите сервис:
```
java -jar showcase-service/target/showcase-service.jar --spring.profiles.active=prod
```

Приложение сервиса будет доступен по адресу: http://localhost:8080/products.
