import { expect, test } from '@playwright/test'

test('tabs submit monthly funds and cyclic expenses', async ({ page }) => {
  await page.goto('/')

  await page.getByRole('tab', { name: 'Monthly Funds' }).click()

  const monthlyFundsForm = page.locator('#monthly-funds-form')
  await expect(monthlyFundsForm).toBeVisible()
  await monthlyFundsForm.getByLabel('Year').fill('2026')
  await monthlyFundsForm.getByLabel('Month').fill('2')
  await monthlyFundsForm.getByLabel('Amount').fill('4321.11')
  await monthlyFundsForm.getByRole('button', { name: 'Save Monthly Funds' }).click()

  await expect(page.getByText('Monthly funds saved.')).toBeVisible()

  await page.getByRole('tab', { name: 'Cyclic Expense' }).click()

  const cyclicForm = page.locator('#cyclic-expense-form')
  await expect(cyclicForm).toBeVisible()
  await cyclicForm.getByLabel('Name').fill('Streaming')
  await cyclicForm.getByLabel('Cycle (months)').fill('1')
  await cyclicForm.getByLabel('Total cycles').fill('12')
  await cyclicForm.getByLabel('Amount').fill('29.99')
  await cyclicForm.getByLabel('Valid from').fill('2026-02-01')
  await cyclicForm.getByRole('button', { name: 'Save Cyclic Expense' }).click()

  await expect(page.getByText('Cyclic expense saved.')).toBeVisible()

  await page.getByRole('tab', { name: 'Yearly Overview' }).click()

  const overview = page.locator('#yearly-overview')
  await expect(overview).toBeVisible()
  await overview.getByRole('button', { name: 'Load Overview' }).click()

  await expect(page.getByText('$4,321.11')).toBeVisible()
})
