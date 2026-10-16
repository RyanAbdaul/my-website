<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { api } from '@/lib/api'
import SiteHeader from '@/components/SiteHeader.vue'

type Course = { id: string; title: string }
type Lesson = { id: string; title: string; videoLink: string | null; createTime?: string }
type Unit = { id: string; title: string; description: string | null; orderIndex: number; lessons: Lesson[] }
type CourseSection = { id: string; title: string; description: string | null; orderIndex: number; units: Unit[] }

const route = useRoute()
const course = ref<Course | null>(null)
const sections = ref<CourseSection[]>([])
const selectedLessonId = ref<string | null>(null)
const completedLessonIds = ref<Set<string>>(new Set())
const error = ref('')
const sidebarOpen = ref(false)
const openSections = ref<Set<string>>(new Set())
const openUnits = ref<Set<string>>(new Set())

const allLessons = computed(() => sections.value.flatMap((section) => section.units.flatMap((unit) => unit.lessons)))
const selectedLesson = computed(() => allLessons.value.find((lesson) => lesson.id === selectedLessonId.value) ?? null)
const selectedLessonIndex = computed(() => allLessons.value.findIndex((lesson) => lesson.id === selectedLessonId.value))
const hasPreviousLesson = computed(() => selectedLessonIndex.value > 0)
const hasNextLesson = computed(() => selectedLessonIndex.value >= 0 && selectedLessonIndex.value < allLessons.value.length - 1)
const completedCount = computed(() => allLessons.value.filter((lesson) => completedLessonIds.value.has(lesson.id)).length)
const completionPercent = computed(() => allLessons.value.length ? Math.round(completedCount.value / allLessons.value.length * 100) : 0)
const selectedLessonCompleted = computed(() => !!selectedLesson.value && completedLessonIds.value.has(selectedLesson.value.id))
const videoUrl = computed(() => selectedLesson.value ? youtubeEmbedUrl(selectedLesson.value.videoLink) : null)

function storageKey(courseId: string) { return `course-progress:${courseId}` }
function loadProgress(courseId: string) { try { const saved: unknown = JSON.parse(localStorage.getItem(storageKey(courseId)) ?? '[]'); if (Array.isArray(saved)) completedLessonIds.value = new Set(saved.filter((id): id is string => typeof id === 'string')) } catch { completedLessonIds.value = new Set() } }
function saveProgress() { if (course.value) localStorage.setItem(storageKey(course.value.id), JSON.stringify([...completedLessonIds.value])) }
function youtubeEmbedUrl(link: string | null) { if (!link) return null; const value = link.trim(); try { const url = new URL(value); if (url.hostname === 'youtu.be') return `https://www.youtube.com/embed/${url.pathname.slice(1)}`; if (url.hostname.endsWith('youtube.com')) { const id = url.searchParams.get('v') ?? url.pathname.split('/').filter(Boolean).at(-1); return id ? `https://www.youtube.com/embed/${id}` : null } } catch { if (/^[\w-]{11}$/.test(value)) return `https://www.youtube.com/embed/${value}` } return null }

function toggleSection(id: string) {
  const next = new Set(openSections.value)
  next.has(id) ? next.delete(id) : next.add(id)
  openSections.value = next
}
function toggleUnit(id: string) {
  const next = new Set(openUnits.value)
  next.has(id) ? next.delete(id) : next.add(id)
  openUnits.value = next
}
function lessonLocation(lessonId: string): { sectionId: string; unitId: string } | null {
  for (const section of sections.value) {
    for (const unit of section.units) {
      if (unit.lessons.some((lesson) => lesson.id === lessonId)) return { sectionId: section.id, unitId: unit.id }
    }
  }
  return null
}
function selectLesson(id: string) {
  selectedLessonId.value = id
  const location = lessonLocation(id)
  if (location) {
    openSections.value = new Set(openSections.value).add(location.sectionId)
    openUnits.value = new Set(openUnits.value).add(location.unitId)
  }
  if (window.innerWidth < 1024) sidebarOpen.value = false
}
function selectAdjacentLesson(direction: -1 | 1) { const lesson = allLessons.value[selectedLessonIndex.value + direction]; if (lesson) selectLesson(lesson.id) }
function toggleLessonComplete(id: string) { const next = new Set(completedLessonIds.value); next.has(id) ? next.delete(id) : next.add(id); completedLessonIds.value = next; saveProgress() }

onMounted(async () => {
  sidebarOpen.value = window.matchMedia('(min-width: 1024px)').matches
  const courseId = String(route.params.courseId)
  try {
    const [courseResponse, contentResponse] = await Promise.all([
      api<Course>(`/courses/${courseId}`),
      api<CourseSection[]>(`/sections/course/${courseId}/content`).catch(() => []),
    ])
    course.value = courseResponse
    sections.value = contentResponse
    loadProgress(courseId)

    const lessons = allLessons.value
    selectedLessonId.value = lessons.find((lesson) => youtubeEmbedUrl(lesson.videoLink))?.id ?? lessons[0]?.id ?? null

    const firstSection = sections.value[0]
    if (firstSection) {
      openSections.value = new Set([firstSection.id])
      const firstUnit = firstSection.units[0]
      if (firstUnit) openUnits.value = new Set([firstUnit.id])
    }
    if (selectedLessonId.value) {
      const location = lessonLocation(selectedLessonId.value)
      if (location) {
        openSections.value = new Set(openSections.value).add(location.sectionId)
        openUnits.value = new Set(openUnits.value).add(location.unitId)
      }
    }
  } catch {
    error.value = 'Course content could not be loaded.'
  }
})
</script>

<template>
  <main class="min-h-svh bg-canvas text-ink"><SiteHeader />
    <p v-if="error" class="mx-auto max-w-350 px-5 py-10 text-red-600 sm:px-10 lg:px-20">{{ error }}</p>
    <div v-else-if="course" class="course-layout mx-auto max-w-350">
      <div v-if="sidebarOpen" class="sidebar-overlay" @click="sidebarOpen = false"></div>

      <aside class="course-sidebar" :class="{ 'course-sidebar-open': sidebarOpen }">
        <div class="border-b border-ink/10 p-5 sm:p-6">
          <div class="flex items-start justify-between gap-4">
            <p class="text-xs font-bold tracking-[.14em] text-accent uppercase">Course content</p>
            <button class="sidebar-close" type="button" aria-label="Close lessons sidebar" @click="sidebarOpen = false">×</button>
          </div>
          <h1 class="mt-2 font-serif text-xl leading-tight tracking-[-.04em]">{{ course.title }}</h1>
          <div class="mt-5 flex items-center justify-between text-xs font-bold text-muted-ink">
            <span>{{ completedCount }} of {{ allLessons.length }} complete</span>
            <span>{{ completionPercent }}%</span>
          </div>
          <div class="mt-2 h-2 overflow-hidden rounded-full bg-ink/10">
            <div class="h-full rounded-full bg-primary transition-[width] duration-300" :style="{ width: `${completionPercent}%` }"></div>
          </div>
        </div>

        <nav class="course-outline" aria-label="Course content">
          <div v-if="sections.length" class="space-y-1">
            <div v-for="section in sections" :key="section.id" class="content-section">
              <button class="section-toggle" type="button" :aria-expanded="openSections.has(section.id)" @click="toggleSection(section.id)">
                <span class="section-toggle-label">
                  <span class="section-number">{{ section.orderIndex }}</span>
                  <span class="min-w-0 flex-1 text-left">{{ section.title }}</span>
                </span>
                <span class="chevron" :class="{ 'is-open': openSections.has(section.id) }">▸</span>
              </button>

              <div v-if="openSections.has(section.id)" class="unit-list">
                <div v-for="unit in section.units" :key="unit.id" class="content-unit">
                  <button class="unit-toggle" type="button" :aria-expanded="openUnits.has(unit.id)" @click="toggleUnit(unit.id)">
                    <span class="min-w-0 flex-1 text-left">{{ unit.title }}</span>
                    <span class="unit-meta">{{ unit.lessons.length }}</span>
                    <span class="chevron" :class="{ 'is-open': openUnits.has(unit.id) }">▸</span>
                  </button>

                  <div v-if="openUnits.has(unit.id)" class="lesson-list">
                    <div v-for="(lesson, index) in unit.lessons" :key="lesson.id" class="lesson-row" :class="{ 'lesson-row-active': lesson.id === selectedLessonId }">
                      <button class="lesson-link" type="button" @click="selectLesson(lesson.id)">
                        <span class="lesson-number">{{ index + 1 }}</span>
                        <span class="min-w-0 flex-1 text-left">{{ lesson.title }}</span>
                      </button>
                      <button class="completion-toggle" :class="{ 'completion-toggle-done': completedLessonIds.has(lesson.id) }" type="button" :aria-label="`${completedLessonIds.has(lesson.id) ? 'Mark incomplete' : 'Mark complete'}: ${lesson.title}`" @click="toggleLessonComplete(lesson.id)">{{ completedLessonIds.has(lesson.id) ? '✓' : '' }}</button>
                    </div>
                  </div>
                </div>
                <p v-if="!section.units.length" class="px-3 py-4 text-sm text-muted-ink">Units will be added soon.</p>
              </div>
            </div>
          </div>
          <p v-else class="px-3 py-5 text-sm text-muted-ink">Course content will be added soon.</p>
        </nav>
      </aside>

      <section class="course-main min-w-0 p-5 sm:p-10 lg:p-14">
        <div class="flex items-center gap-4">
          <button class="sidebar-toggle" type="button" :aria-expanded="sidebarOpen" @click="sidebarOpen = !sidebarOpen">☰ Course content</button>
          <p class="text-xs font-bold tracking-[.14em] text-accent uppercase">{{ course.title }}</p>
        </div>
        <p v-if="selectedLesson" class="mt-8 text-sm font-bold text-muted-ink">Lesson {{ selectedLessonIndex + 1 }} of {{ allLessons.length }}</p>
        <h2 class="mt-2 font-serif text-3xl tracking-[-.045em] sm:text-4xl">{{ selectedLesson?.title ?? 'Choose a lesson' }}</h2>
        <div class="video-player mt-8 overflow-hidden rounded-2xl border border-ink/15 bg-ink shadow-xl shadow-black/25">
          <iframe v-if="videoUrl" class="aspect-video w-full" :src="videoUrl" :title="selectedLesson?.title" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share" allowfullscreen></iframe>
          <div v-else class="grid aspect-video place-items-center bg-linear-to-br from-primary/40 to-secondary/20 p-8 text-center text-ink"><div><p class="text-lg font-bold">Video unavailable</p><p class="mt-2 text-sm text-muted-ink">A YouTube link will appear here when this lesson is ready.</p></div></div>
          <button class="video-nav video-nav-previous" type="button" :disabled="!hasPreviousLesson" aria-label="Previous lesson" @click="selectAdjacentLesson(-1)">‹</button>
          <button class="video-nav video-nav-next" type="button" :disabled="!hasNextLesson" aria-label="Next lesson" @click="selectAdjacentLesson(1)">›</button>
        </div>
        <div v-if="selectedLesson" class="mt-6 flex flex-wrap items-center justify-between gap-4 rounded-2xl border border-ink/12 bg-white p-4 sm:p-5">
          <div><p class="font-bold">{{ selectedLessonCompleted ? 'Lesson completed' : 'Ready when you are' }}</p><p class="mt-1 text-sm text-muted-ink">{{ selectedLessonCompleted ? 'You can uncheck this lesson at any time.' : 'Mark it complete when you finish watching.' }}</p></div>
          <button class="complete-button" :class="{ 'complete-button-done': selectedLessonCompleted }" type="button" @click="toggleLessonComplete(selectedLesson.id)">{{ selectedLessonCompleted ? '✓ Checked' : 'Mark as complete' }}</button>
        </div>
        <div class="mt-5 flex flex-wrap gap-3">
          <button class="resource-button" type="button" disabled>Ask</button>
          <button class="resource-button" type="button" disabled>Assignment</button>
          <button class="resource-button" type="button" disabled>Source code</button>
        </div>
      </section>
    </div>
    <p v-else class="mx-auto max-w-350 px-5 py-10 text-muted-ink sm:px-10 lg:px-20">Loading course content…</p>
  </main>
</template>

<style scoped>
.course-layout { display: flex; min-height: calc(100svh - 5rem); }
.course-sidebar { width: 21rem; flex: 0 0 21rem; border-right: 1px solid color-mix(in srgb, var(--color-ink) 15%, transparent); background: var(--color-surface); }
.course-main { flex: 1; }
.course-outline { padding: .75rem; }

.content-section { overflow: hidden; border: 1px solid color-mix(in srgb, var(--color-ink) 11%, transparent); border-radius: .85rem; background: color-mix(in srgb, var(--color-ink) 2%, var(--color-surface)); }
.section-toggle { display: flex; width: 100%; align-items: center; gap: .6rem; padding: .8rem .9rem; text-align: left; cursor: pointer; }
.section-toggle-label { display: flex; min-width: 0; flex: 1; align-items: center; gap: .6rem; font-size: .82rem; font-weight: 800; color: var(--color-ink); }
.section-number { display: grid; width: 1.45rem; height: 1.45rem; flex: 0 0 1.45rem; place-items: center; border-radius: 999px; background: color-mix(in srgb, var(--color-primary) 18%, var(--color-surface)); color: var(--color-accent); font-size: .65rem; font-weight: 800; }

.unit-list { padding: 0 .5rem .5rem; }
.content-unit { margin-top: .3rem; overflow: hidden; border: 1px solid color-mix(in srgb, var(--color-ink) 9%, transparent); border-radius: .7rem; }
.unit-toggle { display: flex; width: 100%; align-items: center; gap: .5rem; padding: .6rem .7rem; color: var(--color-muted-ink); font-size: .78rem; font-weight: 700; text-align: left; cursor: pointer; }
.unit-toggle:hover { color: var(--color-ink); }
.unit-meta { color: var(--color-muted-ink); font-size: .66rem; font-weight: 700; }

.chevron { flex: 0 0 auto; color: var(--color-muted-ink); font-size: .8rem; transition: transform .18s ease; }
.chevron.is-open { transform: rotate(90deg); }

.lesson-list { border-top: 1px solid color-mix(in srgb, var(--color-ink) 10%, transparent); }
.lesson-row { display: flex; align-items: center; gap: .35rem; border-bottom: 1px solid color-mix(in srgb, var(--color-ink) 8%, transparent); padding: .25rem; }
.lesson-row:last-child { border-bottom: 0; }
.lesson-row-active { background: color-mix(in srgb, var(--color-primary) 18%, var(--color-surface)); }
.lesson-link { display: flex; min-width: 0; flex: 1; align-items: center; gap: .7rem; padding: .65rem .5rem; color: var(--color-muted-ink); font-size: .85rem; font-weight: 650; }
.lesson-link:hover, .lesson-row-active .lesson-link { color: var(--color-ink); }
.lesson-number { display: grid; width: 1.45rem; height: 1.45rem; flex: 0 0 1.45rem; place-items: center; border-radius: 999px; background: color-mix(in srgb, var(--color-ink) 7%, var(--color-surface)); color: var(--color-muted-ink); font-size: .65rem; font-weight: 800; }

.completion-toggle { display: grid; width: 1.5rem; height: 1.5rem; flex: 0 0 1.5rem; place-items: center; border: 2px solid color-mix(in srgb, var(--color-ink) 55%, transparent); border-radius: 999px; color: white; font-size: .8rem; font-weight: 900; transition: .2s; }
.completion-toggle:hover { border-color: var(--color-primary); background: color-mix(in srgb, var(--color-primary) 24%, var(--color-surface)); }
.completion-toggle-done { border-color: var(--color-primary); background: var(--color-primary); }

.complete-button { border-radius: .7rem; background: var(--color-primary); padding: .7rem 1rem; color: white; font-size: .85rem; font-weight: 800; }
.complete-button-done { background: color-mix(in srgb, var(--color-primary) 24%, var(--color-surface)); color: var(--color-accent); }
.resource-button { border: 1px solid color-mix(in srgb, var(--color-ink) 18%, transparent); border-radius: .6rem; background: var(--color-surface); padding: .55rem .9rem; color: var(--color-muted-ink); font-size: .8rem; font-weight: 800; }

.sidebar-close, .sidebar-toggle { border: 1px solid color-mix(in srgb, var(--color-ink) 18%, transparent); border-radius: .6rem; background: var(--color-surface); color: var(--color-ink); font-size: .8rem; font-weight: 800; }
.sidebar-close { display: inline-flex; padding: .1rem .45rem; font-size: 1.35rem; line-height: 1; }
.sidebar-toggle { display: inline-flex; align-items: center; gap: .45rem; padding: .5rem .7rem; }
.sidebar-overlay { display: none; }

.video-player { position: relative; }
.video-nav { position: absolute; top: 50%; z-index: 5; display: grid; width: 2.6rem; height: 2.6rem; place-items: center; transform: translateY(-50%); border-radius: 999px; background: color-mix(in srgb, var(--color-canvas) 70%, transparent); color: var(--color-ink); font-size: 1.6rem; line-height: 1; }
.video-nav:disabled { opacity: .3; cursor: not-allowed; }
.video-nav-previous { left: .6rem; }
.video-nav-next { right: .6rem; }

@media (min-width: 1024px) {
  .course-sidebar:not(.course-sidebar-open) { display: none; }
}
@media (max-width: 1023px) {
  .course-layout { display: block; }
  .course-sidebar { position: fixed; top: 0; bottom: 0; left: 0; z-index: 60; width: min(21rem, 88vw); transform: translateX(-105%); transition: transform .22s ease; overflow-y: auto; box-shadow: 16px 0 40px rgb(0 0 0 / .35); }
  .course-sidebar-open { transform: translateX(0); }
  .sidebar-overlay { position: fixed; inset: 0; z-index: 50; display: block; background: rgb(0 0 0 / .7); }
}
</style>
