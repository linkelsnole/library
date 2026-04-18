# Автоматизация работы библиотеки

## Содержание
- [Описание](#описание)
- [Технологии](#технологии)
- [Архитектура](#архитектура)
- [Установка и запуск](#установка-и-запуск)
- [API Документация](#api-документация)
    - [Книги](#книги)
    - [Читатели](#читатели)
    - [Выдачи](#выдачи)
- [Тестирование](#тестирование)
- [Ограничения](#ограничения)

## Описание

Клиент-серверное приложение для автоматизации работы библиотеки. Тестовое задание, вариант 3.

Система предоставляет REST API для работы с каталогом книг, базой читателей и учётом выдач, а также десктоп-клиент на базе Eclipse RCP:
- Ведение базы книг (название, автор, год издания, ISBN, доступность)
- Ведение базы читателей (ФИО, пол, возраст)
- Выдача книг читателям и возврат
- Отчёт о количестве выданных читателю книг за выбранный период

## Технологии

### Backend
- Java 21
- Jersey (JAX-RS) + Grizzly — REST API без Spring
- Google Guice — внедрение зависимостей
- Hibernate (чистый JPA) — работа с БД
- HikariCP — пул соединений
- H2 — in-memory БД (по умолчанию)
- Jackson — сериализация JSON
- Bean Validation (Hibernate Validator) + `jersey-bean-validation` — валидация DTO
- SLF4J + Logback — логирование
- Gradle 9+ (Kotlin DSL, Wrapper)
- JUnit 5 + Mockito; интеграционный тест (Grizzly + `HttpClient`)
- Lombok

### Frontend
- Java 21
- Eclipse RCP (E4) — десктоп-клиент
- SWT / JFace — UI
- Jackson + HttpURLConnection — HTTP-клиент
- AppConfig — конфигурация через system property / env

## Архитектура

### Backend
```
resource/    — JAX-RS эндпоинты, ExceptionMapper'ы
service/     — бизнес-логика
repository/  — доступ к данным через EntityManager
entity/      — JPA-сущности
dto/         — DTO запросов (BookRequest, ReaderRequest, IssueLoanRequest)
exception/   — NotFoundException, BusinessException
api/         — константы путей (ApiPaths)
persistence/ — Transactions (RESOURCE_LOCAL)
config/      — Guice (AppModule, JpaModule), ApplicationProperties, ObjectMapperProvider
```

Транзакции — `RESOURCE_LOCAL` без JTA. Утилитный класс `Transactions.write/read` открывает EntityManager, управляет `begin/commit/rollback` и гарантирует закрытие через try-with-resources. Операции `issue` и `returnBook` выполняются в одной транзакции.

### Frontend
parts/    — views (BooksView, ReadersView, LoansView) и диалоги (Add/Edit × Book/Reader, IssueLoan, Report)  
model/    — DTO, совпадающие по структуре с backend entity  
service/  — AppConfig (конфигурация), ApiClient (HTTP-клиент, singleton), ApiException  
handlers/ — обработчики меню (Open, Save, Quit, About)

Сетевые запросы выполняются в отдельных потоках, UI обновляется через `Display.asyncExec`. Данные обновляются при действии пользователя и при переключении на вкладку (`@Focus`). Ошибки backend (HTTP 4xx/5xx) парсятся из JSON-поля `error` или `validation_errors` и показываются в MessageBox. Валидация числовых полей (Year, Age, ID) выполняется на клиенте перед отправкой.

## Установка и запуск

### Backend

Требования: JDK 21+, Gradle Wrapper в репозитории.

Файл **`src/main/resources/application.properties`** обязателен в classpath (Gradle копирует его при сборке). Ключи:

| Ключ | Назначение |
|------|------------|
| `api.base` | Базовый URL сервера (например `http://localhost:8080/`) |
| `db.url` | JDBC URL |
| `db.username` | пользователь БД |
| `db.password` | пароль |
| `db.pool.size` | размер пула HikariCP |

Переопределение без правки файла — системные свойства JVM (`-D`):

```bash
java -Dapi.base=http://prod:8080/ -Ddb.url=jdbc:postgresql://host/db -jar app.jar
```

Непустое значение `-D` имеет приоритет над файлом.

```bash
git clone <repo-url>
cd library-back
./gradlew run
```

Сервер слушает адрес из `api.base`. БД по умолчанию — H2 in-memory. Остановка — Enter в консоли.

Проверка:
```bash
curl http://localhost:8080/books
```

### Frontend

Требования: Eclipse IDE for RCP and RAP Developers (или Eclipse с PDE), JDK 21+.

1. Открыть Eclipse
2. File → Import → Existing Projects into Workspace
3. Выбрать корень `library-front/`, импортировать три проекта: `com.example.library.rcp2`, `com.example.library.feature`, `com.example.library.product`
4. Открыть `com.example.library.product/library.product`
5. Нажать **Launch an Eclipse application**
6. **Backend должен быть уже запущен**

#### Конфигурация frontend

Адрес backend API — `http://localhost:8080` по умолчанию. Переопределить:

- System property: `-Dapi.base=http://other-host:8080`
- Env переменная: `API_BASE=http://other-host:8080`

System property имеет приоритет над env, env — над дефолтом.

## API Документация

Базовый URL задаётся в `application.properties` (`api.base`), по умолчанию `http://localhost:8080`.

### Коды ошибок (JSON)

| Ситуация | HTTP | Тело |
|----------|------|------|
| `NotFoundException` | 404 | `{"error":"..."}` |
| `BusinessException` | 400 | `{"error":"..."}` |
| ошибки Bean Validation | 400 | `{"validation_errors":["..."]}` |
| прочие необработанные `RuntimeException` | 500 | `{"error":"..."}` |

### Книги

#### Получить все книги
GET /books  
Ответ: массив книг с полями `id`, `title`, `author`, `year`, `isbn`, `available`.

#### Получить книгу по ID
GET /books/{id}  
404, если книги нет.

#### Создать книгу
POST /books  
Content-Type: application/json

Тело — **BookRequest** (не сырая сущность): `title`, `author`, опционально `year`, `isbn`. Поле **`available` в запросе не передаётся** — сервер выставляет `true`.

```json
{
  "title": "Мастер и Маргарита",
  "author": "Булгаков",
  "year": 1967,
  "isbn": "978-5-699-12014-5"
}
```

#### Обновить книгу
PUT /books/{id}  
Тот же контракт, что и POST (BookRequest).

#### Удалить книгу
DELETE /books/{id}

### Читатели

#### Получить всех читателей
GET /readers

#### Получить читателя по ID
GET /readers/{id}

#### Создать читателя
POST /readers  
Тело — **ReaderRequest**: `fullName` (обязательно), опционально `gender`, `age` (≥ 0).

```json
{
  "fullName": "Иванов Иван Иванович",
  "gender": "M",
  "age": 30
}
```

#### Обновить читателя
PUT /readers/{id}  
ReaderRequest.

#### Удалить читателя
DELETE /readers/{id}

### Выдачи

#### Получить все выдачи
GET /loans

#### Выдать книгу
POST /loans/issue  
Тело — **IssueLoanRequest**: `bookId`, `readerId` (оба обязательны).

```json
{
  "bookId": 1,
  "readerId": 1
}
```

Ответ: 201 и созданная `Loan`. Книга помечается недоступной.

Ошибки: 404 (книга/читатель не найдены), 400 (книга уже выдана — `BusinessException`).

#### Вернуть книгу
PUT /loans/{id}/return

#### Отчёт по выдачам
GET /loans/report?readerId=1&from=2026-01-01&to=2026-12-31

Ответ:
```json
{
  "readerId": 1,
  "from": "2026-01-01",
  "to": "2026-12-31",
  "count": 5
}
```

## Тестирование

```bash
./gradlew clean test
```

Отчёт: `build/reports/tests/test/index.html`

### Юнит-тесты (`src/test/java/org/example/service/`)
- `BookServiceTest` — CRUD книг, проверка `NotFoundException`
- `ReaderServiceTest` — CRUD читателей, проверка `NotFoundException`
- `LoanServiceTest` — `issue` / `returnBook`, проверка транзакционности (commit при успехе, rollback при ошибке), проверка `NotFoundException` и `BusinessException`, отчёт по периоду

Репозитории и `EntityManagerFactory` замоканы через Mockito. В `LoanServiceTest` дополнительно мокается `EntityTransaction` для проверки жизненного цикла транзакции.

### Интеграционный тест (`src/test/java/org/example/integration/`)
- `BookIntegrationTest` — поднимает Grizzly на случайном порту, отправляет реальный HTTP POST и GET на `/books`, проверяет коды ответа и тело. Использует стандартный `java.net.http.HttpClient`.

## Ограничения

- По умолчанию БД — H2 in-memory; данные не сохраняются между перезапусками, пока не сменить `db.url`
- `hibernate.hbm2ddl.auto=create-drop` — схема пересоздаётся при старте (см. `persistence.xml`)
- Авторизация не реализована
- Одна книга — одна активная выдача (флаг `available`)
- Даты выдачи/возврата — `LocalDate`
- HTTP-клиент фронта на голом `HttpURLConnection` без OkHttp/Apache HttpClient — для тестового задания приемлемо; в проде разумнее заменить на клиент с пулами, таймаутами и перехватчиками
- Числовая валидация на клиенте (Year, Age, ID) с выводом `MessageDialog` перед отправкой запроса

---

Автор: Чубак Дмитрий  
Версия: 1.0.0  
Последнее обновление: 18 апреля 2026
