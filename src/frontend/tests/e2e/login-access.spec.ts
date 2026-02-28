import { expect, test } from '@playwright/test'

const fakeLoginResponse = {
  code: '0',
  message: 'OK',
  data: {
    accessToken: 'e2e-access-token',
    refreshToken: 'e2e-refresh-token',
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

test('redirects unauthenticated dashboard access to login', async ({ page }) => {
  await page.goto('/dashboard')

  await expect(page).toHaveURL(/\/login\?redirect=\/dashboard/)
  await expect(page.getByTestId('login-submit')).toBeVisible()
})

test('logs in and reaches protected dashboard menu', async ({ page }) => {
  await page.goto('/dashboard')

  await expect(page).toHaveURL(/\/login\?redirect=\/dashboard/)
  await page.getByPlaceholder('请输入用户名').fill('admin')
  await page.getByPlaceholder('请输入密码').fill('123456')
  await page.getByTestId('login-submit').click()

  await expect(page).toHaveURL(/\/dashboard$/)
  await expect(page.getByTestId('dashboard-shell')).toBeVisible()
  await expect(page.getByTestId('dashboard-card')).toHaveCount(4)

  const persisted = await page.evaluate(() => localStorage.getItem('labor-system.auth.session'))
  expect(persisted).not.toBeNull()
})
