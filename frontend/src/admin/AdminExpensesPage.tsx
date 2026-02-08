import { useEffect, useMemo, useState } from 'react'
import {
  deleteExpense,
  fetchExpensesPage,
  updateExpense,
  createExpense,
} from '../api/budgetApi'
import type { Expense } from '../api/budgetApi'

const PAGE_SIZE = 10

export default function AdminExpensesPage() {
  const [items, setItems] = useState<Expense[]>([])
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(1)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const [editingId, setEditingId] = useState<number | null>(null)
  const [amount, setAmount] = useState('')
  const [category, setCategory] = useState('')
  const [spentAt, setSpentAt] = useState('')
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
      const data = await fetchExpensesPage(pageIndex, PAGE_SIZE)
      const content = Array.isArray(data.content) ? data.content : []
      const total = Number.isFinite(data.totalPages) ? data.totalPages : 1
      const currentPage = Number.isFinite(data.number) ? data.number : pageIndex
      setItems(content)
      setTotalPages(Math.max(1, total))
      setPage(currentPage)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load expenses.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    void loadPage(page)
  }, [page])

  const resetForm = () => {
    setEditingId(null)
    setAmount('')
    setCategory('')
    setSpentAt('')
    setFormError(null)
  }

  const startEdit = (expense: Expense) => {
    setEditingId(expense.id ?? null)
    setAmount(String(expense.amount))
    setCategory(expense.category)
    setSpentAt(expense.spentAt)
    setFormError(null)
  }

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault()
    const amountValue = Number(amount)

    if (!category.trim() || !spentAt || Number.isNaN(amountValue) || amountValue <= 0) {
      setFormError('Provide amount, category, and date.')
      return
    }

    setFormError(null)
    try {
      if (editingId) {
        await updateExpense(editingId, {
          amount: amountValue,
          category: category.trim(),
          spentAt,
        })
      } else {
        await createExpense({
          amount: amountValue,
          category: category.trim(),
          spentAt,
        })
      }
      resetForm()
      await loadPage(page)
    } catch (err) {
      setFormError(err instanceof Error ? err.message : 'Failed to save expense.')
    }
  }

  const handleDelete = async (id?: number) => {
    if (!id) {
      return
    }
    if (!window.confirm('Delete this expense?')) {
      return
    }
    try {
      await deleteExpense(id)
      await loadPage(page)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to delete expense.')
    }
  }

  return (
    <section>
      <div className="panel mb-4">
        <div className="panel-header">
          <h2 className="panel-title">Expenses</h2>
          <span className="muted">Create and edit expense entries</span>
        </div>
        <form className="row g-3" onSubmit={handleSubmit}>
          <div className="col-md-4">
            <label className="form-label" htmlFor="admin-expense-amount">
              Amount
            </label>
            <input
              className="form-control"
              id="admin-expense-amount"
              type="number"
              step="0.01"
              min="0.01"
              value={amount}
              onChange={(event) => setAmount(event.target.value)}
            />
          </div>
          <div className="col-md-4">
            <label className="form-label" htmlFor="admin-expense-category">
              Category
            </label>
            <input
              className="form-control"
              id="admin-expense-category"
              type="text"
              value={category}
              onChange={(event) => setCategory(event.target.value)}
            />
          </div>
          <div className="col-md-4">
            <label className="form-label" htmlFor="admin-expense-date">
              Date
            </label>
            <input
              className="form-control"
              id="admin-expense-date"
              type="date"
              value={spentAt}
              onChange={(event) => setSpentAt(event.target.value)}
            />
          </div>
          {formError ? (
            <div className="col-12">
              <div className="alert alert-warning mb-0">{formError}</div>
            </div>
          ) : null}
          <div className="col-12 d-flex gap-2">
            <button className="btn btn-primary" type="submit">
              {editingId ? 'Save Changes' : 'Add Expense'}
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
          <h2 className="panel-title">Expense List</h2>
          <span className="muted">Showing {PAGE_SIZE} per page</span>
        </div>
        {error ? <div className="alert alert-warning">{error}</div> : null}
        <div className="table-responsive">
          <table className="table table-borderless align-middle mb-0">
            <thead>
              <tr>
                <th>Date</th>
                <th>Category</th>
                <th>Amount</th>
                <th className="text-end">Actions</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr>
                  <td colSpan={4} className="text-center muted py-4">
                    Loading expenses...
                  </td>
                </tr>
              ) : items.length === 0 ? (
                <tr>
                  <td colSpan={4} className="text-center muted py-4">
                    No expenses yet.
                  </td>
                </tr>
              ) : (
                items.map((expense) => (
                  <tr key={expense.id ?? `${expense.spentAt}-${expense.category}`}>
                    <td>{expense.spentAt}</td>
                    <td>{expense.category}</td>
                    <td>{currency.format(expense.amount)}</td>
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
