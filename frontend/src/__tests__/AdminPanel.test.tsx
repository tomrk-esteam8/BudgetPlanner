import { render, screen, waitFor } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { vi } from 'vitest'
import App from '../App'

const expensePageResponse = {
  content: [
    {
      id: 1,
      amount: 45.5,
      category: 'Groceries',
      spentAt: '2026-02-05',
    },
  ],
  totalElements: 1,
  totalPages: 1,
  number: 0,
  size: 10,
}

const monthlyFundsPageResponse = {
  content: [
    {
      id: 10,
      year: 2026,
      month: 2,
      amount: 5000,
    },
  ],
  totalElements: 1,
  totalPages: 1,
  number: 0,
  size: 10,
}

const cyclicPageResponse = {
  content: [
    {
      id: 'test-id',
      name: 'Rent',
      cycleInterval: 1,
      totalCycles: 12,
      active: true,
      rates: [
        {
          id: 101,
          amount: 1500,
          validFrom: '2026-01-01',
          active: true,
        },
      ],
    },
  ],
  totalElements: 1,
  totalPages: 1,
  number: 0,
  size: 10,
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

describe('Admin panel', () => {
  beforeEach(() => {
    vi.stubGlobal(
      'fetch',
      vi.fn(async (input: RequestInfo | URL, init?: FetchInit) => {
        const url = String(input)
        if (url.includes('/api/v1/summary')) {
          return createResponse({
            date: '2026-02-17',
            funds: 5000,
            savings: 1000,
            fixedCosts: 1500,
            spent: 100,
            available: 3400,
            dailyLimit: 226.67,
          })
        }
        if (url.includes('/api/v1/expenses?page=')) {
          return createResponse(expensePageResponse)
        }
        if (url.includes('/api/v1/monthly-funds?page=')) {
          return createResponse(monthlyFundsPageResponse)
        }
        if (url.includes('/api/v1/cyclic-expenses?page=')) {
          return createResponse(cyclicPageResponse)
        }
        if (init?.method === 'DELETE' || init?.method === 'PUT' || init?.method === 'POST') {
          return createResponse({})
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

  it('loads the expenses admin page', async () => {
    render(
      <MemoryRouter initialEntries={["/admin/expenses"]}>
        <App />
      </MemoryRouter>,
    )

    expect(await screen.findByRole('heading', { name: 'Expenses' })).toBeInTheDocument()
    await waitFor(() => {
      expect(screen.getByText('Groceries')).toBeInTheDocument()
    })
    await waitFor(() => {
      expect(vi.mocked(fetch)).toHaveBeenCalledWith(
        expect.stringContaining('/api/v1/expenses?page=0&size=10'),
        expect.anything(),
      )
    })
  })

  it('loads the monthly funds admin page', async () => {
    render(
      <MemoryRouter initialEntries={["/admin/monthly-funds"]}>
        <App />
      </MemoryRouter>,
    )

    expect(await screen.findByRole('heading', { name: 'Monthly Funds' })).toBeInTheDocument()
    await waitFor(() => {
      expect(screen.getByText('2026')).toBeInTheDocument()
    })
    await waitFor(() => {
      expect(vi.mocked(fetch)).toHaveBeenCalledWith(
        expect.stringContaining('/api/v1/monthly-funds?page=0&size=10'),
        expect.anything(),
      )
    })
  })

  it('loads the cyclic expenses admin page', async () => {
    render(
      <MemoryRouter initialEntries={["/admin/cyclic-expenses"]}>
        <App />
      </MemoryRouter>,
    )

    expect(await screen.findByRole('heading', { name: 'Cyclic Expenses' })).toBeInTheDocument()
    await waitFor(() => {
      expect(screen.getByText('Rent')).toBeInTheDocument()
    })
    await waitFor(() => {
      expect(vi.mocked(fetch)).toHaveBeenCalledWith(
        expect.stringContaining('/api/v1/cyclic-expenses?page=0&size=10'),
        expect.anything(),
      )
    })
  })
})
