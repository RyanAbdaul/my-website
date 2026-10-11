<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import DOMPurify from 'dompurify'
import { marked } from 'marked'
import { api } from '@/lib/api'
import SiteHeader from '@/components/SiteHeader.vue'
import SiteFooter from '@/components/SiteFooter.vue'

type Article = { id: string; title: string; content: string; createTime: string; tags: string[] }
const route = useRoute()
const article = ref<Article | null>(null)
const error = ref('')
const renderedContent = computed(() => DOMPurify.sanitize(marked.parse(article.value?.content ?? '', { async: false })))
onMounted(async () => { try { article.value = await api<Article>(`/articles/${route.params.articleId}`) } catch { error.value = 'Article not found.' } })
</script>

<template>
  <main class="min-h-svh bg-canvas text-ink"><SiteHeader /><section class="mx-auto max-w-3xl px-5 py-20 sm:px-10"><RouterLink to="/articles" class="text-sm font-bold text-accent">← All articles</RouterLink><p v-if="error" class="mt-10 text-red-600">{{ error }}</p><article v-else-if="article" class="mt-10"><div class="flex flex-wrap gap-2"><span v-for="tag in article.tags" :key="tag" class="rounded-full bg-primary/15 px-2 py-1 text-xs font-bold text-accent">{{ tag }}</span></div><h1 class="mt-6 font-serif text-6xl tracking-[-.06em]">{{ article.title }}</h1><p class="mt-4 text-sm text-muted-ink">{{ new Intl.DateTimeFormat('en', { dateStyle: 'long' }).format(new Date(article.createTime)) }}</p><div class="article-body mt-10 text-lg leading-relaxed text-muted-ink" v-html="renderedContent"></div></article><p v-else class="mt-10 text-muted-ink">Loading article…</p></section><SiteFooter /></main>
</template>

<style scoped>
.article-body :deep(h1), .article-body :deep(h2), .article-body :deep(h3), .article-body :deep(h4), .article-body :deep(h5), .article-body :deep(h6) { margin-top: 2.2rem; color: var(--color-ink); line-height: 1.2; }
.article-body :deep(h1) { font-size: 2.7rem; }
.article-body :deep(h2) { font-size: 2.1rem; }
.article-body :deep(h3) { font-size: 1.65rem; }
.article-body :deep(p), .article-body :deep(ul), .article-body :deep(ol), .article-body :deep(blockquote), .article-body :deep(pre) { margin-top: 1.25rem; }
.article-body :deep(ul), .article-body :deep(ol) { padding-left: 1.5rem; }
.article-body :deep(ul) { list-style: disc; }
.article-body :deep(ol) { list-style: decimal; }
.article-body :deep(blockquote) { border-left: 4px solid var(--color-primary); padding-left: 1rem; color: var(--color-ink); font-style: italic; }
.article-body :deep(code) { border-radius: .25rem; background: color-mix(in srgb, var(--color-primary) 16%, var(--color-surface)); padding: .1rem .3rem; color: var(--color-ink); font-size: .9em; }
.article-body :deep(pre) { overflow-x: auto; border-radius: 1rem; background: color-mix(in srgb, var(--color-canvas) 78%, var(--color-primary)); padding: 1.25rem; color: var(--color-ink); }
.article-body :deep(pre code) { background: transparent; padding: 0; color: inherit; }
.article-body :deep(a) { color: var(--color-accent); font-weight: 700; text-decoration: underline; }
</style>
