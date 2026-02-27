import { flushPromises, mount } from '@vue/test-utils'
import ElementPlus from 'element-plus'
import App from '../../src/App.vue'
import { router } from '../../src/router'
import { store } from '../../src/store'
import { useAuthStore } from '../../src/store/auth'

describe('lead-detail', () => {
  beforeEach(async () => {
    localStorage.clear()
    const authStore = useAuthStore(store)
    authStore.clearSession()
    await router.push('/login').catch(() => undefined)
    await router.isReady()
  })

  it('should redirect unauthenticated user from lead detail route to login', async () => {
    await router.push('/crm/leads/LEAD-001')

    expect(router.currentRoute.value.path).toBe('/login')
    expect(router.currentRoute.value.query.redirect).toBe('/crm/leads/LEAD-001')
  })

  it('should render lead detail timeline and validate follow-up form for authenticated user', async () => {
    const authStore = useAuthStore(store)
    authStore.setSession({
      accessToken: 'access-token',
      refreshToken: 'refresh-token',
      expiresIn: 3600,
    })

    await router.push('/crm/leads/LEAD-001')
    await router.isReady()

    const wrapper = mount(App, {
      global: {
        plugins: [store, router, ElementPlus],
      },
    })
    await flushPromises()

    expect(router.currentRoute.value.path).toBe('/crm/leads/LEAD-001')
    expect(wrapper.find('[data-testid=\"lead-detail-view\"]').exists()).toBe(true)
    expect(wrapper.find('[data-testid=\"lead-timeline\"]').exists()).toBe(true)
    expect(wrapper.findAll('.el-timeline-item')).toHaveLength(2)

    await wrapper.find('[data-testid=\"lead-followup-submit\"]').trigger('click')
    await flushPromises()
    expect(wrapper.find('[data-testid=\"lead-followup-error\"]').exists()).toBe(true)

    const textarea = wrapper.find('textarea')
    await textarea.setValue('新增跟进：客户要求补充排班明细')
    await wrapper.find('[data-testid=\"lead-followup-submit\"]').trigger('click')
    await flushPromises()

    expect(wrapper.find('[data-testid=\"lead-followup-error\"]').exists()).toBe(false)
    expect(wrapper.findAll('.el-timeline-item').length).toBeGreaterThanOrEqual(3)
  })
})
