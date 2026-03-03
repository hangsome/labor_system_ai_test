import { flushPromises, mount } from '@vue/test-utils'
import ElementPlus from 'element-plus'
import App from '../../src/App.vue'
import { router } from '../../src/router'
import { store } from '../../src/store'
import { useAuthStore } from '../../src/store/auth'

describe('contract-detail', () => {
  beforeEach(async () => {
    localStorage.clear()
    const authStore = useAuthStore(store)
    authStore.clearSession()
    await router.push('/login').catch(() => undefined)
    await router.isReady()
  })

  it('should redirect unauthenticated user from contract detail route to login', async () => {
    await router.push('/contracts/labor-contracts/LC-2026-001')

    expect(router.currentRoute.value.path).toBe('/login')
    expect(router.currentRoute.value.query.redirect).toBe('/contracts/labor-contracts/LC-2026-001')
  })

  it('should render contract detail and complete lifecycle operations for authenticated user', async () => {
    const authStore = useAuthStore(store)
    authStore.setSession({
      accessToken: 'access-token',
      refreshToken: 'refresh-token',
      expiresIn: 3600,
    })

    await router.push('/contracts/labor-contracts/LC-2026-001')
    await router.isReady()

    const wrapper = mount(App, {
      global: {
        plugins: [store, router, ElementPlus],
      },
    })
    await flushPromises()

    expect(router.currentRoute.value.path).toBe('/contracts/labor-contracts/LC-2026-001')
    expect(wrapper.find('[data-testid=\"contract-detail-view\"]').exists()).toBe(true)
    expect(wrapper.find('[data-testid=\"contract-history-list\"]').exists()).toBe(true)
    expect(wrapper.findAll('.el-timeline-item')).toHaveLength(1)
    expect(wrapper.find('[data-testid=\"contract-status\"]').text()).toContain('草稿')
    expect(wrapper.find('[data-testid=\"contract-renew-button\"]').attributes('disabled')).toBeDefined()

    await wrapper.find('[data-testid=\"contract-sign-button\"]').trigger('click')
    await flushPromises()
    expect(wrapper.find('[data-testid=\"contract-status\"]').text()).toContain('进行中')
    expect(wrapper.find('[data-testid=\"contract-action-feedback\"]').text()).toContain(
      '合同签署成功'
    )

    await wrapper.find('[data-testid=\"contract-renew-button\"]').trigger('click')
    await flushPromises()
    expect(wrapper.find('[data-testid=\"contract-action-feedback\"]').text()).toContain(
      '合同续签成功'
    )

    await wrapper.find('[data-testid=\"contract-terminate-button\"]').trigger('click')
    await flushPromises()
    expect(wrapper.find('[data-testid=\"contract-status\"]').text()).toContain('已终止')
    expect(wrapper.find('[data-testid=\"contract-action-feedback\"]').text()).toContain(
      '合同终止成功'
    )
    expect(wrapper.findAll('.el-timeline-item').length).toBeGreaterThanOrEqual(4)
  })
})
