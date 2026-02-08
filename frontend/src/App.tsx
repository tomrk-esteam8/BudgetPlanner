import type { FormEvent } from 'react'
import { useEffect, useMemo, useState } from 'react'
import { Link, Navigate, Route, Routes } from 'react-router-dom'
import './App.css'
import { createExpense, fetchSummary } from './api/budgetApi'
import type { MonthlySummary } from './api/budgetApi'
import AdminLayout from './admin/AdminLayout'
import AdminCyclicExpensesPage from './admin/AdminCyclicExpensesPage'
import AdminExpensesPage from './admin/AdminExpensesPage'
import AdminMonthlyFundsPage from './admin/AdminMonthlyFundsPage'

function App() {
  const today = new Date()
  const todayIso = today.toISOString().slice(0, 10)

  const [summary, setSummary] = useState<MonthlySummary | null>(null)
  const [summaryLoading, setSummaryLoading] = useState(false)
  const [summaryError, setSummaryError] = useState<string | null>(null)

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

  const currency = useMemo(
    () =>
      new Intl.NumberFormat('en-US', {
        style: 'currency',
        currency: 'USD',
        maximumFractionDigits: 2,
      }),
    [],
  )

  const loadSummary = async () => {
    setSummaryLoading(true)
    setSummaryError(null)
    try {
      const data = await fetchSummary()
      setSummary(data)
    } catch (error) {
      setSummaryError(
        error instanceof Error ? error.message : 'Failed to load summary.',
      )
    } finally {
      setSummaryLoading(false)
    }
  }

  useEffect(() => {
    void loadSummary()
  }, [])

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
      await createExpense({
        amount: amountValue,
        category: categoryInput,
        spentAt: dateInput,
      })
      setExpenseAmount('')
      setExpenseCategory('')
      setExpenseDate(todayIso)
      setExpenseFieldErrors({ amount: '', category: '', spentAt: '' })
      setExpenseSubmitSuccess('Expense added successfully.')
      void loadSummary()
    } catch (error) {
      setExpenseSubmitError(
        error instanceof Error ? error.message : 'Failed to add expense.',
      )
    } finally {
      setExpenseSubmitting(false)
    }
  }

  const snapshotLabel = summary?.date
    ? new Date(`${summary.date}T00:00:00`).toLocaleDateString('en-US', {
        month: 'short',
        year: 'numeric',
      })
    : 'No data'

  const homeContent = (
    <div className="app-shell">
      <div className="app-glow" aria-hidden="true" />
      <main className="container py-4 py-lg-5">
        <div className="d-flex justify-content-end mb-3">
          <Link className="btn btn-outline-light" to="/admin">
            Admin Panel
          </Link>
        </div>
        <div className="row align-items-start gy-4">
          <div className="col-lg-6">
            <div className="glass-card p-4 p-lg-5 float-in">
              <div className="d-flex align-items-center justify-content-between">
                <div>
                  <p className="eyebrow mb-2">Budget Summary</p>
                  <h1 className="hero-title mb-0">Available This Month</h1>
                </div>
                <span className="pill">{snapshotLabel}</span>
              </div>
              {summaryError ? (
                <div className="alert alert-warning mt-3 mb-0">
                  {summaryError}
                </div>
              ) : null}
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
          <div className="col-lg-6">
            <div className="panel float-in delay-1" id="expense-form">
              <div className="panel-header">
                <h2 className="panel-title">Add Expense</h2>
                <span className="muted">Log a new expense entry</span>
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
        </div>
      </main>
    </div>
  )

  return (
    <Routes>
      <Route path="/" element={homeContent} />
      <Route path="/admin" element={<AdminLayout />}>
        <Route index element={<Navigate to="expenses" replace />} />
        <Route path="expenses" element={<AdminExpensesPage />} />
        <Route path="monthly-funds" element={<AdminMonthlyFundsPage />} />
        <Route path="cyclic-expenses" element={<AdminCyclicExpensesPage />} />
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}

export default App
