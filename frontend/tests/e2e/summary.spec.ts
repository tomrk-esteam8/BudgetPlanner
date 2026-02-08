import { expect, test } from '@playwright/test'

test('loads summary and expenses', async ({ page }) => {
  await page.goto('/')

  await expect(page.getByText('Budget Summary')).toBeVisible()
  await expect(page.locator('#expense-form')).toBeVisible()
})
