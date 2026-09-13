<script setup lang="ts">
import { computed, onBeforeUnmount, ref, watch } from 'vue'
import { api } from '@/lib/api'
import SiteFooter from '@/components/SiteFooter.vue'
import SiteHeader from '@/components/SiteHeader.vue'

type Article = { id: string; title: string; content: string; createTime: string; tags: string[] }
type Tag = { id: string; name: string }
type DateRange = 'all' | 'month' | 'year'

const articles = ref<Article[]>([])
const tags = ref<Tag[]>([])
const selectedTags = ref<string[]>([])
const query = ref('')
const dateRange = ref<DateRange>('all')
const loading = ref(false)
const error = ref('')
let queryTimer: ReturnType<typeof setTimeout> | undefined

const articleCount = computed(() => `${articles.value.length} article${articles.value.length === 1 ? '' : 's'}`)

function dateFromRange() {
  if (dateRange.value === 'all') return undefined
  const date = new Date()
  if (dateRange.value === 'month') date.setDate(date.getDate() - 30)
  else date.setFullYear(date.getFullYear() - 1)
  return date.toISOString().slice(0, 10)
}

async function loadArticles() {
  loading.value = true
  error.value = ''
  const params = new URLSearchParams()
  if (query.value.trim()) params.set('query', query.value.trim())
  selectedTags.value.forEach((tag) => params.append('tags', tag))
  const from = dateFromRange()
  if (from) params.set('from', from)
  try {
    articles.value = await api<Article[]>(`/articles${params.size ? `?${params}` : ''}`)
  } catch {
    error.value = 'Articles could not be loaded. Please try again.'
  } finally { loading.value = false }
}

function toggleTag(tag: string) {
  selectedTags.value = selectedTags.value.includes(tag)
    ? selectedTags.value.filter((item) => item !== tag)
    : [...selectedTags.value, tag]
}

function formatDate(value: string) {
  return new Intl.DateTimeFormat('en', { dateStyle: 'medium' }).format(new Date(value))
}

watch([selectedTags, dateRange], loadArticles, { immediate: true })
watch(query, () => { if (queryTimer) clearTimeout(queryTimer); queryTimer = setTimeout(loadArticles, 300) })
onBeforeUnmount(() => { if (queryTimer) clearTimeout(queryTimer) })
api<Tag[]>('/tags').then((items) => { tags.value = items }).catch(() => undefined)
</script>

<template>
  <main class="min-h-svh bg-canvas text-ink">
    <SiteHeader />
    <section class="mx-auto grid max-w-350 gap-10 px-5 py-12 sm:px-10 lg:grid-cols-[15rem_minmax(0,1fr)] lg:px-20 lg:py-16">
      <aside class="lg:border-r lg:border-ink/15 lg:pr-8">
        <h1 class="font-serif text-5xl tracking-[-0.06em]">Filters</h1>
        <fieldset class="mt-8"><legend class="text-sm font-bold text-muted-ink">Topics</legend><div class="mt-3 max-h-72 space-y-2 overflow-y-auto"><label v-for="tag in tags" :key="tag.id" class="flex cursor-pointer items-center gap-3 text-sm"><input type="checkbox" :checked="selectedTags.includes(tag.name)" class="size-4 accent-primary" @change="toggleTag(tag.name)" />{{ tag.name }}</label><p v-if="!tags.length" class="text-sm text-muted-ink">No topics available.</p></div></fieldset>
        <fieldset class="mt-8"><legend class="text-sm font-bold text-muted-ink">Published</legend><div class="mt-3 space-y-3"><label v-for="option in [{ value: 'all', label: 'All time' }, { value: 'month', label: 'Last 30 days' }, { value: 'year', label: 'Last year' }]" :key="option.value" class="flex cursor-pointer items-center gap-3 text-sm"><input v-model="dateRange" type="radio" :value="option.value" class="size-4 accent-primary" />{{ option.label }}</label></div></fieldset>
        <button v-if="query || selectedTags.length || dateRange !== 'all'" class="mt-8 text-sm font-bold text-primary hover:text-ink" @click="query = ''; selectedTags = []; dateRange = 'all'">Clear filters</button>
      </aside>

      <div>
        <p class="text-xs font-bold tracking-[0.14em] text-primary uppercase">Writing</p>
        <div class="mt-4 flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between"><div><h2 class="font-serif text-5xl leading-[0.9] tracking-[-0.065em] sm:text-6xl">Notes from the work.</h2><p class="mt-4 text-muted-ink">{{ loading ? 'Loading articles…' : articleCount }}</p></div><label class="sr-only" for="article-search">Search articles</label><input id="article-search" v-model="query" class="article-input w-full sm:max-w-xs" placeholder="Search articles" /></div>
        <p v-if="error" class="mt-12 rounded-xl border border-red-300 bg-red-50 p-4 text-sm text-red-700">{{ error }}</p>
        <div v-else-if="articles.length" class="mt-12 grid gap-6 md:grid-cols-2 xl:grid-cols-3"><RouterLink v-for="article in articles" :key="article.id" :to="`/articles/${article.id}`" class="flex h-full flex-col rounded-2xl border border-ink/15 bg-white p-6 shadow-sm transition hover:-translate-y-1 hover:border-primary"><div class="flex flex-wrap gap-2"><span v-for="tag in article.tags" :key="tag" class="rounded-full bg-primary/15 px-2.5 py-1 text-xs font-bold text-primary">{{ tag }}</span></div><h3 class="mt-5 min-h-16 font-serif text-3xl leading-tight tracking-[-0.045em]">{{ article.title }}</h3><p class="mt-4 min-h-30 line-clamp-5 whitespace-pre-line text-sm leading-relaxed text-muted-ink">{{ article.content }}</p><p class="mt-auto border-t border-ink/10 pt-4 text-xs font-bold tracking-wide text-muted-ink uppercase">Published {{ formatDate(article.createTime) }}</p></RouterLink></div>
        <p v-else-if="!loading" class="mt-12 rounded-2xl border border-dashed border-ink/20 p-8 text-muted-ink">No articles match these filters.</p>
      </div>
    </section>
    <SiteFooter />
  </main>
</template>

<style scoped>
.article-input { border: 1px solid color-mix(in srgb, var(--color-ink) 15%, transparent); border-radius: .75rem; background: white; padding: .75rem 1rem; color: var(--color-ink); font-size: .875rem; outline: none; }
.article-input:focus { border-color: var(--color-primary); }
</style>
