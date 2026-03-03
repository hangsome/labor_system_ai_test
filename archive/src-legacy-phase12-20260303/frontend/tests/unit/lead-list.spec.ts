import { flushPromises, mount } from '@vue/test-utils'
import ElementPlus from 'element-plus'
import App from '../../src/App.vue'
import { router } from '../../src/router'
import { store } from '../../src/store'
import { useAuthStore } from '../../src/store/auth'

describe('lead-list', () => {
  beforeEach(async () => {
    localStorage.clear()
    const authStore = useAuthStore(store)
    authStore.clearSession()
    await router.push('/login').catch(() => undefined)
    await router.isReady()
  })

  it('should redirect unauthenticated user from lead list route to login', async () => {
    await router.push('/crm/leads')

    expect(router.currentRoute.value.path).toBe('/login')
    expect(router.currentRoute.value.query.redirect).toBe('/crm/leads')
  })

  it('should render lead list view with filter form and pagination for authenticated user', async () => {
    const authStore = useAuthStore(store)
    authStore.setSession({
      accessToken: 'access-token',
      refreshToken: 'refresh-token',
      expiresIn: 3600,
    })

    await router.push('/crm/leads')
    await router.isReady()

    const wrapper = mount(App, {
      global: {
        plugins: [store, router, ElementPlus],
      },
    })
    await flushPromises()

    expect(router.currentRoute.value.path).toBe('/crm/leads')
    expect(wrapper.find('[data-testid=\"lead-list-view\"]').exists()).toBe(true)
    expect(wrapper.find('[data-testid=\"lead-filter-form\"]').exists()).toBe(true)
    expect(wrapper.find('[data-testid=\"lead-table\"]').exists()).toBe(true)
    expect(wrapper.find('[data-testid=\"lead-pagination\"]').exists()).toBe(true)
    expect(wrapper.text()).toContain('客户线索列表')
    expect(wrapper.text()).toContain('LEAD-001')
  })
})
