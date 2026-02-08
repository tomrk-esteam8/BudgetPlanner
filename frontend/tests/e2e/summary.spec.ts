import { expect, test } from '@playwright/test'

test('loads summary and expenses', async ({ page }) => {
  await page.goto('/')

  await expect(page.getByText('Monthly Snapshot')).toBeVisible()

  await page.getByRole('tab', { name: 'Summary' }).click()

  await page.locator('#summary-year').fill('2026')
  await page.locator('#summary-month').fill('2')
  await page.locator('#summary-day').fill('17')
  await page.getByRole('button', { name: 'Fetch Summary' }).click()

  await expect(page.getByText('Latest Expenses')).toBeVisible()
})
