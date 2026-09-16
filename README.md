# bankofcli

A Java 21, Maven, JDBC, and PostgreSQL command-line banking application using a layered architecture.

## Features

- User registration and login with BCrypt password hashing
- Checking and saving accounts with separately hashed PINs
- Balance lookup, deposits, withdrawals, and atomic transfers
- Transaction history and database-backed system logs
- SLF4J + Logback console/file logging
- HikariCP connection pooling
- JUnit 5 and H2 dependencies for tests

## Code structure

```text
bankofcli/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── bankofcli/
│   │   │           ├── api/
│   │   │           ├── enums/
│   │   │           ├── exception/
│   │   │           ├── main/
│   │   │           ├── model/
│   │   │           ├── repository/
│   │   │           ├── service/
│   │   │           └── util/
│   │   └── resources/
│   └── test/
│       └── java/
├── logs/
├── target/
├── .gitignore
├── pom.xml
└── README.md
```

`logs/` contains runtime logs, and Maven generates `target/` for build output.
Both directories are excluded from version control. Tests belong in `src/test/java/` (currently empty). Implementation classes live
in `impl/` subpackages under `api/`, `repository/`, and `service/`.

- `api/`: CLI interfaces, shared console input.
- `main/Main.java`: application entry point and service wiring.
- `api/impl/`: user, account, and transaction menus.
- `service/`: `UserService`, `AccountService`, `TransactionService`, and `SystemLogService` interfaces.
- `service/impl/`: business logic for each service.
- `repository/`: database access interfaces.
- `repository/impl/`: JDBC implementations of those interfaces.
- `exception/BankException.java`: the shared application exception.

The CLI calls services, and services call repositories. `TransactionServiceImpl`
keeps balance updates and transaction records in the same database transaction.
`SystemLogService` records internal events; it has no public CLI menu.

## Setup

1. Create a PostgreSQL database named `bank_of_cli`.
2. Run `src/main/resources/schema.sql` against that database.
3. Set `DB_URL`, `DB_USER`, and `DB_PASSWORD`, or edit `db.properties` locally.
4. Run `mvn test`.
5. Run `mvn exec:java`.

Never commit real database credentials. The application never logs passwords or PINs.

