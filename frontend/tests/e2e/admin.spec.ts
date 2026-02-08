import { expect, test } from '@playwright/test'

test('admin panel navigation works', async ({ page }) => {
  await page.goto('/')

  await page.getByRole('link', { name: 'Admin Panel' }).click()
  await expect(page).toHaveURL(/\/admin\/expenses/)

  await expect(page.getByRole('heading', { name: 'Manage Budget Data' })).toBeVisible()
  await expect(page.getByLabel('Category')).toBeVisible()

  await page.getByRole('link', { name: 'Monthly Funds' }).dispatchEvent('click')
  await expect(page).toHaveURL(/\/admin\/monthly-funds/)
  await expect(page.getByLabel('Year')).toBeVisible()

  await page.getByRole('link', { name: 'Cyclic Expenses' }).dispatchEvent('click')
  await expect(page).toHaveURL(/\/admin\/cyclic-expenses/)
  await expect(page.getByLabel('Name')).toBeVisible()
})
