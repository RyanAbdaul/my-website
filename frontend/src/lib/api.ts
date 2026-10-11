import { authToken } from './auth'

export class ApiError extends Error {
  constructor(public readonly status: number, message: string) {
    super(message)
  }
}

const apiBaseUrl = '/api'

async function errorMessage(response: Response) {
  try {
    const body: unknown = await response.json()
    if (body && typeof body === 'object') {
      const values = Object.values(body as Record<string, unknown>)
      const message = values.find((value): value is string => typeof value === 'string' && value.trim().length > 0)
      if (message) return message
    }
    return 'The request could not be completed.'
  } catch {
    return response.statusText || 'The request could not be completed.'
  }
}

export async function api<T>(path: string, options: RequestInit = {}): Promise<T> {
  const headers = new Headers(options.headers)
  if (authToken.value) headers.set('Authorization', `Bearer ${authToken.value}`)
  if (options.body && !(options.body instanceof FormData) && !headers.has('Content-Type')) headers.set('Content-Type', 'application/json')

  const response = await fetch(`${apiBaseUrl}${path}`, { ...options, headers })
  if (!response.ok) {
    throw new ApiError(response.status, await errorMessage(response))
  }
  if (response.status === 204) return undefined as T

  const body = await response.text()
  return (body ? JSON.parse(body) : undefined) as T
}
