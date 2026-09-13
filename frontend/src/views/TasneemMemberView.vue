<script setup lang="ts">
import { onMounted, ref } from 'vue'
import SiteHeader from '@/components/SiteHeader.vue'
import SiteFooter from '@/components/SiteFooter.vue'
import ExperienceTimeline from '@/components/ExperienceTimeline.vue'
import profilePlaceholder from '@/assets/profile-placeholder.svg'
import { api } from '@/lib/api'

type Experience = { id: string; role: string; organization: string; organizationUrl: string | null; location: string | null; startDate: string; endDate: string | null; summary: string }
type Member = { name: string; headline: string | null; biography: string | null; photoUrl: string | null; experiences: Experience[] }
const member = ref<Member | null>(null)
const dates = (start: string, end: string | null) => {
  const format = (date: string) => new Intl.DateTimeFormat('en', { month: 'short', year: 'numeric', timeZone: 'UTC' }).format(new Date(date))
  return `${format(start)} – ${end ? format(end) : 'Present'}`
}
const timelineExperiences = () => (member.value?.experiences ?? []).map((experience) => ({
  role: experience.role,
  company: experience.organization,
  companyUrl: experience.organizationUrl,
  dates: dates(experience.startDate, experience.endDate),
  summary: experience.location ?? '',
  highlights: experience.summary.split('\n').map((line) => line.replace(/^•\s*/, '').trim()).filter(Boolean),
}))
onMounted(async () => { try { member.value = await api<Member>('/members/tasneem') } catch { /* Migration may not be applied yet. */ } })
</script>

<template>
  <main>
    <section
      class="relative isolate min-h-svh overflow-hidden bg-canvas px-5 pt-5 pb-7 text-ink sm:px-10 lg:px-20"
      aria-labelledby="member-heading"
    >
      <SiteHeader class="-mx-5 -mt-5 sm:-mx-10 lg:-mx-20" />

      <div class="mx-auto grid min-h-[calc(100svh-6rem)] w-full max-w-350 grid-cols-1 items-center gap-10 pt-14 sm:pt-20 lg:grid-cols-[0.9fr_1.1fr] lg:gap-28">
        <div class="mx-auto max-w-156 text-center lg:mx-0 lg:text-left">
          <p class="mb-6 text-xs font-bold tracking-[0.14em] text-primary uppercase">Member profile</p>
          <h1 id="member-heading" class="max-w-none font-serif text-6xl leading-[0.87] tracking-[-0.075em] sm:text-7xl lg:max-w-[9ch] lg:text-8xl xl:text-9xl">Hi, I’m {{ member?.name ?? 'Tasneem' }}.</h1>
          <p v-if="member?.headline" class="mt-6 text-xl text-ink/80 sm:text-2xl">{{ member.headline }}</p>
          <p class="mx-auto mt-8 max-w-104 text-base leading-relaxed text-muted-ink sm:text-lg lg:mx-0">{{ member?.biography ?? 'This is Tasneem’s personal member page.' }}</p>
        </div>

        <div class="flex h-full min-h-96 items-end justify-center lg:justify-end" aria-label="Tasneem profile image">
          <img class="member-photo max-h-[68svh] w-full max-w-140 object-contain object-bottom lg:max-h-[calc(100svh-6rem)] lg:max-w-none" :src="member?.photoUrl || profilePlaceholder" :alt="`${member?.name ?? 'Tasneem'} profile photo`" />
        </div>
      </div>
    </section>
    <ExperienceTimeline v-if="member?.experiences.length" :experiences="timelineExperiences()" />
    <SiteFooter />
  </main>
</template>

<style scoped>
.member-photo { mask-image: linear-gradient(to bottom, black 92%, transparent 100%); }
</style>
