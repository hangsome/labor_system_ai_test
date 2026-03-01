import { router } from '../../src/router'
import { store } from '../../src/store'
import { useAuthStore } from '../../src/store/auth'

describe('router-auth-guard', () => {
  beforeEach(async () => {
    localStorage.clear()
    const authStore = useAuthStore(store)
    authStore.clearSession()
    await router.push('/login').catch(() => undefined)
    await router.isReady()
  })

  it('should redirect unauthenticated user to login when visiting protected route', async () => {
    await router.push('/dashboard')

    expect(router.currentRoute.value.path).toBe('/login')
    expect(router.currentRoute.value.query.redirect).toBe('/dashboard')
  })

  it('should redirect authenticated user from login to dashboard', async () => {
    const authStore = useAuthStore(store)
    authStore.setSession({
      accessToken: 'access-token',
      refreshToken: 'refresh-token',
      expiresIn: 3600,
    })

    await router.push('/login?from=test')

    expect(router.currentRoute.value.path).toBe('/dashboard')
  })
})
