<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { api } from '@/lib/api'
import { isSignedIn } from '@/lib/auth'
import SiteHeader from '@/components/SiteHeader.vue'
import SiteFooter from '@/components/SiteFooter.vue'

type Lesson = { id: string; title: string }
type Unit = { id: string; title: string; description: string | null; orderIndex: number; lessons: Lesson[] }
type CourseSection = { id: string; title: string; description: string | null; orderIndex: number; units: Unit[] }
type Review = { id: string; studentName: string; rating: number; comment: string | null }
type Course = { id: string; title: string; description: string; price: number; thumbnailLink: string | null }

const route = useRoute()
const router = useRouter()
const course = ref<Course | null>(null)
const sections = ref<CourseSection[]>([])
const reviews = ref<Review[]>([])
const error = ref('')
const enrollmentError = ref('')
const enrolling = ref(false)

const totalUnits = computed(() => sections.value.reduce((total, section) => total + section.units.length, 0))
const totalLessons = computed(() => sections.value.reduce((total, section) => total + section.units.reduce((count, unit) => count + unit.lessons.length, 0), 0))

onMounted(async () => {
  const courseId = String(route.params.courseId)
  try {
    const [courseResponse, contentResponse, reviewResponse] = await Promise.all([
      api<Course>(`/courses/${courseId}`),
      api<CourseSection[]>(`/sections/course/${courseId}/content`).catch(() => []),
      api<Review[]>(`/reviews/course/${courseId}`).catch(() => []),
    ])
    course.value = courseResponse
    sections.value = contentResponse
    reviews.value = reviewResponse
  } catch {
    error.value = 'Course not found.'
  }
})

async function enroll() {
  if (!course.value || course.value.price !== 0 || enrolling.value) return

  enrolling.value = true
  enrollmentError.value = ''
  try {
    await api(`/enrollments/courses/${course.value.id}/me`, { method: 'POST' })
    await router.push({ name: 'course-content', params: { courseId: course.value.id } })
  } catch (reason) {
    enrollmentError.value = reason instanceof Error ? reason.message : 'Enrollment could not be completed.'
  } finally {
    enrolling.value = false
  }
}
</script>

<template>
  <main class="min-h-svh bg-canvas text-ink">
    <SiteHeader />
    <section class="border-b border-ink/15 bg-linear-to-br from-canvas via-canvas to-secondary/20">
      <div class="mx-auto grid max-w-350 gap-10 px-5 py-12 sm:px-10 lg:grid-cols-[minmax(0,1fr)_27rem] lg:px-20 lg:py-18">
        <div>
          <RouterLink to="/courses" class="text-sm font-bold text-accent transition hover:text-ink">← All courses</RouterLink>
          <p v-if="error" class="mt-10 text-red-600">{{ error }}</p>
          <template v-else-if="course">
            <p class="mt-10 text-xs font-bold tracking-[.14em] text-accent uppercase">Course</p>
            <h1 class="mt-4 max-w-4xl font-serif text-5xl leading-tight tracking-[-.055em] sm:text-6xl">{{ course.title }}</h1>
            <p class="mt-6 max-w-3xl whitespace-pre-line text-lg leading-relaxed text-muted-ink">{{ course.description }}</p>
            <div class="mt-8 flex flex-wrap items-center gap-5"><p class="text-xl font-bold">${{ course.price }}</p><RouterLink v-if="!isSignedIn" to="/auth?mode=login" class="enroll-button">Log in to enroll</RouterLink><button v-else class="enroll-button disabled:cursor-not-allowed disabled:opacity-50" type="button" :disabled="course.price !== 0 || enrolling" @click="enroll">{{ course.price === 0 ? (enrolling ? 'Enrolling…' : 'Enroll') : 'Paid course' }}</button></div>
            <p v-if="enrollmentError" class="mt-4 text-sm font-semibold text-red-600">{{ enrollmentError }}</p>
          </template>
          <p v-else class="mt-10 text-muted-ink">Loading course…</p>
        </div>
        <div v-if="course" class="overflow-hidden rounded-3xl border border-ink/10 bg-white shadow-lg shadow-black/40"><img v-if="course.thumbnailLink" :src="course.thumbnailLink" :alt="course.title" class="aspect-video w-full object-cover" /><div v-else class="grid aspect-video place-items-center bg-linear-to-br from-primary/35 to-secondary/50 p-8 text-center font-serif text-3xl leading-tight text-accent">{{ course.title }}</div></div>
      </div>
    </section>

    <section v-if="course" class="mx-auto max-w-350 px-5 py-14 sm:px-10 lg:px-20 lg:py-18">
      <div>
        <div>
          <section>
            <div class="flex flex-wrap items-end justify-between gap-3">
              <p class="section-label">Course content</p>
              <p v-if="sections.length" class="text-sm font-semibold text-muted-ink">{{ totalUnits }} units · {{ totalLessons }} lessons</p>
            </div>
            <div v-if="sections.length" class="mt-6 space-y-3">
              <div v-for="section in sections" :key="section.id" class="content-block">
                <div class="content-block-head">
                  <span class="content-block-number">{{ section.orderIndex }}</span>
                  <div class="min-w-0">
                    <h3 class="content-block-title">{{ section.title }}</h3>
                    <p v-if="section.description" class="content-block-desc">{{ section.description }}</p>
                  </div>
                </div>
                <ul v-if="section.units.length" class="unit-overview">
                  <li v-for="unit in section.units" :key="unit.id" class="unit-overview-item">
                    <div class="unit-overview-row">
                      <span class="unit-overview-title">{{ unit.title }}</span>
                      <span class="unit-overview-count">{{ unit.lessons.length }} {{ unit.lessons.length === 1 ? 'lesson' : 'lessons' }}</span>
                    </div>
                    <p v-if="unit.description" class="unit-overview-desc">{{ unit.description }}</p>
                  </li>
                </ul>
                <p v-else class="unit-overview-empty">Units will be added soon.</p>
              </div>
            </div>
            <p v-else class="mt-6 rounded-2xl border border-dashed border-ink/20 p-6 text-muted-ink">Course content will be added soon.</p>
          </section>
          <section class="mt-16 border-t border-ink/15 pt-14"><p class="section-label">Reviews</p><div v-if="reviews.length" class="mt-7 space-y-5"><article v-for="review in reviews" :key="review.id" class="rounded-2xl border border-ink/12 bg-white p-6"><div class="flex items-center justify-between gap-4"><h3 class="font-bold">{{ review.studentName }}</h3><span class="text-sm font-bold text-accent">{{ '★'.repeat(review.rating) }}<span class="text-ink/20">{{ '★'.repeat(5 - review.rating) }}</span></span></div><p v-if="review.comment" class="mt-3 leading-relaxed text-muted-ink">{{ review.comment }}</p></article></div><p v-else class="mt-6 text-muted-ink">No reviews yet.</p></section>
        </div>
      </div>
    </section>
    <SiteFooter />
  </main>
</template>

<style scoped>
.section-label { color: var(--color-accent); font-size: .75rem; font-weight: 800; letter-spacing: .14em; text-transform: uppercase; }
.content-block { overflow: hidden; border: 1px solid color-mix(in srgb, var(--color-ink) 14%, transparent); border-radius: 1rem; background: var(--color-surface); }
.content-block-head { display: flex; align-items: flex-start; gap: .8rem; border-bottom: 1px solid color-mix(in srgb, var(--color-ink) 10%, transparent); padding: 1.1rem 1.25rem; }
.content-block-number { display: grid; width: 1.8rem; height: 1.8rem; flex: 0 0 1.8rem; place-items: center; border-radius: 999px; background: color-mix(in srgb, var(--color-primary) 20%, var(--color-surface)); color: var(--color-accent); font-size: .78rem; font-weight: 800; }
.content-block-title { font-family: var(--font-serif); font-size: 1.25rem; letter-spacing: -.03em; }
.content-block-desc { margin-top: .25rem; color: var(--color-muted-ink); font-size: .85rem; line-height: 1.5; }
.unit-overview { display: grid; }
.unit-overview-item { border-bottom: 1px solid color-mix(in srgb, var(--color-ink) 8%, transparent); padding: .85rem 1.25rem; }
.unit-overview-item:last-child { border-bottom: 0; }
.unit-overview-row { display: flex; align-items: center; justify-content: space-between; gap: 1rem; }
.unit-overview-title { font-weight: 700; font-size: .92rem; }
.unit-overview-count { flex: 0 0 auto; border-radius: 999px; background: color-mix(in srgb, var(--color-secondary) 22%, var(--color-surface)); color: var(--color-accent); padding: .18rem .5rem; font-size: .68rem; font-weight: 800; }
.unit-overview-desc { margin-top: .25rem; color: var(--color-muted-ink); font-size: .8rem; line-height: 1.5; }
.unit-overview-empty { padding: .85rem 1.25rem; color: var(--color-muted-ink); font-size: .85rem; }
.enroll-button { border-radius: 9999px; background: var(--color-primary); padding: .75rem 1.35rem; color: white; font-size: .875rem; font-weight: 800; transition: background .2s ease, transform .2s ease; }
.enroll-button:hover { background: color-mix(in srgb, var(--color-primary) 85%, black); transform: translateY(-1px); }
</style>
