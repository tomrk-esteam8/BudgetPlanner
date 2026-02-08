import { expect, test } from '@playwright/test'

test('submits an expense from the start page', async ({ page }) => {
  await page.goto('/')

  const expenseForm = page.locator('#expense-form')
  await expect(expenseForm).toBeVisible()
  await expenseForm.getByLabel('Amount').fill('29.99')
  await expenseForm.getByLabel('Category').fill('Streaming')
  await expenseForm.getByLabel('Date').fill('2026-02-01')
  await expenseForm.getByRole('button', { name: 'Add Expense' }).click()

  await expect(page.getByText('Expense added successfully.')).toBeVisible()
})
