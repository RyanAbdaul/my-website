<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import SiteHeader from '@/components/SiteHeader.vue'
import SiteFooter from '@/components/SiteFooter.vue'
import ExperienceTimeline from '@/components/ExperienceTimeline.vue'
import profilePlaceholder from '@/assets/profile-placeholder.svg'
import { api } from '@/lib/api'

type Experience = { id: string; role: string; organization: string; organizationUrl: string | null; location: string | null; startDate: string; endDate: string | null; summary: string }
type Project = { id: string; title: string; description: string | null; imagePath: string | null; projectUrl: string }
type Certificate = { id: string; title: string; issuer: string | null; imagePath: string }
type Course = { id: string; title: string; description: string; thumbnailLink: string | null; price: number }
type Article = { id: string; title: string; content: string; createTime: string; tags: string[] }
type Member = { name: string; headline: string | null; biography: string | null; photoUrl: string | null; experiences: Experience[]; projects: Project[]; certificates: Certificate[]; courses: Course[]; articles: Article[] }
const props = defineProps<{ slug?: string }>()
const route = useRoute()
const slug = computed(() => props.slug ?? String(route.params.slug ?? 'ryan'))
const member = ref<Member | null>(null)
const dates = (start: string, end: string | null) => `${new Intl.DateTimeFormat('en', { month: 'short', year: 'numeric', timeZone: 'UTC' }).format(new Date(start))} – ${end ? new Intl.DateTimeFormat('en', { month: 'short', year: 'numeric', timeZone: 'UTC' }).format(new Date(end)) : 'Present'}`
const timelineExperiences = () => (member.value?.experiences ?? []).map((item) => ({ role: item.role, company: item.organization, companyUrl: item.organizationUrl, dates: dates(item.startDate, item.endDate), summary: item.location ?? '', highlights: item.summary.split('\n').map((line) => line.replace(/^•\s*/, '').trim()).filter(Boolean) }))
onMounted(async () => { try { member.value = await api<Member>(`/members/${slug.value}`) } catch { member.value = null } })
</script>

<template>
  <main>
    <section class="relative isolate min-h-svh overflow-hidden bg-canvas px-5 pt-5 pb-7 text-ink sm:px-10 lg:px-20" aria-labelledby="member-heading">
      <SiteHeader class="-mx-5 -mt-5 sm:-mx-10 lg:-mx-20" />
      <div class="mx-auto grid min-h-[calc(100svh-6rem)] w-full max-w-350 grid-cols-1 items-center gap-8 py-10 sm:gap-10 sm:py-16 lg:grid-cols-[0.9fr_1.1fr] lg:gap-28 lg:py-20">
        <div class="mx-auto max-w-156 text-center lg:mx-0 lg:text-left"><p class="mb-6 text-xs font-bold tracking-[0.14em] text-primary uppercase">Member profile</p><h1 id="member-heading" class="font-serif text-5xl leading-[0.9] tracking-[-0.075em] sm:text-7xl lg:max-w-[9ch] lg:text-8xl">Hi, I’m {{ member?.name ?? 'there' }}.</h1><p v-if="member?.headline" class="mt-6 text-xl text-ink/80 sm:text-2xl">{{ member.headline }}</p><p v-if="member?.biography" class="mx-auto mt-8 max-w-104 text-base leading-relaxed text-muted-ink sm:text-lg lg:mx-0">{{ member.biography }}</p></div>
        <div class="flex min-h-96 items-end justify-center lg:justify-end"><img class="member-photo max-h-[68svh] w-full max-w-140 object-contain object-bottom lg:max-h-[calc(100svh-6rem)] lg:max-w-none" :src="member?.photoUrl || profilePlaceholder" :alt="`${member?.name ?? 'Member'} profile photo`" /></div>
      </div>
    </section>
    <ExperienceTimeline v-if="member" :experiences="timelineExperiences()" />
    <section v-if="member?.projects.length" class="bg-canvas px-5 py-20 text-ink sm:px-10 lg:px-20"><div class="mx-auto max-w-300"><p class="text-xs font-bold tracking-[.14em] text-primary uppercase">Projects</p><h2 class="mt-5 font-serif text-5xl tracking-[-.06em] sm:text-7xl">Things I’ve built.</h2><div class="mt-12 grid gap-6 sm:grid-cols-2 lg:grid-cols-3"><a v-for="item in member.projects" :key="item.id" :href="item.projectUrl" target="_blank" rel="noopener" class="overflow-hidden rounded-2xl border border-ink/15 bg-white shadow-sm transition hover:-translate-y-1 hover:border-primary"><img v-if="item.imagePath" :src="item.imagePath" :alt="item.title" class="aspect-video w-full object-cover"><div class="p-6"><h3 class="font-serif text-3xl">{{ item.title }}</h3><p v-if="item.description" class="mt-3 text-sm leading-relaxed text-muted-ink">{{ item.description }}</p></div></a></div></div></section>
    <section v-if="member?.courses.length" class="border-y border-ink/15 bg-canvas px-5 py-20 text-ink sm:px-10 lg:px-20"><div class="mx-auto max-w-300"><p class="text-xs font-bold tracking-[.14em] text-primary uppercase">Courses</p><h2 class="mt-5 font-serif text-5xl tracking-[-.06em] sm:text-7xl">Learn with me.</h2><div class="mt-12 grid gap-6 sm:grid-cols-2 lg:grid-cols-3"><article v-for="item in member.courses" :key="item.id" class="overflow-hidden rounded-2xl border border-ink/15 bg-white shadow-sm"><img v-if="item.thumbnailLink" :src="item.thumbnailLink" :alt="item.title" class="aspect-video w-full object-cover"><div class="p-6"><h3 class="font-serif text-3xl">{{ item.title }}</h3><p class="mt-3 text-sm leading-relaxed text-muted-ink">{{ item.description }}</p></div></article></div></div></section>
    <section v-if="member?.certificates.length" class="bg-canvas px-5 py-20 text-ink sm:px-10 lg:px-20"><div class="mx-auto max-w-300"><p class="text-xs font-bold tracking-[.14em] text-primary uppercase">Learning</p><h2 class="mt-5 font-serif text-5xl tracking-[-.06em] sm:text-7xl">Certificates.</h2><div class="mt-12 grid gap-6 sm:grid-cols-2 lg:grid-cols-3"><article v-for="item in member.certificates" :key="item.id" class="overflow-hidden rounded-2xl border border-ink/15 bg-white shadow-sm"><img :src="item.imagePath" :alt="item.title" class="aspect-[1.55/1] w-full object-cover"><div class="p-5"><h3 class="font-semibold">{{ item.title }}</h3><p v-if="item.issuer" class="mt-1 text-sm text-muted-ink">{{ item.issuer }}</p></div></article></div></div></section>
    <section v-if="member?.articles.length" class="border-y border-ink/15 bg-canvas px-5 py-20 text-ink sm:px-10 lg:px-20"><div class="mx-auto max-w-300"><p class="text-xs font-bold tracking-[.14em] text-primary uppercase">Writing</p><h2 class="mt-5 font-serif text-5xl tracking-[-.06em] sm:text-7xl">Notes from the work.</h2><div class="mt-12 grid gap-6 md:grid-cols-2 xl:grid-cols-3"><article v-for="item in member.articles" :key="item.id" class="rounded-2xl border border-ink/15 bg-white p-6 shadow-sm"><div class="flex flex-wrap gap-2"><span v-for="tag in item.tags" :key="tag" class="rounded-full bg-primary/15 px-2 py-1 text-xs font-bold text-primary">{{ tag }}</span></div><h3 class="mt-5 font-serif text-3xl">{{ item.title }}</h3><p class="mt-4 line-clamp-5 whitespace-pre-line text-sm leading-relaxed text-muted-ink">{{ item.content }}</p></article></div></div></section>
    <SiteFooter />
  </main>
</template>

<style scoped>.member-photo { mask-image: linear-gradient(to bottom, black 92%, transparent 100%); }</style>
