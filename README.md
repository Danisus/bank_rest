# 💳 Bank Cards Management API

![Java](https://img.shields.io/badge/Java-17-orange?style=flat-square&logo=java)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3-green?style=flat-square&logo=springboot)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-blue?style=flat-square&logo=postgresql)
![Docker](https://img.shields.io/badge/Docker-ready-blue?style=flat-square&logo=docker)
![Liquibase](https://img.shields.io/badge/Liquibase-migrations-red?style=flat-square)

REST API для управления банковскими картами.
JWT-авторизация, роли ADMIN/USER, переводы между картами,
шифрование номеров карт по AES, миграции через Liquibase.

---

## ✨ Возможности

- 🔐 **JWT-аутентификация** — роли ADMIN и USER
- 💳 **Карты** — создание с генерацией номера по алгоритму Луна
- 🔒 **Шифрование** — номера карт хранятся в AES-зашифрованном виде
- 🙈 **Маскировка** — в ответах номер показывается как **** **** **** 1234
- 💸 **Переводы** — между своими картами с проверкой баланса и статуса
- 💰 **Пополнение** — депозит на карту
- 🚫 **Блокировка / активация** карт
- 📄 **Пагинация** в списках карт
- 🗄️ **Liquibase** — версионирование схемы БД
- 🧪 **Тесты** — unit-тесты CardService (JUnit 5 + Mockito)
- 📘 **Swagger** — интерактивная документация

---

## 🛠 Технологии

- **Java 17**
- **Spring Boot 3** (MVC, Security, Data JPA)
- **Spring Security + JWT**
- **PostgreSQL**
- **Liquibase**
- **AES-шифрование**
- **JUnit 5 + Mockito**
- **Swagger / OpenAPI**
- **Lombok**, **Maven**
- **Docker & Docker Compose**

---

## 🚀 Запуск

### С Docker (рекомендуется)

    git clone https://github.com/Danisus/bank_rest.git
    cd bank_rest
    docker-compose up --build

Приложение: http://localhost:8080
Swagger UI: http://localhost:8080/swagger-ui.html

### Локально

    mvn spring-boot:run

---

## 🔑 Аутентификация

Регистрация:

    POST /api/auth/register
    Content-Type: application/json

    {
      "username": "admin",
      "password": "password123"
    }

> ⚠️ Первый зарегистрированный пользователь получает роль ADMIN

Вход:

    POST /api/auth/login
    Content-Type: application/json

    {
      "username": "admin",
      "password": "password123"
    }

Токен передавать в заголовке:

    Authorization: Bearer <token>

---

## 📡 Эндпоинты

### Аутентификация

| Метод | Эндпоинт | Описание |
|-------|----------|----------|
| POST | /api/auth/register | Регистрация |
| POST | /api/auth/login | Вход, получение токена |

### Карты

| Метод | Эндпоинт | Описание | Доступ |
|-------|----------|----------|--------|
| POST | /api/cards/{userId} | Создать карту | ADMIN |
| GET | /api/cards | Все карты (пагинация) | ADMIN |
| GET | /api/cards/my | Свои карты | USER |
| GET | /api/cards/user/{userId} | Карты пользователя | ADMIN |
| POST | /api/cards/{cardId}/deposit | Пополнить карту | AUTH |
| POST | /api/cards/transfer | Перевод между картами | USER |
| PATCH | /api/cards/block/{cardId} | Заблокировать | AUTH |
| PATCH | /api/cards/activate/{cardId} | Активировать | ADMIN |
| DELETE | /api/cards/{cardId} | Удалить карту | ADMIN |

---

## 🧪 Тесты

    mvn test

Покрыто: CardService — создание, переводы, блокировка, депозит

---

## 📁 Структура проекта

    src/main/java/com/example/bankcards/
    ├── controller/     # REST-контроллеры
    ├── service/        # Бизнес-логика
    ├── repository/     # JPA-репозитории
    ├── entity/         # JPA-сущности (Card, User, Role, Status)
    ├── dto/            # DTO запросов и ответов
    ├── security/       # JWT-фильтр, UserDetailsService
    ├── config/         # SecurityConfig
    └── util/           # JwtUtil, CardNumberConverter, SecurityUtil

    src/main/resources/db/migration/
    ├── changelog-master.yaml
    ├── V1__create_users_table.yaml
    └── V2__create_cards_table.yaml

---

## 🔮 Планирую добавить

- Refresh-токены
- Rate limiting
- Integration-тесты с Testcontainers
- CI/CD через GitHub Actions
