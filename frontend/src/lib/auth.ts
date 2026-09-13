import { computed, ref } from 'vue'

function readToken() {
  return typeof window === 'undefined' ? '' : sessionStorage.getItem('portfolio-auth-token') ?? ''
}

export const authToken = ref(readToken())
export const currentUser = ref<{ email: string; role: string } | null>(null)
export const isAdmin = computed(() => currentUser.value?.role === 'ADMIN')
export const isSignedIn = computed(() => Boolean(authToken.value))

export function setAuthToken(token: string, role?: string) {
  authToken.value = token
  sessionStorage.setItem('portfolio-auth-token', token)
  if (role) currentUser.value = { email: '', role }
}

export function signOut() {
  authToken.value = ''
  currentUser.value = null
  sessionStorage.removeItem('portfolio-auth-token')
}

export async function verifyAdminAccess() {
  if (!authToken.value) {
    return false
  }

  try {
    const response = await fetch('/api/auth/me', {
      headers: { Authorization: `Bearer ${authToken.value}` },
    })
    if (!response.ok) {
      signOut()
      return false
    }
    currentUser.value = await response.json()
    return isAdmin.value
  } catch {
    signOut()
    return false
  }
}
