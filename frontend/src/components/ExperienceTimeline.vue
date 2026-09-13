<script setup lang="ts">
import { nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'

type Experience = {
  role: string
  company: string
  companyUrl?: string | null
  dates: string
  summary: string
  highlights: string[]
}

const experiences = ref<Experience[]>([])

const props = defineProps<{ experiences?: Experience[] }>()

const markers = ref<HTMLElement[]>([])
const timeline = ref<HTMLElement | null>(null)
const progress = ref(0)
const activeIndex = ref(-1)
const lineStart = ref(0)
const lineLength = ref(0)

function setMarker(el: unknown, index: number) {
  if (el instanceof HTMLElement) markers.value[index] = el
}

function updateTimeline() {
  const items = markers.value.filter((marker): marker is HTMLElement => marker instanceof HTMLElement)
  const first = items[0]
  const last = items.at(-1)
  const timelineElement = timeline.value
  if (!first || !last || !timelineElement) return

  const triggerY = window.innerHeight * 0.52
  const rootY = timelineElement.getBoundingClientRect().top
  const firstRect = first.getBoundingClientRect()
  const lastRect = last.getBoundingClientRect()
  const firstY = firstRect.top + firstRect.height / 2
  const lastY = lastRect.top + lastRect.height / 2
  const rawProgress = items.length === 1 ? (triggerY - firstY) : (triggerY - firstY) / (lastY - firstY)
  progress.value = Math.min(1, Math.max(0, rawProgress))
  lineStart.value = firstY - rootY
  lineLength.value = lastY - firstY
  activeIndex.value = items.reduce((reached, marker, index) => {
    const rect = marker.getBoundingClientRect()
    return rect.top + rect.height / 2 <= triggerY ? index : reached
  }, -1)
}

function formatDates(startDate: string, endDate: string | null) {
  const startYear = new Date(startDate).getUTCFullYear()
  return `${startYear} - ${endDate ? new Date(endDate).getUTCFullYear() : 'Present'}`
}

onMounted(() => {
  updateTimeline()
  window.addEventListener('scroll', updateTimeline, { passive: true })
  window.addEventListener('resize', updateTimeline)

  if (props.experiences) {
    experiences.value = props.experiences
    nextTick(updateTimeline)
    return
  }

  fetch('/api/work-experiences')
    .then((response) => response.ok ? response.json() : Promise.reject())
    .then(async (items) => {
      if (!Array.isArray(items) || !items.length) return
      experiences.value = items.map((item) => ({
        role: item.role,
        company: item.company,
        dates: formatDates(item.startDate, item.endDate),
        summary: item.summary,
        highlights: item.highlights ?? [],
      }))
      await nextTick()
      updateTimeline()
    })
    .catch(() => undefined)
})

watch(() => props.experiences, async (items) => {
  if (!items) return
  experiences.value = items
  await nextTick()
  updateTimeline()
}, { deep: true })

onBeforeUnmount(() => {
  window.removeEventListener('scroll', updateTimeline)
  window.removeEventListener('resize', updateTimeline)
})
</script>

<template>
  <section id="experience" class="relative overflow-hidden border-b border-ink/15 bg-canvas px-5 py-28 text-ink sm:px-10 sm:py-36 lg:px-20" aria-labelledby="experience-heading">
    <div class="pointer-events-none absolute top-1/4 left-1/3 size-80 rounded-full bg-secondary/20 blur-[130px]"></div>

    <div class="relative mx-auto max-w-300">
      <div class="max-w-2xl">
        <p class="text-xs font-bold tracking-[0.14em] text-primary uppercase">Experience</p>
        <h2 id="experience-heading" class="mt-5 font-serif text-5xl leading-[0.9] tracking-[-0.065em] sm:text-7xl">The work behind the ideas.</h2>
        <p class="mt-6 max-w-xl text-base leading-relaxed text-muted-ink sm:text-lg">Scroll through the milestones that have shaped how I build, learn, and collaborate.</p>
      </div>

      <div ref="timeline" class="relative mt-14 max-w-4xl sm:mt-20">
        <div class="absolute left-3 w-px bg-ink/15 sm:left-5" :style="{ top: `${lineStart}px`, height: `${lineLength}px` }" aria-hidden="true">
          <div class="w-full bg-primary shadow-[0_0_16px_2px_var(--color-primary)]" :style="{ height: `${progress * 100}%` }"></div>
        </div>

        <article v-for="(experience, index) in experiences" :key="experience.role + index" class="relative grid grid-cols-[1.5rem_minmax(0,1fr)] gap-x-5 pb-14 sm:min-h-64 sm:grid-cols-[2.5rem_minmax(0,1fr)] sm:gap-x-10 sm:pb-24">
          <div class="relative z-10 pt-0.5">
            <span
              :ref="(el) => setMarker(el, index)"
              class="block size-6 rounded-full border-2 border-muted-ink/60 bg-canvas transition-all duration-500 sm:size-10"
              :class="index <= activeIndex ? 'scale-125 border-primary bg-primary shadow-[0_0_0_6px_rgba(14,165,233,0.18),0_0_36px_10px_rgba(14,165,233,0.55)]' : ''"
            ></span>
          </div>

          <div class="transition-all duration-500" :class="index <= activeIndex ? 'translate-x-0 opacity-100' : 'translate-x-3 opacity-45'">
            <p class="text-sm font-bold tracking-[0.12em] text-primary uppercase">{{ experience.dates }}</p>
            <h3 class="mt-3 font-serif text-3xl leading-none tracking-[-0.05em] sm:text-5xl">{{ experience.role }}</h3>
            <a v-if="experience.companyUrl" class="mt-2 inline-block text-lg text-ink/80 underline decoration-primary underline-offset-4 hover:text-primary sm:text-xl" :href="experience.companyUrl" target="_blank" rel="noreferrer">{{ experience.company }}</a>
            <p v-else class="mt-2 text-lg text-ink/80 sm:text-xl">{{ experience.company }}</p>
            <p class="mt-6 max-w-2xl text-base leading-relaxed text-muted-ink sm:text-lg">{{ experience.summary }}</p>
            <ul v-if="experience.highlights.length" class="mt-6 max-w-3xl space-y-3 text-sm leading-relaxed text-muted-ink sm:text-base">
              <li v-for="highlight in experience.highlights" :key="highlight" class="flex gap-3"><span class="mt-2 size-1.5 shrink-0 rounded-full bg-primary"></span><span>{{ highlight }}</span></li>
            </ul>
          </div>
        </article>
        <p v-if="!experiences.length" class="rounded-2xl border border-dashed border-ink/20 p-8 text-muted-ink">Experience details are coming soon.</p>
      </div>
    </div>
  </section>
</template>
