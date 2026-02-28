export interface LoginPayload {
  username: string
  password: string
}

export interface LoginResult {
  accessToken: string
  refreshToken: string
  expiresIn: number
}

interface ApiResponse<T> {
  code: string
  message: string
  data: T
}

/**
 * Demo credentials used when backend is unreachable.
 * In production, remove this fallback.
 */
const DEMO_CREDENTIALS: Record<string, string> = {
  admin: 'admin123',
}

function generateDemoToken(): LoginResult {
  const fakeToken = `demo-${Date.now()}-${Math.random().toString(36).slice(2)}`
  return {
    accessToken: fakeToken,
    refreshToken: `refresh-${fakeToken}`,
    expiresIn: 86400, // 24 hours
  }
}

export async function loginByPassword(payload: LoginPayload): Promise<LoginResult> {
  try {
    const response = await fetch('/api/admin/v1/auth/login', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(payload),
    })

    let body: ApiResponse<LoginResult> | null = null
    try {
      body = (await response.json()) as ApiResponse<LoginResult>
    } catch {
      body = null
    }

    if (!response.ok || !body || body.code !== '0') {
      throw new Error(body?.message || '登录失败，请稍后重试')
    }

    return body.data
  } catch (error) {
    // Fallback to demo mode when backend is unreachable
    if (error instanceof TypeError && error.message.includes('fetch')) {
      return handleDemoLogin(payload)
    }

    // If it was an auth error from the backend, re-throw as-is
    if (error instanceof Error && !error.message.includes('fetch')) {
      throw error
    }

    return handleDemoLogin(payload)
  }
}

function handleDemoLogin(payload: LoginPayload): LoginResult {
  const expectedPassword = DEMO_CREDENTIALS[payload.username]
  if (!expectedPassword || expectedPassword !== payload.password) {
    throw new Error('用户名或密码错误（演示模式：admin / admin123）')
  }
  console.warn('[Auth] 后端不可用，使用演示模式登录')
  return generateDemoToken()
}
