import { expect, test } from '@playwright/test'

const fakeLoginResponse = {
  code: '0',
  message: 'OK',
  data: {
    accessToken: 'e2e-lead-access-token',
    refreshToken: 'e2e-lead-refresh-token',
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

test('redirects unauthenticated lead list access to login', async ({ page }) => {
  await page.goto('/crm/leads')

  await expect(page).toHaveURL(/\/login\?redirect=\/crm\/leads/)
  await expect(page.getByTestId('login-submit')).toBeVisible()
})

test('completes lead flow from list to follow-up submission after login', async ({ page }) => {
  await page.goto('/crm/leads')
  await expect(page).toHaveURL(/\/login\?redirect=\/crm\/leads/)

  await page.getByPlaceholder('Enter username').fill('admin')
  await page.getByPlaceholder('Enter password').fill('123456')
  await page.getByTestId('login-submit').click()

  await expect(page).toHaveURL(/\/crm\/leads$/)
  await expect(page.getByTestId('lead-list-view')).toBeVisible()

  const filterInput = page.locator('[data-testid="lead-filter-form"] input').first()
  await filterInput.fill('LEAD-001')
  await page.getByTestId('lead-search-button').click()
  await expect(page.getByText('LEAD-001')).toBeVisible()

  await page.goto('/crm/leads/LEAD-001')
  await expect(page.getByTestId('lead-detail-view')).toBeVisible()

  const timelineItems = page.locator('.el-timeline-item')
  await expect(timelineItems).toHaveCount(2)

  await page.getByTestId('lead-followup-submit').click()
  await expect(page.getByTestId('lead-followup-error')).toBeVisible()

  await page.locator('textarea').fill('E2E follow-up note')
  await page.getByTestId('lead-followup-submit').click()

  await expect(page.getByTestId('lead-followup-error')).toHaveCount(0)
  await expect(timelineItems).toHaveCount(3)
})
