import type { FormEvent } from 'react'
import { useEffect, useMemo, useState } from 'react'
import './App.css'
import {
  createCyclicExpense,
  createExpense,
  createMonthlyFunds,
  fetchCyclicExpenses,
  fetchExpenses,
  fetchMonthlyFundsByYear,
  fetchSummary,
} from './api/budgetApi'
import type {
  CyclicExpense,
  Expense,
  MonthlyFunds,
  MonthlySummary,
  SummaryParams,
} from './api/budgetApi'

function App() {
  const today = new Date()
  const todayYear = String(today.getFullYear())
  const todayMonth = String(today.getMonth() + 1)
  const todayDay = String(today.getDate())
  const todayIso = today.toISOString().slice(0, 10)

  const [summary, setSummary] = useState<MonthlySummary | null>(null)
  const [expenses, setExpenses] = useState<Expense[]>([])
  const [summaryLoading, setSummaryLoading] = useState(false)
  const [expensesLoading, setExpensesLoading] = useState(false)
  const [expensesError, setExpensesError] = useState<string | null>(null)
  const [summaryError, setSummaryError] = useState<string | null>(null)
  const [activeTab, setActiveTab] = useState<
    'overview' | 'summary' | 'expense' | 'funds' | 'cyclic' | 'yearly'
  >('overview')
  const [year, setYear] = useState(todayYear)
  const [month, setMonth] = useState(todayMonth)
  const [day, setDay] = useState(todayDay)

  const [expenseAmount, setExpenseAmount] = useState('')
  const [expenseCategory, setExpenseCategory] = useState('')
  const [expenseDate, setExpenseDate] = useState(todayIso)
  const [expenseSubmitting, setExpenseSubmitting] = useState(false)
  const [expenseSubmitError, setExpenseSubmitError] = useState<string | null>(null)
  const [expenseSubmitSuccess, setExpenseSubmitSuccess] = useState<string | null>(null)
  const [expenseFieldErrors, setExpenseFieldErrors] = useState({
    amount: '',
    category: '',
    spentAt: '',
  })

  const [monthlyFundsYear, setMonthlyFundsYear] = useState(todayYear)
  const [monthlyFundsMonth, setMonthlyFundsMonth] = useState(todayMonth)
  const [monthlyFundsAmount, setMonthlyFundsAmount] = useState('')
  const [monthlyFundsSubmitting, setMonthlyFundsSubmitting] = useState(false)
  const [monthlyFundsError, setMonthlyFundsError] = useState<string | null>(null)
  const [monthlyFundsSuccess, setMonthlyFundsSuccess] = useState<string | null>(null)
  const [monthlyFundsFieldErrors, setMonthlyFundsFieldErrors] = useState({
    year: '',
    month: '',
    amount: '',
  })

  const [cyclicName, setCyclicName] = useState('')
  const [cyclicInterval, setCyclicInterval] = useState('1')
  const [cyclicTotalCycles, setCyclicTotalCycles] = useState('')
  const [cyclicAmount, setCyclicAmount] = useState('')
  const [cyclicValidFrom, setCyclicValidFrom] = useState(todayIso)
  const [cyclicActive, setCyclicActive] = useState(true)
  const [cyclicSubmitting, setCyclicSubmitting] = useState(false)
  const [cyclicError, setCyclicError] = useState<string | null>(null)
  const [cyclicSuccess, setCyclicSuccess] = useState<string | null>(null)
  const [cyclicFieldErrors, setCyclicFieldErrors] = useState({
    name: '',
    cycleInterval: '',
    totalCycles: '',
    initialAmount: '',
    validFrom: '',
  })

  const [yearlyOverviewYear, setYearlyOverviewYear] = useState(todayYear)
  const [yearlyOverviewData, setYearlyOverviewData] = useState<MonthlyFunds[]>([])
  const [yearlyOverviewCyclicExpenses, setYearlyOverviewCyclicExpenses] =
    useState<CyclicExpense[]>([])
  const [yearlyOverviewExpenseTotal, setYearlyOverviewExpenseTotal] = useState(0)
  const [yearlyOverviewExpenseCount, setYearlyOverviewExpenseCount] = useState(0)
  const [yearlyOverviewCyclicTotal, setYearlyOverviewCyclicTotal] = useState(0)
  const [yearlyOverviewLoading, setYearlyOverviewLoading] = useState(false)
  const [yearlyOverviewError, setYearlyOverviewError] = useState<string | null>(null)

  const currency = useMemo(
    () =>
      new Intl.NumberFormat('en-US', {
        style: 'currency',
        currency: 'USD',
        maximumFractionDigits: 2,
      }),
    [],
  )

  const loadSummary = async (params?: SummaryParams) => {
    setSummaryLoading(true)
    setSummaryError(null)
    try {
      const data = await fetchSummary(params)
      setSummary(data)
    } catch (error) {
      setSummaryError(
        error instanceof Error ? error.message : 'Failed to load summary.',
      )
    } finally {
      setSummaryLoading(false)
    }
  }

  const loadExpenses = async () => {
    setExpensesLoading(true)
    setExpensesError(null)
    try {
      const data = await fetchExpenses()
      setExpenses(data)
    } catch (error) {
      setExpensesError(
        error instanceof Error ? error.message : 'Failed to load expenses.',
      )
      setExpenses([])
    } finally {
      setExpensesLoading(false)
    }
  }

  useEffect(() => {
    void loadSummary()
    void loadExpenses()
  }, [])

  const fetchFromInputs = () => {
    const hasAnyDate = year || month || day

    if (hasAnyDate && (!year || !month)) {
      setSummaryError('Year and month are required when selecting a date.')
      return
    }

    const params: SummaryParams | undefined = hasAnyDate
      ? {
          year: Number(year),
          month: Number(month),
          day: day ? Number(day) : undefined,
        }
      : undefined

    void loadSummary(params)
  }

  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    fetchFromInputs()
  }

  const handleToday = () => {
    setYear(todayYear)
    setMonth(todayMonth)
    setDay(todayDay)
    void loadSummary({
      year: Number(todayYear),
      month: Number(todayMonth),
      day: Number(todayDay),
    })
  }

  const handleAddExpense = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setExpenseSubmitError(null)
    setExpenseSubmitSuccess(null)

    const formData = new FormData(event.currentTarget)
    const amountInput = (formData.get('amount')?.toString() ?? expenseAmount).trim()
    const categoryInput = (formData.get('category')?.toString() ?? expenseCategory).trim()
    const dateInput = (formData.get('spentAt')?.toString() ?? expenseDate).trim()

    if (amountInput !== expenseAmount) {
      setExpenseAmount(amountInput)
    }
    if (categoryInput !== expenseCategory) {
      setExpenseCategory(categoryInput)
    }
    if (dateInput !== expenseDate) {
      setExpenseDate(dateInput)
    }

    const fieldErrors = {
      amount: amountInput ? '' : 'Amount is required.',
      category: categoryInput ? '' : 'Category is required.',
      spentAt: dateInput ? '' : 'Date is required.',
    }

    setExpenseFieldErrors(fieldErrors)

    if (fieldErrors.amount || fieldErrors.category || fieldErrors.spentAt) {
      return
    }

    const amountValue = Number(amountInput)
    if (Number.isNaN(amountValue) || amountValue <= 0) {
      setExpenseFieldErrors({
        amount: 'Amount must be a positive number.',
        category: '',
        spentAt: '',
      })
      return
    }

    setExpenseSubmitting(true)
    try {
      const createdExpense = await createExpense({
        amount: amountValue,
        category: categoryInput,
        spentAt: dateInput,
      })
      setExpenseAmount('')
      setExpenseCategory('')
      setExpenseDate('')
      setExpenseFieldErrors({ amount: '', category: '', spentAt: '' })
      setExpenseSubmitSuccess('Expense added successfully.')
      setExpenses((prev) => [createdExpense, ...prev])
      void loadExpenses()
      void loadSummary()
    } catch (error) {
      setExpenseSubmitError(
        error instanceof Error ? error.message : 'Failed to add expense.',
      )
    } finally {
      setExpenseSubmitting(false)
    }
  }

  const handleAddMonthlyFunds = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setMonthlyFundsError(null)
    setMonthlyFundsSuccess(null)

    const yearValue = Number(monthlyFundsYear)
    const monthValue = Number(monthlyFundsMonth)
    const amountValue = Number(monthlyFundsAmount)

    const fieldErrors = {
      year:
        Number.isNaN(yearValue) || yearValue < 1900 || yearValue > 2100
          ? 'Year must be between 1900 and 2100.'
          : '',
      month:
        Number.isNaN(monthValue) || monthValue < 1 || monthValue > 12
          ? 'Month must be between 1 and 12.'
          : '',
      amount:
        Number.isNaN(amountValue) || amountValue <= 0
          ? 'Amount must be a positive number.'
          : '',
    }

    setMonthlyFundsFieldErrors(fieldErrors)
    if (fieldErrors.year || fieldErrors.month || fieldErrors.amount) {
      return
    }

    setMonthlyFundsSubmitting(true)
    try {
      const created = await createMonthlyFunds({
        year: yearValue,
        month: monthValue,
        amount: amountValue,
      })
      setMonthlyFundsAmount('')
      setMonthlyFundsFieldErrors({ year: '', month: '', amount: '' })
      setMonthlyFundsSuccess('Monthly funds saved.')
      if (String(created.year) === yearlyOverviewYear) {
        setYearlyOverviewData((prev) => [created, ...prev])
      }
      void loadSummary({
        year: yearValue,
        month: monthValue,
        day: day ? Number(day) : undefined,
      })
    } catch (error) {
      setMonthlyFundsError(
        error instanceof Error ? error.message : 'Failed to save monthly funds.',
      )
    } finally {
      setMonthlyFundsSubmitting(false)
    }
  }

  const handleAddCyclicExpense = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setCyclicError(null)
    setCyclicSuccess(null)

    const intervalValue = Number(cyclicInterval)
    const totalCyclesValue = cyclicTotalCycles ? Number(cyclicTotalCycles) : undefined
    const amountValue = Number(cyclicAmount)

    const fieldErrors = {
      name: cyclicName.trim() ? '' : 'Name is required.',
      cycleInterval:
        Number.isNaN(intervalValue) || intervalValue < 1
          ? 'Cycle interval must be at least 1.'
          : '',
      totalCycles:
        totalCyclesValue !== undefined && (Number.isNaN(totalCyclesValue) || totalCyclesValue < 1)
          ? 'Total cycles must be at least 1 when provided.'
          : '',
      initialAmount:
        Number.isNaN(amountValue) || amountValue <= 0
          ? 'Amount must be a positive number.'
          : '',
      validFrom: cyclicValidFrom ? '' : 'Valid from date is required.',
    }

    setCyclicFieldErrors(fieldErrors)
    if (
      fieldErrors.name ||
      fieldErrors.cycleInterval ||
      fieldErrors.totalCycles ||
      fieldErrors.initialAmount ||
      fieldErrors.validFrom
    ) {
      return
    }

    setCyclicSubmitting(true)
    try {
      await createCyclicExpense({
        name: cyclicName.trim(),
        cycleInterval: intervalValue,
        totalCycles: totalCyclesValue,
        active: cyclicActive,
        initialAmount: amountValue,
        validFrom: cyclicValidFrom,
      })
      setCyclicName('')
      setCyclicInterval('1')
      setCyclicTotalCycles('')
      setCyclicActive(true)
      setCyclicAmount('')
      setCyclicValidFrom(todayIso)
      setCyclicFieldErrors({
        name: '',
        cycleInterval: '',
        totalCycles: '',
        initialAmount: '',
        validFrom: '',
      })
      setCyclicSuccess('Cyclic expense saved.')
      void loadSummary({
        year: Number(year),
        month: Number(month),
        day: day ? Number(day) : undefined,
      })
    } catch (error) {
      setCyclicError(
        error instanceof Error ? error.message : 'Failed to save cyclic expense.',
      )
    } finally {
      setCyclicSubmitting(false)
    }
  }

  const handleLoadYearlyOverview = async (event?: FormEvent<HTMLFormElement>) => {
    event?.preventDefault()
    setYearlyOverviewError(null)

    const yearValue = Number(yearlyOverviewYear)
    if (Number.isNaN(yearValue) || yearValue < 1900 || yearValue > 2100) {
      setYearlyOverviewError('Year must be between 1900 and 2100.')
      return
    }

    setYearlyOverviewLoading(true)
    try {
      const [fundsData, expensesData, cyclicData] = await Promise.all([
        fetchMonthlyFundsByYear(yearValue),
        fetchExpenses(),
        fetchCyclicExpenses(),
      ])

      setYearlyOverviewData(fundsData)
      setYearlyOverviewCyclicExpenses(cyclicData)

      const yearExpenses = expensesData.filter((expense) => {
        const expenseYear = new Date(`${expense.spentAt}T00:00:00`).getFullYear()
        return expenseYear === yearValue
      })

      const expenseTotal = yearExpenses.reduce(
        (total, expense) => total + expense.amount,
        0,
      )

      const endOfYear = new Date(yearValue, 11, 31)
      const cyclicTotal = cyclicData.reduce((total, expense) => {
        const activeRates = expense.rates.filter((rate) => rate.active)
        const applicable = activeRates
          .filter((rate) => new Date(`${rate.validFrom}T00:00:00`) <= endOfYear)
          .sort((a, b) => b.validFrom.localeCompare(a.validFrom))
        const fallback = activeRates.sort((a, b) => b.validFrom.localeCompare(a.validFrom))
        const rate = (applicable.length > 0 ? applicable : fallback)[0]
        return total + (rate?.amount ?? 0)
      }, 0)

      setYearlyOverviewExpenseCount(yearExpenses.length)
      setYearlyOverviewExpenseTotal(expenseTotal)
      setYearlyOverviewCyclicTotal(cyclicTotal)
    } catch (error) {
      setYearlyOverviewError(
        error instanceof Error ? error.message : 'Failed to load yearly overview.',
      )
      setYearlyOverviewData([])
      setYearlyOverviewCyclicExpenses([])
      setYearlyOverviewExpenseTotal(0)
      setYearlyOverviewExpenseCount(0)
      setYearlyOverviewCyclicTotal(0)
    } finally {
      setYearlyOverviewLoading(false)
    }
  }

  const snapshotLabel = summary?.date
    ? new Date(`${summary.date}T00:00:00`).toLocaleDateString('en-US', {
        month: 'short',
        year: 'numeric',
      })
    : 'No data'

  const recentExpenses = [...expenses]
    .sort((a, b) => b.spentAt.localeCompare(a.spentAt))
    .slice(0, 5)

  return (
    <div className="app-shell">
      <div className="app-glow" aria-hidden="true" />
      <header className="container py-4 py-lg-5">
        <div className="row align-items-center gy-4">
          <div className="col-lg-7">
            <p className="eyebrow">Budget Planner</p>
            <h1 className="hero-title">
              Plan the month, spend with confidence.
            </h1>
            <p className="hero-subtitle">
              Track funds, savings, fixed costs, and daily limits. Query any day
              in the month to see how expenses change your runway.
            </p>
            <div className="d-flex flex-wrap gap-3 mt-4">
              <button
                className="btn btn-primary btn-lg px-4"
                type="button"
                onClick={() => setActiveTab('summary')}
              >
                View Summary
              </button>
              <button
                className="btn btn-outline-light btn-lg px-4"
                type="button"
                onClick={() => setActiveTab('expense')}
              >
                Add Expense
              </button>
            </div>
          </div>
          <div className="col-lg-5">
            <div className="glass-card p-4 p-lg-5 float-in delay-1">
              <div className="d-flex align-items-center justify-content-between">
                <span className="muted">Monthly Snapshot</span>
                <span className="pill">{snapshotLabel}</span>
              </div>
              <div className="display-6 mt-3">
                {summaryLoading
                  ? 'Loading...'
                  : summary
                    ? currency.format(summary.available)
                    : '$0.00'}
              </div>
              <div className="muted">Available after expenses</div>
              <div className="row g-3 mt-4">
                <div className="col-6">
                  <div className="mini-card">
                    <span className="muted">Daily Limit</span>
                    <div className="value">
                      {summary ? currency.format(summary.dailyLimit) : '$0.00'}
                    </div>
                  </div>
                </div>
                <div className="col-6">
                  <div className="mini-card">
                    <span className="muted">Spent to Date</span>
                    <div className="value">
                      {summary ? currency.format(summary.spent) : '$0.00'}
                    </div>
                  </div>
                </div>
                <div className="col-6">
                  <div className="mini-card">
                    <span className="muted">Fixed Costs</span>
                    <div className="value">
                      {summary ? currency.format(summary.fixedCosts) : '$0.00'}
                    </div>
                  </div>
                </div>
                <div className="col-6">
                  <div className="mini-card">
                    <span className="muted">Savings</span>
                    <div className="value">
                      {summary ? currency.format(summary.savings) : '$0.00'}
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </header>

      <main className="container pb-5">
        <div className="budget-tabs mb-4">
          <ul className="nav nav-tabs" role="tablist">
            {[
              { id: 'overview', label: 'Overview' },
              { id: 'summary', label: 'Summary' },
              { id: 'expense', label: 'Add Expense' },
              { id: 'funds', label: 'Monthly Funds' },
              { id: 'cyclic', label: 'Cyclic Expense' },
              { id: 'yearly', label: 'Yearly Overview' },
            ].map((tab) => (
              <li className="nav-item" role="presentation" key={tab.id}>
                <button
                  className={`nav-link${activeTab === tab.id ? ' active' : ''}`}
                  type="button"
                  role="tab"
                  aria-selected={activeTab === tab.id}
                  onClick={() => setActiveTab(tab.id as typeof activeTab)}
                >
                  {tab.label}
                </button>
              </li>
            ))}
          </ul>
        </div>

        {activeTab === 'overview' ? (
          <section className="row g-4">
            <div className="col-12">
              <div className="panel float-in delay-2">
                <div className="panel-header">
                  <h2 className="panel-title">Latest Expenses</h2>
                  <span className="muted">Most recent entries</span>
                </div>
                {expensesError ? (
                  <div className="alert alert-warning">{expensesError}</div>
                ) : null}
                <div className="table-responsive">
                  <table className="table table-borderless align-middle mb-0">
                    <thead>
                      <tr>
                        <th>Date</th>
                        <th>Category</th>
                        <th>Amount</th>
                        <th className="text-end">Status</th>
                      </tr>
                    </thead>
                    <tbody>
                      {expensesLoading ? (
                        <tr>
                          <td colSpan={4} className="text-center muted py-4">
                            Loading expenses...
                          </td>
                        </tr>
                      ) : recentExpenses.length === 0 ? (
                        <tr>
                          <td colSpan={4} className="text-center muted py-4">
                            No expenses yet.
                          </td>
                        </tr>
                      ) : (
                        recentExpenses.map((expense) => (
                          <tr key={`${expense.spentAt}-${expense.category}`}>
                            <td>{expense.spentAt}</td>
                            <td>{expense.category}</td>
                            <td>{currency.format(expense.amount)}</td>
                            <td className="text-end">
                              <span className="badge rounded-pill text-bg-success">
                                Cleared
                              </span>
                            </td>
                          </tr>
                        ))
                      )}
                    </tbody>
                  </table>
                </div>
              </div>
            </div>
          </section>
        ) : null}

        {activeTab === 'summary' ? (
          <section className="row g-4">
            <div className="col-12">
              <div className="panel float-in delay-1">
                <div className="panel-header">
                  <h2 className="panel-title">Create Monthly Summary</h2>
                  <span className="muted">Query by date</span>
                </div>
                <form className="row g-3" onSubmit={handleSubmit}>
                  <div className="col-md-4">
                    <label className="form-label" htmlFor="summary-year">
                      Year
                    </label>
                    <input
                      className="form-control"
                      type="number"
                      min="1900"
                      max="2100"
                      id="summary-year"
                      name="year"
                      value={year}
                      onChange={(event) => setYear(event.target.value)}
                      placeholder="2026"
                    />
                  </div>
                  <div className="col-md-4">
                    <label className="form-label" htmlFor="summary-month">
                      Month
                    </label>
                    <input
                      className="form-control"
                      type="number"
                      min="1"
                      max="12"
                      id="summary-month"
                      name="month"
                      value={month}
                      onChange={(event) => setMonth(event.target.value)}
                      placeholder="2"
                    />
                  </div>
                  <div className="col-md-4">
                    <label className="form-label" htmlFor="summary-day">
                      Day (optional)
                    </label>
                    <input
                      className="form-control"
                      type="number"
                      min="1"
                      max="31"
                      id="summary-day"
                      name="day"
                      value={day}
                      onChange={(event) => setDay(event.target.value)}
                      placeholder="17"
                    />
                  </div>
                  {summaryError ? (
                    <div className="col-12">
                      <div className="alert alert-warning mb-0">{summaryError}</div>
                    </div>
                  ) : null}
                  <div className="col-12 d-flex flex-wrap gap-2">
                    <button
                      type="submit"
                      className="btn btn-primary"
                      disabled={summaryLoading}
                    >
                      {summaryLoading ? 'Fetching...' : 'Fetch Summary'}
                    </button>
                    <button
                      type="button"
                      className="btn btn-outline-light"
                      onClick={handleToday}
                    >
                      Use Today
                    </button>
                    <span className="helper">
                      Leaving day blank defaults to month end.
                    </span>
                  </div>
                </form>
              </div>
            </div>
          </section>
        ) : null}

        {activeTab === 'expense' ? (
          <section className="row g-4">
            <div className="col-12">
              <div className="panel float-in delay-2" id="expense-form">
                <div className="panel-header">
                  <h2 className="panel-title">Log Expense</h2>
                  <span className="muted">Add a new entry</span>
                </div>
                <form className="row g-3" onSubmit={handleAddExpense}>
                  <div className="col-md-4">
                    <label className="form-label" htmlFor="expense-amount">
                      Amount
                    </label>
                    <input
                      className={`form-control${
                        expenseFieldErrors.amount ? ' is-invalid' : ''
                      }`}
                      type="number"
                      step="0.01"
                      min="0.01"
                      id="expense-amount"
                      name="amount"
                      value={expenseAmount}
                      onChange={(event) => setExpenseAmount(event.target.value)}
                      placeholder="45.50"
                    />
                    {expenseFieldErrors.amount ? (
                      <div className="invalid-feedback">
                        {expenseFieldErrors.amount}
                      </div>
                    ) : null}
                  </div>
                  <div className="col-md-4">
                    <label className="form-label" htmlFor="expense-category">
                      Category
                    </label>
                    <input
                      className={`form-control${
                        expenseFieldErrors.category ? ' is-invalid' : ''
                      }`}
                      type="text"
                      id="expense-category"
                      name="category"
                      value={expenseCategory}
                      onChange={(event) => setExpenseCategory(event.target.value)}
                      placeholder="Groceries"
                    />
                    {expenseFieldErrors.category ? (
                      <div className="invalid-feedback">
                        {expenseFieldErrors.category}
                      </div>
                    ) : null}
                  </div>
                  <div className="col-md-4">
                    <label className="form-label" htmlFor="expense-date">
                      Date
                    </label>
                    <input
                      className={`form-control${
                        expenseFieldErrors.spentAt ? ' is-invalid' : ''
                      }`}
                      type="date"
                      id="expense-date"
                      name="spentAt"
                      value={expenseDate}
                      onChange={(event) => setExpenseDate(event.target.value)}
                    />
                    {expenseFieldErrors.spentAt ? (
                      <div className="invalid-feedback">
                        {expenseFieldErrors.spentAt}
                      </div>
                    ) : null}
                  </div>
                  {expenseSubmitError ? (
                    <div className="col-12">
                      <div className="alert alert-warning mb-0">
                        {expenseSubmitError}
                      </div>
                    </div>
                  ) : null}
                  {expenseSubmitSuccess ? (
                    <div className="col-12">
                      <div className="alert alert-success mb-0">
                        {expenseSubmitSuccess}
                      </div>
                    </div>
                  ) : null}
                  <div className="col-12">
                    <button
                      type="submit"
                      className="btn btn-primary w-100"
                      disabled={expenseSubmitting}
                    >
                      {expenseSubmitting ? 'Saving...' : 'Add Expense'}
                    </button>
                  </div>
                </form>
              </div>
            </div>
          </section>
        ) : null}

        {activeTab === 'funds' ? (
          <section className="row g-4">
            <div className="col-12">
              <div className="panel float-in delay-2" id="monthly-funds-form">
                <div className="panel-header">
                  <h2 className="panel-title">Add Monthly Funds</h2>
                  <span className="muted">Budget allocation</span>
                </div>
                <form className="row g-3" onSubmit={handleAddMonthlyFunds}>
                  <div className="col-md-4">
                    <label className="form-label" htmlFor="monthly-funds-year">
                      Year
                    </label>
                    <input
                      className={`form-control${
                        monthlyFundsFieldErrors.year ? ' is-invalid' : ''
                      }`}
                      type="number"
                      min="1900"
                      max="2100"
                      id="monthly-funds-year"
                      value={monthlyFundsYear}
                      onChange={(event) => setMonthlyFundsYear(event.target.value)}
                    />
                    {monthlyFundsFieldErrors.year ? (
                      <div className="invalid-feedback">
                        {monthlyFundsFieldErrors.year}
                      </div>
                    ) : null}
                  </div>
                  <div className="col-md-4">
                    <label className="form-label" htmlFor="monthly-funds-month">
                      Month
                    </label>
                    <input
                      className={`form-control${
                        monthlyFundsFieldErrors.month ? ' is-invalid' : ''
                      }`}
                      type="number"
                      min="1"
                      max="12"
                      id="monthly-funds-month"
                      value={monthlyFundsMonth}
                      onChange={(event) => setMonthlyFundsMonth(event.target.value)}
                    />
                    {monthlyFundsFieldErrors.month ? (
                      <div className="invalid-feedback">
                        {monthlyFundsFieldErrors.month}
                      </div>
                    ) : null}
                  </div>
                  <div className="col-md-4">
                    <label className="form-label" htmlFor="monthly-funds-amount">
                      Amount
                    </label>
                    <input
                      className={`form-control${
                        monthlyFundsFieldErrors.amount ? ' is-invalid' : ''
                      }`}
                      type="number"
                      step="0.01"
                      min="0.01"
                      id="monthly-funds-amount"
                      value={monthlyFundsAmount}
                      onChange={(event) => setMonthlyFundsAmount(event.target.value)}
                      placeholder="5000"
                    />
                    {monthlyFundsFieldErrors.amount ? (
                      <div className="invalid-feedback">
                        {monthlyFundsFieldErrors.amount}
                      </div>
                    ) : null}
                  </div>
                  {monthlyFundsError ? (
                    <div className="col-12">
                      <div className="alert alert-warning mb-0">
                        {monthlyFundsError}
                      </div>
                    </div>
                  ) : null}
                  {monthlyFundsSuccess ? (
                    <div className="col-12">
                      <div className="alert alert-success mb-0">
                        {monthlyFundsSuccess}
                      </div>
                    </div>
                  ) : null}
                  <div className="col-12">
                    <button
                      type="submit"
                      className="btn btn-primary w-100"
                      disabled={monthlyFundsSubmitting}
                    >
                      {monthlyFundsSubmitting ? 'Saving...' : 'Save Monthly Funds'}
                    </button>
                  </div>
                </form>
              </div>
            </div>
          </section>
        ) : null}

        {activeTab === 'cyclic' ? (
          <section className="row g-4">
            <div className="col-12">
              <div className="panel float-in delay-2" id="cyclic-expense-form">
                <div className="panel-header">
                  <h2 className="panel-title">Add Cyclic Expense</h2>
                  <span className="muted">Recurring costs</span>
                </div>
                <form className="row g-3" onSubmit={handleAddCyclicExpense}>
                  <div className="col-12">
                    <label className="form-label" htmlFor="cyclic-name">
                      Name
                    </label>
                    <input
                      className={`form-control${
                        cyclicFieldErrors.name ? ' is-invalid' : ''
                      }`}
                      type="text"
                      id="cyclic-name"
                      value={cyclicName}
                      onChange={(event) => setCyclicName(event.target.value)}
                      placeholder="Rent"
                    />
                    {cyclicFieldErrors.name ? (
                      <div className="invalid-feedback">{cyclicFieldErrors.name}</div>
                    ) : null}
                  </div>
                  <div className="col-md-4">
                    <label className="form-label" htmlFor="cyclic-interval">
                      Cycle (months)
                    </label>
                    <input
                      className={`form-control${
                        cyclicFieldErrors.cycleInterval ? ' is-invalid' : ''
                      }`}
                      type="number"
                      min="1"
                      id="cyclic-interval"
                      value={cyclicInterval}
                      onChange={(event) => setCyclicInterval(event.target.value)}
                    />
                    {cyclicFieldErrors.cycleInterval ? (
                      <div className="invalid-feedback">
                        {cyclicFieldErrors.cycleInterval}
                      </div>
                    ) : null}
                  </div>
                  <div className="col-md-4">
                    <label className="form-label" htmlFor="cyclic-total-cycles">
                      Total cycles
                    </label>
                    <input
                      className={`form-control${
                        cyclicFieldErrors.totalCycles ? ' is-invalid' : ''
                      }`}
                      type="number"
                      min="1"
                      id="cyclic-total-cycles"
                      value={cyclicTotalCycles}
                      onChange={(event) => setCyclicTotalCycles(event.target.value)}
                      placeholder="12"
                    />
                    {cyclicFieldErrors.totalCycles ? (
                      <div className="invalid-feedback">
                        {cyclicFieldErrors.totalCycles}
                      </div>
                    ) : null}
                  </div>
                  <div className="col-md-4">
                    <label className="form-label" htmlFor="cyclic-amount">
                      Amount
                    </label>
                    <input
                      className={`form-control${
                        cyclicFieldErrors.initialAmount ? ' is-invalid' : ''
                      }`}
                      type="number"
                      step="0.01"
                      min="0.01"
                      id="cyclic-amount"
                      value={cyclicAmount}
                      onChange={(event) => setCyclicAmount(event.target.value)}
                      placeholder="1500"
                    />
                    {cyclicFieldErrors.initialAmount ? (
                      <div className="invalid-feedback">
                        {cyclicFieldErrors.initialAmount}
                      </div>
                    ) : null}
                  </div>
                  <div className="col-md-6">
                    <label className="form-label" htmlFor="cyclic-valid-from">
                      Valid from
                    </label>
                    <input
                      className={`form-control${
                        cyclicFieldErrors.validFrom ? ' is-invalid' : ''
                      }`}
                      type="date"
                      id="cyclic-valid-from"
                      value={cyclicValidFrom}
                      onChange={(event) => setCyclicValidFrom(event.target.value)}
                    />
                    {cyclicFieldErrors.validFrom ? (
                      <div className="invalid-feedback">
                        {cyclicFieldErrors.validFrom}
                      </div>
                    ) : null}
                  </div>
                  <div className="col-md-6 d-flex align-items-end">
                    <div className="form-check">
                      <input
                        className="form-check-input"
                        type="checkbox"
                        id="cyclic-active"
                        checked={cyclicActive}
                        onChange={(event) => setCyclicActive(event.target.checked)}
                      />
                      <label className="form-check-label" htmlFor="cyclic-active">
                        Active
                      </label>
                    </div>
                  </div>
                  {cyclicError ? (
                    <div className="col-12">
                      <div className="alert alert-warning mb-0">{cyclicError}</div>
                    </div>
                  ) : null}
                  {cyclicSuccess ? (
                    <div className="col-12">
                      <div className="alert alert-success mb-0">{cyclicSuccess}</div>
                    </div>
                  ) : null}
                  <div className="col-12">
                    <button
                      type="submit"
                      className="btn btn-primary w-100"
                      disabled={cyclicSubmitting}
                    >
                      {cyclicSubmitting ? 'Saving...' : 'Save Cyclic Expense'}
                    </button>
                  </div>
                </form>
              </div>
            </div>
          </section>
        ) : null}

        {activeTab === 'yearly' ? (
          <section className="row g-4">
            <div className="col-12">
              <div className="panel float-in delay-2" id="yearly-overview">
                <div className="panel-header">
                  <h2 className="panel-title">Yearly Overview</h2>
                  <span className="muted">Monthly funds by year</span>
                </div>
                <form className="row g-3" onSubmit={handleLoadYearlyOverview}>
                  <div className="col-md-3">
                    <label className="form-label" htmlFor="yearly-overview-year">
                      Year
                    </label>
                    <input
                      className="form-control"
                      type="number"
                      min="1900"
                      max="2100"
                      id="yearly-overview-year"
                      value={yearlyOverviewYear}
                      onChange={(event) => setYearlyOverviewYear(event.target.value)}
                    />
                  </div>
                  <div className="col-md-3 d-flex align-items-end">
                    <button type="submit" className="btn btn-outline-light w-100">
                      {yearlyOverviewLoading ? 'Loading...' : 'Load Overview'}
                    </button>
                  </div>
                  {yearlyOverviewError ? (
                    <div className="col-12">
                      <div className="alert alert-warning mb-0">
                        {yearlyOverviewError}
                      </div>
                    </div>
                  ) : null}
                </form>
                <div className="row g-3 mt-3">
                  <div className="col-md-6">
                    <div className="mini-card">
                      <span className="muted">Total Expenses (year)</span>
                      <div className="value">
                        {currency.format(yearlyOverviewExpenseTotal)}
                      </div>
                      <div className="helper">
                        {yearlyOverviewExpenseCount} expense entries
                      </div>
                    </div>
                  </div>
                  <div className="col-md-6">
                    <div className="mini-card">
                      <span className="muted">Cyclic Costs (current rate)</span>
                      <div className="value">
                        {currency.format(yearlyOverviewCyclicTotal)}
                      </div>
                      <div className="helper">Active cyclic expense rates</div>
                    </div>
                  </div>
                </div>
                <div className="table-responsive mt-3">
                  <table className="table table-borderless align-middle mb-0">
                    <thead>
                      <tr>
                        <th>Cyclic Expense</th>
                        <th>Interval</th>
                        <th>Status</th>
                        <th className="text-end">Current Amount</th>
                      </tr>
                    </thead>
                    <tbody>
                      {yearlyOverviewLoading ? (
                        <tr>
                          <td colSpan={4} className="text-center muted py-4">
                            Loading cyclic expenses...
                          </td>
                        </tr>
                      ) : yearlyOverviewCyclicExpenses.length === 0 ? (
                        <tr>
                          <td colSpan={4} className="text-center muted py-4">
                            No cyclic expenses found.
                          </td>
                        </tr>
                      ) : (
                        yearlyOverviewCyclicExpenses.map((expense) => {
                          const activeRates = expense.rates.filter((rate) => rate.active)
                          const rate = activeRates.sort((a, b) => b.validFrom.localeCompare(a.validFrom))[0]
                          return (
                            <tr key={expense.id ?? expense.name}>
                              <td>{expense.name}</td>
                              <td>Every {expense.cycleInterval} mo</td>
                              <td>{expense.active ? 'Active' : 'Inactive'}</td>
                              <td className="text-end">
                                {rate ? currency.format(rate.amount) : '—'}
                              </td>
                            </tr>
                          )
                        })
                      )}
                    </tbody>
                  </table>
                </div>
                <div className="table-responsive mt-3">
                  <table className="table table-borderless align-middle mb-0">
                    <thead>
                      <tr>
                        <th>Month</th>
                        <th>Amount</th>
                      </tr>
                    </thead>
                    <tbody>
                      {yearlyOverviewLoading ? (
                        <tr>
                          <td colSpan={2} className="text-center muted py-4">
                            Loading overview...
                          </td>
                        </tr>
                      ) : yearlyOverviewData.length === 0 ? (
                        <tr>
                          <td colSpan={2} className="text-center muted py-4">
                            No monthly funds found.
                          </td>
                        </tr>
                      ) : (
                        yearlyOverviewData
                          .slice()
                          .sort((a, b) => a.month - b.month)
                          .map((fund) => (
                            <tr key={`${fund.year}-${fund.month}-${fund.id ?? 'row'}`}>
                              <td>{fund.month}</td>
                              <td>{currency.format(fund.amount)}</td>
                            </tr>
                          ))
                      )}
                    </tbody>
                  </table>
                </div>
              </div>
            </div>
          </section>
        ) : null}
      </main>
    </div>
  )
}

export default App
