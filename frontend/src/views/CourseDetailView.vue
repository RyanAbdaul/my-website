<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { api } from '@/lib/api'
import { isSignedIn } from '@/lib/auth'
import SiteHeader from '@/components/SiteHeader.vue'
import SiteFooter from '@/components/SiteFooter.vue'

type Lesson = { id: string; title: string }
type Review = { id: string; studentName: string; rating: number; comment: string | null }
type Course = { id: string; title: string; description: string; price: number; thumbnailLink: string | null }

const route = useRoute()
const router = useRouter()
const course = ref<Course | null>(null)
const lessons = ref<Lesson[]>([])
const reviews = ref<Review[]>([])
const error = ref('')
const enrollmentError = ref('')
const enrolling = ref(false)

onMounted(async () => {
  const courseId = String(route.params.courseId)
  try {
    const [courseResponse, lessonResponse, reviewResponse] = await Promise.all([
      api<Course>(`/courses/${courseId}`),
      api<Lesson[]>(`/lessons/course/${courseId}`).catch(() => []),
      api<Review[]>(`/reviews/course/${courseId}`).catch(() => []),
    ])
    course.value = courseResponse
    lessons.value = lessonResponse
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
          <RouterLink to="/courses" class="text-sm font-bold text-primary transition hover:text-ink">← All courses</RouterLink>
          <p v-if="error" class="mt-10 text-red-600">{{ error }}</p>
          <template v-else-if="course">
            <p class="mt-10 text-xs font-bold tracking-[.14em] text-primary uppercase">Course</p>
            <h1 class="mt-4 max-w-4xl font-serif text-5xl leading-tight tracking-[-.055em] sm:text-6xl">{{ course.title }}</h1>
            <p class="mt-6 max-w-3xl whitespace-pre-line text-lg leading-relaxed text-muted-ink">{{ course.description }}</p>
            <div class="mt-8 flex flex-wrap items-center gap-5"><p class="text-xl font-bold">${{ course.price }}</p><RouterLink v-if="!isSignedIn" to="/auth?mode=login" class="enroll-button">Log in to enroll</RouterLink><button v-else class="enroll-button disabled:cursor-not-allowed disabled:opacity-50" type="button" :disabled="course.price !== 0 || enrolling" @click="enroll">{{ course.price === 0 ? (enrolling ? 'Enrolling…' : 'Enroll') : 'Paid course' }}</button></div>
            <p v-if="enrollmentError" class="mt-4 text-sm font-semibold text-red-600">{{ enrollmentError }}</p>
          </template>
          <p v-else class="mt-10 text-muted-ink">Loading course…</p>
        </div>
        <div v-if="course" class="overflow-hidden rounded-3xl border border-ink/10 bg-white shadow-lg shadow-sky-900/8"><img v-if="course.thumbnailLink" :src="course.thumbnailLink" :alt="course.title" class="aspect-video w-full object-cover" /><div v-else class="grid aspect-video place-items-center bg-linear-to-br from-primary/35 to-secondary/50 p-8 text-center font-serif text-3xl leading-tight text-primary">{{ course.title }}</div></div>
      </div>
    </section>

    <section v-if="course" class="mx-auto max-w-350 px-5 py-14 sm:px-10 lg:px-20 lg:py-18">
      <div>
        <div>
          <section><p class="section-label">Table of contents</p><ol v-if="lessons.length" class="mt-6 overflow-hidden rounded-2xl border border-ink/15 bg-white"><li v-for="(lesson, index) in lessons" :key="lesson.id" class="flex items-center gap-4 border-b border-ink/10 px-5 py-4 last:border-b-0"><span class="grid size-7 shrink-0 place-items-center rounded-full bg-primary/12 text-xs font-bold text-primary">{{ index + 1 }}</span><span class="font-semibold">{{ lesson.title }}</span></li></ol><p v-else class="mt-6 rounded-2xl border border-dashed border-ink/20 p-6 text-muted-ink">Lessons will be added soon.</p></section>
          <section class="mt-16 border-t border-ink/15 pt-14"><p class="section-label">Reviews</p><div v-if="reviews.length" class="mt-7 space-y-5"><article v-for="review in reviews" :key="review.id" class="rounded-2xl border border-ink/12 bg-white p-6"><div class="flex items-center justify-between gap-4"><h3 class="font-bold">{{ review.studentName }}</h3><span class="text-sm font-bold text-primary">{{ '★'.repeat(review.rating) }}<span class="text-ink/20">{{ '★'.repeat(5 - review.rating) }}</span></span></div><p v-if="review.comment" class="mt-3 leading-relaxed text-muted-ink">{{ review.comment }}</p></article></div><p v-else class="mt-6 text-muted-ink">No reviews yet.</p></section>
        </div>
      </div>
    </section>
    <SiteFooter />
  </main>
</template>

<style scoped>
.section-label { color: var(--color-primary); font-size: .75rem; font-weight: 800; letter-spacing: .14em; text-transform: uppercase; }
.enroll-button { border-radius: 9999px; background: var(--color-primary); padding: .75rem 1.35rem; color: white; font-size: .875rem; font-weight: 800; transition: background .2s ease, transform .2s ease; }
.enroll-button:hover { background: color-mix(in srgb, var(--color-primary) 85%, black); transform: translateY(-1px); }
</style>
