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
type Member = { id: string; name: string }

type PriceRange = { label: string; minPrice?: number; maxPrice?: number }

const priceRanges: PriceRange[] = [
  { label: 'Free', minPrice: 0, maxPrice: 0 },
  { label: 'Under $50', minPrice: 0, maxPrice: 50 },
  { label: '$50 – $100', minPrice: 50, maxPrice: 100 },
  { label: 'Over $100', minPrice: 100 },
]

const courses = ref<Course[]>([])
const title = ref('')
const members = ref<Member[]>([])
const selectedMembers = ref<Member[]>([])
const memberDropdownOpen = ref(false)
const selectedRange = ref<PriceRange | null>(null)
const enrolledOnly = ref(false)
const loading = ref(false)
const error = ref('')
let titleTimer: ReturnType<typeof setTimeout> | undefined

const courseCount = computed(() => `${courses.value.length} course${courses.value.length === 1 ? '' : 's'}`)
const selectedMemberLabel = computed(() => selectedMembers.value.length ? selectedMembers.value.map((member) => member.name).join(', ') : 'All members')

async function loadCourses() {
  loading.value = true
  error.value = ''
  const params = new URLSearchParams()
  if (title.value.trim()) params.set('title', title.value.trim())
  if (selectedMembers.value.length) params.set('membersName', selectedMembers.value.map((member) => member.name).join(','))
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

function toggleMember(member: Member) {
  selectedMembers.value = selectedMembers.value.some((item) => item.id === member.id)
    ? selectedMembers.value.filter((item) => item.id !== member.id)
    : [...selectedMembers.value, member]
}

async function loadMembers() {
  try {
    members.value = await api<Member[]>('/members')
  } catch {
    // Courses remain usable even when the member filter cannot load.
  }
}

watch([selectedMembers, selectedRange, enrolledOnly, isSignedIn], loadCourses, { immediate: true })
watch(title, () => {
  if (titleTimer) clearTimeout(titleTimer)
  titleTimer = setTimeout(loadCourses, 300)
})
onBeforeUnmount(() => { if (titleTimer) clearTimeout(titleTimer) })
loadMembers()
</script>

<template>
  <main class="min-h-svh bg-canvas text-ink">
    <SiteHeader />

    <section class="mx-auto grid max-w-350 gap-10 px-5 py-12 sm:px-10 lg:grid-cols-[15rem_minmax(0,1fr)] lg:px-20 lg:py-16">
      <aside class="lg:border-r lg:border-ink/15 lg:pr-8">
        <h1 class="font-serif text-5xl tracking-[-0.06em]">Filters</h1>
        <div class="relative mt-8"><p class="text-sm font-bold text-muted-ink">Members</p><button class="filter-input mt-2 flex w-full items-center justify-between gap-3 text-left" type="button" :aria-expanded="memberDropdownOpen" @click="memberDropdownOpen = !memberDropdownOpen"><span class="truncate">{{ selectedMemberLabel }}</span><span class="shrink-0 text-primary">⌄</span></button><div v-if="memberDropdownOpen" class="absolute z-20 mt-2 max-h-60 w-full overflow-y-auto rounded-xl border border-ink/15 bg-white p-3 shadow-xl"><label v-for="member in members" :key="member.id" class="flex cursor-pointer items-center gap-3 rounded-lg px-2 py-2 text-sm transition hover:bg-primary/10"><input type="checkbox" :checked="selectedMembers.some((item) => item.id === member.id)" class="size-4 accent-primary" @change="toggleMember(member)" />{{ member.name }}</label></div></div>
        <fieldset class="mt-8"><legend class="text-sm font-bold text-muted-ink">Price</legend><div class="mt-3 space-y-3"><label v-for="range in priceRanges" :key="range.label" class="flex cursor-pointer items-center gap-3 text-sm"><input type="checkbox" :checked="selectedRange?.label === range.label" class="size-4 accent-primary" @change="setPriceRange(range)" />{{ range.label }}</label></div></fieldset>
        <fieldset class="mt-8"><legend class="text-sm font-bold text-muted-ink">Access</legend><label class="mt-3 flex items-center gap-3 text-sm" :class="isSignedIn ? 'cursor-pointer' : 'cursor-not-allowed text-muted-ink/60'"><input v-model="enrolledOnly" type="checkbox" class="size-4 accent-primary" :disabled="!isSignedIn" />Enrolled courses</label><p v-if="!isSignedIn" class="mt-2 text-xs text-muted-ink">Log in to filter your enrolled courses.</p></fieldset>
        <button v-if="title || selectedMembers.length || selectedRange || enrolledOnly" class="mt-8 text-sm font-bold text-primary hover:text-ink" @click="title = ''; selectedMembers = []; selectedRange = null; enrolledOnly = false">Clear filters</button>
      </aside>

      <div>
        <p class="text-xs font-bold tracking-[0.14em] text-primary uppercase">Browse courses</p>
        <div class="mt-4 flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between"><div><h2 class="font-serif text-5xl leading-[0.9] tracking-[-0.065em] sm:text-6xl">Build your next skill.</h2><p class="mt-4 text-muted-ink">{{ loading ? 'Loading courses…' : courseCount }}</p></div><label class="sr-only" for="course-search">Search courses</label><input id="course-search" v-model="title" class="filter-input w-full sm:max-w-xs" placeholder="Search courses" /></div>

        <p v-if="error" class="mt-12 rounded-xl border border-red-300 bg-red-50 p-4 text-sm text-red-700">{{ error }}</p>
        <div v-else-if="courses.length" class="mt-12 grid gap-6 sm:grid-cols-2 xl:grid-cols-3">
          <RouterLink v-for="course in courses" :key="course.id" :to="`/courses/${course.id}`" class="flex h-full flex-col overflow-hidden rounded-2xl border border-ink/15 bg-white shadow-sm transition hover:-translate-y-1 hover:border-primary">
            <div class="grid aspect-video place-items-center bg-linear-to-br from-primary/35 to-secondary/30 text-sm font-bold text-primary">Thumbnail coming soon</div>
            <div class="flex flex-1 flex-col p-5"><h3 class="min-h-16 font-serif text-2xl leading-tight tracking-[-0.04em]">{{ course.title }}</h3><p class="mt-3 min-h-[4.5rem] line-clamp-3 text-sm leading-relaxed text-muted-ink">{{ course.description }}</p><div class="mt-auto flex items-center justify-between border-t border-ink/10 pt-4 text-xs font-bold tracking-wide text-muted-ink uppercase"><span>Total hours —</span><span>Rating —</span></div></div>
          </RouterLink>
        </div>
        <p v-else-if="!loading" class="mt-12 rounded-2xl border border-dashed border-ink/20 p-8 text-muted-ink">No courses match these filters.</p>
      </div>
    </section>
    <SiteFooter />
  </main>
</template>

<style scoped>
.filter-input { border: 1px solid color-mix(in srgb, var(--color-ink) 15%, transparent); border-radius: .75rem; background: white; padding: .75rem 1rem; color: var(--color-ink); font-size: .875rem; outline: none; }
.filter-input:focus { border-color: var(--color-primary); }
</style>
