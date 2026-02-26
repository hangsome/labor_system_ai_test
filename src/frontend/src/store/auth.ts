import { defineStore } from 'pinia'

const SESSION_KEY = 'labor-system.auth.session'

interface PersistedSession {
  accessToken: string
  refreshToken: string
  expiresAt: number
}

interface SetSessionPayload {
  accessToken: string
  refreshToken: string
  expiresIn: number
}

export const useAuthStore = defineStore('auth', {
  state: () => ({
    accessToken: '',
    refreshToken: '',
    expiresAt: 0,
    hydrated: false,
  }),
  getters: {
    isAuthenticated: (state) => Boolean(state.accessToken) && Date.now() < state.expiresAt,
  },
  actions: {
    hydrate() {
      if (this.hydrated) {
        return
      }
      this.hydrated = true
      const raw = localStorage.getItem(SESSION_KEY)
      if (!raw) {
        return
      }
      try {
        const parsed = JSON.parse(raw) as PersistedSession
        if (!parsed.accessToken || !parsed.refreshToken || !parsed.expiresAt) {
          this.clearSession()
          return
        }
        this.accessToken = parsed.accessToken
        this.refreshToken = parsed.refreshToken
        this.expiresAt = parsed.expiresAt
      } catch {
        this.clearSession()
      }
    },
    setSession(payload: SetSessionPayload) {
      this.hydrated = true
      this.accessToken = payload.accessToken
      this.refreshToken = payload.refreshToken
      this.expiresAt = Date.now() + payload.expiresIn * 1000
      const snapshot: PersistedSession = {
        accessToken: this.accessToken,
        refreshToken: this.refreshToken,
        expiresAt: this.expiresAt,
      }
      localStorage.setItem(SESSION_KEY, JSON.stringify(snapshot))
    },
    clearSession() {
      this.hydrated = true
      this.accessToken = ''
      this.refreshToken = ''
      this.expiresAt = 0
      localStorage.removeItem(SESSION_KEY)
    },
  },
})

