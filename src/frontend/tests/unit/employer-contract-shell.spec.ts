import { flushPromises, mount } from '@vue/test-utils'
import ElementPlus from 'element-plus'
import App from '../../src/App.vue'
import { router } from '../../src/router'
import { store } from '../../src/store'
import { useAuthStore } from '../../src/store/auth'

describe('employer-contract-shell', () => {
  beforeEach(async () => {
    localStorage.clear()
    const authStore = useAuthStore(store)
    authStore.clearSession()
    await router.push('/login').catch(() => undefined)
    await router.isReady()
  })

  it('should redirect unauthenticated user from employer route to login', async () => {
    await router.push('/crm/employer-units')

    expect(router.currentRoute.value.path).toBe('/login')
    expect(router.currentRoute.value.query.redirect).toBe('/crm/employer-units')
  })

  it('should render employer shell and contract shell for authenticated user', async () => {
    const authStore = useAuthStore(store)
    authStore.setSession({
      accessToken: 'access-token',
      refreshToken: 'refresh-token',
      expiresIn: 3600,
    })

    await router.push('/crm/employer-units')
    await router.isReady()

    const wrapper = mount(App, {
      global: {
        plugins: [store, router, ElementPlus],
      },
    })
    await flushPromises()

    expect(router.currentRoute.value.path).toBe('/crm/employer-units')
    expect(wrapper.find('[data-testid=\"employer-unit-view\"]').exists()).toBe(true)
    expect(wrapper.find('[data-testid=\"employer-unit-filter-form\"]').exists()).toBe(true)
    expect(wrapper.find('[data-testid=\"employer-unit-table\"]').exists()).toBe(true)
    expect(wrapper.text()).toContain('用工单位管理')
    expect(wrapper.text()).toContain('EMP-001')

    await router.push('/contracts/labor-contracts')
    await flushPromises()

    expect(router.currentRoute.value.path).toBe('/contracts/labor-contracts')
    expect(wrapper.find('[data-testid=\"contract-list-view\"]').exists()).toBe(true)
    expect(wrapper.find('[data-testid=\"contract-filter-form\"]').exists()).toBe(true)
    expect(wrapper.find('[data-testid=\"contract-table\"]').exists()).toBe(true)
    expect(wrapper.text()).toContain('合同管理')
    expect(wrapper.text()).toContain('LC-2026-001')
  })
})
