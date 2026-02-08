import { fireEvent, render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { vi } from 'vitest'
import App from '../App'

const summaryResponse = {
  date: '2026-02-17',
  funds: 5000,
  savings: 1000,
  fixedCosts: 1500,
  spent: 100,
  available: 3400,
  dailyLimit: 226.67,
}

const expensesResponse = [
  {
    id: 1,
    amount: 45.5,
    category: 'Groceries',
    spentAt: '2026-02-05',
  },
]

const monthlyFundsResponse = [
  {
    id: 10,
    year: 2026,
    month: 2,
    amount: 5000,
  },
]

type FetchResponse = {
  ok: boolean
  status: number
  json: () => Promise<unknown>
  text: () => Promise<string>
}

type FetchInit = RequestInit | undefined

function createResponse(payload: unknown): FetchResponse {
  return {
    ok: true,
    status: 200,
    json: async () => payload,
    text: async () => JSON.stringify(payload),
  }
}

describe('App', () => {
  beforeEach(() => {
    vi.stubGlobal(
      'fetch',
      vi.fn(async (input: RequestInfo | URL, init?: FetchInit) => {
        const url = String(input)
        if (url.includes('/api/v1/summary')) {
          return createResponse(summaryResponse)
        }
        if (url.includes('/api/v1/expenses')) {
          if (init?.method === 'POST') {
            const payload = init.body ? JSON.parse(String(init.body)) : null
            return createResponse({ id: 2, ...payload })
          }
          return createResponse(expensesResponse)
        }
        if (url.includes('/api/v1/monthly-funds')) {
          if (init?.method === 'POST') {
            const payload = init.body ? JSON.parse(String(init.body)) : null
            return createResponse({ id: 11, ...payload })
          }
          return createResponse(monthlyFundsResponse)
        }
        if (url.includes('/api/v1/cyclic-expenses')) {
          if (init?.method === 'POST') {
            const payload = init.body ? JSON.parse(String(init.body)) : null
            return createResponse({ id: 'test-id', ...payload })
          }
          return createResponse([])
        }
        return {
          ok: false,
          status: 404,
          json: async () => ({}),
          text: async () => 'Not Found',
        } satisfies FetchResponse
      }),
    )
  })

  afterEach(() => {
    vi.unstubAllGlobals()
  })

  it('renders summary data after fetching', async () => {
    render(<App />)

    expect(await screen.findByText('$3,400.00')).toBeInTheDocument()
    expect(screen.getByText('Monthly Snapshot')).toBeInTheDocument()
  })

  it('requests summary with form inputs', async () => {
    render(<App />)

    await screen.findByText('$3,400.00')

    await userEvent.click(screen.getByRole('tab', { name: 'Summary' }))

    await userEvent.clear(screen.getByLabelText('Year', { selector: '#summary-year' }))
    await userEvent.type(screen.getByLabelText('Year', { selector: '#summary-year' }), '2026')
    await userEvent.clear(screen.getByLabelText('Month', { selector: '#summary-month' }))
    await userEvent.type(screen.getByLabelText('Month', { selector: '#summary-month' }), '2')
    await userEvent.clear(
      screen.getByLabelText('Day (optional)', { selector: '#summary-day' }),
    )
    await userEvent.type(
      screen.getByLabelText('Day (optional)', { selector: '#summary-day' }),
      '17',
    )

    await userEvent.click(screen.getByRole('button', { name: 'Fetch Summary' }))

    const fetchMock = vi.mocked(fetch)
    await waitFor(() => {
      expect(fetchMock).toHaveBeenCalledWith(
        expect.stringContaining('/api/v1/summary?year=2026&month=2&day=17'),
        expect.any(Object),
      )
    })
  })

  it('submits an expense and refreshes data', async () => {
    render(<App />)

    await screen.findByText('$3,400.00')

    await userEvent.click(screen.getByRole('tab', { name: 'Add Expense' }))

    await userEvent.type(
      screen.getByLabelText('Amount', { selector: '#expense-amount' }),
      '45.5',
    )
    await userEvent.type(
      screen.getByLabelText('Category', { selector: '#expense-category' }),
      'Groceries',
    )
    fireEvent.change(screen.getByLabelText('Date', { selector: '#expense-date' }), {
      target: { value: '2026-02-08' },
    })

    const addButtons = screen.getAllByRole('button', { name: 'Add Expense' })
    await userEvent.click(addButtons[1])

    expect(await screen.findByText('Expense added successfully.')).toBeInTheDocument()

    const fetchMock = vi.mocked(fetch)
    await waitFor(() => {
      const postCall = fetchMock.mock.calls.find(([, init]) => init?.method === 'POST')
      expect(postCall).toBeTruthy()
    })

    const postCall = fetchMock.mock.calls.find(([, init]) => init?.method === 'POST')
    const body = JSON.parse(String(postCall?.[1]?.body ?? '{}'))
    expect(body).toEqual({
      amount: 45.5,
      category: 'Groceries',
      spentAt: '2026-02-08',
    })
  })

  it('submits monthly funds from the quick action form', async () => {
    render(<App />)

    await screen.findByText('$3,400.00')

    await userEvent.click(screen.getByRole('tab', { name: 'Monthly Funds' }))

    await userEvent.clear(
      screen.getByLabelText('Year', { selector: '#monthly-funds-year' }),
    )
    await userEvent.type(
      screen.getByLabelText('Year', { selector: '#monthly-funds-year' }),
      '2026',
    )
    await userEvent.clear(
      screen.getByLabelText('Month', { selector: '#monthly-funds-month' }),
    )
    await userEvent.type(
      screen.getByLabelText('Month', { selector: '#monthly-funds-month' }),
      '2',
    )
    await userEvent.type(
      screen.getByLabelText('Amount', { selector: '#monthly-funds-amount' }),
      '5000',
    )

    await userEvent.click(
      screen.getByRole('button', { name: 'Save Monthly Funds' }),
    )

    expect(await screen.findByText('Monthly funds saved.')).toBeInTheDocument()

    const fetchMock = vi.mocked(fetch)
    await waitFor(() => {
      const postCall = fetchMock.mock.calls.find(
        ([url, init]) => String(url).includes('/api/v1/monthly-funds') && init?.method === 'POST',
      )
      expect(postCall).toBeTruthy()
    })
  })

  it('loads yearly overview data', async () => {
    render(<App />)

    await screen.findByText('$3,400.00')

    await userEvent.click(screen.getByRole('tab', { name: 'Yearly Overview' }))

    await userEvent.clear(
      screen.getByLabelText('Year', { selector: '#yearly-overview-year' }),
    )
    await userEvent.type(
      screen.getByLabelText('Year', { selector: '#yearly-overview-year' }),
      '2026',
    )
    await userEvent.click(screen.getByRole('button', { name: 'Load Overview' }))

    expect(await screen.findByText('$5,000.00')).toBeInTheDocument()
  })

  it('submits a cyclic expense from the quick action form', async () => {
    render(<App />)

    await screen.findByText('$3,400.00')

    await userEvent.click(screen.getByRole('tab', { name: 'Cyclic Expense' }))

    await userEvent.type(screen.getByLabelText('Name', { selector: '#cyclic-name' }), 'Rent')
    await userEvent.clear(
      screen.getByLabelText('Cycle (months)', { selector: '#cyclic-interval' }),
    )
    await userEvent.type(
      screen.getByLabelText('Cycle (months)', { selector: '#cyclic-interval' }),
      '1',
    )
    await userEvent.type(
      screen.getByLabelText('Total cycles', { selector: '#cyclic-total-cycles' }),
      '12',
    )
    await userEvent.type(
      screen.getByLabelText('Amount', { selector: '#cyclic-amount' }),
      '1500',
    )
    fireEvent.change(screen.getByLabelText('Valid from', { selector: '#cyclic-valid-from' }), {
      target: { value: '2026-02-01' },
    })

    await userEvent.click(
      screen.getByRole('button', { name: 'Save Cyclic Expense' }),
    )

    expect(await screen.findByText('Cyclic expense saved.')).toBeInTheDocument()
  })
})
