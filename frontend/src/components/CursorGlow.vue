<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref } from 'vue'

const dot = ref<HTMLElement | null>(null)
const ring = ref<HTMLElement | null>(null)

let targetX = 0
let targetY = 0
let ringX = 0
let ringY = 0
let frameId: number | undefined
let hovering = false
let active = false

function onMove(event: MouseEvent) {
  targetX = event.clientX
  targetY = event.clientY
  if (!active) {
    active = true
    ringX = targetX
    ringY = targetY
    document.documentElement.classList.add('custom-cursor')
  }
}

function onLeave() {
  active = false
  document.documentElement.classList.remove('custom-cursor')
}

function onOver(event: MouseEvent) {
  const element = event.target as HTMLElement | null
  hovering = !!element?.closest('a, button, [role="button"], summary, label, select, input, textarea')
}

function tick() {
  if (dot.value) dot.value.style.transform = `translate3d(${targetX}px, ${targetY}px, 0) translate(-50%, -50%)`
  ringX += (targetX - ringX) * 0.18
  ringY += (targetY - ringY) * 0.18
  if (ring.value) ring.value.style.transform = `translate3d(${ringX}px, ${ringY}px, 0) translate(-50%, -50%)`
  frameId = requestAnimationFrame(tick)
}

onMounted(() => {
  window.addEventListener('mousemove', onMove, { passive: true })
  window.addEventListener('mouseover', onOver, { passive: true })
  document.documentElement.addEventListener('mouseleave', onLeave)
  frameId = requestAnimationFrame(tick)
})

onBeforeUnmount(() => {
  window.removeEventListener('mousemove', onMove)
  window.removeEventListener('mouseover', onOver)
  document.documentElement.removeEventListener('mouseleave', onLeave)
  if (frameId) window.cancelAnimationFrame(frameId)
  document.documentElement.classList.remove('custom-cursor')
})
</script>

<template>
  <div class="cursor-glow" :class="{ 'is-active': active, 'is-hovering': hovering && active }" aria-hidden="true">
    <span ref="dot" class="cursor-dot"></span>
    <span ref="ring" class="cursor-ring"></span>
  </div>
</template>

<style scoped>
.cursor-glow { position: fixed; inset: 0; z-index: 9999; pointer-events: none; }
.cursor-dot, .cursor-ring { position: fixed; top: 0; left: 0; border-radius: 999px; will-change: transform; opacity: 0; }
.cursor-glow.is-active .cursor-dot, .cursor-glow.is-active .cursor-ring { opacity: 1; }

.cursor-dot { width: 8px; height: 8px; background: var(--color-accent); box-shadow: 0 0 10px var(--color-accent), 0 0 22px var(--color-primary); transition: opacity .18s ease; }

.cursor-ring { width: 36px; height: 36px; border: 1.5px solid color-mix(in srgb, var(--color-accent) 65%, transparent); transition: width .18s ease, height .18s ease, border-color .18s ease, background-color .18s ease, opacity .18s ease; }
.cursor-glow.is-hovering .cursor-ring { width: 58px; height: 58px; border-color: var(--color-primary); background: color-mix(in srgb, var(--color-primary) 14%, transparent); }

@media (prefers-reduced-motion: reduce) {
  .cursor-ring { transition: none; }
}
</style>
