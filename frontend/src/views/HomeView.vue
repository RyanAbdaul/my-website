<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import SiteFooter from '@/components/SiteFooter.vue'
import SiteHeader from '@/components/SiteHeader.vue'
import { api } from '@/lib/api'
import codingBoy from '@/assets/hero/coding-boy.png'
import codingSword from '@/assets/hero/coding-sword.png'

type Course = { id: string; title: string; description: string; price: number }
const fallback: Course[] = [
  {
    id: 'spring',
    title: 'Spring Boot & Hibernate for Beginners',
    description: 'Build Java APIs with Spring Boot, Hibernate, JPA, and PostgreSQL.',
    price: 0,
  },
  {
    id: 'fastapi',
    title: 'FastAPI for Beginners',
    description: 'Create fast, documented Python APIs with modern backend patterns.',
    price: 0,
  },
  {
    id: 'python',
    title: 'Python for Beginners',
    description: 'Learn Python fundamentals through exercises and practical projects.',
    price: 0,
  },
  {
    id: 'java',
    title: 'Java for Beginners',
    description: 'Master core Java, object-oriented programming, and problem-solving.',
    price: 0,
  },
]
const courses = ref<Course[]>([])
const topCourses = computed(() => (courses.value.length ? courses.value.slice(0, 5) : fallback))
onMounted(async () => {
  try {
    courses.value = await api<Course[]>('/courses/featured')
  } catch {
    courses.value = []
  }
})
</script>

<template>
  <main class="min-h-svh bg-canvas text-ink">
    <SiteHeader />
    <section class="hero">
      <div class="hero-copy">
        <p class="kicker">✦ BASHMOBARMEG ACADEMY</p>
        <h1>Code. <span>Learn.</span><br />Build the future.</h1>
        <p>Modern, project-first programming courses for the skills that matter.</p>
        <div class="hero-actions">
          <RouterLink to="/courses" class="primary-action">Start learning →</RouterLink
          ><RouterLink to="/courses" class="secondary-action">Explore courses</RouterLink>
        </div>
      </div>
      <div class="hero-art" aria-hidden="true">
        <div class="glow"></div>
        <img class="hero-boy" :src="codingBoy" alt="" />
        <img class="hero-sword" :src="codingSword" alt="" />
      </div>
    </section>
    <section class="featured">
      <div class="section-title">
        <div>
          <p class="kicker">START HERE</p>
          <h2>Top courses.</h2>
        </div>
        <RouterLink to="/courses">View all courses →</RouterLink>
      </div>
      <div class="course-grid">
        <RouterLink
          v-for="(course, index) in topCourses"
          :key="course.id"
          :to="course.id.length > 20 ? `/courses/${course.id}` : '/courses'"
          class="course-card"
          ><div class="course-icon">{{ ['✦', '↗', '⌁', '◇', '◌'][index] }}</div>
          <p>BEGINNER FRIENDLY</p>
          <h3>{{ course.title }}</h3>
          <span>{{ course.description }}</span>
          <footer>
            <b>{{ course.price === 0 ? 'Free' : `$${course.price}` }}</b
            ><i>→</i>
          </footer></RouterLink
        >
      </div>
    </section>
    <section class="benefits">
      <div><b>⌘ Build as you learn</b><span>Small steps, real projects.</span></div>
      <div><b>⌁ Beginner-first lessons</b><span>Clarity over complexity.</span></div>
      <div><b>✦ Skills with momentum</b><span>Keep growing after the course.</span></div>
    </section>
    <SiteFooter />
  </main>
</template>

<style scoped>
.hero {
  position: relative;
  display: grid;
  min-height: calc(100svh - 8.75rem);
  grid-template-columns: 1fr 1fr;
  gap: 3rem;
  overflow: hidden;
  border-bottom: 1px solid color-mix(in srgb, var(--color-secondary) 52%, transparent);
  padding: clamp(4rem, 8vw, 8rem) max(1.5rem, calc((100vw - 76rem) / 2)) 14rem;
  background:
    radial-gradient(
      circle at 77% 42%,
      color-mix(in srgb, var(--color-secondary) 20%, transparent),
      transparent 23rem
    ),
    radial-gradient(
      circle at 20% 3%,
      color-mix(in srgb, var(--color-primary) 23%, transparent),
      transparent 30rem
    );
}
.hero-copy,
.hero-art {
  position: relative;
  z-index: 1;
}
.hero-copy {
  align-self: center;
  max-width: 40rem;
}
.kicker {
  margin: 0;
  color: var(--color-secondary);
  font-size: 0.72rem;
  font-weight: 800;
  letter-spacing: 0.15em;
}
h1 {
  margin: 1rem 0;
  font-size: clamp(3.6rem, 7vw, 6.5rem);
  line-height: 0.9;
  letter-spacing: -0.08em;
}
h1 span {
  color: var(--color-secondary);
  text-shadow: 0 0 30px color-mix(in srgb, var(--color-secondary) 33%, transparent);
}
.hero-copy > p:not(.kicker) {
  max-width: 29rem;
  color: var(--color-muted-ink);
  font-size: 1.05rem;
  line-height: 1.7;
}
.hero-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 0.8rem;
  margin-top: 2rem;
}
.primary-action,
.secondary-action {
  border-radius: 0.8rem;
  padding: 0.85rem 1.1rem;
  font-size: 0.9rem;
  font-weight: 800;
  transition: 0.2s;
}
.primary-action {
  background: linear-gradient(135deg, var(--color-secondary), var(--color-primary));
  box-shadow: 0 12px 30px color-mix(in srgb, var(--color-secondary) 24%, transparent);
  color: white;
}
.secondary-action {
  border: 1px solid color-mix(in srgb, var(--color-secondary) 60%, transparent);
  color: var(--color-secondary);
}
.primary-action:hover,
.secondary-action:hover {
  transform: translateY(-3px);
  color: white;
  box-shadow: 0 18px 34px color-mix(in srgb, var(--color-secondary) 25%, transparent);
}
.hero-art {
  min-height: 34rem;
  isolation: isolate;
}
.glow {
  position: absolute;
  inset: 7%;
  border-radius: 50%;
  background: radial-gradient(
    circle,
    color-mix(in srgb, var(--color-secondary) 50%, transparent),
    color-mix(in srgb, var(--color-primary) 32%, transparent) 42%,
    transparent 70%
  );
  filter: blur(20px);
}
.hero-boy {
  position: absolute;
  z-index: 1;
  top: 0;
  left: 50%;
  width: min(48rem, 140%);
  max-width: none;
  object-fit: contain;
  transform: translateX(-50%);
  filter: drop-shadow(0 1.25rem 1.5rem rgb(2 8 23 / 45%));
}
.hero-sword {
  position: absolute;
  z-index: 2;
  top: 98%;
  left: 50%;
  width: min(42rem, 125%);
  transform: translate(-50%, -50%);
  filter: drop-shadow(0 0.35rem 1rem rgb(6 182 212 / 45%));
  animation: sword-float 3.4s ease-in-out infinite;
}
@keyframes sword-float {
  0%,
  100% {
    translate: 0 -12px;
  }
  50% {
    translate: 0 12px;
  }
}
@media (prefers-reduced-motion: reduce) {
  .hero-sword {
    animation: none;
  }
}
.featured {
  padding: 1.5rem max(1.5rem, calc((100vw - 76rem) / 2)) 5rem;
}
.section-title {
  display: flex;
  align-items: end;
  justify-content: space-between;
  gap: 1rem;
  margin-bottom: 1.8rem;
}
.section-title h2 {
  margin: 0.7rem 0 0;
  font-size: clamp(2.3rem, 4vw, 3.7rem);
  letter-spacing: -0.07em;
}
.section-title > a {
  color: var(--color-secondary);
  font-size: 0.85rem;
  font-weight: 800;
}
.section-title > a:hover {
  color: white;
}
.course-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 1rem;
}
.course-card {
  display: flex;
  min-height: 17rem;
  flex-direction: column;
  border: 1px solid color-mix(in srgb, var(--color-secondary) 26%, transparent);
  border-radius: 1.2rem;
  background: linear-gradient(
    145deg,
    color-mix(in srgb, var(--color-surface) 90%, transparent),
    color-mix(in srgb, var(--color-primary) 10%, transparent)
  );
  padding: 1.25rem;
  transition: 0.23s;
}
.course-card:hover {
  transform: translateY(-7px);
  border-color: var(--color-secondary);
  box-shadow:
    0 20px 34px #02081780,
    0 0 28px color-mix(in srgb, var(--color-secondary) 18%, transparent);
}
.course-icon {
  display: grid;
  width: 3rem;
  height: 3rem;
  place-items: center;
  border-radius: 0.85rem;
  background: color-mix(in srgb, var(--color-secondary) 18%, transparent);
  color: var(--color-secondary);
  font-size: 1.3rem;
}
.course-card p {
  margin: 1.3rem 0 0.55rem;
  color: var(--color-secondary);
  font-size: 0.62rem;
  font-weight: 800;
  letter-spacing: 0.12em;
}
.course-card h3 {
  margin: 0;
  font-size: 1.28rem;
  line-height: 1.15;
  letter-spacing: -0.045em;
}
.course-card > span {
  margin-top: 0.8rem;
  color: var(--color-muted-ink);
  font-size: 0.82rem;
  line-height: 1.55;
}
.course-card footer {
  display: flex;
  justify-content: space-between;
  margin-top: auto;
  padding-top: 1rem;
  color: var(--color-secondary);
  font-size: 0.8rem;
}
.course-card i {
  font-size: 1.25rem;
}
.benefits {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  border-block: 1px solid color-mix(in srgb, var(--color-secondary) 23%, transparent);
}
.benefits div {
  display: grid;
  gap: 0.35rem;
  padding: 1.5rem max(1.5rem, calc((100vw - 76rem) / 2));
  background: color-mix(in srgb, var(--color-canvas) 87%, transparent);
}
.benefits div + div {
  border-left: 1px solid color-mix(in srgb, var(--color-secondary) 18%, transparent);
  padding-inline: 1.5rem;
}
.benefits b {
  color: var(--color-secondary);
  font-size: 0.88rem;
}
.benefits span {
  color: var(--color-muted-ink);
  font-size: 0.78rem;
}
@media (max-width: 850px) {
  .hero {
    min-height: auto;
    grid-template-columns: 1fr;
  }
  .hero-art {
    min-height: 25rem;
    order: -1;
  }
  .hero-boy {
    top: 0;
    width: min(44rem, 145%);
  }
  .hero-sword {
    top: 86%;
    width: min(34rem, 125%);
  }
  .course-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .benefits {
    grid-template-columns: 1fr;
  }
  .benefits div + div {
    border-top: 1px solid color-mix(in srgb, var(--color-secondary) 18%, transparent);
    border-left: 0;
  }
}
@media (max-width: 540px) {
  .course-grid {
    grid-template-columns: 1fr;
  }
  .section-title {
    align-items: start;
    flex-direction: column;
  }
  .hero-art {
    min-height: 22rem;
  }
  .hero-boy {
    width: min(38rem, 145%);
  }
  .hero-sword {
    top: 86%;
    width: min(30rem, 130%);
  }
}
</style>
