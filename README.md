# MiniBank (Spring Core)

Консольное учебное банковское приложение на Java + Spring Core + Hibernate.

## Что умеет
- создавать пользователей;
- показывать всех пользователей и их счета;
- создавать дополнительные счета;
- пополнять и снимать деньги;
- переводить между счетами (с комиссией для разных пользователей);
- закрывать счет с переносом остатка;
- завершать работу по команде `EXIT`.

## Технологии
- Java 21
- Spring Core (`spring-context`)
- Hibernate
- Конфигурация через `@Configuration`, `@PropertySource`, `@Component`
- Хранение данных в PostgreSql

## Архитектура
- `User`, `Account` — POJO-модели.
- `UserService`, `AccountService` — бизнес-логика и хранение данных.
- `OperationCommand` + `ConsoleOperationType` — обработка команд (Command pattern).
- `OperationsConsoleListener` — главный цикл приложения.
- `ConsoleInput` — единая точка чтения/валидации консольного ввода.

## Команды
- `USER_CREATE`
- `SHOW_ALL_USERS`
- `ACCOUNT_CREATE`
- `ACCOUNT_DEPOSIT`
- `ACCOUNT_WITHDRAW`
- `ACCOUNT_TRANSFER`
- `ACCOUNT_CLOSE`
- `EXIT`

## Настройки
Файл: `src/main/resources/application.properties`

```properties
account.default-amount=500
account.transfer-commission=0.02

db.driver=org.postgresql.Driver
db.url=jdbc:postgresql://localhost:5432/bank
db.username=postgres
db.password=root
db.dialect=org.hibernate.dialect.PostgreSQLDialect
hibernate.hbm2ddl.auto=update
hibernate.current_session_context_class=thread
hibernate.show_sql=true
hibernate.format_sql=true
```

## Запуск
1. Собрать проект:
```bash
mvn clean package
```
2. Запустить PostgreSql:
```bash
docker run --name minibank-postgres \
-e POSTGRES_DB=bank \
-e POSTGRES_USER=postgres \
-e POSTGRES_PASSWORD=root \
-p 5432:5432 \
-d postgres:16
```
3. Запустить:
```bash
mvn exec:java -Dexec.mainClass="sorokin.java.course.Main"
```

Если `exec-maven-plugin` не настроен, можно запускать из IDE через класс `Main`.

## Дополнительные материалы
- Подробная формулировка Hibernate-ДЗ: `docs/hibernate-homework.md`
- Подсказки: `docs/hibernate-hints.md`
