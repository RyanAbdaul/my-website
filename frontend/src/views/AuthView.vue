<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { isAdmin, setAuthToken, verifyAdminAccess } from '@/lib/auth'
import { api } from '@/lib/api'

const route = useRoute()
const router = useRouter()
const mode = computed(() => route.query.mode === 'register' ? 'register' : 'login')
const email = ref('')
const password = ref('')
const name = ref('')
const error = ref('')
const loading = ref(false)

async function submit() {
  loading.value = true
  error.value = ''
  try {
    const url = mode.value === 'register' ? '/auth/students' : '/auth/login'
    const body = mode.value === 'register' ? { name: name.value, email: email.value, password: password.value } : { email: email.value, password: password.value }
    const authentication = await api<{ token: string; role: string }>(url, { method: 'POST', body: JSON.stringify(body) })
    setAuthToken(authentication.token, authentication.role)
    await verifyAdminAccess()
    await router.push(isAdmin.value ? '/admin' : '/about')
  } catch (reason) {
    error.value = reason instanceof Error ? reason.message : 'Something went wrong.'
  } finally { loading.value = false }
}
</script>

<template>
  <main class="min-h-svh bg-canvas px-5 py-8 text-ink sm:px-10">
    <section class="mx-auto mt-[12vh] max-w-md rounded-3xl border border-ink/15 bg-white p-7 shadow-sm sm:p-10">
      <RouterLink to="/about" class="text-sm font-bold tracking-[0.12em] text-primary uppercase">← BashMobarmeg Academy</RouterLink>
      <h1 class="mt-7 font-serif text-5xl leading-none tracking-[-0.06em]">{{ mode === 'register' ? 'Create account' : 'Welcome back' }}</h1>
      <p class="mt-4 text-muted-ink">{{ mode === 'register' ? 'Register as a learner to get started.' : 'Sign in to your account.' }}</p>
      <form class="mt-8 space-y-4" @submit.prevent="submit">
        <input v-if="mode === 'register'" v-model="name" class="auth-input" placeholder="Name" required />
        <input v-model="email" class="auth-input" type="email" placeholder="Email" required />
        <input v-model="password" class="auth-input" type="password" placeholder="Password (8+ characters)" required minlength="8" />
        <p v-if="error" class="text-sm text-red-600">{{ error }}</p>
        <button class="w-full rounded-xl bg-primary px-4 py-3 text-sm font-bold text-white" :disabled="loading">{{ loading ? 'Please wait…' : mode === 'register' ? 'Register' : 'Log in' }}</button>
      </form>
      <p class="mt-6 text-sm text-muted-ink"><RouterLink v-if="mode === 'login'" class="text-primary" to="/auth?mode=register">Need an account? Register</RouterLink><RouterLink v-else class="text-primary" to="/auth?mode=login">Already registered? Log in</RouterLink></p>
    </section>
  </main>
</template>

<style scoped>
.auth-input { width: 100%; border: 1px solid color-mix(in srgb, var(--color-ink) 15%, transparent); border-radius: .75rem; background: var(--color-canvas); padding: .75rem 1rem; color: var(--color-ink); font-size: .875rem; outline: none; }
.auth-input:focus { border-color: var(--color-primary); }
</style>
