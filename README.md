# Credit Telegram Bot

Telegram-бот для расчёта графиков погашения кредита, сохранения истории запросов и просмотра менеджерской аналитики.

Проект поддерживает пошаговый диалог в Telegram. Дополнительно предусмотрено развитие приложения до Telegram Mini App с интерфейсом на React и REST API на Spring Boot.

Оба интерфейса используют общую бизнес-логику расчётов и общее хранилище данных.

> Расчёты носят справочный характер и не являются кредитным предложением банка. 
> Итоговые условия кредитования определяются конкретной финансовой организацией.

---

## Содержание

- [Статус проекта](#статус-проекта)
- [Возможности](#возможности)
- [Технологии](#технологии)
- [Архитектура](#архитектура)
- [Структура проекта](#структура-проекта)
- [Требования](#требования)
- [Подготовка Telegram-бота](#подготовка-telegram-бота)
- [Настройка окружения](#настройка-окружения)
- [Запуск PostgreSQL](#запуск-postgresql)
- [Запуск backend](#запуск-backend)
- [Работа с Telegram-ботом](#работа-с-telegram-ботом)
- [Telegram Mini App](#telegram-mini-app)
- [REST API Mini App](#rest-api-mini-app)
- [Авторизация и разграничение доступа](#авторизация-и-разграничение-доступа)
- [База данных и миграции](#база-данных-и-миграции)
- [Расчёт кредита](#расчёт-кредита)
- [Тестирование](#тестирование)
- [Сборка приложения](#сборка-приложения)
- [Диагностика ошибок](#диагностика-ошибок)
- [Безопасность](#безопасность)
- [Ограничения](#ограничения)
- [Планы развития](#планы-развития)

---

## Статус проекта

### Существующий интерфейс Telegram-бота

- Пошаговый ввод параметров кредита.
- Расчёт аннуитетного графика.
- Расчёт дифференцированного графика.
- Сохранение кредитных запросов.
- Просмотр личной истории.
- Вход менеджера.
- Просмотр менеджерской аналитики.
- Отмена текущего диалога.

### Рефакторинг Telegram-слоя

Предусмотрено разделение Telegram-части на:

- координатор входящих обновлений;
- обработчик команд;
- обработчик шагов диалога;
- парсер пользовательского ввода;
- фабрику клавиатур;
- форматтер сообщений;
- отправщик сообщений.

### Расширение Mini App

Следующий этап развития:

- React-интерфейс кредитного калькулятора;
- REST API для расчётов и истории;
- серверная проверка данных авторизации Telegram;
- открытие приложения кнопкой меню бота;
- единая история расчётов из чата и Mini App.

> Разделы README, посвящённые Mini App, описывают целевую реализацию. 
> Для их использования соответствующие классы backend и файлы frontend должны быть добавлены в проект.

---

## Возможности

### Для пользователя

- Ввод суммы кредита.
- Выбор срока от 1 до 360 месяцев.
- Ввод годовой процентной ставки.
- Выбор типа платежа:
    - аннуитетный;
    - дифференцированный.
- Получение графика погашения.
- Просмотр общей суммы выплат.
- Просмотр переплаты.
- Просмотр истории собственных запросов.
- Отмена незавершённого диалога.

### Для менеджера

- Проверка допуска по Telegram ID.
- Вход по менеджерскому паролю.
- Выход из менеджерской сессии.
- Просмотр аналитики:
    - общее количество запросов;
    - распределение по типам платежей;
    - популярный срок кредита.

### Для Mini App

После внедрения веб-интерфейса:

- заполнение параметров кредита в форме;
- отображение графика в таблице;
- просмотр последних 50 личных запросов;
- авторизация через подписанные данные Telegram;
- сохранение расчётов в общей базе приложения.

---

## Технологии

### Backend

- Java 21.
- Spring Boot 3.5.0.
- Spring Data JPA.
- Hibernate.
- PostgreSQL 16.
- Flyway.
- TelegramBots с long polling.
- Maven.

### Frontend Mini App

- React.
- Vite.
- JavaScript.
- CSS.
- Telegram Mini Apps JavaScript API.

### Инфраструктура

- Docker.
- Docker Compose.
- HTTPS-туннель для локальной демонстрации Mini App.

Для REST API дополнительно используются:

- Spring Web;
- Jakarta Bean Validation.

Точные версии зависимостей указаны в `pom.xml` и `mini-app/package.json`.

---

## Архитектура

### Telegram-бот

```text
Telegram Update
      ↓
CreditTelegramBot
      ↓
CommandHandler / ConversationHandler
      ↓
CreditService / HistoryService / AnalyticsService
      ↓
CreditRequestRepository
      ↓
PostgreSQL
```

### Telegram Mini App

```text
Telegram
      ↓
React Mini App
      ↓ HTTPS / JSON
MiniAppController
      ↓
Проверка Telegram initData
      ↓
CreditService / HistoryService
      ↓
CreditRequestRepository
      ↓
PostgreSQL
```

### Принцип разделения ответственности

Telegram-слой не должен содержать формулы кредитного расчёта.

React-интерфейс не выполняет финансовые расчёты самостоятельно.
Он отправляет параметры на backend и отображает полученный результат.

Бизнес-логика находится в сервисах и калькуляторах. 
Поэтому расчёт из чата и расчёт из Mini App используют один алгоритм.

### Основные компоненты

| Компонент | Назначение |
|---|---|
| `CreditTelegramBot` | Приём обновлений Telegram и маршрутизация |
| `CommandHandler` | Обработка команд |
| `ConversationHandler` | Обработка шагов диалога |
| `ConversationService` | Хранение пользовательских сессий |
| `UserSession` | Параметры текущего диалога |
| `InputParser` | Преобразование и проверка текстового ввода |
| `BotKeyboardFactory` | Создание inline-клавиатур |
| `BotMessageFormatter` | Формирование текстовых ответов |
| `TelegramMessageSender` | Отправка сообщений и разбиение длинных текстов |
| `CreditService` | Запуск расчёта и сохранение запроса |
| `HistoryService` | Получение истории пользователя |
| `AnalyticsService` | Получение статистики |
| `ManagerAuthService` | Проверка менеджерского доступа |
| `PaymentCalculatorFactory` | Выбор алгоритма расчёта |
| `MiniAppController` | REST API веб-интерфейса |
| `TelegramInitDataService` | Проверка авторизации Mini App |

---

## Структура проекта

Целевая структура после рефакторинга и подключения Mini App:

```text
credit-telegram-bot/
├── src/
│   ├── main/
│   │   ├── java/com/example/creditbot/
│   │   │   ├── CreditBotApplication.java
│   │   │   ├── bot/
│   │   │   │   ├── CreditTelegramBot.java
│   │   │   │   ├── ConversationService.java
│   │   │   │   ├── ConversationStep.java
│   │   │   │   ├── UserSession.java
│   │   │   │   ├── handler/
│   │   │   │   ├── keyboard/
│   │   │   │   ├── message/
│   │   │   │   └── support/
│   │   │   ├── calculator/
│   │   │   │   ├── PaymentCalculator.java
│   │   │   │   ├── AnnuityPaymentCalculator.java
│   │   │   │   ├── DifferentialPaymentCalculator.java
│   │   │   │   └── PaymentCalculatorFactory.java
│   │   │   ├── config/
│   │   │   │   └── TelegramConfig.java
│   │   │   ├── domain/
│   │   │   │   ├── CreditRequest.java
│   │   │   │   ├── CreditSchedule.java
│   │   │   │   ├── Payment.java
│   │   │   │   └── PaymentType.java
│   │   │   ├── persistence/
│   │   │   │   ├── entity/
│   │   │   │   ├── repository/
│   │   │   │   └── PostgresCreditRequestRepository.java
│   │   │   ├── repository/
│   │   │   │   ├── CreditRequestRepository.java
│   │   │   │   └── InMemoryCreditRequestRepository.java
│   │   │   ├── security/
│   │   │   │   └── ManagerAuthService.java
│   │   │   ├── service/
│   │   │   └── web/
│   │   │       ├── MiniAppController.java
│   │   │       ├── MiniAppExceptionHandler.java
│   │   │       ├── dto/
│   │   │       └── security/
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── db/migration/
│   │       └── static/
│   └── test/
├── credit-bot-mini-app/
│   ├── src/
│   │   ├── App.jsx
│   │   ├── App.css
│   │   ├── api.js
│   │   └── main.jsx
│   ├── index.html
│   ├── package.json
│   ├── package-lock.json
│   └── vite.config.js
├── docker-compose.yml
├── pom.xml
├── .gitignore
└── README.md
```

---

## Требования

Для backend:

- JDK 21;
- Maven;
- Docker Desktop;
- доступ к Telegram API;
- токен Telegram-бота.

Для frontend:

- Node.js версии, совместимой с установленной версией Vite;
- npm.

Для проверки Mini App внутри Telegram:

- публичный HTTPS-адрес;
- работающий backend;
- настроенная кнопка меню бота.

### Проверка окружения

```powershell
java -version
mvn -version
docker --version
docker compose version
node --version
npm --version
```

Команда `mvn -version` должна показывать JDK, подходящую для проекта.

---

## Подготовка Telegram-бота

1. Откройте официальный `@BotFather` в Telegram.
2. Отправьте `/newbot`.
3. Укажите имя и username бота.
4. Получите токен.
5. Сохраните токен в переменной окружения `TELEGRAM_BOT_TOKEN`.

Не добавляйте токен в `application.yml`, Java-код, React-код или Git.

Если токен был опубликован, отзовите его через BotFather и используйте новый.

### Настройка списка команд

В BotFather можно настроить команды через `/setcommands`:

```text
start - Главное меню
help - Помощь
calculate - Рассчитать кредит
history - История запросов
cancel - Отменить действие
manager_login - Вход менеджера
manager_logout - Выход менеджера
analytics - Аналитика для менеджера
```

---

## Настройка окружения

### 1. Клонирование репозитория

```powershell
git clone <URL_РЕПОЗИТОРИЯ>
cd credit-telegram-bot
```

### 2. Переменные backend

Пример для PowerShell:

```powershell
$env:TELEGRAM_BOT_TOKEN="ВАШ_ТОКЕН"
$env:TELEGRAM_BOT_USERNAME="USERNAME_ВАШЕГО_БОТА"

$env:TELEGRAM_MANAGER_PASSWORD="ВАШ_МЕНЕДЖЕРСКИЙ_ПАРОЛЬ"
$env:TELEGRAM_MANAGER_IDS="ВАШ_TELEGRAM_ID"

$env:DB_URL="jdbc:postgresql://localhost:5432/credit_bot"
$env:DB_USERNAME="credit_user"
$env:DB_PASSWORD="credit_password"
```

Переменные `$env:...` действуют в текущем PowerShell-сеансе. Запускайте Maven из этого же окна.

Пример для Bash:

```bash
export TELEGRAM_BOT_TOKEN="ВАШ_ТОКЕН"
export TELEGRAM_BOT_USERNAME="USERNAME_ВАШЕГО_БОТА"

export TELEGRAM_MANAGER_PASSWORD="ВАШ_МЕНЕДЖЕРСКИЙ_ПАРОЛЬ"
export TELEGRAM_MANAGER_IDS="ВАШ_TELEGRAM_ID"

export DB_URL="jdbc:postgresql://localhost:5432/credit_bot"
export DB_USERNAME="credit_user"
export DB_PASSWORD="credit_password"
```

### Назначение переменных

| Переменная | Назначение |
|---|---|
| `TELEGRAM_BOT_TOKEN` | Токен бота |
| `TELEGRAM_BOT_USERNAME` | Username бота |
| `TELEGRAM_MANAGER_PASSWORD` | Пароль менеджерского входа |
| `TELEGRAM_MANAGER_IDS` | Разрешённые Telegram ID менеджеров |
| `DB_URL` | JDBC URL базы |
| `DB_USERNAME` | Пользователь PostgreSQL |
| `DB_PASSWORD` | Пароль PostgreSQL |

Формат нескольких ID должен соответствовать реализации `ManagerAuthService`. Для первоначального запуска достаточно одного ID.

### Конфликтующие переменные Spring

Проверьте, нет ли старых значений:

```powershell
Get-ChildItem Env:SPRING_DATASOURCE* -ErrorAction SilentlyContinue
```

Переменные:

```text
SPRING_DATASOURCE_URL
SPRING_DATASOURCE_USERNAME
SPRING_DATASOURCE_PASSWORD
```

могут переопределять свойства из `application.yml`.

Если они нужны другим проектам, не обязательно удалять их постоянно. Для текущего окна можно явно задать:

```powershell
$env:SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/credit_bot"
$env:SPRING_DATASOURCE_USERNAME="credit_user"
$env:SPRING_DATASOURCE_PASSWORD="credit_password"
```

### Файл `.env`

Сам по себе файл `.env` не означает, что Maven или Spring Boot автоматически загрузят из него переменные.

В приведённом запуске backend использует переменные процесса. Если применяется `.env`, необходимо отдельно настроить способ его загрузки.

---

## Запуск PostgreSQL

Пример `docker-compose.yml` для локальной разработки:

```yaml
services:
  postgres:
    image: postgres:16
    container_name: credit-bot-postgres
    restart: unless-stopped

    environment:
      POSTGRES_DB: credit_bot
      POSTGRES_USER: credit_user
      POSTGRES_PASSWORD: credit_password

    ports:
      - "127.0.0.1:5432:5432"

    volumes:
      - credit_bot_postgres_data:/var/lib/postgresql/data

volumes:
  credit_bot_postgres_data:
```

Проброс на `127.0.0.1` ограничивает доступ к порту базы локальным компьютером.

Пароль `credit_password` приведён только для локального примера. 
Для опубликованного окружения используйте отдельные секреты.

### Запуск

```powershell
docker compose up -d
```

### Проверка состояния

```powershell
docker ps
docker logs credit-bot-postgres
```

### Проверка базы внутри контейнера

```powershell
docker exec -it credit-bot-postgres `
  psql -U credit_user -d credit_bot `
  -c "SELECT current_database(), current_user;"
```

Ожидаемый результат:

```text
 current_database | current_user
------------------+--------------
 credit_bot       | credit_user
```

### Проверка TCP-подключения внутри контейнера

```powershell
docker exec -e PGPASSWORD=credit_password `
  -it credit-bot-postgres `
  psql -h 127.0.0.1 -p 5432 `
  -U credit_user -d credit_bot `
  -c "SELECT current_database(), current_user;"
```

Эта проверка обращается к PostgreSQL внутри контейнера. 
Она не проверяет маршрут от Windows к опубликованному порту.

### Остановка без удаления данных

```powershell
docker compose down
```

### Удаление данных

```powershell
docker compose down -v
```

> Команда `down -v` удаляет подключённые именованные volumes проекта. Не используйте её для обычного исправления ошибок подключения или если данные нужно сохранить.

Изменение `POSTGRES_PASSWORD` в Compose не меняет пароль пользователя в уже инициализированной базе.

---

## Запуск backend

### Сборка и тесты

```powershell
mvn clean test
```

### Запуск из PowerShell

```powershell
mvn spring-boot:run "-Dspring-boot.run.profiles=postgres"
```

Кавычки вокруг Maven-параметра позволяют избежать проблем разбора аргумента PowerShell.

### Запуск из Bash

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=postgres
```

### Запуск из IntelliJ IDEA

1. Откройте проект как Maven-проект.
2. Выберите JDK 21.
3. Откройте конфигурацию запуска `CreditBotApplication`.
4. Укажите переменные окружения.
5. При необходимости добавьте аргумент:

```text
--spring.profiles.active=postgres
```

6. Запустите приложение.

Переменные, заданные в отдельном PowerShell после запуска IntelliJ IDEA,
не обязательно будут доступны процессу, запущенному из IDE.

### Проверка успешного запуска

Ориентируйтесь на отсутствие ошибок и строку:

```text
Started CreditBotApplication
```

После подключения Spring Web приложение также должно запустить HTTP-сервер на порту `8080`.

Не запускайте одновременно две копии приложения с одним токеном long polling.

---

## Работа с Telegram-ботом

### Команды

| Команда | Описание |
|---|---|
| `/start` | Главное меню |
| `/help` | Инструкция |
| `/calculate` | Начать новый расчёт |
| `/history` | Личная история запросов |
| `/cancel` | Отменить текущий диалог |
| `/manager_login` | Вход менеджера |
| `/manager_logout` | Выход менеджера |
| `/analytics` | Менеджерская аналитика |

### Пример расчёта

```text
/calculate
500000
36
18.5
```

После ввода параметров выберите тип платежа кнопкой.

Бот возвращает:

- общую сумму выплат;
- переплату;
- график по месяцам;
- сумму основного долга в каждом платеже;
- проценты;
- остаток задолженности.

### Проверка ввода

В Telegram-диалоге:

- сумма должна быть больше нуля;
- срок — целое число от 1 до 360;
- ставка не должна быть отрицательной;
- тип платежа выбирается из доступных вариантов.

Некорректное значение не должно переводить пользователя на следующий шаг.

### Менеджерская авторизация

```text
/manager_login
```

Бот проверяет, входит ли Telegram ID в список разрешённых менеджеров, затем запрашивает пароль.

После успешного входа:

```text
/analytics
```

Для завершения сессии:

```text
/manager_logout
```

---

## Telegram Mini App

Mini App добавляет графический интерфейс к существующему боту.

### Экраны первой версии

- Калькулятор.
- Результат с графиком платежей.
- Личная история запросов.

### Предварительные условия backend

Должны быть добавлены:

- `spring-boot-starter-web`;
- `spring-boot-starter-validation`;
- `MiniAppController`;
- `MiniAppExceptionHandler`;
- `CalculationRequest`;
- `TelegramInitDataService`;
- `TelegramUser`.

Если существует настройка:

```yaml
spring:
  main:
    web-application-type: none
```

её нужно убрать для запуска HTTP API.

### Установка frontend

Из корня проекта:

```powershell
cd mini-app
npm install
```

Для воспроизводимой установки при актуальном `package-lock.json`:

```powershell
npm ci
```

### Локальная разработка

Backend:

```powershell
mvn spring-boot:run "-Dspring-boot.run.profiles=postgres"
```

В другом окне:

```powershell
cd mini-app
npm run dev
```

Адрес frontend:

```text
http://localhost:5173
```

### Vite proxy

В разработке запросы `/api` перенаправляются на backend:

```javascript
import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

export default defineConfig({
  plugins: [react()],

  server: {
    port: 5173,
    strictPort: true,

    proxy: {
      "/api": {
        target: "http://localhost:8080",
        changeOrigin: true,
      },
    },
  },
});
```

Frontend использует относительные адреса:

```text
/api/mini-app/me
/api/mini-app/calculations
/api/mini-app/history
```

Не указывайте `http://localhost:8080` как адрес API для Mini App на телефоне: 
там `localhost` означает само устройство пользователя.

### Открытие вне Telegram

При открытии страницы обычным браузером интерфейс может отображаться, 
но защищённые API-запросы не выполняются без действительного `initData`.

Это предусмотренное поведение, а не ошибка.

### Подключение SDK

В `mini-app/index.html`:

```html
<script src="[https://telegram.org/js/telegram-web-app.js?64](https://telegram.org/js/telegram-web-app.js?64)"></script>
```

Скрипт подключается в `<head>` до модулей приложения.

В React:

```javascript
const telegram = window.Telegram?.WebApp;

telegram?.ready();
telegram?.expand();
```

### Сборка frontend

```powershell
cd mini-app
npm run build
```

Результат:

```text
mini-app/dist/
```

### Раздача React через Spring Boot

Из корня проекта:

```powershell
New-Item -ItemType Directory `
  -Force `
  -Path src\main\resources\static
```

Скопируйте сборку:

```powershell
Copy-Item `
  -Path mini-app\dist\* `
  -Destination src\main\resources\static `
  -Recurse `
  -Force
```

Если в `static` уже есть собственные страницы, 
проверьте их перед копированием: файл `index.html` может быть заменён.

Перезапустите backend:

```powershell
mvn clean spring-boot:run "-Dspring-boot.run.profiles=postgres"
```

Приложение будет доступно:

```text
http://localhost:8080
```

Один сервер обслуживает:

```text
/                 → React
/api/mini-app/*   → REST API
```

Для этой схемы отдельная настройка CORS между frontend и backend не требуется.

### Подключение к Telegram через HTTPS-туннель

При установленном и настроенном ngrok:

```powershell
ngrok http 8080
```

Используйте полученный публичный HTTPS-адрес.

Туннель направляется на `8080`, потому что Spring Boot уже отдаёт React и API.

### Кнопка меню

В BotFather:

1. Выполните `/setmenubutton`.
2. Выберите бота.
3. Укажите HTTPS-адрес приложения.
4. Укажите текст кнопки, например `Калькулятор кредита`.

Откройте личный чат с ботом и запустите приложение через кнопку меню.

Если адрес туннеля изменился, обновите его в BotFather.

### Обновление frontend

После изменения React-кода для собранной версии:

1. Выполните `npm run build`.
2. Скопируйте `dist` в `resources/static`.
3. Перезапустите или пересоберите backend.
4. Закройте и заново откройте Mini App.

Изменения в исходниках React не обновляют автоматически уже скопированную сборку.

---

## REST API Mini App

Все перечисленные endpoints требуют заголовок:

```http
X-Telegram-Init-Data: <подписанные данные Telegram>
```

### Получить текущего пользователя

```http
GET /api/mini-app/me
```

Пример ответа:

```json
{
  "id": 123456789,
  "firstName": "Анна",
  "username": "example_user"
}
```

### Выполнить расчёт

```http
POST /api/mini-app/calculations
Content-Type: application/json
```

Тело запроса:

```json
{
  "amount": "500000.00",
  "termMonths": 36,
  "annualRate": "18.5",
  "paymentType": "ANNUITY"
}
```

Допустимые типы:

```text
ANNUITY
DIFFERENTIAL
```

Backend возвращает `CreditSchedule`, включая:

- `totalInterest`;
- `totalPayment`;
- `payments`.

Каждый элемент `payments` содержит:

- `month`;
- `totalPayment`;
- `principal`;
- `interest`;
- `remainingDebt`.

Идентификатор пользователя не передаётся в JSON. Backend извлекает его из проверенного `initData`.

### Получить историю

```http
GET /api/mini-app/history
```

В первой реализации возвращаются последние 50 запросов авторизованного пользователя.

Параметр `userId` не требуется и не должен использоваться как доказательство личности.

### Ограничения веб-формы

В предложенном DTO:

| Поле | Ограничение |
|---|---|
| Сумма | От 0,01 до 1 000 000 000 |
| Точность суммы | До двух знаков после запятой |
| Срок | От 1 до 360 месяцев |
| Ставка | От 0 до 100% |
| Точность ставки | До четырёх знаков после запятой |
| Тип платежа | `ANNUITY` или `DIFFERENTIAL` |

Эти ограничения должны быть согласованы с правилами бизнес-слоя и точностью колонок БД.

### Ошибки API

Пример:

```json
{
  "message": "Срок должен быть не меньше 1 месяца."
}
```

| Код | Значение |
|---|---|
| `400` | Некорректные параметры |
| `401` | Нет действительного Telegram initData |
| `500` | Внутренняя ошибка |

Если произошла ошибка после отправки расчёта, проверьте историю перед повторным нажатием: операция могла сохраниться до сбоя ответа.

---

## Авторизация и разграничение доступа

### Пользователь Mini App

1. Telegram открывает приложение.
2. React получает `Telegram.WebApp.initData`.
3. React отправляет `initData` backend.
4. Backend проверяет подпись.
5. Backend проверяет `auth_date`.
6. Backend извлекает Telegram ID.
7. Операции выполняются от имени проверенного пользователя.

### Проверка подписи

В предложенной реализации используется HMAC-SHA256.

Токен бота хранится только на backend и не передаётся в браузер.

Нельзя считать проверкой авторизации:

```javascript
window.Telegram.WebApp.initDataUnsafe.user.id
```

или произвольный ID из тела запроса.

### Срок действия

В первой версии:

```yaml
mini-app:
  auth:
    max-age-seconds: 3600
```

После истечения срока приложение нужно закрыть и открыть заново.

Это ограничение конкретной реализации backend, а не универсальное время жизни всех Telegram Mini Apps.

### Менеджерские endpoints

Менеджерская аналитика Mini App пока не входит в базовую реализацию.

При её добавлении права необходимо проверять на backend. Скрытие вкладки в React не защищает API.

---

## База данных и миграции

### Подключение

```yaml
spring:
  datasource:
    url: ${DB_URL:jdbc:postgresql://localhost:5432/credit_bot}
    username: ${DB_USERNAME:credit_user}
    password: ${DB_PASSWORD:credit_password}
    driver-class-name: org.postgresql.Driver

  jpa:
    hibernate:
      ddl-auto: validate
    open-in-view: false

  flyway:
    enabled: true
    locations: classpath:db/migration
```

### Миграции

Каталог:

```text
src/main/resources/db/migration
```

Имена файлов:

```text
V1__описание.sql
V2__описание.sql
```

После применения миграции не изменяйте её содержимое в общей базе. Для изменения схемы создавайте следующую миграцию.

### Проверка таблиц

```powershell
docker exec -it credit-bot-postgres `
  psql -U credit_user -d credit_bot -c "\dt"
```

### Проверка истории Flyway

```powershell
docker exec -it credit-bot-postgres `
  psql -U credit_user -d credit_bot `
  -c "SELECT installed_rank, version, description, success FROM flyway_schema_history ORDER BY installed_rank;"
```

### Ответственность инструментов

- Flyway изменяет схему БД.
- Hibernate при `ddl-auto: validate` проверяет соответствие сущностей схеме.
- PostgreSQL хранит данные.
- Docker volume сохраняет данные независимо от пересоздания контейнера.

Flyway не исправляет неверный пароль подключения и не заменяет создание самой базы PostgreSQL.

---

## Расчёт кредита

### Аннуитетный платёж

При положительной месячной ставке:

```text
A = S × r × (1 + r)^n / ((1 + r)^n − 1)
```

Где:

- `A` — платёж;
- `S` — сумма кредита;
- `r` — месячная ставка в долях единицы;
- `n` — количество месяцев.

Годовая ставка переводится в месячную:

```text
r = annualRate / 100 / 12
```

Для нулевой ставки требуется отдельная ветка:

```text
A = S / n
```

### Дифференцированный платёж

Основной долг распределяется по сроку, а проценты начисляются на остаток.

```text
Платёж = часть основного долга + проценты за месяц
```

По мере уменьшения остатка задолженности сумма процентов снижается.

### Денежные значения

Целевой подход — использовать `BigDecimal` для денежных вычислений, явно задавая точность и правила округления.

Если в текущем аннуитетном алгоритме ещё используется `double`, переход на полностью десятичный расчёт остаётся отдельной задачей.

React не должен дублировать формулы backend.

### Округление

Необходимо проверять:

- точность промежуточных вычислений;
- округление итогового платежа;
- корректировку последнего платежа;
- погашение остатка долга;
- согласованность суммы платежей с итогами графика.

В данном проекте месячная модель расчёта не должна автоматически трактоваться как точное воспроизведение банковского договора с учётом календарных дней, комиссий и дополнительных услуг.

---

## Тестирование

### Автоматические тесты

```powershell
mvn clean test
```

### Проверка сборки frontend

```powershell
cd mini-app
npm ci
npm run build
```

Успешная сборка frontend не заменяет функциональные тесты.

### Рекомендуемые тесты калькуляторов

- Нулевая ставка.
- Один месяц.
- Максимальный допустимый срок.
- Отрицательная сумма.
- Отрицательная ставка.
- Сумма частей основного долга.
- Нулевой остаток после последнего платежа.
- Согласованность итогов графика.

### Рекомендуемые тесты Telegram-слоя

- Неверная сумма не меняет шаг.
- Неверный срок не меняет шаг.
- Неверная ставка не меняет шаг.
- `/cancel` очищает сессию.
- `/start` возвращает меню.
- Недопущенный пользователь не проходит менеджерский вход.
- Аналитика недоступна без авторизации.
- Длинный график разбивается на сообщения.

### Рекомендуемые тесты Mini App

- Запрос без `initData` получает `401`.
- Неверная подпись получает `401`.
- Просроченный `initData` получает `401`.
- Подмена пользователя нарушает подпись.
- Некорректный запрос получает `400`.
- Пользователь получает только собственную историю.
- Расчёт из Mini App виден в истории бота.
- Ошибки backend отображаются в интерфейсе.

### Ручной сценарий

```text
/start
/calculate
500000
36
18.5
```

Выберите тип платежа.

Затем:

```text
/history
```

После внедрения Mini App откройте его кнопкой меню и проверьте ту же историю.

---

## Сборка приложения

### Только backend

```powershell
mvn clean package
```

### Backend вместе с Mini App

Сначала соберите frontend:

```powershell
cd mini-app
npm ci
npm run build
cd ..
```

Скопируйте сборку:

```powershell
New-Item -ItemType Directory `
  -Force `
  -Path src\main\resources\static

Copy-Item `
  -Path mini-app\dist\* `
  -Destination src\main\resources\static `
  -Recurse `
  -Force
```

Соберите JAR:

```powershell
mvn clean package
```

### Запуск JAR

Для текущего имени и версии артефакта:

```powershell
java -jar target\credit-telegram-bot-1.0.0.jar `
  --spring.profiles.active=postgres
```

Если версия в `pom.xml` изменилась, используйте фактическое имя JAR.

Перед запуском должны быть заданы переменные окружения.

---

## Диагностика ошибок

### `Unknown lifecycle phase ".run.profiles=postgres"`

PowerShell некорректно передал Maven-параметр.

Используйте:

```powershell
cd D:\repo\java\credit-telegram-bot\credit-bot-mini-app
npm run dev
```

### `Process terminated with exit code: 1`

Это итоговое сообщение Maven, а не первопричина.

Ищите выше:

```text
Caused by:
Application run failed
SQL State:
```

Сохранить лог:

```powershell
mvn spring-boot:run `
  "-Dspring-boot.run.profiles=postgres" `
  2>&1 | Tee-Object startup.log
```

Найти ключевые строки:

```powershell
Select-String `
  -Path startup.log `
  -Pattern "ERROR|Caused by|SQL State|Application run failed"
```

Перед публикацией лога проверьте, что в нём нет секретов.

### `SQL State: 3D000`

В экземпляре PostgreSQL, к которому обратилось приложение, не найдена указанная база.

Проверьте:

```powershell
docker ps
```

```powershell
docker exec -it credit-bot-postgres `
  psql -U credit_user -d postgres -c "\l"
```

Также проверьте фактический URL:

```powershell
Write-Host "DB_URL=[$env:DB_URL]"
Write-Host "SPRING_DATASOURCE_URL=[$env:SPRING_DATASOURCE_URL]"
```

Успешное подключение через `docker exec` доказывает наличие базы внутри контейнера, но не доказывает, что Java обращается к тому же серверу через порт Windows.

### `SQL State: 28P01`

PostgreSQL отклонил аутентификацию.

Возможные причины:

- неверный пароль;
- подключение к другому экземпляру PostgreSQL;
- переопределённые переменные окружения;
- другие настройки запуска в IDE.

Не удаляйте volume до проверки этих вариантов.

### Смена пароля роли

Откройте `psql`:

```powershell
docker exec -it credit-bot-postgres `
  psql -U credit_user -d postgres
```

Внутри `psql`:

```sql
\password credit_user
```

Введите новый пароль дважды.

Выйдите:

```sql
\q
```

После этого обновите пароль в окружении приложения.

### PowerShell-команды введены внутри PostgreSQL

Приглашение:

```text
postgres=#
```

означает, что открыта консоль PostgreSQL.

Здесь выполняются SQL и команды `psql`, а не:

```powershell
$env:...
Get-ChildItem ...
```

Если приглашение стало:

```text
postgres-#
```

очистите незавершённый запрос:

```sql
\r
```

Выйдите:

```sql
\q
```

Команды PowerShell выполняются после появления:

```text
PS ...>
```

### Проверка владельцев порта

```powershell
Get-NetTCPConnection `
  -LocalPort 5432 `
  -State Listen `
  -ErrorAction SilentlyContinue |
  Select-Object LocalAddress, LocalPort, OwningProcess
```

Не делайте вывод о маршруте подключения только по наличию записи о пробросе порта в `docker ps`.

### Mini App возвращает `401`

Проверьте:

- приложение открыто через Telegram;
- передаётся `X-Telegram-Init-Data`;
- backend использует токен того же бота;
- срок авторизации не истёк;
- системное время сервера корректно.

### Mini App возвращает `404`

Проверьте:

- добавлен `MiniAppController`;
- пакет находится внутри области сканирования Spring;
- backend перезапущен;
- frontend использует правильный путь `/api/mini-app`.

### Mini App возвращает `403`

Проверьте существующую конфигурацию Spring Security.

Не отключайте безопасность всего приложения без понимания причины.

### На телефоне не работает API

Не используйте `localhost` компьютера как адрес API в опубликованном frontend.

В описанной схеме используются относительные `/api/...`, а React и backend доступны по одному HTTPS-адресу.

### Открывается старая версия интерфейса

Повторите:

```powershell
cd mini-app
npm run build
cd ..
```

Затем скопируйте `dist` в `resources/static` и перезапустите backend.

---

## Безопасность

### Не хранить в репозитории

- токен бота;
- менеджерский пароль;
- реальные пароли БД;
- `initData` пользователей;
- локальные скрипты с секретами.

### Пример `.gitignore`

```gitignore
# Java
target/

# IntelliJ IDEA
.idea/
*.iml

# Node.js
mini-app/node_modules/
mini-app/dist/

# Local environment
.env
.env.*
!.env.example

# Local configuration
application-local.yml

# Scripts with secrets
run-local.ps1
run-credit-bot.ps1

# Logs
*.log
```

Решение о хранении сгенерированной React-сборки в `resources/static` принимается отдельно. 
В перспективе лучше собирать её автоматически в CI.

### Если секрет опубликован

1. Отзовите или замените его.
2. Обновите окружение.
3. Удалите секрет из исходников.
4. При необходимости очистите историю Git.
5. Не считайте обычный новый коммит достаточной защитой старого секрета.

### Права PostgreSQL

Название `root` не является обязательным для PostgreSQL.

Для локального примера используется `credit_user`. Пользователь, создаваемый официальным образом через `POSTGRES_USER`, обладает повышенными правами; это не готовая модель минимальных привилегий для production.

В опубликованном окружении желательно разделить:

- владельца схемы и миграций;
- пользователя приложения;
- административный доступ.

### Менеджерский доступ

Проверка прав выполняется на сервере.

Нельзя защищать аналитику только скрытием кнопки или вкладки в React.

### Сетевая доступность

Для production:

- используйте HTTPS;
- не публикуйте PostgreSQL без необходимости;
- ограничьте доступ к административным интерфейсам;
- настройте резервное копирование;
- не используйте демонстрационные пароли.

---

## Ограничения

- Состояние диалога хранится в памяти приложения.
- После перезапуска незавершённые диалоги теряются.
- Хранение сессий в `ConcurrentHashMap` не делает все операции над `UserSession` автоматически потокобезопасными.
- Telegram-обработка рассчитана на существующую модель single-thread update consumer.
- Минимальная версия Mini App проверяет `initData` на каждом запросе и требует переоткрытия после истечения срока.
- История ограничена 50 записями в ответе.
- В первой версии ограничение истории применяется после получения данных сервисом, а не как полноценная пагинация БД.
- Идемпотентность сохранения расчёта пока не предусмотрена: повторный запрос может создать повторную запись.
- Менеджерская аналитика Mini App не входит в базовую версию.
- Backend и frontend собираются отдельными командами.
- Локальный HTTPS-туннель зависит от работающего компьютера и не заменяет постоянный хостинг.
- График является модельным расчётом и не учитывает все особенности банковских договоров.

---

## Планы развития

### Корректность и архитектура

- Согласовать ограничения Telegram-диалога, REST DTO и бизнес-слоя.
- Завершить переход финансовых вычислений на `BigDecimal`.
- Добавить тесты округления и последнего платежа.
- Сохранить тонкий координатор Telegram-обновлений.

### Аналитика

- Добавить статистику по суммам.
- Заменить повторные `findAll()` агрегирующими запросами.
- Добавить менеджерский фильтр.
- Добавить экспорт в CSV.

### Mini App

- Добавить фильтры личной истории.
- Добавить менеджерскую аналитику с серверной проверкой прав.
- Добавить графики погашения.
- Добавить сравнение двух вариантов кредита.
- Добавить пагинацию истории на уровне БД.
- Добавить идемпотентность создания расчёта.
- Добавить отдельную серверную сессию после Telegram-авторизации.

### Инфраструктура

- Автоматизировать сборку React и включение в JAR.
- Настроить CI.
- Добавить интеграционные тесты PostgreSQL.
- Настроить постоянный HTTPS-домен.
- Настроить резервное копирование.
- Подготовить отдельную конфигурацию production.