# Budget UI (Frontend)

A React + TypeScript UI for the Budget API. It focuses on a clean, fast dashboard
experience while keeping the data flow and API integration straightforward.

## Tech Stack

- React 19 + TypeScript
- Vite for dev/build tooling
- Bootstrap 5 for base UI primitives
- Vitest + React Testing Library for unit tests
- Playwright for end-to-end tests

## Key Decisions

- API calls are centralized in `src/api/budgetApi.ts` to keep components focused
  on rendering and state transitions.
- The API base path is `/api/v1`, which is proxied by Vite to the backend during
  development. This avoids hardcoded hostnames in the UI.
- Types are declared for summary and expense responses to keep the UI and API
  contract aligned.
- Bootstrap provides consistent layout and form styling, with custom CSS for the
  dashboard look and feel.

## Local Development

Install dependencies and start the dev server:

```bash
npm install
npm run dev
```

The UI runs at:

```
http://localhost:5173
```

## API Proxy

The Vite dev server proxies API calls from `/api` to the backend at
`http://localhost:8080`. The UI uses `/api/v1` so it matches the Spring Boot
context path.

## Scripts

- `npm run dev` - Start the Vite dev server
- `npm run build` - Type-check and build the production bundle
- `npm run preview` - Preview the production build
- `npm test` - Run Vitest once
- `npm run test:ui` - Run Vitest in watch mode
- `npm run test:e2e` - Run Playwright tests (requires backend and UI running)

## Project Layout

- `src/App.tsx` - Main dashboard page
- `src/api/budgetApi.ts` - API client and types
- `src/__tests__/App.test.tsx` - UI unit tests
- `tests/e2e/summary.spec.ts` - Playwright test for summary and expenses
