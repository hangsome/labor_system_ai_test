import { flushPromises, mount } from '@vue/test-utils'
import ElementPlus from 'element-plus'
import App from '../../src/App.vue'
import { router } from '../../src/router'
import { store } from '../../src/store'
import { useAuthStore } from '../../src/store/auth'

describe('audit-log-view', () => {
  beforeEach(async () => {
    localStorage.clear()
    const authStore = useAuthStore(store)
    authStore.clearSession()
    await router.push('/login').catch(() => undefined)
    await router.isReady()
  })

  it('should redirect unauthenticated user when visiting audit log route', async () => {
    await router.push('/system/audit-logs')

    expect(router.currentRoute.value.path).toBe('/login')
    expect(router.currentRoute.value.query.redirect).toBe('/system/audit-logs')
  })

  it('should render audit log page shell for authenticated user', async () => {
    const authStore = useAuthStore(store)
    authStore.setSession({
      accessToken: 'access-token',
      refreshToken: 'refresh-token',
      expiresIn: 3600,
    })

    await router.push('/system/audit-logs')
    await router.isReady()

    const wrapper = mount(App, {
      global: {
        plugins: [store, router, ElementPlus],
      },
    })
    await flushPromises()

    expect(router.currentRoute.value.path).toBe('/system/audit-logs')
    expect(wrapper.find('[data-testid=\"audit-log-view\"]').exists()).toBe(true)
    expect(wrapper.find('[data-testid=\"audit-filter-form\"]').exists()).toBe(true)
    expect(wrapper.find('[data-testid=\"audit-log-table\"]').exists()).toBe(true)
    expect(wrapper.text()).toContain('审计日志中心')
  })
})
