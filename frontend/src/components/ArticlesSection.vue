<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { api } from '@/lib/api'

type Article = { id: string; title: string; content: string; tags: string[]; createTime: string }

const articles = ref<Article[]>([])

onMounted(async () => {
  try {
    articles.value = await api<Article[]>('/articles')
  } catch {
    // Articles are optional public content, so a failed request should not block the page.
  }
})
</script>

<template>
  <section id="articles" class="border-y border-ink/15 bg-canvas px-5 py-28 text-ink sm:px-10 sm:py-36 lg:px-20" aria-labelledby="articles-heading">
    <div class="mx-auto max-w-300">
      <p class="text-xs font-bold tracking-[0.14em] text-primary uppercase">Writing</p>
      <h2 id="articles-heading" class="mt-5 font-serif text-5xl leading-[0.9] tracking-[-0.065em] sm:text-7xl">Notes from the work.</h2>
      <div v-if="articles.length" class="mt-14 grid gap-5 md:grid-cols-2 lg:grid-cols-3">
        <article v-for="article in articles" :key="article.id" class="rounded-2xl border border-ink/15 bg-canvas p-6">
          <div v-if="article.tags.length" class="flex flex-wrap gap-2"><span v-for="tag in article.tags" :key="tag" class="rounded-full bg-primary/15 px-2.5 py-1 text-xs font-semibold text-primary">{{ tag }}</span></div>
          <h3 class="mt-5 font-serif text-3xl leading-tight tracking-[-0.045em]">{{ article.title }}</h3>
          <p class="mt-4 line-clamp-5 whitespace-pre-line text-sm leading-relaxed text-muted-ink">{{ article.content }}</p>
        </article>
      </div>
      <p v-else class="mt-12 text-muted-ink">No articles published yet.</p>
    </div>
  </section>
</template>
