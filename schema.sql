-- =========================
-- MONTHLY FUNDS
-- =========================
CREATE TABLE monthly_funds (
    id UUID PRIMARY KEY,
    year INT NOT NULL,
    month INT NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    created_at TIMESTAMP NOT NULL,

    CONSTRAINT uq_month UNIQUE (year, month)
);

-- =========================
-- MONTHLY SAVINGS
-- =========================
CREATE TABLE monthly_savings (
    id UUID PRIMARY KEY,
    amount DECIMAL(12,2) NOT NULL
);

-- =========================
-- CYCLIC EXPENSES
-- =========================
CREATE TABLE cyclic_expenses (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,

    cycle_interval INT NOT NULL,   -- co ile miesięcy (1 = co miesiąc)
    total_cycles INT,              -- NULL = bezterminowy

    active BOOLEAN NOT NULL DEFAULT true
);

-- =========================
-- CYCLIC EXPENSE RATES
-- =========================
CREATE TABLE cyclic_expense_rates (
    id UUID PRIMARY KEY,
    cyclic_expense_id UUID NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    valid_from DATE NOT NULL,

    CONSTRAINT fk_cyclic_expense
        FOREIGN KEY (cyclic_expense_id)
        REFERENCES cyclic_expenses(id)
        ON DELETE CASCADE
);

-- =========================
-- EXPENSES (DAILY)
-- =========================
CREATE TABLE expenses (
    id UUID PRIMARY KEY,
    amount DECIMAL(12,2) NOT NULL,
    category VARCHAR(100),
    spent_at DATE NOT NULL,
    created_at TIMESTAMP NOT NULL
);
