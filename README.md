# Backend — Personal Expense Tracker

Spring Boot 3.3.4 REST API backend application built with Java 21, Spring Data JPA, Spring Security, and MySQL.

## Structure
- `src/main/java/com/expensetracker/config`: Security configuration, CORS, BCrypt encoder.
- `src/main/java/com/expensetracker/controller`: REST controllers for Auth, Transactions, Dashboard, Budgets, Reports, and Health.
- `src/main/java/com/expensetracker/dto`: Request and response DTOs with validation.
- `src/main/java/com/expensetracker/entity`: JPA entities (`User`, `Transaction`, `Budget`).
- `src/main/java/com/expensetracker/exception`: Global exception handling returning structured JSON errors.
- `src/main/java/com/expensetracker/repository`: Spring Data JPA repositories with custom query methods.
- `src/main/java/com/expensetracker/security`: JWT provider, authentication filter, and security context utilities.
- `src/main/java/com/expensetracker/service`: Business logic, budget threshold algorithms, and CSV export.

## Running the Backend

### Standard MySQL Mode
```bash
mvn clean spring-boot:run
```

### Standalone H2 Mode (No MySQL required)
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=h2
```

## Running Tests
```bash
mvn test
```
Runs 11 automated unit and integration tests with JUnit 5 and Mockito.
