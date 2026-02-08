import { useEffect, useMemo, useState } from 'react'
import {
  createMonthlyFunds,
  deleteMonthlyFunds,
  fetchMonthlyFundsPage,
  updateMonthlyFunds,
} from '../api/budgetApi'
import type { MonthlyFunds } from '../api/budgetApi'

const PAGE_SIZE = 10

export default function AdminMonthlyFundsPage() {
  const [items, setItems] = useState<MonthlyFunds[]>([])
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(1)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const [editingId, setEditingId] = useState<number | null>(null)
  const [year, setYear] = useState('')
  const [month, setMonth] = useState('')
  const [amount, setAmount] = useState('')
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
      const data = await fetchMonthlyFundsPage(pageIndex, PAGE_SIZE)
      const content = Array.isArray(data.content) ? data.content : []
      const total = Number.isFinite(data.totalPages) ? data.totalPages : 1
      const currentPage = Number.isFinite(data.number) ? data.number : pageIndex
      setItems(content)
      setTotalPages(Math.max(1, total))
      setPage(currentPage)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load monthly funds.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    void loadPage(page)
  }, [page])

  const resetForm = () => {
    setEditingId(null)
    setYear('')
    setMonth('')
    setAmount('')
    setFormError(null)
  }

  const startEdit = (funds: MonthlyFunds) => {
    setEditingId(funds.id ?? null)
    setYear(String(funds.year))
    setMonth(String(funds.month))
    setAmount(String(funds.amount))
    setFormError(null)
  }

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault()
    const yearValue = Number(year)
    const monthValue = Number(month)
    const amountValue = Number(amount)

    if (
      Number.isNaN(yearValue) ||
      Number.isNaN(monthValue) ||
      Number.isNaN(amountValue) ||
      amountValue <= 0
    ) {
      setFormError('Provide valid year, month, and amount.')
      return
    }

    setFormError(null)
    try {
      if (editingId) {
        await updateMonthlyFunds(editingId, {
          year: yearValue,
          month: monthValue,
          amount: amountValue,
        })
      } else {
        await createMonthlyFunds({
          year: yearValue,
          month: monthValue,
          amount: amountValue,
        })
      }
      resetForm()
      await loadPage(page)
    } catch (err) {
      setFormError(err instanceof Error ? err.message : 'Failed to save monthly funds.')
    }
  }

  const handleDelete = async (id?: number) => {
    if (!id) {
      return
    }
    if (!window.confirm('Delete this monthly funds entry?')) {
      return
    }
    try {
      await deleteMonthlyFunds(id)
      await loadPage(page)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to delete monthly funds.')
    }
  }

  return (
    <section>
      <div className="panel mb-4">
        <div className="panel-header">
          <h2 className="panel-title">Monthly Funds</h2>
          <span className="muted">Create and edit monthly allocations</span>
        </div>
        <form className="row g-3" onSubmit={handleSubmit}>
          <div className="col-md-4">
            <label className="form-label" htmlFor="admin-funds-year">
              Year
            </label>
            <input
              className="form-control"
              id="admin-funds-year"
              type="number"
              min="1900"
              max="2100"
              value={year}
              onChange={(event) => setYear(event.target.value)}
            />
          </div>
          <div className="col-md-4">
            <label className="form-label" htmlFor="admin-funds-month">
              Month
            </label>
            <input
              className="form-control"
              id="admin-funds-month"
              type="number"
              min="1"
              max="12"
              value={month}
              onChange={(event) => setMonth(event.target.value)}
            />
          </div>
          <div className="col-md-4">
            <label className="form-label" htmlFor="admin-funds-amount">
              Amount
            </label>
            <input
              className="form-control"
              id="admin-funds-amount"
              type="number"
              step="0.01"
              min="0.01"
              value={amount}
              onChange={(event) => setAmount(event.target.value)}
            />
          </div>
          {formError ? (
            <div className="col-12">
              <div className="alert alert-warning mb-0">{formError}</div>
            </div>
          ) : null}
          <div className="col-12 d-flex gap-2">
            <button className="btn btn-primary" type="submit">
              {editingId ? 'Save Changes' : 'Add Monthly Funds'}
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
          <h2 className="panel-title">Monthly Funds List</h2>
          <span className="muted">Showing {PAGE_SIZE} per page</span>
        </div>
        {error ? <div className="alert alert-warning">{error}</div> : null}
        <div className="table-responsive">
          <table className="table table-borderless align-middle mb-0">
            <thead>
              <tr>
                <th>Year</th>
                <th>Month</th>
                <th>Amount</th>
                <th className="text-end">Actions</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr>
                  <td colSpan={4} className="text-center muted py-4">
                    Loading monthly funds...
                  </td>
                </tr>
              ) : items.length === 0 ? (
                <tr>
                  <td colSpan={4} className="text-center muted py-4">
                    No monthly funds found.
                  </td>
                </tr>
              ) : (
                items.map((fund) => (
                  <tr key={fund.id ?? `${fund.year}-${fund.month}`}>
                    <td>{fund.year}</td>
                    <td>{fund.month}</td>
                    <td>{currency.format(fund.amount)}</td>
                    <td className="text-end">
                      <div className="btn-group btn-group-sm">
                        <button
                          type="button"
                          className="btn btn-outline-light"
                          onClick={() => startEdit(fund)}
                        >
                          Edit
                        </button>
                        <button
                          type="button"
                          className="btn btn-outline-danger"
                          onClick={() => handleDelete(fund.id)}
                        >
                          Delete
                        </button>
                      </div>
                    </td>
                  </tr>
                ))
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
