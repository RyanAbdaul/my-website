<script setup lang="ts">
import { onMounted, ref } from 'vue'
import SiteHeader from '@/components/SiteHeader.vue'
import SiteFooter from '@/components/SiteFooter.vue'
import ExperienceTimeline from '@/components/ExperienceTimeline.vue'
import profilePlaceholder from '@/assets/profile-placeholder.svg'
import { api } from '@/lib/api'

type AdminProfile = { id: string; name: string; headline: string | null; biography: string | null; photoUrl: string | null }
const profile = ref<AdminProfile | null>(null)
onMounted(async () => { try { profile.value = await api<AdminProfile>('/profile') } catch { profile.value = null } })
</script>

<template>
  <main>
    <section class="relative isolate min-h-svh overflow-hidden bg-canvas px-5 pt-5 pb-7 text-ink sm:px-10 lg:px-20" aria-labelledby="profile-heading">
      <SiteHeader class="-mx-5 -mt-5 sm:-mx-10 lg:-mx-20" />
      <div class="mx-auto grid min-h-[calc(100svh-6rem)] w-full max-w-350 grid-cols-1 items-center gap-8 py-10 sm:gap-10 sm:py-16 lg:grid-cols-[0.9fr_1.1fr] lg:gap-28 lg:py-20">
        <div class="mx-auto max-w-156 text-center lg:mx-0 lg:text-left"><p class="mb-6 text-xs font-bold tracking-[0.14em] text-primary uppercase">About</p><h1 id="profile-heading" class="font-serif text-5xl leading-[0.9] tracking-[-0.075em] sm:text-7xl lg:max-w-[9ch] lg:text-8xl">Hi, I’m {{ profile?.name ?? 'Ryan' }}.</h1><p v-if="profile?.headline" class="mt-6 text-xl text-ink/80 sm:text-2xl">{{ profile.headline }}</p><p v-if="profile?.biography" class="mx-auto mt-8 max-w-104 text-base leading-relaxed text-muted-ink sm:text-lg lg:mx-0">{{ profile.biography }}</p></div>
        <div class="flex min-h-96 items-end justify-center lg:justify-end"><img class="profile-photo max-h-[68svh] w-full max-w-140 object-contain object-bottom lg:max-h-[calc(100svh-6rem)] lg:max-w-none" :src="profile?.photoUrl || profilePlaceholder" :alt="`${profile?.name ?? 'Ryan'} profile photo`" /></div>
      </div>
    </section>
    <ExperienceTimeline />
    <SiteFooter />
  </main>
</template>

<style scoped>.profile-photo { mask-image: linear-gradient(to bottom, black 92%, transparent 100%); }</style>
