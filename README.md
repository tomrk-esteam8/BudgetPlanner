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
- Node.js 20+ and npm (for the React frontend)

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

### Run Backend (Spring Boot)
```bash
./mvnw spring-boot:run
```

The API will be available at:
```
http://localhost:8080/api
```

### Run Frontend (React + Vite)
```bash
cd frontend
npm install
npm run dev
```

The UI will be available at:
```
http://localhost:5173
```

The Vite dev server proxies API requests to the backend, so `/api/*` calls are
sent to `http://localhost:8080`.

### Run Backend + Frontend Together (macOS/Linux)
```bash
bash scripts/dev.sh
```

This script runs both dev servers and shuts them down together when you press
Ctrl+C. By default it preloads sample data for the current year.

To skip seed data:
```bash
bash scripts/dev.sh --no-seed
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
The monthly summary endpoint includes a **daily limit** calculated based on remaining days in the month from a specific date. Expenses are calculated only up to the requested date.

**Formula:** `daily limit = available / remaining days` (including the current day)

**Key behavior:**
- **Expenses are date-filtered:** Only expenses on or before the requested date are included
- **Default date:** When year/month are provided without day, defaults to end-of-month (to show full month's expenses)
- **With specific date:** Shows available budget based on expenses up to that date

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
  "available": 2400.00,
  "dailyLimit": 160.00
}
```

In this example, for March 17:
- Only expenses through March 17 are counted (earlier expenses within March)
- Remaining days: 31 - 17 + 1 = 15 days
- Daily limit: 3400.00 / 15 = 226.67

**How values change throughout March as expenses accumulate** (with funds=5000, savings=1000, fixedCosts=1500):
- **March 1** (before any expenses): spent = 0.00, available = 2500.00, daily limit = 2500.00 / 31 = 80.65
- **March 10** (some expenses already recorded): spent = 50.00, available = 2450.00, daily limit = 2450.00 / 22 = 111.36
- **March 17** (more expenses): spent = 100.00, available = 2400.00, daily limit = 2400.00 / 15 = 160.00
- **March 31** (all month's expenses): spent = 240.00, available = 2260.00, daily limit = 2260.00 / 1 = 2260.00

**Request for monthly totals** (omit day to default to end-of-month):
```bash
curl -X GET "http://localhost:8080/api/v1/summary?year=2026&month=3" \
  -H "Content-Type: application/json"
```
Returns summary for March 31 with all month's expenses included.

**Notes:**
- If available ≤ 0: daily limit is always 0
- Expenses are only counted if they fall on or before the requested date

****

## Testing

The project includes comprehensive backend and frontend test coverage:

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

**Run backend UI integration test (summary + expenses):**
```bash
./mvnw test -Dtest=UiSummaryExpensesIntegrationTest
```

**Run frontend unit tests (Vitest):**
```bash
cd frontend
npm test
```

**Run frontend E2E tests (Playwright):**
```bash
cd frontend
npm run test:e2e
```

Playwright tests require the backend and frontend dev servers to be running.

**Test coverage includes:**
- Domain entity tests (4 classes)
- Service layer unit tests with Mockito mocking
- Calculator component tests including cycleInterval validation
- Controller integration tests with MockMvc
- Application context loading test
- Backend UI integration test for summary + expenses
- Frontend unit tests with Vitest + React Testing Library
- Frontend E2E tests with Playwright
- **Validation test suite (42 new tests):**
  - `ValidationTest` (19 tests): Domain entity validation (Expense, MonthlyFunds, CyclicExpense, CyclicExpenseRate)
  - `MonthlySummaryControllerValidationTest` (15 tests): Date parameter validation (year ranges 1900-2100, month 1-12, day 1-31)
  - `MonthlySummaryServiceEdgeCasesTest` (8 tests): Business logic edge cases (null funds, negative available, missing rates, month boundary dates)
  - Centralized error handling with `GlobalExceptionHandler` for HTTP 400 responses with descriptive messages
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

Where:
- **funds**: Total monthly budget allocation
- **savings**: Amount set aside (subtracted from available)
- **fixedCosts**: Sum of all active cyclic expenses for the month
- **spent**: Sum of all individual expenses **up to the requested date** (not the whole month)

The daily limit is calculated based on remaining days in the month from a specific date:
```
dailyLimit = available / remainingDays (including current day)
```

**Expense Filtering by Date:**
Expenses are only included in the `spent` calculation if they occur on or before the requested date. This allows you to:
- Check your budget status at any point during the month
- See how available funds change as expenses accumulate
- Plan daily spending based on actual expenses incurred so far

**Example:** On March 17 in a 31-day month with $800 available and an expense on March 20:
- On March 17: spent = $100 (only March 5, 10 expenses), available = $700, daily limit = $700 / 15 = $46.67
- On March 28: spent = $240 (all expenses including March 20), available = $560, daily limit = $560 / 4 = $140.00

**Date Handling:**
- If no date is specified in the request, uses today's date
- If year/month provided without day, defaults to end-of-month (shows full month's expenses)
- This allows flexible querying: get today's status or plan based on specific dates

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
- January-May 2026: Uses Rate A ($1500)
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
