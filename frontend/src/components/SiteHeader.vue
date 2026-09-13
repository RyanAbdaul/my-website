<script setup lang="ts">
import { isAdmin, isSignedIn, signOut } from '@/lib/auth'
import { useRoute } from 'vue-router'

const route = useRoute()
const linkClass = (active: boolean) => active
  ? 'nav-link nav-link-active'
  : 'nav-link'
</script>

<template>
  <header class="border-b border-ink/15 bg-canvas/95 px-5 py-4 text-ink backdrop-blur sm:px-10 lg:px-20">
    <div class="mx-auto flex max-w-350 flex-wrap items-center justify-between gap-x-4 gap-y-3">
      <RouterLink to="/" class="flex min-w-0 items-center gap-2 sm:gap-3" aria-label="BashMobarmeg Academy home"><img class="size-9 shrink-0 rounded-xl object-cover sm:size-10" src="@/assets/logo.png" alt="" /><span class="hidden font-serif text-xl font-bold tracking-[-0.05em] sm:inline">BashMobarmeg <span class="text-primary">Academy</span></span></RouterLink>
      <nav class="order-3 flex w-full items-center gap-5 overflow-x-auto pb-1 text-xs font-semibold sm:order-2 sm:w-auto sm:gap-7 sm:pb-0 sm:text-sm" aria-label="Primary navigation"><RouterLink to="/about" :class="linkClass(route.path === '/' || route.path === '/about')">About</RouterLink><RouterLink to="/courses" :class="linkClass(route.path === '/courses')">Courses</RouterLink><RouterLink to="/articles" :class="linkClass(route.path === '/articles')">Articles</RouterLink><RouterLink v-if="isAdmin" to="/admin" :class="linkClass(route.path.startsWith('/admin'))">Admin</RouterLink></nav>
      <div class="flex items-center justify-end gap-2 text-xs font-semibold sm:order-3 sm:gap-3 sm:text-sm">
        <button v-if="isSignedIn" class="rounded-full px-3 py-2 text-muted-ink transition hover:text-ink sm:px-4" @click="signOut">Log out</button>
        <template v-else><RouterLink class="rounded-full border border-ink/20 px-3 py-2 text-ink transition hover:border-primary hover:text-primary sm:px-4" to="/auth?mode=login">Log in</RouterLink><RouterLink class="rounded-full bg-primary px-3 py-2 text-white transition hover:bg-primary/85 sm:px-4" to="/auth?mode=register">Sign up</RouterLink></template>
      </div>
    </div>
  </header>
</template>

<style scoped>
.nav-link { position: relative; color: var(--color-muted-ink); transition: color .2s ease, text-shadow .2s ease; }
.nav-link:hover { color: var(--color-ink); }
.nav-link-active { color: var(--color-primary); font-weight: 800; text-shadow: 0 0 14px color-mix(in srgb, var(--color-primary) 80%, transparent); }
.nav-link-active::after { position: absolute; right: 0; bottom: -7px; left: 0; height: 2px; border-radius: 9999px; background: var(--color-primary); box-shadow: 0 0 10px var(--color-primary); content: ''; }
</style>
