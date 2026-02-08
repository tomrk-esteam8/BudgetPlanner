export type MonthlySummary = {
  date: string
  funds: number
  savings: number
  fixedCosts: number
  spent: number
  available: number
  dailyLimit: number
}

export type Expense = {
  id?: number
  amount: number
  category: string
  spentAt: string
}

export type ExpenseInput = {
  amount: number
  category: string
  spentAt: string
}

export type SummaryParams = {
  year?: number
  month?: number
  day?: number
}

export type MonthlyFunds = {
  id?: number
  year: number
  month: number
  amount: number
}

export type MonthlyFundsInput = {
  year: number
  month: number
  amount: number
}

export type CreateCyclicExpenseRequest = {
  name: string
  cycleInterval: number
  totalCycles?: number
  active: boolean
  initialAmount: number
  validFrom: string
}

export type UpdateCyclicExpenseRequest = {
  name?: string
  cycleInterval?: number
  totalCycles?: number
  active?: boolean
  amount?: number
  validFrom?: string
}

export type CyclicExpenseRate = {
  id?: number
  amount: number
  validFrom: string
  active: boolean
}

export type CyclicExpense = {
  id?: string
  name: string
  cycleInterval: number
  totalCycles?: number
  active: boolean
  rates: CyclicExpenseRate[]
}

export type PageResponse<T> = {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
  size: number
}

const API_BASE = '/api/v1'

const defaultHeaders = {
  'Content-Type': 'application/json',
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(`${API_BASE}${path}`, {
    headers: defaultHeaders,
    ...init,
  })

  if (!response.ok) {
    const message = await response.text()
    throw new Error(message || `Request failed with status ${response.status}`)
  }

  return response.json() as Promise<T>
}

function normalizePageResponse<T>(payload: unknown): PageResponse<T> {
  if (Array.isArray(payload)) {
    return {
      content: payload as T[],
      totalElements: payload.length,
      totalPages: 1,
      number: 0,
      size: payload.length,
    }
  }

  if (payload && typeof payload === 'object') {
    const data = payload as Partial<PageResponse<T>> & { content?: unknown }
    const content = Array.isArray(data.content) ? (data.content as T[]) : []
    return {
      content,
      totalElements: Number.isFinite(data.totalElements)
        ? Number(data.totalElements)
        : content.length,
      totalPages: Number.isFinite(data.totalPages) ? Number(data.totalPages) : 1,
      number: Number.isFinite(data.number) ? Number(data.number) : 0,
      size: Number.isFinite(data.size) ? Number(data.size) : content.length,
    }
  }

  return {
    content: [],
    totalElements: 0,
    totalPages: 1,
    number: 0,
    size: 0,
  }
}

export async function fetchSummary(params?: SummaryParams): Promise<MonthlySummary> {
  const search = new URLSearchParams()
  if (params?.year !== undefined) {
    search.set('year', String(params.year))
  }
  if (params?.month !== undefined) {
    search.set('month', String(params.month))
  }
  if (params?.day !== undefined) {
    search.set('day', String(params.day))
  }

  const query = search.toString()
  return request<MonthlySummary>(`/summary${query ? `?${query}` : ''}`)
}

export async function fetchExpenses(): Promise<Expense[]> {
  return request<Expense[]>('/expenses')
}

export async function fetchExpensesPage(
  page: number,
  size: number,
): Promise<PageResponse<Expense>> {
  const payload = await request<unknown>(`/expenses?page=${page}&size=${size}`)
  return normalizePageResponse<Expense>(payload)
}

export async function createExpense(payload: ExpenseInput): Promise<Expense> {
  return request<Expense>('/expenses', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export async function updateExpense(id: number, payload: ExpenseInput): Promise<Expense> {
  return request<Expense>(`/expenses/${id}`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  })
}

export async function deleteExpense(id: number): Promise<void> {
  await request<void>(`/expenses/${id}`, {
    method: 'DELETE',
  })
}

export async function createMonthlyFunds(
  payload: MonthlyFundsInput,
): Promise<MonthlyFunds> {
  return request<MonthlyFunds>('/monthly-funds', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export async function fetchMonthlyFundsPage(
  page: number,
  size: number,
): Promise<PageResponse<MonthlyFunds>> {
  const payload = await request<unknown>(
    `/monthly-funds?page=${page}&size=${size}`,
  )
  return normalizePageResponse<MonthlyFunds>(payload)
}

export async function updateMonthlyFunds(
  id: number,
  payload: MonthlyFundsInput,
): Promise<MonthlyFunds> {
  return request<MonthlyFunds>(`/monthly-funds/${id}`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  })
}

export async function deleteMonthlyFunds(id: number): Promise<void> {
  await request<void>(`/monthly-funds/${id}`, {
    method: 'DELETE',
  })
}

export async function fetchMonthlyFundsByYear(year: number): Promise<MonthlyFunds[]> {
  return request<MonthlyFunds[]>(`/monthly-funds/${year}`)
}

export async function createCyclicExpense(
  payload: CreateCyclicExpenseRequest,
): Promise<unknown> {
  return request<unknown>('/cyclic-expenses', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export async function fetchCyclicExpenses(): Promise<CyclicExpense[]> {
  return request<CyclicExpense[]>('/cyclic-expenses')
}

export async function fetchCyclicExpensesPage(
  page: number,
  size: number,
): Promise<PageResponse<CyclicExpense>> {
  const payload = await request<unknown>(
    `/cyclic-expenses?page=${page}&size=${size}`,
  )
  return normalizePageResponse<CyclicExpense>(payload)
}

export async function updateCyclicExpense(
  id: string,
  payload: UpdateCyclicExpenseRequest,
): Promise<CyclicExpense> {
  return request<CyclicExpense>(`/cyclic-expenses/${id}`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  })
}

export async function deleteCyclicExpense(id: string): Promise<void> {
  await request<void>(`/cyclic-expenses/${id}`, {
    method: 'DELETE',
  })
}
