<script setup lang="ts">
import { onMounted, ref } from 'vue'

type Project = { id: string; title: string; description: string | null; imagePath: string | null; projectUrl: string }
const projects = ref<Project[]>([])
onMounted(async () => { const response = await fetch('/api/projects'); if (response.ok) projects.value = await response.json() })
</script>

<template>
  <section id="projects" class="bg-canvas px-5 py-16 text-ink sm:px-10 sm:py-24 lg:px-20" aria-labelledby="projects-heading">
    <div class="mx-auto max-w-300"><p class="text-xs font-bold tracking-[0.14em] text-accent uppercase">Projects</p><h2 id="projects-heading" class="mt-5 font-serif text-5xl leading-[0.9] tracking-[-0.065em] sm:text-7xl">Things I’ve built.</h2><p class="mt-6 max-w-xl text-base leading-relaxed text-muted-ink sm:text-lg">A collection of projects, experiments, and useful ideas.</p>
      <div v-if="projects.length" class="mt-14 grid gap-6 sm:grid-cols-2 lg:grid-cols-3"><a v-for="project in projects" :key="project.id" :href="project.projectUrl" target="_blank" rel="noopener noreferrer" class="group overflow-hidden rounded-2xl border border-ink/15 bg-white shadow-sm transition hover:-translate-y-1 hover:border-primary/60 hover:shadow-lg"><img v-if="project.imagePath" :src="project.imagePath" :alt="project.title" class="aspect-video w-full object-cover" /><div v-else class="aspect-video bg-linear-to-br from-primary/20 to-secondary/35"></div><div class="p-6"><div class="flex items-center justify-between gap-4"><h3 class="font-serif text-3xl tracking-[-0.05em]">{{ project.title }}</h3><span class="text-accent">↗</span></div><p v-if="project.description" class="mt-3 text-sm leading-relaxed text-muted-ink">{{ project.description }}</p></div></a></div>
      <p v-else class="mt-14 text-muted-ink">Projects are coming soon.</p>
    </div>
  </section>
</template>
