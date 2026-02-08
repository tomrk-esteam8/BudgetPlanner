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

export type CreateCyclicExpenseRequest = {
  name: string
  cycleInterval: number
  totalCycles?: number
  active: boolean
  initialAmount: number
  validFrom: string
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

export async function createExpense(payload: Expense): Promise<Expense> {
  return request<Expense>('/expenses', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export async function createMonthlyFunds(payload: MonthlyFunds): Promise<MonthlyFunds> {
  return request<MonthlyFunds>('/monthly-funds', {
    method: 'POST',
    body: JSON.stringify(payload),
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
