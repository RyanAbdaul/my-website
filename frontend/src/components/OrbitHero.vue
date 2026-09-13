<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { isAdmin, verifyAdminAccess } from '@/lib/auth'
import SiteHeader from '@/components/SiteHeader.vue'

type Project = { id: string; title: string; projectUrl: string }
const projects = ref<Project[]>([])

onMounted(() => {
  verifyAdminAccess()
  fetch('/api/projects').then((response) => response.ok ? response.json() : []).then((items) => { projects.value = items.slice(0, 5) }).catch(() => undefined)
})
</script>

<template>
  <section
    id="about"
    class="relative isolate min-h-svh overflow-hidden bg-canvas px-5 pt-5 pb-7 text-ink sm:px-10 lg:px-20"
    aria-labelledby="hero-heading"
  >
    <div class="pointer-events-none absolute top-[15%] left-[8%] -z-10 size-80 rounded-full bg-secondary/30 blur-[120px]"></div>
    <div class="pointer-events-none absolute top-[28%] right-[12%] -z-10 size-96 rounded-full bg-primary/20 blur-[130px]"></div>

    <div class="pointer-events-none absolute inset-0 z-0 overflow-hidden" aria-hidden="true">
      <span class="hero-spark absolute top-[22%] left-[9%] size-1.5 rounded-full bg-primary shadow-[0_0_18px_4px_var(--color-primary)]" style="--spark-x: 210px; --spark-y: 100px; --spark-delay: -1s"></span>
      <span class="hero-spark absolute top-[37%] left-[28%] size-1 rounded-full bg-primary shadow-[0_0_16px_3px_var(--color-primary)]" style="--spark-x: -110px; --spark-y: 190px; --spark-delay: -4s"></span>
      <span class="hero-spark absolute top-[68%] left-[12%] size-1.5 rounded-full bg-primary shadow-[0_0_18px_4px_var(--color-primary)]" style="--spark-x: 230px; --spark-y: -125px; --spark-delay: -2s"></span>
      <span class="hero-spark absolute top-[18%] left-[47%] size-1 rounded-full bg-primary shadow-[0_0_16px_3px_var(--color-primary)]" style="--spark-x: -155px; --spark-y: 155px; --spark-delay: -5s"></span>
      <span class="hero-spark absolute top-[76%] left-[43%] size-1.5 rounded-full bg-primary shadow-[0_0_18px_4px_var(--color-primary)]" style="--spark-x: 130px; --spark-y: -210px; --spark-delay: -3s"></span>
      <span class="hero-spark absolute top-[25%] right-[19%] size-1 rounded-full bg-primary shadow-[0_0_16px_3px_var(--color-primary)]" style="--spark-x: -185px; --spark-y: 95px; --spark-delay: -6s"></span>
      <span class="hero-spark absolute top-[57%] right-[8%] size-1.5 rounded-full bg-primary shadow-[0_0_18px_4px_var(--color-primary)]" style="--spark-x: -210px; --spark-y: -145px; --spark-delay: -2.5s"></span>
    </div>

    <SiteHeader class="-mx-5 -mt-5 sm:-mx-10 lg:-mx-20" />
    <RouterLink v-if="isAdmin" class="absolute top-20 right-5 z-20 text-xs font-bold text-primary sm:right-10 lg:right-20" to="/admin">Admin</RouterLink>

    <div class="mx-auto grid min-h-[calc(100svh-9rem)] w-full max-w-350 grid-cols-1 items-center gap-8 py-10 sm:gap-10 sm:py-16 lg:grid-cols-[0.9fr_1.1fr] lg:gap-28 lg:py-20">
      <div class="mx-auto max-w-156 text-center lg:mx-0 lg:text-left">
        <p class="mb-6 text-xs font-bold tracking-[0.14em] text-primary uppercase">About me · educator · builder</p>
        <h1 id="hero-heading" class="max-w-none font-serif text-5xl leading-[0.9] tracking-[-0.075em] sm:text-7xl lg:max-w-[9ch] lg:text-8xl xl:text-9xl">
          Hi, I’m Ryan.<br />I build to learn.
        </h1>
        <p class="mx-auto mt-8 max-w-104 text-base leading-relaxed text-muted-ink sm:text-lg lg:mx-0">
          I turn curious ideas into useful experiences, then share what I learn through projects, courses, and articles.
        </p>
      </div>

      <div class="relative mx-auto aspect-square w-[min(100%,34rem)] sm:w-[min(42rem,50vw)] lg:w-full" aria-label="Projects orbiting Ryan's character">
        <span class="absolute inset-[3%] rounded-full border border-primary/35" aria-hidden="true"></span>
        <span class="absolute inset-[23%] rounded-full border border-ink/15" aria-hidden="true"></span>
        <a
          v-for="project in projects"
          :key="project.id"
          class="orbit-project absolute inset-[3%] motion-reduce:animate-none"
          :style="{ '--orbit-start': `${projects.indexOf(project) * 72}deg` }"
          :href="project.projectUrl"
          target="_blank"
          rel="noopener noreferrer"
          :aria-label="`Open ${project.title} in a new tab`"
        >
          <span class="orbit-project-card absolute top-[-8%] left-1/2 grid aspect-square w-[16%] place-items-center rounded-2xl border border-primary/70 bg-white/85 p-1 text-center text-[0.42rem] leading-tight text-primary shadow-[0_0_28px_rgba(14,165,233,0.12)]">{{ project.title }}</span>
        </a>

        <div class="absolute inset-0 z-10" aria-label="Ryan Abdulaziz">
          <div class="absolute top-1/2 left-1/2 aspect-square w-[52%] -translate-x-1/2 -translate-y-1/2 overflow-hidden rounded-full border border-primary/70 bg-canvas/80 p-2 shadow-[0_0_55px_rgba(14,165,233,0.32)] sm:w-[56%]">
            <img class="size-full scale-[1.12] object-contain drop-shadow-[0_12px_18px_rgba(0,0,0,0.45)]" src="@/assets/profile.png" alt="Ryan Abdulaziz" />
          </div>
          <p class="absolute top-[78%] left-1/2 w-full -translate-x-1/2 text-center font-serif text-xl leading-tight font-bold tracking-[-0.05em] sm:top-[80%] sm:text-2xl">
            Ryan Abdulaziz<br /><span class="font-sans text-xs font-bold tracking-[0.12em] text-primary uppercase sm:text-sm">Spring Boot Engineer · Instructor · Content Creator</span>
          </p>
        </div>
      </div>
    </div>

    <footer class="mx-auto flex max-w-350 items-center justify-between text-xs font-bold tracking-[0.11em] text-muted-ink uppercase">
      <p>Scroll to explore</p>
      <span class="text-xl text-primary" aria-hidden="true">↓</span>
    </footer>
  </section>
</template>

<style scoped>
.orbit-project {
  animation: orbit 30s linear infinite;
  pointer-events: none;
}

.orbit-project-card {
  animation: counter-orbit 30s linear infinite;
  pointer-events: auto;
  cursor: pointer;
  transition: border-color .2s ease, background-color .2s ease, box-shadow .2s ease, color .2s ease, filter .2s ease;
}

.orbit-project-card:hover,
.orbit-project:focus-visible .orbit-project-card {
  border-color: color-mix(in srgb, var(--color-primary) 35%, white);
  background-color: color-mix(in srgb, var(--color-primary) 12%, white);
  box-shadow: 0 0 12px 3px rgba(255, 255, 255, .8), 0 0 42px 12px rgba(14, 165, 233, .38);
  color: var(--color-ink);
  filter: brightness(1.18);
}

.hero-spark { animation: sparkle 9s ease-in-out var(--spark-delay, 0s) infinite alternate; }

@keyframes sparkle {
  0% { transform: translate(0, 0); opacity: .25; filter: brightness(.8); }
  45% { opacity: .9; filter: brightness(1.45); }
  100% { transform: translate(var(--spark-x), var(--spark-y)); opacity: .4; filter: brightness(1); }
}

@media (prefers-reduced-motion: reduce) {
  .hero-spark { animation: none; }
}

@keyframes orbit {
  from { transform: rotate(var(--orbit-start)); }
  to { transform: rotate(calc(var(--orbit-start) + 360deg)); }
}

@keyframes counter-orbit {
  from { transform: translateX(-50%) rotate(calc(-1 * var(--orbit-start))); }
  to { transform: translateX(-50%) rotate(calc(-1 * var(--orbit-start) - 360deg)); }
}
</style>
