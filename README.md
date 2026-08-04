Credit Telegram Bot
Telegram-бот для расчёта графиков погашения кредитов с поддержкой аннуитетных и дифференцированных платежей.
Проект демонстрирует применение принципов Abstraction, Composition, Low Coupling, High Cohesion, а также базовых
подходов SOLID, DRY, KISS, YAGNI.

Возможности
Для пользователей
Ввод параметров кредита: сумма, срок, процентная ставка, тип платежа.

Получение помесячного графика платежей.

Просмотр истории своих запросов.

Для менеджеров
Просмотр агрегированной статистики по запросам.

Фильтрация заявок по суммам и типам платежей.

Статистика по популярным параметрам кредитов.

Технологии
Java 21

Spring Boot 3.5

Telegram Bots API

Java Collections Framework

Maven

Архитектура
Проект разделён на несколько слоёв:

bot — обработка Telegram-команд и диалогов.

calculator — расчёт графиков платежей.

domain — модели предметной области.

repository — хранение запросов пользователей.

service — бизнес-логика и аналитика.

validation — проверка входных данных.

Основные принципы
Abstraction — расчёт вынесен в интерфейс PaymentCalculator.

Composition — бот и сервисы собираются из независимых компонентов.

Low Coupling — классы слабо зависят друг от друга.

High Cohesion — каждый класс отвечает только за свою задачу.

Структура проекта
text
src/main/java/com/example/creditbot
├── CreditBotApplication.java
├── bot
│ ├── CreditTelegramBot.java
│ ├── BotCommandHandler.java
│ ├── ConversationService.java
│ ├── ConversationStep.java
│ └── UserSession.java
├── calculator
│ ├── PaymentCalculator.java
│ ├── AnnuityPaymentCalculator.java
│ ├── DifferentialPaymentCalculator.java
│ └── PaymentCalculatorFactory.java
├── domain
│ ├── CreditRequest.java
│ ├── CreditSchedule.java
│ ├── Payment.java
│ └── PaymentType.java
├── repository
│ ├── CreditRequestRepository.java
│ └── InMemoryCreditRequestRepository.java
├── service
│ ├── CreditService.java
│ ├── HistoryService.java
│ └── AnalyticsService.java
└── validation
└── CreditRequestValidator.java
Как работает бот
Пользовательский сценарий
Пользователь отправляет /start.

Бот предлагает начать расчёт.

Пользователь отправляет /calculate.

Бот последовательно запрашивает:

сумму кредита;

срок в месяцах;

процентную ставку;

тип платежа.

Бот возвращает подробный график погашения.

Менеджерский сценарий
Менеджер авторизуется.

Получает доступ к аналитике.

Просматривает статистику по заявкам.

Фильтрует запросы по сумме и типу платежа.

Примеры расчётов
Аннуитетный платёж
Ежемесячный платёж остаётся одинаковым на всём сроке кредита. В начале большая часть платежа — проценты, позже — тело
кредита.

Дифференцированный платёж
Основная часть долга делится равномерно, а проценты уменьшаются каждый месяц.
Из-за этого платёж в начале выше, а затем постепенно уменьшается.

Настройка и запуск

1. Создайте бота в Telegram
   Откройте @BotFather.

Выполните команду /newbot.

Укажите имя и username бота.

Скопируйте токен.

2. Добавьте конфигурацию
   Создайте файл src/main/resources/application.yml:

text
telegram:
bot:
username: credit_olga_telegram_bot
token: "YOUR_TELEGRAM_BOT_TOKEN"

manager-password: "YOUR_MANAGER_PASSWORD"
manager-ids:

- 123456789

3. Рекомендуемый безопасный вариант
   Лучше хранить секреты через переменные окружения:

text
telegram:
bot:
username: credit_olga_telegram_bot
token: ${TELEGRAM_BOT_TOKEN}

manager-password: ${TELEGRAM_MANAGER_PASSWORD}
manager-ids: ${TELEGRAM_MANAGER_IDS:}

4. Запуск приложения
   bash
   mvn clean package
   java -jar target/credit-telegram-bot-1.0.0.jar
   Или из IntelliJ IDEA:

открой CreditBotApplication;

нажми Run.

Как пользоваться
Команды пользователя
/start — приветствие и список команд.

/calculate — запуск расчёта кредита.

/history — история запросов.

/help — справка.

Пример диалога
text
Пользователь: /calculate
Бот: Введите сумму кредита:
Пользователь: 1000000
Бот: Введите срок кредита в месяцах:
Пользователь: 60
Бот: Введите годовую процентную ставку:
Пользователь: 12
Бот: Выберите тип платежа:
1 — аннуитетный
2 — дифференцированный
Пользователь: 1
Бот: [готовый график платежей]
Хранение данных
Для хранения запросов используется ArrayList и HashMap / ConcurrentHashMap.
Это упрощённая in-memory реализация, подходящая для учебного проекта.
При необходимости её можно заменить на PostgreSQL без изменения бизнес-логики.

Тестирование
Рекомендуется проверить:

корректность расчёта аннуитетных платежей;

корректность расчёта дифференцированных платежей;

обработку пустых и некорректных данных;

сохранение истории запросов;

доступ менеджера к аналитике.

Безопасность
Не храните токен бота в открытом виде в репозитории.

Используйте переменные окружения или .env.

При утечке токена перевыпустите его через @BotFather.

Не публикуйте менеджерский пароль.

Возможные улучшения
Перевод хранения данных на PostgreSQL.

Экспорт графика в PDF или CSV.

Inline-кнопки вместо текстового ввода.

Отдельная админ-панель для менеджеров.

Досрочное погашение и пересчёт графика.

Лицензия
Проект создан в учебных целях.

