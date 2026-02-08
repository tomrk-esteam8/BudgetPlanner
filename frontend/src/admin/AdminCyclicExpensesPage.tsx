import { useEffect, useMemo, useState } from 'react'
import {
  createCyclicExpense,
  deleteCyclicExpense,
  fetchCyclicExpensesPage,
  updateCyclicExpense,
} from '../api/budgetApi'
import type { CyclicExpense } from '../api/budgetApi'

const PAGE_SIZE = 10

function getCurrentRate(expense: CyclicExpense) {
  const activeRates = expense.rates.filter((rate) => rate.active)
  const pool = activeRates.length > 0 ? activeRates : expense.rates
  return pool.slice().sort((a, b) => b.validFrom.localeCompare(a.validFrom))[0]
}

export default function AdminCyclicExpensesPage() {
  const [items, setItems] = useState<CyclicExpense[]>([])
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(1)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const [editingId, setEditingId] = useState<string | null>(null)
  const [name, setName] = useState('')
  const [cycleInterval, setCycleInterval] = useState('1')
  const [totalCycles, setTotalCycles] = useState('')
  const [amount, setAmount] = useState('')
  const [validFrom, setValidFrom] = useState('')
  const [active, setActive] = useState(true)
  const [formError, setFormError] = useState<string | null>(null)

  const currency = useMemo(
    () =>
      new Intl.NumberFormat('en-US', {
        style: 'currency',
        currency: 'USD',
        maximumFractionDigits: 2,
      }),
    [],
  )

  const loadPage = async (pageIndex: number) => {
    setLoading(true)
    setError(null)
    try {
      const data = await fetchCyclicExpensesPage(pageIndex, PAGE_SIZE)
      const content = Array.isArray(data.content) ? data.content : []
      const total = Number.isFinite(data.totalPages) ? data.totalPages : 1
      const currentPage = Number.isFinite(data.number) ? data.number : pageIndex
      setItems(content)
      setTotalPages(Math.max(1, total))
      setPage(currentPage)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load cyclic expenses.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    void loadPage(page)
  }, [page])

  const resetForm = () => {
    setEditingId(null)
    setName('')
    setCycleInterval('1')
    setTotalCycles('')
    setAmount('')
    setValidFrom('')
    setActive(true)
    setFormError(null)
  }

  const startEdit = (expense: CyclicExpense) => {
    const rate = getCurrentRate(expense)
    setEditingId(expense.id ?? null)
    setName(expense.name)
    setCycleInterval(String(expense.cycleInterval))
    setTotalCycles(expense.totalCycles ? String(expense.totalCycles) : '')
    setAmount(rate ? String(rate.amount) : '')
    setValidFrom(rate ? rate.validFrom : '')
    setActive(expense.active)
    setFormError(null)
  }

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault()
    const intervalValue = Number(cycleInterval)
    const totalCyclesValue = totalCycles ? Number(totalCycles) : undefined
    const amountValue = Number(amount)

    if (!name.trim() || Number.isNaN(intervalValue) || intervalValue < 1) {
      setFormError('Provide name and a valid cycle interval.')
      return
    }
    if (totalCycles && (Number.isNaN(totalCyclesValue) || totalCyclesValue < 1)) {
      setFormError('Total cycles must be at least 1 when provided.')
      return
    }
    if (!validFrom || Number.isNaN(amountValue) || amountValue <= 0) {
      setFormError('Provide a valid amount and start date.')
      return
    }

    setFormError(null)
    try {
      if (editingId) {
        await updateCyclicExpense(editingId, {
          name: name.trim(),
          cycleInterval: intervalValue,
          totalCycles: totalCyclesValue,
          active,
          amount: amountValue,
          validFrom,
        })
      } else {
        await createCyclicExpense({
          name: name.trim(),
          cycleInterval: intervalValue,
          totalCycles: totalCyclesValue,
          active,
          initialAmount: amountValue,
          validFrom,
        })
      }
      resetForm()
      await loadPage(page)
    } catch (err) {
      setFormError(err instanceof Error ? err.message : 'Failed to save cyclic expense.')
    }
  }

  const handleDelete = async (id?: string) => {
    if (!id) {
      return
    }
    if (!window.confirm('Delete this cyclic expense?')) {
      return
    }
    try {
      await deleteCyclicExpense(id)
      await loadPage(page)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to delete cyclic expense.')
    }
  }

  return (
    <section>
      <div className="panel mb-4">
        <div className="panel-header">
          <h2 className="panel-title">Cyclic Expenses</h2>
          <span className="muted">Create and edit recurring expenses</span>
        </div>
        <form className="row g-3" onSubmit={handleSubmit}>
          <div className="col-md-4">
            <label className="form-label" htmlFor="admin-cyclic-name">
              Name
            </label>
            <input
              className="form-control"
              id="admin-cyclic-name"
              type="text"
              value={name}
              onChange={(event) => setName(event.target.value)}
            />
          </div>
          <div className="col-md-2">
            <label className="form-label" htmlFor="admin-cyclic-interval">
              Cycle (months)
            </label>
            <input
              className="form-control"
              id="admin-cyclic-interval"
              type="number"
              min="1"
              value={cycleInterval}
              onChange={(event) => setCycleInterval(event.target.value)}
            />
          </div>
          <div className="col-md-2">
            <label className="form-label" htmlFor="admin-cyclic-cycles">
              Total cycles
            </label>
            <input
              className="form-control"
              id="admin-cyclic-cycles"
              type="number"
              min="1"
              value={totalCycles}
              onChange={(event) => setTotalCycles(event.target.value)}
            />
          </div>
          <div className="col-md-2">
            <label className="form-label" htmlFor="admin-cyclic-amount">
              Amount
            </label>
            <input
              className="form-control"
              id="admin-cyclic-amount"
              type="number"
              step="0.01"
              min="0.01"
              value={amount}
              onChange={(event) => setAmount(event.target.value)}
            />
          </div>
          <div className="col-md-2">
            <label className="form-label" htmlFor="admin-cyclic-date">
              Valid from
            </label>
            <input
              className="form-control"
              id="admin-cyclic-date"
              type="date"
              value={validFrom}
              onChange={(event) => setValidFrom(event.target.value)}
            />
          </div>
          <div className="col-12 d-flex align-items-center gap-2">
            <div className="form-check">
              <input
                className="form-check-input"
                id="admin-cyclic-active"
                type="checkbox"
                checked={active}
                onChange={(event) => setActive(event.target.checked)}
              />
              <label className="form-check-label" htmlFor="admin-cyclic-active">
                Active
              </label>
            </div>
          </div>
          {formError ? (
            <div className="col-12">
              <div className="alert alert-warning mb-0">{formError}</div>
            </div>
          ) : null}
          <div className="col-12 d-flex gap-2">
            <button className="btn btn-primary" type="submit">
              {editingId ? 'Save Changes' : 'Add Cyclic Expense'}
            </button>
            {editingId ? (
              <button className="btn btn-outline-light" type="button" onClick={resetForm}>
                Cancel
              </button>
            ) : null}
          </div>
        </form>
      </div>

      <div className="panel">
        <div className="panel-header">
          <h2 className="panel-title">Cyclic Expense List</h2>
          <span className="muted">Showing {PAGE_SIZE} per page</span>
        </div>
        {error ? <div className="alert alert-warning">{error}</div> : null}
        <div className="table-responsive">
          <table className="table table-borderless align-middle mb-0">
            <thead>
              <tr>
                <th>Name</th>
                <th>Interval</th>
                <th>Status</th>
                <th>Current Amount</th>
                <th className="text-end">Actions</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr>
                  <td colSpan={5} className="text-center muted py-4">
                    Loading cyclic expenses...
                  </td>
                </tr>
              ) : items.length === 0 ? (
                <tr>
                  <td colSpan={5} className="text-center muted py-4">
                    No cyclic expenses found.
                  </td>
                </tr>
              ) : (
                items.map((expense) => {
                  const rate = getCurrentRate(expense)
                  return (
                    <tr key={expense.id ?? expense.name}>
                      <td>{expense.name}</td>
                      <td>Every {expense.cycleInterval} mo</td>
                      <td>{expense.active ? 'Active' : 'Inactive'}</td>
                      <td>{rate ? currency.format(rate.amount) : '—'}</td>
                      <td className="text-end">
                        <div className="btn-group btn-group-sm">
                          <button
                            type="button"
                            className="btn btn-outline-light"
                            onClick={() => startEdit(expense)}
                          >
                            Edit
                          </button>
                          <button
                            type="button"
                            className="btn btn-outline-danger"
                            onClick={() => handleDelete(expense.id)}
                          >
                            Delete
                          </button>
                        </div>
                      </td>
                    </tr>
                  )
                })
              )}
            </tbody>
          </table>
        </div>
        <div className="d-flex justify-content-between align-items-center mt-3">
          <button
            className="btn btn-outline-light btn-sm"
            type="button"
            onClick={() => setPage((prev) => Math.max(0, prev - 1))}
            disabled={page <= 0}
          >
            Previous
          </button>
          <span className="muted">
            Page {page + 1} of {totalPages}
          </span>
          <button
            className="btn btn-outline-light btn-sm"
            type="button"
            onClick={() => setPage((prev) => Math.min(totalPages - 1, prev + 1))}
            disabled={page + 1 >= totalPages}
          >
            Next
          </button>
        </div>
      </div>
    </section>
  )
}
