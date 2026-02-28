import { flushPromises, mount } from '@vue/test-utils'
import { nextTick } from 'vue'
import ElementPlus from 'element-plus'
import { createPinia } from 'pinia'
import LoginView from '../../src/views/LoginView.vue'
import { loginByPassword } from '../../src/api/auth'

const pushMock = vi.fn()

vi.mock('vue-router', () => ({
  useRouter: () => ({
    push: pushMock,
  }),
  useRoute: () => ({
    query: {},
  }),
}))

vi.mock('../../src/api/auth', () => ({
  loginByPassword: vi.fn(),
}))

describe('login-view', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('should show required error when credentials are empty', async () => {
    const wrapper = mount(LoginView, {
      global: {
        plugins: [ElementPlus, createPinia()],
      },
    })

    await wrapper.find('[data-testid=\"login-submit\"]').trigger('click')
    await flushPromises()

    expect(loginByPassword).not.toHaveBeenCalled()
    expect(pushMock).not.toHaveBeenCalled()
    expect(wrapper.text()).toContain('请输入用户名和密码')
  })

  it('should submit and navigate when login succeeded', async () => {
    vi.mocked(loginByPassword).mockResolvedValue({
      accessToken: 'access-token',
      refreshToken: 'refresh-token',
      expiresIn: 3600,
    })

    const wrapper = mount(LoginView, {
      global: {
        plugins: [ElementPlus, createPinia()],
      },
    })

    const inputs = wrapper.findAll('input')
    await inputs[0].setValue('admin')
    await inputs[1].setValue('123456')
    await wrapper.find('[data-testid=\"login-submit\"]').trigger('click')
    await flushPromises()

    expect(loginByPassword).toHaveBeenCalledWith({
      username: 'admin',
      password: '123456',
    })
    expect(pushMock).toHaveBeenCalledWith('/dashboard')
  })

  it('should set submitting loading state while login request pending', async () => {
    let resolveLogin:
      | ((value: { accessToken: string; refreshToken: string; expiresIn: number }) => void)
      | undefined
    const pendingLogin = new Promise<{ accessToken: string; refreshToken: string; expiresIn: number }>((resolve) => {
      resolveLogin = resolve
    })
    vi.mocked(loginByPassword).mockReturnValue(pendingLogin)

    const wrapper = mount(LoginView, {
      global: {
        plugins: [ElementPlus, createPinia()],
      },
    })

    const inputs = wrapper.findAll('input')
    await inputs[0].setValue('admin')
    await inputs[1].setValue('123456')
    await wrapper.find('[data-testid=\"login-submit\"]').trigger('click')
    await flushPromises()
    await nextTick()

    const submitButton = wrapper.find('[data-testid=\"login-submit\"]')
    expect(loginByPassword).toHaveBeenCalledTimes(1)
    expect(submitButton.classes()).toContain('is-loading')

    resolveLogin?.({
      accessToken: 'access-token',
      refreshToken: 'refresh-token',
      expiresIn: 3600,
    })
    await flushPromises()

    expect(submitButton.classes()).not.toContain('is-loading')
    expect(pushMock).toHaveBeenCalledWith('/dashboard')
  })

  it('should show backend error when login failed', async () => {
    vi.mocked(loginByPassword).mockRejectedValue(new Error('用户名或密码错误'))

    const wrapper = mount(LoginView, {
      global: {
        plugins: [ElementPlus, createPinia()],
      },
    })

    const inputs = wrapper.findAll('input')
    await inputs[0].setValue('admin')
    await inputs[1].setValue('123456')
    await wrapper.find('[data-testid=\"login-submit\"]').trigger('click')
    await flushPromises()

    expect(loginByPassword).toHaveBeenCalledTimes(1)
    expect(pushMock).not.toHaveBeenCalled()
    expect(wrapper.text()).toContain('用户名或密码错误')
  })
})
