# Expense Manager Backend Design Document

## 1. Overview
A Spring Boot REST API backend for a Personal Expense Manager application, supporting secure user authentication, expense CRUD operations, budgeting, analytics, and export capabilities.

---

## 2. Architecture Layers

1. **Model** (entities)
2. **DAO** (data access objects)
3. **Repository** (Spring Data JPA interfaces)
4. **Service** (business logic)
5. **Controller** (REST endpoints)
6. **Security** (JWT & Spring Security)

---

## 3. Packages & Classes

### 3.1 model
- **User**
  ```text
    Long id
    String username
    String password
    String email
    BigDecimal monthlyBudget
    List<Expense> expenses
  ```
  **Responsibility**: Represents application user, stores credentials, email, configured monthly budget, and associated expense list.

- **Expense**
  ```text
    Long id
    String title
    BigDecimal amount
    String category
    LocalDate date
    List<String> tags
    String note
    User user
  ```
  **Responsibility**: Encapsulates a single expense entry, linked to its owner and carrying metadata.

### 3.2 dao
- **UserDao**
  - Custom queries (e.g., find by username/email)
  - Abstracts complex native or JPQL operations not covered by repository methods.

- **ExpenseDao**
  - Methods for filtered fetch (by date range, category, tags)
  - Summary aggregations (total, monthly, by category)

### 3.3 repository
- **UserRepository** extends `JpaRepository<User,Long>`
- **ExpenseRepository** extends `JpaRepository<Expense,Long>`

**Responsibility**: Provide out-of-the-box CRUD, pagination, and simple finder methods.

### 3.4 service
- **UserService**
  - Registration (hash password)
  - Profile management
  - Budget update & validation

- **ExpenseService**
  - Create, read, update, delete expenses
  - Query via DAO for complex filters
  - Compute summaries and aggregations
  - Trigger budget-exceeded alerts when `monthlyTotal > user.monthlyBudget`

### 3.5 controller
- **AuthController** (`/auth`)
  - `POST /signup` – create user, default monthlyBudget = 0
  - `POST /login`  – authenticate and return JWT
  - `POST /auth/refresh` – refresh token

- **ExpenseController** (`/expenses`)
  - `POST`       – add new expense
  - `GET`        – list expenses (filters: category, dateFrom/dateTo, tags)
  - `GET /{id}`  – get expense by ID
  - `PUT /{id}`  – update expense
  - `DELETE /{id}`– delete expense
  - `GET /stats/summary` – get totals and breakdowns
  - `GET /export` – export CSV

- **UserController** (`/users`)
  - `PUT /budget` – update user monthly budget

### 3.6 security
- **JwtUtil**: generate/validate tokens, extract username
- **JwtFilter**: intercept requests, parse Authorization header, set security context
- **SecurityConfig**: configure permitted endpoints, add `JwtFilter`, stateless sessions

---

## 4. Database Choice & Justification
- **PostgreSQL** (production):
  - ACID compliance ensures reliable financial records.
  - Rich query support (window functions, JSON, CTEs) for advanced analytics.
  - Scales well and supports extensions (e.g., `pgcrypto` for encryption).

- **H2** (development & testing):
  - In-memory mode for fast integration tests.
  - Zero configuration simplifies CI pipelines.

---

## 5. Budget Feature
- **monthlyBudget** field on `User` entity.
- **BudgetService** (in `service` layer) checks on every expense create/update:
  ```java
  if (currentMonthTotal.add(newExpense.amount).compareTo(user.monthlyBudget) > 0) {
    // trigger alert (email or push notification)
  }
  ```
- **UserController** endpoint to set or update budget:
  - `PUT /users/budget` with payload `{ "monthlyBudget": BigDecimal }`

---

## 6. Relationships Summary
1. **Entity**: `User 1—* Expense`
2. **DAO ↔ Repository**: DAOs use repositories for basic operations and add custom queries.
3. **Service ↔ DAO**: Services delegate complex data-fetch & summary to DAOs, simple CRUD to repositories.
4. **Controller ↔ Service / Util**: Controllers orchestrate HTTP layer, invoke services or `JwtUtil`.
5. **Security**: `JwtFilter` → `JwtUtil`, `UserService`; `SecurityConfig` → `JwtFilter`, `UserService`.

---

## 7. Endpoints Overview
| Method | Path                        | Description                                |
|--------|-----------------------------|--------------------------------------------|
| POST   | `/auth/signup`              | Register user                             |
| POST   | `/auth/login`               | Login, issue JWT                           |
| POST   | `/auth/refresh`             | Refresh JWT                                |
| POST   | `/expenses`                 | Create expense                             |
| GET    | `/expenses`                 | List expenses with filters & pagination    |
| GET    | `/expenses/{id}`            | Get a single expense                       |
| PUT    | `/expenses/{id}`            | Update expense                             |
| DELETE | `/expenses/{id}`            | Delete expense                             |
| GET    | `/expenses/stats/summary`   | Totals & breakdowns                        |
| GET    | `/expenses/export`          | Export all expenses to CSV                 |
| PUT    | `/users/budget`             | Set or update monthly budget for user      |

---

*This document extends the original design to include a DAO layer for custom queries, a budget feature with alert logic, and rationale for database selection.*
