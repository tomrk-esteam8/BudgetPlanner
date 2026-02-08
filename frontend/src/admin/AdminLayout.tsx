import { Link, NavLink, Outlet } from 'react-router-dom'

export default function AdminLayout() {
  return (
    <div className="app-shell">
      <div className="app-glow" aria-hidden="true" />
      <main className="container py-4 py-lg-5">
        <div className="d-flex flex-wrap align-items-center justify-content-between gap-3 mb-4">
          <div>
            <p className="eyebrow mb-2">Admin Panel</p>
            <h1 className="hero-title mb-0">Manage Budget Data</h1>
          </div>
          <Link className="btn btn-outline-light" to="/">
            Back to Summary
          </Link>
        </div>
        <ul className="nav nav-pills gap-2 mb-4">
          <li className="nav-item">
            <NavLink
              className={({ isActive }) =>
                `nav-link${isActive ? ' active' : ''}`
              }
              to="/admin/expenses"
            >
              Expenses
            </NavLink>
          </li>
          <li className="nav-item">
            <NavLink
              className={({ isActive }) =>
                `nav-link${isActive ? ' active' : ''}`
              }
              to="/admin/cyclic-expenses"
            >
              Cyclic Expenses
            </NavLink>
          </li>
          <li className="nav-item">
            <NavLink
              className={({ isActive }) =>
                `nav-link${isActive ? ' active' : ''}`
              }
              to="/admin/monthly-funds"
            >
              Monthly Funds
            </NavLink>
          </li>
        </ul>
        <Outlet />
      </main>
    </div>
  )
}
