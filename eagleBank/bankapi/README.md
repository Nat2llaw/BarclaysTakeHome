# Eagle Bank API

A REST API for Eagle Bank built with Spring Boot, implementing the account management and transaction endpoints defined in the OpenAPI specification.

## Tech Stack

- **Java 17**
- **Spring Boot 4.0.6**
- **Spring Security** with stateless JWT authentication (JJWT 0.12.6)
- **Spring Data JPA** with **H2** (in-memory, for local development)
- **Lombok** + **Jakarta Bean Validation**
- **Maven**

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.x
- `EAGLE_BANK_JWT_SECRET` environment variable set (minimum 256-bit / 32-character string)

### Run

```bash
export EAGLE_BANK_JWT_SECRET=your-secret-here
cd bankapi
mvn spring-boot:run
```

The API starts on `http://localhost:8080`.

#### Local development profile

To enable the H2 console during local development, activate the `dev` profile:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

The H2 console is then available at `http://localhost:8080/h2-console`:
- JDBC URL: `jdbc:h2:mem:eaglebank`
- Username: `sa`
- Password: _(empty)_

The `dev` profile also provides a local fallback for `EAGLE_BANK_JWT_SECRET` so you do not need to set it manually during development.

> **Note:** The H2 console is disabled in the default profile and must never be enabled in any deployed environment.

### Authentication

All endpoints except `POST /v1/users` require a JWT passed as a Bearer token:

```
Authorization: Bearer <token>
```

The token is validated using HMAC-SHA256. The signing key is read from the `EAGLE_BANK_JWT_SECRET` environment variable at startup — the application will fail fast if this variable is absent. The `sub` claim of the token is used as the user ID.

## API Endpoints

Full specification is defined in `openapi.yaml`.

### Accounts

#### `POST /v1/accounts` — Create a bank account

Creates a new bank account for the authenticated user.

**Request body** (`application/json`):
```json
{
  "name": "Personal Bank Account",
  "accountType": "personal"
}
```

**Responses:**

| Status | Description |
|--------|-------------|
| `201` | Account created — returns `BankAccountResponse` |
| `400` | Validation failure — returns `BadRequestErrorResponse` with field-level details |
| `401` | Missing or invalid JWT |
| `403` | Forbidden |
| `500` | Unexpected error |

**Example `201` response:**
```json
{
  "accountNumber": "01482931",
  "sortCode": "10-10-10",
  "name": "Personal Bank Account",
  "accountType": "personal",
  "balance": 0.00,
  "currency": "GBP",
  "createdTimestamp": "2026-05-06T10:00:00Z",
  "updatedTimestamp": "2026-05-06T10:00:00Z"
}
```

#### `GET /v1/accounts` — List accounts

Returns all bank accounts belonging to the authenticated user.

**Responses:**

| Status | Description |
|--------|-------------|
| `200` | Returns `ListBankAccountsResponse` |
| `401` | Missing or invalid JWT |
| `500` | Unexpected error |

**Example `200` response:**
```json
{
  "accounts": [
    {
      "accountNumber": "01482931",
      "sortCode": "10-10-10",
      "name": "Personal Bank Account",
      "accountType": "personal",
      "balance": 0.00,
      "currency": "GBP",
      "createdTimestamp": "2026-05-06T10:00:00Z",
      "updatedTimestamp": "2026-05-06T10:00:00Z"
    }
  ]
}
```

#### `GET /v1/accounts/{accountNumber}` — Fetch account by account number

Returns the bank account matching the given account number, scoped to the authenticated user.

**Path parameter:** `accountNumber` — must match `^01\d{6}$` (invalid format returns `400`)

**Responses:**

| Status | Description |
|--------|-------------|
| `200` | Returns `BankAccountResponse` |
| `400` | Invalid account number format |
| `401` | Missing or invalid JWT |
| `403` | Account belongs to a different user |
| `404` | Account not found |
| `500` | Unexpected error |

**Example `200` response:**
```json
{
  "accountNumber": "01482931",
  "sortCode": "10-10-10",
  "name": "Personal Bank Account",
  "accountType": "personal",
  "balance": 100.00,
  "currency": "GBP",
  "createdTimestamp": "2026-05-06T10:00:00Z",
  "updatedTimestamp": "2026-05-06T10:00:00Z"
}
```

#### `PATCH /v1/accounts/{accountNumber}` — Update account by account number

Partially updates the bank account. Both fields are optional; omitted fields are left unchanged.

**Path parameter:** `accountNumber` — must match `^01\d{6}$` (invalid format returns `400`)

**Request body** (`application/json`):
```json
{
  "name": "Updated Account Name",
  "accountType": "personal"
}
```

**Responses:**

| Status | Description |
|--------|-------------|
| `200` | Returns updated `BankAccountResponse` |
| `400` | Validation failure — returns `BadRequestErrorResponse` |
| `401` | Missing or invalid JWT |
| `403` | Account belongs to a different user |
| `404` | Account not found |
| `500` | Unexpected error |

#### `DELETE /v1/accounts/{accountNumber}` — Delete account by account number

Deletes the bank account. The account balance must be zero before deletion is permitted.

**Path parameter:** `accountNumber` — must match `^01\d{6}$` (invalid format returns `400`)

**Responses:**

| Status | Description |
|--------|-------------|
| `204` | Account deleted successfully |
| `400` | Account has an outstanding balance / invalid account number format |
| `401` | Missing or invalid JWT |
| `403` | Account belongs to a different user |
| `404` | Account not found |
| `500` | Unexpected error |

---

### Transactions

#### `POST /v1/accounts/{accountNumber}/transactions` — Create a transaction

Deposits or withdraws funds from the specified bank account. The account balance is updated atomically with the transaction record.

**Path parameter:** `accountNumber` — must match `^01\d{6}$` (invalid format returns `400`)

**Request body** (`application/json`):
```json
{
  "amount": 100.00,
  "currency": "GBP",
  "type": "deposit",
  "reference": "Salary payment"
}
```

| Field | Required | Constraints |
|-------|----------|-------------|
| `amount` | Yes | `0.00` – `10000.00` |
| `currency` | Yes | Must be `"GBP"` |
| `type` | Yes | `"deposit"` or `"withdrawal"` |
| `reference` | No | Free text |

**Responses:**

| Status | Description |
|--------|-------------|
| `201` | Transaction created — returns `TransactionResponse` |
| `400` | Validation failure — returns `BadRequestErrorResponse` with field-level details |
| `401` | Missing or invalid JWT |
| `403` | Account belongs to a different user |
| `404` | Account not found |
| `422` | Insufficient funds to process withdrawal |
| `500` | Unexpected error |

**Example `201` response:**
```json
{
  "id": "tan-aB3xYz12",
  "amount": 100.00,
  "currency": "GBP",
  "type": "deposit",
  "reference": "Salary payment",
  "userId": "usr-abc123",
  "createdTimestamp": "2026-05-06T10:00:00Z"
}
```

#### `GET /v1/accounts/{accountNumber}/transactions` — List transactions

Returns all transactions for the specified bank account, scoped to the authenticated user.

**Path parameter:** `accountNumber` — must match `^01\d{6}$` (invalid format returns `400`)

**Responses:**

| Status | Description |
|--------|-------------|
| `200` | Returns `ListTransactionsResponse` |
| `400` | Invalid account number format |
| `401` | Missing or invalid JWT |
| `403` | Account belongs to a different user |
| `404` | Account not found |
| `500` | Unexpected error |

**Example `200` response:**
```json
{
  "transactions": [
    {
      "id": "tan-aB3xYz12",
      "amount": 100.00,
      "currency": "GBP",
      "type": "deposit",
      "reference": "Salary payment",
      "userId": "usr-abc123",
      "createdTimestamp": "2026-05-06T10:00:00Z"
    }
  ]
}
```

#### `GET /v1/accounts/{accountNumber}/transactions/{transactionId}` — Fetch transaction by ID

Returns a single transaction by ID, scoped to the specified account and authenticated user.

**Path parameters:**
- `accountNumber` — must match `^01\d{6}$`
- `transactionId` — must match `^tan-[A-Za-z0-9]+$`

Invalid formats return `400`.

**Responses:**

| Status | Description |
|--------|-------------|
| `200` | Returns `TransactionResponse` |
| `400` | Invalid path parameter format |
| `401` | Missing or invalid JWT |
| `403` | Account belongs to a different user |
| `404` | Account or transaction not found |
| `500` | Unexpected error |

---

### Users

#### `POST /v1/users` — Create a user

Creates a new user. This endpoint is public — no JWT required.

**Request body** (`application/json`):
```json
{
  "name": "Test User",
  "address": {
    "line1": "1 High Street",
    "line2": "Flat 2",
    "town": "London",
    "county": "Greater London",
    "postcode": "EC1A 1BB"
  },
  "phoneNumber": "+447911123456",
  "email": "test@example.com"
}
```

| Field | Required | Constraints |
|-------|----------|-------------|
| `name` | Yes | Non-blank |
| `address.line1` | Yes | Non-blank |
| `address.line2` | No | Free text |
| `address.line3` | No | Free text |
| `address.town` | Yes | Non-blank |
| `address.county` | Yes | Non-blank |
| `address.postcode` | Yes | Non-blank |
| `phoneNumber` | Yes | E.164 format (e.g. `+447911123456`) |
| `email` | Yes | Valid email address |

**Responses:**

| Status | Description |
|--------|-------------|
| `201` | User created — returns `UserResponse` |
| `400` | Validation failure — returns `BadRequestErrorResponse` |
| `409` | Email address already registered |
| `500` | Unexpected error |

#### `GET /v1/users/{userId}` — Fetch user by ID

Returns the user matching the given ID. Users may only fetch their own record.

**Path parameter:** `userId` — must match `^usr-[A-Za-z0-9]+$` (invalid format returns `400`)

**Responses:**

| Status | Description |
|--------|-------------|
| `200` | Returns `UserResponse` |
| `400` | Invalid userId format |
| `401` | Missing or invalid JWT |
| `403` | Requesting user does not own this record |
| `404` | User not found |
| `500` | Unexpected error |

#### `PATCH /v1/users/{userId}` — Update user by ID

Partially updates the user. All fields are optional; omitted fields are left unchanged.

**Path parameter:** `userId` — must match `^usr-[A-Za-z0-9]+$` (invalid format returns `400`)

**Responses:**

| Status | Description |
|--------|-------------|
| `200` | Returns updated `UserResponse` |
| `400` | Validation failure — returns `BadRequestErrorResponse` |
| `401` | Missing or invalid JWT |
| `403` | Requesting user does not own this record |
| `404` | User not found |
| `409` | New email address already registered to another user |
| `500` | Unexpected error |

#### `DELETE /v1/users` — Delete authenticated user

Deletes the authenticated user. The user must have no associated bank accounts before deletion is permitted. The user ID is derived from the JWT — no path parameter is required.

**Responses:**

| Status | Description |
|--------|-------------|
| `204` | User deleted successfully |
| `401` | Missing or invalid JWT |
| `404` | User not found |
| `409` | User has associated bank accounts — delete accounts first |
| `500` | Unexpected error |

## Design Decisions

### Monetary values

`balance` and `amount` are stored and transported as `BigDecimal` (not `double`) to avoid floating-point rounding errors — critical for financial data. The database columns use `precision = 19, scale = 2`. `BigDecimal` arithmetic (`add`, `subtract`, `compareTo`) is used throughout the service layer.

### Concurrency safety

`BankAccount` carries a `@Version Long version` field. JPA uses optimistic locking: concurrent requests that both read the same balance will race to commit, and one will receive an `OptimisticLockException` rather than both silently succeeding and leaving the balance corrupt.

### ID generation

- **User IDs** — `usr-` + UUID v4 (hyphens stripped). UUIDs are statistically collision-free without a DB retry loop.
- **Account numbers** — `01` + 6 random digits, with a DB collision-retry loop (`existsByAccountNumber`).
- **Transaction IDs** — `tan-` + 8 random alphanumeric chars, with a collision-retry loop (`existsById`).

Both account number and transaction ID generators use `SecureRandom` (not `java.util.Random`) to prevent ID enumeration attacks.

### AccountType validation

`accountType` is validated via a custom `@ValidAccountType` constraint backed by `AccountTypeValidator` and the `AccountType` enum. This provides a single place to add new account types — currently only `"personal"` is supported.

### Duplicate email

`POST /v1/users` and `PATCH /v1/users/{userId}` check for email uniqueness before saving using `UserRepository.existsByEmail`. A duplicate returns `409 Conflict` via `DuplicateEmailException` rather than letting the DB unique constraint surface as an opaque 500.

### Secret management

The JWT signing key is read from the `EAGLE_BANK_JWT_SECRET` environment variable — never from a committed properties file. The application fails fast on startup if the variable is absent.

## Testing

Run the full test suite with:

```bash
export EAGLE_BANK_JWT_SECRET=eagle-bank-super-secret-key-which-is-long-enough-for-hs256-algorithm
mvn test
```

103 tests across two layers:

### Unit tests (service layer)

JUnit 5 + Mockito (`@ExtendWith(MockitoExtension.class)`) — no Spring context, fast execution.

| Test class | Scenarios covered |
|---|---|
| `AccountServiceTest` | Create (happy path, field mapping, collision retry, pattern), list, fetch (200/404/403/404-before-403), update (partial, 404, 403), delete (204, 404, 403, 400 non-zero balance) |
| `TransactionServiceTest` | Create (deposit, withdrawal, balance update, field persistence, collision retry, 404, 403, 422), list (200, empty, 404, 403), fetch (200, 404, 403) |
| `UserServiceTest` | Create (happy path, field mapping, ID pattern), fetch (200, 404, 403), update (name, address, phone, email, null fields unchanged, 404, 403), delete (204, 404, 409) |

### Integration tests (controller layer)

`@SpringBootTest` + `MockMvcBuilders.webAppContextSetup` against real H2 database. Tests the full HTTP stack including JWT filter, path variable pattern validation, request body validation, and all HTTP status codes.

| Test class | Scenarios covered |
|---|---|
| `AccountControllerTest` | All 5 account endpoints — 201/400/401/403/404/204 across create, list, fetch, update, delete |
| `TransactionControllerTest` | All 3 transaction endpoints — 201/400/401/403/404/422 across create, list, fetch |
| `UserControllerTest` | All 4 user endpoints — 201/400/401/403/404/409/204 across create, fetch, update, delete |

## Project Structure

```
src/main/java/eagle/bank/bankapi/
├── BankapiApplication.java
├── controller/
│   ├── AccountController.java
│   ├── TransactionController.java
│   └── UserController.java
├── service/
│   ├── AccountService.java
│   ├── TransactionService.java
│   └── UserService.java
├── repository/
│   ├── AccountRepository.java
│   ├── TransactionRepository.java
│   └── UserRepository.java
├── entity/
│   ├── BankAccount.java
│   ├── Transaction.java
│   ├── User.java
│   └── AccountType.java    (enum)
├── dto/
│   ├── AddressDto.java
│   ├── CreateBankAccountRequest.java
│   ├── UpdateBankAccountRequest.java
│   ├── BankAccountResponse.java
│   ├── ListBankAccountsResponse.java
│   ├── CreateTransactionRequest.java
│   ├── TransactionResponse.java
│   ├── ListTransactionsResponse.java
│   ├── CreateUserRequest.java
│   ├── UpdateUserRequest.java
│   ├── UserResponse.java
│   ├── ErrorResponse.java
│   └── BadRequestErrorResponse.java
├── security/
│   ├── JwtService.java
│   ├── JwtAuthenticationFilter.java
│   └── SecurityConfig.java
├── validation/
│   ├── ValidAccountType.java  (custom constraint annotation)
│   └── AccountTypeValidator.java
└── exception/
    ├── GlobalExceptionHandler.java
    ├── AccountNotFoundException.java
    ├── ForbiddenException.java
    ├── AccountBalanceException.java
    ├── InsufficientFundsException.java
    ├── TransactionNotFoundException.java
    ├── UserNotFoundException.java
    ├── UserHasAccountsException.java
    └── DuplicateEmailException.java
```

## Error Responses

### `ErrorResponse` (401, 403, 404, 422, 500)
```json
{
  "message": "Insufficient funds in account 01482931"
}
```

### `BadRequestErrorResponse` (400)
```json
{
  "message": "Invalid details supplied",
  "details": [
    {
      "field": "type",
      "message": "type must be 'deposit' or 'withdrawal'",
      "type": "Pattern"
    }
  ]
}
```

## Configuration Reference

| Property | Default | Description |
|----------|---------|-------------|
| `spring.datasource.url` | `jdbc:h2:mem:eaglebank` | H2 in-memory DB |
| `spring.jpa.hibernate.ddl-auto` | `create-drop` | Schema rebuilt on restart |
| `jwt.secret` | `${EAGLE_BANK_JWT_SECRET}` | HMAC-SHA256 signing key — read from environment variable; app fails fast if absent |
