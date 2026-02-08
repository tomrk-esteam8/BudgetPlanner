import { fireEvent, render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'
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
    render(
      <MemoryRouter>
        <App />
      </MemoryRouter>,
    )

    expect(await screen.findByText('$3,400.00')).toBeInTheDocument()
    expect(screen.getByText('Budget Summary')).toBeInTheDocument()
  })

  it('submits an expense and refreshes data', async () => {
    render(
      <MemoryRouter>
        <App />
      </MemoryRouter>,
    )

    await screen.findByText('$3,400.00')

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

    await userEvent.click(screen.getByRole('button', { name: 'Add Expense' }))

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
})
