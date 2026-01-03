# Hotel Booking System (Spring Boot microservices)

Учебная распределённая система бронирования отелей на базе Java 17 и Spring Boot 3.  
Проект демонстрирует микросервисную архитектуру, целостность данных при бронировании и безопасность на JWT.

## Архитектура

- `eureka-server` — Netflix Eureka Server (service discovery), порт `8761`.
- `gateway` — Spring Cloud Gateway:
  - `/api/bookings/**` → `booking-service`
  - `/api/hotels/**` → `hotel-service`
- `booking-service` — пользователи, аутентификация, бронирования, поиск доступных/рекомендованных номеров.
- `hotel-service` — отели, номера, статистика загруженности, временные блокировки (HOLD).
- `common` — общие DTO и обработка ошибок (`ErrorDTO`, `GlobalExceptionHandler`).

Каждый сервис — отдельное Spring Boot приложение с H2 (in‑memory) и регистрацией в Eureka.

## Технологический стек

- Java 17, Maven (multi‑module).
- Spring Boot 3.5.x, Spring Web, Spring Data JPA, H2.
- Spring Security 6 + OAuth2 Resource Server (JWT).
- Spring Cloud Gateway, Spring Cloud Netflix Eureka, Spring Cloud LoadBalancer.
- Resilience4j (Retry + CircuitBreaker для вызовов Booking → Hotel).
- Lombok, Jakarta Validation.

## Инициализация данных (`data.sql`)

- `booking-service/src/main/resources/data.sql`:
  - пользователь `user` / пароль `password`, роль `USER`;
  - пользователь `admin` / пароль `password`, роль `ADMIN`.
- `hotel-service/src/main/resources/data.sql`:
  - несколько отелей и набор комнат с различной доступностью (`available`) и статистикой `times_booked`,  
    что позволяет демонстрировать алгоритм рекомендаций и статистику загрузки.

## Запуск

1. Собрать проект:
   ```bash
   mvn clean package
   ```
2. Запустить сервисы (в отдельных терминалах):
   ```bash
   mvn -pl eureka-server spring-boot:run
   mvn -pl hotel-service spring-boot:run
   mvn -pl booking-service spring-boot:run
   mvn -pl gateway spring-boot:run
   ```

Точки входа:
- Eureka: `http://localhost:8761`
- Gateway: `http://localhost:8081`

## Запуск в Docker (docker-compose)

В репозитории есть `Dockerfile` для каждого сервиса и `docker-compose.yml` в корне.

1. Собрать JAR’ы (как выше):
   ```bash
   mvn clean package
   ```
2. Собрать и запустить контейнеры:
   ```bash
   docker-compose up --build
   ```

После старта:
- Eureka будет доступен по `http://localhost:8761`.
- Gateway — по `http://localhost:8081` (маршрутизирует запросы к booking‑ и hotel‑service через Eureka).

## Аутентификация и JWT

JWT выдаётся в `booking-service` и далее валидируется как Resource Server в `booking-service` и `hotel-service`.

1. Регистрация пользователя:
   ```http
   POST http://localhost:8081/api/bookings/user/register
   Content-Type: application/json

   {"username":"u","password":"p"}
   ```
2. Аутентификация и получение токена:
   ```http
   POST http://localhost:8081/api/bookings/user/auth
   Content-Type: application/json

   {"username":"user","password":"password"}
   ```
   Ответ: `{"token":"<JWT>"}`.
3. Использование токена:
   ```http
   Authorization: Bearer <JWT>
   ```

Роли: `USER` / `ADMIN`.  
`booking-service` и `hotel-service` защищены как Resource Server; доступ к административным эндпойнтам — только для `ADMIN`.

## Ключевые бизнес‑фичи

- Алгоритм планирования занятости:
  - проверка пересечений по датам в `BookingService` с блокировкой (`PESSIMISTIC_WRITE`);
  - HOLD на стороне `hotel-service` (`lastBookingId`, `holdUntil`) с TTL 5 минут;
  - учёт и сортировка по `times_booked` для рекомендаций.
- Сага и согласованность:
  - бронирование создаётся в статусе `PENDING`;
  - вызов `confirm-availability` в Hotel; при успехе — `CONFIRMED`, при ошибке — `CANCELLED` (компенсация);
  - при отмене брони вызывается `release`.
- Устойчивость:
  - Resilience4j `@Retry` + `@CircuitBreaker` на вызовах `HotelClient` (`confirmAvailability`, `release`, `getAllRooms`, `getRecommendedRooms`).
- Идемпотентность:
  - `POST /api/bookings/bookings` поддерживает идемпотентный заголовок `X-Idempotency-Key` (уникален на пользователя);
  - при повторном запросе с тем же ключом возвращается уже созданная бронь.
- Пагинация:
  - `GET /api/bookings/bookings` принимает параметры `page` и `size`.
- Безопасность:
  - `booking-service`: публичные `/user/register`, `/user/auth`; `/admin/**` только для `ADMIN`, остальное — для аутентифицированных;
  - `hotel-service`: создание/управление отелями и номерами — только для `ADMIN`, остальные `/api/hotels/**` — для аутентифицированных;
  - методная безопасность `@PreAuthorize` для админ‑операций с пользователями.
- Gateway‑трассировка:
  - глобальный фильтр в `gateway` добавляет/прокидывает заголовок `X-Request-ID` и логирует входящие запросы.

## Обзор API

### Booking Service (через Gateway `http://localhost:8081/api/bookings/...`)

**Аутентификация / пользователи**
- `POST /api/bookings/user/register` — регистрация пользователя.
- `POST /api/bookings/user/auth` — выдача JWT.

**Доступность и рекомендации**
- `GET /api/bookings/availability/rooms?startDate=YYYY-MM-DD&endDate=YYYY-MM-DD&hotelId=&limit=`  
  — доступные номера по датам (фильтр по отелю, ограничение по количеству).
- `GET /api/bookings/availability/recommend?...`  
  — рекомендованные номера, отсортированные по `timesBooked` (при равенстве — по `id`).

**Бронирования (роль USER/ADMIN)**
- `POST /api/bookings/bookings`  
  — создание брони; поддерживает заголовок `X-Idempotency-Key` для идемпотентности.
- `GET /api/bookings/bookings?page=&size=`  
  — список бронирований текущего пользователя (только свои).
- `GET /api/bookings/bookings/{id}`  
  — детали конкретной брони (чужие брони недоступны).
- `DELETE /api/bookings/bookings/{id}`  
  — отмена брони (и вызов `release` в `hotel-service`).

### Hotel Service (через Gateway `http://localhost:8081/api/hotels/...`)

**CRUD и статистика**
- `POST /api/hotels/hotels` — создание отеля (ADMIN).
- `POST /api/hotels/rooms` — создание номера (ADMIN).
- `GET /api/hotels/hotels` — список отелей.
- `GET /api/hotels/rooms` — список доступных номеров.
- `GET /api/hotels/rooms/recommend` — рекомендованные номера (по `times_booked`).
- `GET /api/hotels/stats/rooms?hotelId=...` — статистика по номерам отеля:
  `totalRooms`, `availableRooms`, `totalTimesBooked`.

**Алгоритм HOLD**
- `POST /api/rooms/{id}/confirm-availability`  
  Request: `{"bookingId": "...", "startDate": "...", "endDate": "..."}`  
  Room помечается как удерживаемый: заполняются `lastBookingId`, `holdUntil`, увеличивается `times_booked`.
- `POST /api/rooms/{id}/release`  
  Request: `{"bookingId": "..."}`  
  Сбрасываются `lastBookingId` и `holdUntil` (если `bookingId` совпадает).

## Ошибки и форматы ответов

Глобальный обработчик исключений из модуля `common` возвращает `ErrorDTO`:

```json
{
  "error": "BAD_REQUEST | CONFLICT | RUNTIME_ERROR | UNAUTHORIZED | FORBIDDEN",
  "message": "Описание ошибки",
  "correlationId": "uuid"
}
```

- `400 BAD_REQUEST` — ошибки валидации входных данных (`IllegalArgumentException`).
- `409 CONFLICT` — конфликтные состояния (пересечение дат, занятый номер и т.п., `IllegalStateException`).
- `401 UNAUTHORIZED` — проблемы с аутентификацией (JWT).
- `403 FORBIDDEN` — недостаточно прав (`AccessDeniedException`).
- `500 INTERNAL_SERVER_ERROR` — прочие ошибки.

## Тестирование

В проекте предусмотрен базовый набор unit/integration тестов:

- `booking-service`:
  - `BookingServiceTest` — успешное бронирование, пересечение дат, идемпотентность, сага с компенсацией, доступ только к своим бронированиям, пагинация.
  - `BookingConcurrencyTest` — конкурентные бронирования на один номер/диапазон дат (должна пройти только одна бронь).
- `hotel-service`:
  - `HotelServiceTest` — поведение `confirmRoomAvailability`/`releaseRoom` (HOLD, счётчик `times_booked`, защита от чужого `bookingId`).

Запуск всех тестов:

```bash
mvn test
```
