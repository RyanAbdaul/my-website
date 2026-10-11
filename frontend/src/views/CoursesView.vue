<script setup lang="ts">
import { computed, onBeforeUnmount, ref, watch } from 'vue'
import { api } from '@/lib/api'
import { isSignedIn } from '@/lib/auth'
import SiteFooter from '@/components/SiteFooter.vue'
import SiteHeader from '@/components/SiteHeader.vue'

type Course = {
  id: string
  title: string
  description: string
  price: number
  thumbnailLink: string | null
}
type PriceRange = { label: string; minPrice?: number; maxPrice?: number }

const priceRanges: PriceRange[] = [
  { label: 'Free', minPrice: 0, maxPrice: 0 },
  { label: 'Under $50', minPrice: 0, maxPrice: 50 },
  { label: '$50 – $100', minPrice: 50, maxPrice: 100 },
  { label: 'Over $100', minPrice: 100 },
]

const courses = ref<Course[]>([])
const title = ref('')
const selectedRange = ref<PriceRange | null>(null)
const enrolledOnly = ref(false)
const loading = ref(false)
const error = ref('')
const filtersOpen = ref(true)
let titleTimer: ReturnType<typeof setTimeout> | undefined

const courseCount = computed(() => `${courses.value.length} course${courses.value.length === 1 ? '' : 's'}`)

async function loadCourses() {
  loading.value = true
  error.value = ''
  const params = new URLSearchParams()
  if (title.value.trim()) params.set('title', title.value.trim())
  if (selectedRange.value?.minPrice !== undefined) params.set('minPrice', String(selectedRange.value.minPrice))
  if (selectedRange.value?.maxPrice !== undefined) params.set('maxPrice', String(selectedRange.value.maxPrice))

  try {
    const matchingCourses = await api<Course[]>(`/courses${params.size ? `?${params}` : ''}`)
    if (enrolledOnly.value && isSignedIn.value) {
      const enrollmentChecks = await Promise.all(matchingCourses.map(async (course) => ({
        course,
        isEnrolled: await api<boolean>(`/enrollments/courses/${course.id}/me`).catch(() => false),
      })))
      courses.value = enrollmentChecks.filter((item) => item.isEnrolled).map((item) => item.course)
    } else {
      courses.value = enrolledOnly.value ? [] : matchingCourses
    }
  } catch {
    error.value = 'Courses could not be loaded. Please try again.'
  } finally {
    loading.value = false
  }
}

function setPriceRange(range: PriceRange) {
  selectedRange.value = selectedRange.value?.label === range.label ? null : range
}

watch([selectedRange, enrolledOnly, isSignedIn], loadCourses, { immediate: true })
watch(title, () => {
  if (titleTimer) clearTimeout(titleTimer)
  titleTimer = setTimeout(loadCourses, 300)
})
onBeforeUnmount(() => { if (titleTimer) clearTimeout(titleTimer) })
</script>

<template>
  <main class="min-h-svh bg-canvas text-ink">
    <SiteHeader />

    <section class="mx-auto max-w-350 px-5 py-12 sm:px-10 lg:px-20 lg:py-16">
      <div class="mb-10 flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between"><div><p class="text-xs font-bold tracking-[0.14em] text-primary uppercase">Browse courses</p><h1 class="mt-4 font-serif text-5xl leading-[0.9] tracking-[-0.065em] sm:text-6xl">Build your next skill.</h1><p class="mt-4 text-muted-ink">{{ loading ? 'Loading courses…' : courseCount }}</p></div><div class="flex w-full items-center gap-3 sm:w-auto"><label class="sr-only" for="course-search">Search courses</label><input id="course-search" v-model="title" class="filter-input min-w-0 flex-1 sm:w-64" placeholder="Search courses" /><button type="button" class="filter-toggle" :aria-expanded="filtersOpen" aria-controls="course-filters" @click="filtersOpen = !filtersOpen"><svg viewBox="0 0 24 24" aria-hidden="true"><path d="M4 6h16M7 12h10m-7 6" /></svg>{{ filtersOpen ? 'Hide filters' : 'Filters' }}<span v-if="selectedRange || enrolledOnly" class="filter-count">{{ (selectedRange ? 1 : 0) + (enrolledOnly ? 1 : 0) }}</span></button></div></div>

      <div class="courses-layout" :class="{ 'filters-collapsed': !filtersOpen }">
      <aside id="course-filters" class="filters-panel" :aria-hidden="!filtersOpen">
        <div class="flex items-center justify-between"><h2 class="font-serif text-3xl tracking-[-0.045em]">Filters</h2><button type="button" class="close-filters" aria-label="Close filters" @click="filtersOpen = false">×</button></div>
        <fieldset class="mt-8"><legend class="text-sm font-bold text-muted-ink">Price</legend><div class="mt-3 space-y-3"><label v-for="range in priceRanges" :key="range.label" class="flex cursor-pointer items-center gap-3 text-sm"><input type="checkbox" :checked="selectedRange?.label === range.label" class="size-4 accent-primary" @change="setPriceRange(range)" />{{ range.label }}</label></div></fieldset>
        <fieldset class="mt-8"><legend class="text-sm font-bold text-muted-ink">Access</legend><label class="mt-3 flex items-center gap-3 text-sm" :class="isSignedIn ? 'cursor-pointer' : 'cursor-not-allowed text-muted-ink/60'"><input v-model="enrolledOnly" type="checkbox" class="size-4 accent-primary" :disabled="!isSignedIn" />Enrolled courses</label><p v-if="!isSignedIn" class="mt-2 text-xs text-muted-ink">Log in to filter your enrolled courses.</p></fieldset>
        <button v-if="title || selectedRange || enrolledOnly" class="mt-8 text-sm font-bold text-primary hover:text-ink" @click="title = ''; selectedRange = null; enrolledOnly = false">Clear filters</button>
      </aside>

      <div>
        <p v-if="error" class="mt-12 rounded-xl border border-red-300 bg-red-50 p-4 text-sm text-red-700">{{ error }}</p>
        <div v-else-if="courses.length" class="mt-12 grid gap-6 sm:grid-cols-2 xl:grid-cols-3">
          <RouterLink v-for="course in courses" :key="course.id" :to="`/courses/${course.id}`" class="flex h-full flex-col overflow-hidden rounded-2xl border border-ink/15 bg-white shadow-sm transition hover:-translate-y-1 hover:border-primary">
            <div class="grid aspect-video place-items-center bg-linear-to-br from-primary/35 to-secondary/30 text-sm font-bold text-primary">Thumbnail coming soon</div>
            <div class="flex flex-1 flex-col p-5"><h3 class="min-h-16 font-serif text-2xl leading-tight tracking-[-0.04em]">{{ course.title }}</h3><p class="mt-3 min-h-[4.5rem] line-clamp-3 text-sm leading-relaxed text-muted-ink">{{ course.description }}</p><div class="mt-auto flex items-center justify-between border-t border-ink/10 pt-4 text-xs font-bold tracking-wide text-muted-ink uppercase"><span>Total hours —</span><span>Rating —</span></div></div>
          </RouterLink>
        </div>
        <p v-else-if="!loading" class="mt-12 rounded-2xl border border-dashed border-ink/20 p-8 text-muted-ink">No courses match these filters.</p>
      </div>
      </div>
    </section>
    <SiteFooter />
  </main>
</template>

<style scoped>
.filter-input { border: 1px solid color-mix(in srgb, var(--color-ink) 15%, transparent); border-radius: .75rem; background: var(--color-surface); padding: .75rem 1rem; color: var(--color-ink); font-size: .875rem; outline: none; }
.filter-input:focus { border-color: var(--color-primary); }
.courses-layout { display: grid; grid-template-columns: 15rem minmax(0, 1fr); gap: 2.5rem; align-items: start; transition: grid-template-columns .2s ease, gap .2s ease; }
.courses-layout.filters-collapsed { grid-template-columns: minmax(0, 1fr); gap: 0; }
.filters-panel { position: sticky; top: 1.5rem; border-right: 1px solid color-mix(in srgb, var(--color-ink) 14%, transparent); padding-right: 2rem; overflow: hidden; }
.filters-collapsed .filters-panel { display: none; }
.filter-toggle, .close-filters { display: inline-flex; align-items: center; gap: .45rem; border: 1px solid color-mix(in srgb, var(--color-ink) 15%, transparent); border-radius: .7rem; background: var(--color-surface); padding: .68rem .8rem; color: var(--color-ink); font-size: .84rem; font-weight: 700; cursor: pointer; }
.filter-toggle:hover, .close-filters:hover { border-color: var(--color-primary); color: var(--color-primary); }
.filter-toggle svg { width: 1.1rem; height: 1.1rem; fill: none; stroke: currentColor; stroke-width: 1.8; stroke-linecap: round; }
.filter-count { display: grid; min-width: 1.15rem; height: 1.15rem; place-items: center; border-radius: 999px; background: var(--color-primary); color: white; font-size: .68rem; }
.close-filters { border: 0; padding: .2rem .45rem; font-size: 1.5rem; line-height: 1; }
@media (max-width: 760px) { .courses-layout, .courses-layout.filters-collapsed { grid-template-columns: 1fr; gap: 1.25rem; } .filters-panel { position: static; border-right: 0; border-bottom: 1px solid color-mix(in srgb, var(--color-ink) 14%, transparent); padding: 0 0 1.5rem; } }
</style>
