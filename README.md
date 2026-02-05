# Budget Application

A Spring Boot application for managing budget, monthly funds, savings, and cyclic expenses.

## Architecture

The application follows a layered architecture pattern:

- **Domain Layer**: Core business entities
  - MonthlyFunds: Monthly budget allocation
  - MonthlySavings: Savings tracking
  - CyclicExpense: Recurring expenses with historical rates
  - CyclicExpenseRate: Amount rates for cyclic expenses
  - Expense: Individual expense entries
  - MonthlySummary: Aggregated monthly financial summary
  - AccountingMonth: Month utility calculations

- **Application Layer**: Services and Controllers for business logic
  - Services: MonthlySummaryService, CyclicExpenseCalculator, DailyLimitCalculator
  - Controllers: MonthlyFundsController, CyclicExpenseController, ExpenseController, MonthlySummaryController

- **Infrastructure Layer**: Data access objects and repositories
  - Repositories: MonthlyFundsRepository, MonthlySavingsRepository, CyclicExpenseRepository, ExpenseRepository

## Project Structure

```
src/main/java/com/budget/
├── domain/
│   ├── MonthlyFunds.java
│   ├── MonthlySavings.java
│   ├── CyclicExpense.java
│   ├── CyclicExpenseRate.java
│   ├── Expense.java
│   ├── MonthlySummary.java
│   └── AccountingMonth.java
├── application/
│   ├── service/
│   │   ├── MonthlySummaryService.java
│   │   ├── MonthlySummaryServiceImpl.java
│   │   ├── CyclicExpenseCalculator.java
│   │   ├── DailyLimitCalculator.java
│   │   ├── MonthlyFundsService.java
│   │   └── CyclicExpenseService.java
│   └── controller/
│       ├── MonthlyFundsController.java
│       ├── CyclicExpenseController.java
│       ├── ExpenseController.java
│       └── MonthlySummaryController.java
└── infrastructure/
    └── repository/
        ├── MonthlyFundsRepository.java
        ├── MonthlySavingsRepository.java
        ├── CyclicExpenseRepository.java
        └── ExpenseRepository.java

src/test/java/com/budget/
├── domain/           # Domain entity tests
├── application/      # Service and controller integration tests
│   ├── service/
│   └── controller/
└── BudgetApplicationTest.java
```

## Building and Running

### Prerequisites
- Java 25 or higher
- Maven 3.9+ (optional - Maven Wrapper is included)

### Using Maven Wrapper (Recommended)

This project includes a Maven Wrapper that automatically downloads and uses the correct Maven version. No need to install Maven globally.

**On macOS/Linux:**
```bash
./mvnw clean install
./mvnw spring-boot:run
```

**On Windows:**
```cmd
mvnw clean install
mvnw spring-boot:run
```

### Using Global Maven

If you have Maven installed globally, you can use:
```bash
mvn clean install
mvn spring-boot:run
```

### Build
```bash
./mvnw clean install
```

Or with global Maven:
```bash
mvn clean install
```

## API Documentation

### Swagger UI
Interactive API documentation is available at:
```
http://localhost:8080/api/swagger-ui.html
```

### OpenAPI JSON
Raw OpenAPI specification is available at:
```
http://localhost:8080/api/v3/api-docs
```

## API Endpoints

### Monthly Funds
- `POST /v1/monthly-funds` - Create new monthly funds
- `GET /v1/monthly-funds` - Get all monthly funds
- `GET /v1/monthly-funds/{year}/{month}` - Get funds by year and month
- `GET /v1/monthly-funds/{year}` - Get funds by year
- `DELETE /v1/monthly-funds/{id}` - Delete monthly funds

### Cyclic Expenses
- `POST /v1/cyclic-expenses` - Create new cyclic expense with initial rate (required: name, cycleInterval, active, initialAmount, validFrom)
- `GET /v1/cyclic-expenses` - Get all cyclic expenses
- `GET /v1/cyclic-expenses/active` - Get active expenses only
- `GET /v1/cyclic-expenses/{id}` - Get expense by ID
- `DELETE /v1/cyclic-expenses/{id}` - Delete cyclic expense

### Expenses
- `POST /v1/expenses` - Create new expense
- `GET /v1/expenses` - Get all expenses
- `GET /v1/expenses/{id}` - Get expense by ID
- `GET /v1/expenses/category/{category}` - Get expenses by category
- `DELETE /v1/expenses/{id}` - Delete expense

### Monthly Summary
- `GET /v1/summary` - Get monthly budget summary (uses current date by default, or query params for specific date)

## Example Requests

### Create Monthly Funds
```bash
curl -X POST http://localhost:8080/api/v1/monthly-funds \
  -H "Content-Type: application/json" \
  -d '{
    "year": 2026,
    "month": 2,
    "amount": 5000.00
  }'
```

### Create Cyclic Expense
When creating a cyclic expense, you must provide an initial rate that defines when the expense starts and its amount:

```bash
curl -X POST http://localhost:8080/api/v1/cyclic-expenses \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Monthly Rent",
    "cycleInterval": 1,
    "totalCycles": 12,
    "active": true,
    "initialAmount": 1500.00,
    "validFrom": "2026-01-01"
  }'
```

### Create Expense
```bash
curl -X POST http://localhost:8080/api/v1/expenses \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 45.50,
    "category": "Groceries",
    "spentAt": "2026-02-05"
  }'
```

### Get Monthly Summary
The monthly summary endpoint includes a **daily limit** calculated based on remaining days in the month from a specific date.

**Formula:** `daily limit = available / remaining days` (including the current day)

**Request without parameters** (uses current date):
```bash
curl -X GET http://localhost:8080/api/v1/summary \
  -H "Content-Type: application/json"
```

**Request with specific date** (March 17, 2026):
```bash
curl -X GET "http://localhost:8080/api/v1/summary?year=2026&month=3&day=17" \
  -H "Content-Type: application/json"
```

Response:
```json
{
  "date": "2026-03-17",
  "funds": 5000.00,
  "savings": 1000.00,
  "fixedCosts": 1500.00,
  "spent": 100.00,
  "available": 3400.00,
  "dailyLimit": 226.67
}
```

In this example, for March 17:
- Remaining days: 31 - 17 + 1 = 15 days
- Daily limit: 3400.00 / 15 = 226.67

**How daily limit changes throughout the month:**
- March 1 (31 remaining days): 3400.00 / 31 = 109.68
- March 15 (17 remaining days): 3400.00 / 17 = 200.00
- March 31 (1 remaining day): 3400.00 / 1 = 3400.00
- If available ≤ 0: daily limit is 0

****

## Testing

The project includes comprehensive test coverage with 67 tests:

**Run all tests:**
```bash
./mvnw test
```

**Run specific test class:**
```bash
./mvnw test -Dtest=MonthlyFundsControllerTest
```

**Run the yearly integration test:**
```bash
./mvnw test -Dtest=YearlyBudgetIntegrationTest
```

**Test coverage includes:**
- Domain entity tests (4 classes)
- Service layer unit tests with Mockito mocking
- Calculator component tests including cycleInterval validation
- Controller integration tests with MockMvc
- Application context loading test
- **Comprehensive yearly budget integration test** that validates:
  - Full-year budget with monthly allocations
  - Multiple cyclic expenses with different intervals (monthly, trimonthly)
  - Rate changes mid-year
  - Random individual expenses throughout the year
  - Monthly summary calculations including fixed costs, spent amounts, and available budget

## Key Design Decisions

### 1. Monthly Summary Calculation
The available budget is calculated as:
```
available = funds - savings - fixedCosts - spent
```

The daily limit is calculated based on remaining days in the month from a specific date:
```
dailyLimit = available / remainingDays (including current day)
```

**Example:** On March 17 in a 31-day month with $800 available:
- Remaining days: 31 - 17 + 1 = 15 days
- Daily limit: $800 / 15 = $53.33

If no date is specified in the request, the calculation uses today's date. This allows users to see how much they can spend per day based on actual remaining time in the month.

If available ≤ 0, daily limit is always 0.

Savings are subtracted from available funds to represent money set aside.

### 2. Cyclic Expense Intervals
Cyclic expenses support different frequency patterns through the `cycleInterval` field:

- **cycleInterval=1**: Monthly (every month)
- **cycleInterval=3**: Trimonthly (every 3 months)  
- **cycleInterval=6**: Biannual (every 6 months)
- **cycleInterval=12**: Annual (every 12 months)

An expense only applies to a month if:
1. It's active
2. A valid rate exists for that month
3. The month falls within the cycle pattern (calculated from rate's validFrom date)

**Example:** If a trimonthly expense starts on 2026-01-01, it applies in January, April, July, and October.

### 3. Cyclic Expense Rates
Each cyclic expense maintains a history of rates through `CyclicExpenseRate` entries. When calculating an expense for a month:

- The system finds the most recent **active** rate valid before the month starts
- Only rates marked as `active = true` are considered
- When a new rate is added to an expense, all previous rates are automatically deactivated
- This prevents overlapping rates from the same expense and creates a clear audit trail

**How Rate Transitions Work:**

When you add a new rate to a cyclic expense:
1. The new rate is set to `active = true`
2. All existing rates for that expense are set to `active = false`
3. The old rates remain in the database for historical reference (soft deactivation)

**Example Scenario:**
Rent starts at $1500/month on 2026-01-01:
```
Rate A: $1500, validFrom 2026-01-01, active = true
```

On 2026-06-01, rent increases to $1600:
```
Rate A: $1500, validFrom 2026-01-01, active = false  (automatically deactivated)
Rate B: $1600, validFrom 2026-06-01, active = true   (newly added)
```

Calculations for months:
- January-May 2026: Uses Rate B ($1600) if B was added before those months, else Rate A
- June 2026 onwards: Uses Rate B ($1600)
- Only active rates are considered; inactive rates are ignored in calculations

This design ensures:
- No accidental overlaps between rates of the **same** expense
- Clear historical record of all past rates
- Easy debugging and auditing of price changes

## Database

By default, the application uses H2 in-memory database. Configuration can be found in `application.yml`. You can easily switch to PostgreSQL or MySQL by updating the datasource configuration.

## Dependencies

- Spring Boot 4.0.2
- Spring Data JPA
- Spring Web
- Springdoc OpenAPI (Swagger) 2.1.0
- Jackson Datatype JSR310 (Java Time support)
- Lombok
- H2 Database
- Java 25
- JUnit 5
- Mockito (testing)
