# Bank Cards Management API

Тестовое задание — backend для управления банковскими картами.

## Функционал
- JWT аутентификация (register/login).
- Роли: ADMIN (полный доступ), USER (только свои карты).
- Создание карт (ADMIN, генерация номера Luhn + уникальность).
- Просмотр (пагинация, /my для USER, все для ADMIN).
- Переводы между своими картами (проверки баланса, статуса, expired, владельца).
- Пополнение баланса.
- Блокировка/активация.
- Шифрование номера карты (AES).
- Маскировка в ответах.
- Обработка ошибок (JSON).

## Запуск
- Docker Compose: `docker-compose up --build` (Postgres + app).
- Локально: Postgres запущен, `mvn spring-boot:run`.

## API
- /api/auth/register — регистрация (первый пользователь ADMIN).
- /api/auth/login — логин (JWT токен).
- Bearer token в Header для protected.

- Карты:
  - POST /api/cards/{userId} — создание (ADMIN).
  - GET /api/cards — все (ADMIN, пагинация).
  - GET /api/cards/my — свои (USER).
  - GET /api/cards/user/{userId} — чужие (ADMIN).
  - POST /api/cards/{cardId}/deposit — пополнение.
  - POST /api/cards/transfer — перевод.
  - PATCH /api/cards/block/{cardId} — блокировка.
  - PATCH /api/cards/activate/{cardId} — активация (ADMIN).
  - DELETE /api/cards/{cardId} — удаление (ADMIN).

Swagger: http://localhost:8080/swagger-ui.html

## Тесты
JUnit + Mockito для CardService.

Repo: https://github.com/Danisus/bank-rest-test
