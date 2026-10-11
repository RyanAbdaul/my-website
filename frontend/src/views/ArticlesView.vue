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
const filtersOpen = ref(true)
const expandedArticleIds = ref<string[]>([])
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
    articles.value = (await api<Article[]>(`/articles${params.size ? `?${params}` : ''}`))
      .sort((first, second) => new Date(second.createTime).getTime() - new Date(first.createTime).getTime())
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

function formatRelativeDate(value: string) {
  const seconds = Math.round((new Date(value).getTime() - Date.now()) / 1000)
  const rtf = new Intl.RelativeTimeFormat('en', { numeric: 'auto' })
  const units: [Intl.RelativeTimeFormatUnit, number][] = [
    ['year', 31_536_000], ['month', 2_592_000], ['week', 604_800], ['day', 86_400], ['hour', 3_600], ['minute', 60], ['second', 1],
  ]
  const [unit, amount] = units.find(([, amount]) => Math.abs(seconds) >= amount) ?? ['second', 1]
  return rtf.format(Math.round(seconds / amount), unit)
}

function isExpanded(articleId: string) {
  return expandedArticleIds.value.includes(articleId)
}

function toggleArticle(articleId: string) {
  expandedArticleIds.value = isExpanded(articleId)
    ? expandedArticleIds.value.filter((id) => id !== articleId)
    : [...expandedArticleIds.value, articleId]
}

function needsExpansion(content: string) {
  return content.length > 420 || content.split('\n').length > 5
}

watch([selectedTags, dateRange], loadArticles, { immediate: true })
watch(query, () => { if (queryTimer) clearTimeout(queryTimer); queryTimer = setTimeout(loadArticles, 300) })
onBeforeUnmount(() => { if (queryTimer) clearTimeout(queryTimer) })
api<Tag[]>('/tags').then((items) => { tags.value = items }).catch(() => undefined)
</script>

<template>
  <main class="min-h-svh bg-canvas text-ink">
    <SiteHeader />
    <section class="mx-auto max-w-6xl px-5 py-10 sm:px-10 lg:py-14">
      <div class="mb-8 flex flex-wrap items-end justify-between gap-4">
        <div><p class="text-xs font-bold tracking-[0.14em] text-primary uppercase">Writing</p><h1 class="mt-3 font-serif text-5xl leading-[0.9] tracking-[-0.065em] sm:text-6xl">Notes from the work.</h1><p class="mt-4 text-muted-ink">{{ loading ? 'Loading articles…' : articleCount }} · newest first</p></div>
        <div class="flex w-full items-center gap-3 sm:w-auto"><label class="sr-only" for="article-search">Search articles</label><input id="article-search" v-model="query" class="article-input min-w-0 flex-1 sm:w-64" placeholder="Search articles" /><button type="button" class="filter-toggle" :aria-expanded="filtersOpen" aria-controls="article-filters" @click="filtersOpen = !filtersOpen"><svg viewBox="0 0 24 24" aria-hidden="true"><path d="M4 6h16M7 12h10m-7 6h4" /></svg>{{ filtersOpen ? 'Hide filters' : 'Filters' }}<span v-if="selectedTags.length || dateRange !== 'all'" class="filter-count">{{ selectedTags.length + (dateRange !== 'all' ? 1 : 0) }}</span></button></div>
      </div>

      <div class="articles-layout" :class="{ 'filters-collapsed': !filtersOpen }">
        <aside id="article-filters" class="filters-panel" :aria-hidden="!filtersOpen">
          <div class="flex items-center justify-between"><h2 class="font-serif text-3xl tracking-[-0.045em]">Filters</h2><button type="button" class="close-filters" aria-label="Close filters" @click="filtersOpen = false">×</button></div>
          <fieldset class="mt-7"><legend class="text-sm font-bold text-muted-ink">Topics</legend><div class="mt-3 max-h-56 space-y-2 overflow-y-auto"><label v-for="tag in tags" :key="tag.id" class="flex cursor-pointer items-center gap-3 text-sm"><input type="checkbox" :checked="selectedTags.includes(tag.name)" class="size-4 accent-primary" @change="toggleTag(tag.name)" />{{ tag.name }}</label><p v-if="!tags.length" class="text-sm text-muted-ink">No topics available.</p></div></fieldset>
          <fieldset class="mt-7"><legend class="text-sm font-bold text-muted-ink">Published</legend><div class="mt-3 space-y-3"><label v-for="option in [{ value: 'all', label: 'All time' }, { value: 'month', label: 'Last 30 days' }, { value: 'year', label: 'Last year' }]" :key="option.value" class="flex cursor-pointer items-center gap-3 text-sm"><input v-model="dateRange" type="radio" :value="option.value" class="size-4 accent-primary" />{{ option.label }}</label></div></fieldset>
          <button v-if="query || selectedTags.length || dateRange !== 'all'" class="mt-7 text-sm font-bold text-primary hover:text-ink" @click="query = ''; selectedTags = []; dateRange = 'all'">Clear filters</button>
        </aside>

        <div class="feed">
        <p v-if="error" class="mt-12 rounded-xl border border-red-300 bg-red-50 p-4 text-sm text-red-700">{{ error }}</p>
        <div v-else-if="articles.length" class="feed-list"><article v-for="article in articles" :key="article.id" class="feed-post"><div class="post-header"><div class="post-avatar" aria-hidden="true">R</div><div><p class="font-bold">Ryan Abd Elaziz</p><p class="text-xs text-muted-ink"><time :datetime="article.createTime" :title="formatDate(article.createTime)">{{ formatRelativeDate(article.createTime) }}</time> · Article</p></div><span class="post-more" aria-hidden="true">•••</span></div><div class="post-body"><div v-if="article.tags.length" class="mt-5 flex flex-wrap gap-2"><span v-for="tag in article.tags" :key="tag" class="rounded-full bg-primary/12 px-2.5 py-1 text-xs font-bold text-primary">{{ tag }}</span></div><h2 class="mt-4 font-serif text-3xl leading-tight tracking-[-0.045em] sm:text-4xl">{{ article.title }}</h2><p class="post-content" :class="{ 'post-content-collapsed': needsExpansion(article.content) && !isExpanded(article.id) }">{{ article.content }}</p><button v-if="needsExpansion(article.content)" type="button" class="show-more" :aria-expanded="isExpanded(article.id)" @click="toggleArticle(article.id)">{{ isExpanded(article.id) ? 'Show less' : 'Show more' }}</button><RouterLink :to="`/articles/${article.id}`" class="open-article">Open article <span aria-hidden="true">↗</span></RouterLink></div><div class="post-actions"><button type="button" aria-label="Like this article"><svg viewBox="0 0 24 24" aria-hidden="true"><path d="M7 10v10H4V10h3Zm2 10h7.3a2 2 0 0 0 1.95-1.55l1.2-5A2 2 0 0 0 17.5 11H14l.5-3.1A2.3 2.3 0 0 0 12.2 5L9 10v10Z" /></svg><span>Like</span></button><button type="button" aria-label="Comment on this article"><svg viewBox="0 0 24 24" aria-hidden="true"><path d="M20 11.5a7.5 7.5 0 0 1-8 7.48A8.6 8.6 0 0 1 8.2 18L4 20l1.35-3.8A7.3 7.3 0 0 1 4 11.5 8 8 0 0 1 12 4a8 8 0 0 1 8 7.5Z" /></svg><span>Comment</span></button><button type="button" aria-label="Share this article"><svg viewBox="0 0 24 24" aria-hidden="true"><path d="m12 4 5 5-5 5m4-5H5m0 5v5h14v-5" /></svg><span>Share</span></button></div></article></div>
        <p v-else-if="!loading" class="rounded-2xl border border-dashed border-ink/20 p-8 text-muted-ink">No articles match these filters.</p>
        </div>
      </div>
    </section>
    <SiteFooter />
  </main>
</template>

<style scoped>
.article-input { border: 1px solid color-mix(in srgb, var(--color-ink) 15%, transparent); border-radius: .75rem; background: var(--color-surface); padding: .75rem 1rem; color: var(--color-ink); font-size: .875rem; outline: none; }
.article-input:focus { border-color: var(--color-primary); }
.articles-layout { display: grid; grid-template-columns: 15rem minmax(0, 42rem); gap: 3rem; align-items: start; justify-content: center; transition: grid-template-columns .2s ease, gap .2s ease; }
.articles-layout.filters-collapsed { grid-template-columns: minmax(0, 42rem); gap: 0; }
.filters-panel { position: sticky; top: 1.5rem; border-right: 1px solid color-mix(in srgb, var(--color-ink) 14%, transparent); padding-right: 2rem; overflow: hidden; }
.filters-collapsed .filters-panel { display: none; }
.filter-toggle, .close-filters { display: inline-flex; align-items: center; gap: .45rem; border: 1px solid color-mix(in srgb, var(--color-ink) 15%, transparent); border-radius: .7rem; background: var(--color-surface); padding: .68rem .8rem; color: var(--color-ink); font-size: .84rem; font-weight: 700; cursor: pointer; }
.filter-toggle:hover, .close-filters:hover { border-color: var(--color-primary); color: var(--color-primary); }
.filter-toggle svg, .post-actions svg { width: 1.1rem; height: 1.1rem; fill: none; stroke: currentColor; stroke-width: 1.8; stroke-linecap: round; stroke-linejoin: round; }
.filter-count { display: grid; min-width: 1.15rem; height: 1.15rem; place-items: center; border-radius: 999px; background: var(--color-primary); color: white; font-size: .68rem; }
.close-filters { border: 0; padding: .2rem .45rem; font-size: 1.5rem; line-height: 1; }
.feed-list { display: grid; gap: 1rem; }
.feed-post { overflow: hidden; border: 1px solid color-mix(in srgb, var(--color-ink) 13%, transparent); border-radius: 1rem; background: var(--color-surface); box-shadow: 0 5px 20px rgb(0 0 0 / 20%); }
.post-header { display: flex; align-items: center; gap: .7rem; padding: 1.15rem 1.25rem 0; }
.post-avatar { display: grid; width: 2.45rem; height: 2.45rem; flex: 0 0 auto; place-items: center; border-radius: 999px; background: linear-gradient(135deg, var(--color-primary), var(--color-secondary)); color: white; font-family: var(--font-serif); font-weight: 700; }
.post-more { margin-left: auto; color: var(--color-muted-ink); letter-spacing: .12em; }
.post-body { padding: 0 1.25rem 1.15rem; }
.post-content { margin-top: .8rem; white-space: pre-line; color: var(--color-muted-ink); font-size: .95rem; line-height: 1.65; }
.post-content-collapsed { display: -webkit-box; overflow: hidden; -webkit-box-orient: vertical; -webkit-line-clamp: 5; }
.show-more, .open-article { display: inline-flex; margin-top: .9rem; border: 0; background: transparent; padding: 0; color: var(--color-primary); font-size: .87rem; font-weight: 700; text-decoration: none; cursor: pointer; }
.open-article { margin-left: 1.1rem; color: var(--color-muted-ink); }
.show-more:hover, .open-article:hover { color: var(--color-ink); text-decoration: underline; }
.post-actions { display: grid; grid-template-columns: repeat(3, 1fr); border-top: 1px solid color-mix(in srgb, var(--color-ink) 10%, transparent); padding: .25rem .75rem; }
.post-actions button { display: flex; align-items: center; justify-content: center; gap: .45rem; border: 0; border-radius: .55rem; background: transparent; padding: .7rem .3rem; color: var(--color-muted-ink); font-size: .83rem; font-weight: 700; cursor: pointer; }
.post-actions button:hover { background: color-mix(in srgb, var(--color-primary) 9%, transparent); color: var(--color-primary); }
@media (max-width: 760px) { .articles-layout, .articles-layout.filters-collapsed { grid-template-columns: 1fr; gap: 1.25rem; } .filters-panel { position: static; border-right: 0; border-bottom: 1px solid color-mix(in srgb, var(--color-ink) 14%, transparent); padding: 0 0 1.5rem; } }
</style>
