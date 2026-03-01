import { expect, test } from '@playwright/test'

const fakeLoginResponse = {
  code: '0',
  message: 'OK',
  data: {
    accessToken: 'e2e-contract-access-token',
    refreshToken: 'e2e-contract-refresh-token',
    expiresIn: 3600,
  },
}

test.beforeEach(async ({ page }) => {
  await page.route('**/api/admin/v1/auth/login', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(fakeLoginResponse),
    })
  })
})

test('completes lead to contract main flow', async ({ page }) => {
  await page.goto('/crm/leads')
  await expect(page).toHaveURL(/\/login\?redirect=\/crm\/leads/)

  await page.getByPlaceholder('Enter username').fill('admin')
  await page.getByPlaceholder('Enter password').fill('123456')
  await page.getByTestId('login-submit').click()

  await expect(page).toHaveURL(/\/crm\/leads$/)
  await expect(page.getByTestId('lead-list-view')).toBeVisible()
  await expect(page.getByText('LEAD-001')).toBeVisible()

  await page.goto('/contracts/labor-contracts')
  await expect(page.getByTestId('contract-list-view')).toBeVisible()
  await expect(page.getByText('LC-2026-001')).toBeVisible()

  await page.locator('[data-testid="contract-detail-entry"]').first().click()
  await expect(page).toHaveURL(/\/contracts\/labor-contracts\/LC-2026-001$/)
  await expect(page.getByTestId('contract-detail-view')).toBeVisible()

  await page.getByTestId('contract-sign-button').click()
  await expect(page.getByTestId('contract-status')).toContainText('ACTIVE')
})
