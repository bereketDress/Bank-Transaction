# Bank of CLI

A CLI-based banking application with a Swing GUI, built with Java 21, Maven, JDBC, PostgreSQL, HikariCP, and BCrypt.

## Features

- User registration and login
- BCrypt password hashing
- Checking and savings accounts
- Hashed account PINs
- Balance checking
- Deposits and withdrawals
- Money transfers
- Transaction history
- Database connection pooling with HikariCP
- Application logging

## Technologies Used

- Java 21
- Java Swing
- Maven
- JDBC
- PostgreSQL
- HikariCP
- BCrypt
- JUnit

## Project Structure

```text
BankOfCLI/
├── .github/
│   └── workflows/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/bankofcli/
│   │   │       ├── api/
│   │   │       ├── document/
│   │   │       ├── enums/
│   │   │       ├── exception/
│   │   │       ├── model/
│   │   │       ├── repository/
│   │   │       ├── service/
│   │   │       ├── ui/
│   │   │       └── util/
│   │   └── resources/
│   │       ├── images/
│   │       ├── db.properties
│   │       ├── logback.xml
│   │       └── schema.sql
│   └── test/
│       └── java/
├── .gitignore
├── pom.xml
└── README.md
