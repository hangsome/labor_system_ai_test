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

export async function loginByPassword(payload: LoginPayload): Promise<LoginResult> {
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
}

