<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref } from 'vue'

const ambient = ref<HTMLElement | null>(null)
let frameId: number | undefined
let scatterFrameId: number | undefined
let pointerX = 0
let pointerY = 0
let scrollY = 0
let wakeAngle = 0
let travelSpeed = 0
let scatter = 1
let lastMoveTime = 0
let backgroundOrbs: HTMLElement[] = []

const IDLE_THRESHOLD = 400
const SCATTER_MAX = 1.7

const cursorOrbs = [
  { x: -70, y: -36, size: 4, lag: '1320ms', delay: '-1.6s' },
  { x: -49, y: -58, size: 7, lag: '1180ms', delay: '-0.8s' },
  { x: -18, y: -73, size: 3, lag: '1440ms', delay: '-2.4s' },
  { x: 19, y: -64, size: 6, lag: '1080ms', delay: '-1.1s' },
  { x: 52, y: -49, size: 4, lag: '1260ms', delay: '-3.2s' },
  { x: 73, y: -19, size: 8, lag: '920ms', delay: '-0.3s' },
  { x: 78, y: 17, size: 5, lag: '1360ms', delay: '-2.8s' },
  { x: 55, y: 47, size: 3, lag: '1160ms', delay: '-1.9s' },
  { x: 27, y: 68, size: 7, lag: '980ms', delay: '-0.5s' },
  { x: -12, y: 74, size: 5, lag: '1280ms', delay: '-3.6s' },
  { x: -48, y: 59, size: 9, lag: '840ms', delay: '-2.1s' },
  { x: -74, y: 25, size: 3, lag: '1520ms', delay: '-0.7s' },
  { x: -38, y: -19, size: 6, lag: '1040ms', delay: '-2.6s' },
  { x: -5, y: -34, size: 4, lag: '1220ms', delay: '-1.3s' },
  { x: 31, y: -22, size: 8, lag: '780ms', delay: '-3.1s' },
  { x: 42, y: 12, size: 4, lag: '1400ms', delay: '-0.2s' },
  { x: 20, y: 35, size: 6, lag: '960ms', delay: '-2.3s' },
  { x: -13, y: 34, size: 3, lag: '1500ms', delay: '-1.7s' },
  { x: -38, y: 11, size: 7, lag: '1100ms', delay: '-3.4s' },
  { x: 2, y: 3, size: 5, lag: '690ms', delay: '-1.0s' },
  { x: 94, y: 4, size: 3, lag: '1640ms', delay: '-2.7s' },
  { x: -96, y: -4, size: 5, lag: '1560ms', delay: '-0.4s' },
  { x: 4, y: -96, size: 4, lag: '1480ms', delay: '-3.0s' },
  { x: -2, y: 98, size: 6, lag: '1200ms', delay: '-1.4s' },
]

function paintScene() {
  const scene = ambient.value
  if (scene) {
    scene.style.setProperty('--pointer-x', `${pointerX}px`)
    scene.style.setProperty('--pointer-y', `${pointerY}px`)
    scene.style.setProperty('--wake-y', `${pointerY}px`)
    scene.style.setProperty('--pointer-shift-x', `${(pointerX / window.innerWidth - 0.5) * 18}px`)
    scene.style.setProperty('--scroll-shift', `${Math.min(scrollY * 0.09, 140)}px`)
    scene.style.setProperty('--wake-angle', `${wakeAngle}deg`)
    scene.style.setProperty('--wake-speed', `${Math.min(travelSpeed, 1).toFixed(2)}`)

    if (!backgroundOrbs.length) {
      backgroundOrbs = Array.from(scene.querySelectorAll<HTMLElement>('.orb'))
    }
    for (const orb of backgroundOrbs) {
      const bounds = orb.getBoundingClientRect()
      const deltaX = bounds.left + bounds.width / 2 - pointerX
      const deltaY = bounds.top + bounds.height / 2 - pointerY
      const distance = Math.hypot(deltaX, deltaY)
      const range = 150
      const strength = distance < range ? ((range - distance) / range) * 58 : 0
      const divisor = distance || 1
      orb.style.setProperty('--repel-x', `${(deltaX / divisor) * strength}px`)
      orb.style.setProperty('--repel-y', `${(deltaY / divisor) * strength}px`)
      orb.style.setProperty('--repel-scale', `${1 + strength / 180}`)
    }
  }
  frameId = undefined
}

function requestPaint() {
  if (!frameId) frameId = window.requestAnimationFrame(paintScene)
}
function followPointer(event: PointerEvent) {
  lastMoveTime = performance.now()
  const deltaX = event.clientX - pointerX
  const deltaY = event.clientY - pointerY
  const distance = Math.hypot(deltaX, deltaY)
  if (distance > 2) {
    wakeAngle = (Math.atan2(deltaY, deltaX) * 180) / Math.PI
    travelSpeed = Math.min(distance / 70, 1)
  }
  pointerX = event.clientX
  pointerY = event.clientY
  requestPaint()
}
function followScroll() {
  scrollY = window.scrollY
  backgroundOrbs = []
  requestPaint()
}

function tickScatter() {
  const idle = performance.now() - lastMoveTime
  const target = idle > IDLE_THRESHOLD ? SCATTER_MAX : 1
  const ease = target > scatter ? 0.12 : 0.05
  scatter += (target - scatter) * ease
  const scene = ambient.value
  if (scene) scene.style.setProperty('--scatter', scatter.toFixed(3))
  scatterFrameId = window.requestAnimationFrame(tickScatter)
}

onMounted(() => {
  pointerX = window.innerWidth / 2
  pointerY = window.innerHeight / 2
  scrollY = window.scrollY
  lastMoveTime = performance.now()
  paintScene()
  scatterFrameId = window.requestAnimationFrame(tickScatter)
  window.addEventListener('pointermove', followPointer, { passive: true })
  window.addEventListener('scroll', followScroll, { passive: true })
  window.addEventListener('resize', requestPaint, { passive: true })
})

onBeforeUnmount(() => {
  window.removeEventListener('pointermove', followPointer)
  window.removeEventListener('scroll', followScroll)
  window.removeEventListener('resize', requestPaint)
  if (frameId) window.cancelAnimationFrame(frameId)
  if (scatterFrameId) window.cancelAnimationFrame(scatterFrameId)
})
</script>

<template>
  <div ref="ambient" class="ambient-orbs" aria-hidden="true">
    <span class="aurora aurora-one"></span>
    <span class="aurora aurora-two"></span>
    <span class="aurora aurora-three"></span>
    <div class="cursor-orbital">
      <span
        v-for="(orb, index) in cursorOrbs"
        :key="index"
        class="cursor-orb"
        :style="{
          '--orbit-x': `${orb.x}px`,
          '--orbit-y': `${orb.y}px`,
          '--size': `${orb.size}px`,
          '--lag': orb.lag,
          '--delay': orb.delay,
        }"
      ></span>
    </div>
    <div class="particle-layer particle-layer-back">
      <span v-for="orb in 16" :key="orb" class="orb" :class="`orb-${orb}`"></span>
    </div>
    <div class="particle-layer particle-layer-front">
      <span v-for="orb in 16" :key="orb + 16" class="orb" :class="`orb-${orb + 16}`"></span>
    </div>
  </div>
</template>

<style scoped>
.ambient-orbs {
  position: fixed;
  inset: 0;
  z-index: 0;
  overflow: hidden;
  pointer-events: none;
}
.aurora {
  position: absolute;
  width: 34rem;
  height: 34rem;
  border-radius: 999px;
  filter: blur(86px);
  opacity: 0.38;
  will-change: transform;
  animation: aurora 20s ease-in-out infinite alternate;
}
.aurora-one {
  top: 6%;
  left: -13rem;
  background: var(--color-primary);
  transform: translate3d(
    calc(var(--pointer-shift-x, 0px) * -0.2),
    calc(var(--scroll-shift, 0px) * -0.18),
    0
  );
}
.aurora-two {
  top: 38%;
  right: -15rem;
  background: var(--color-secondary);
  animation-delay: -9s;
  transform: translate3d(
    calc(var(--pointer-shift-x, 0px) * 0.28),
    calc(var(--scroll-shift, 0px) * -0.38),
    0
  );
}
.aurora-three {
  top: 76%;
  left: 25%;
  width: 26rem;
  height: 26rem;
  background: color-mix(in srgb, var(--color-primary) 55%, var(--color-secondary));
  opacity: 0.26;
  animation-delay: -15s;
  transform: translate3d(
    calc(var(--pointer-shift-x, 0px) * -0.34),
    calc(var(--scroll-shift, 0px) * -0.52),
    0
  );
}
.water-light {
  position: fixed;
  top: var(--pointer-y, 50vh);
  left: var(--pointer-x, 50vw);
  width: 34rem;
  height: 16rem;
  border-radius: 999px;
  background: radial-gradient(
    ellipse,
    color-mix(in srgb, var(--color-secondary) 27%, transparent),
    color-mix(in srgb, var(--color-tertiary) 18%, transparent) 43%,
    transparent 70%
  );
  filter: blur(15px);
  transform: translate(-50%, -50%);
  transition:
    top 430ms cubic-bezier(0.16, 1, 0.3, 1),
    left 430ms cubic-bezier(0.16, 1, 0.3, 1);
}
.cursor-orbital {
  position: fixed;
  inset: 0;
  pointer-events: none;
}
.cursor-orb {
  position: fixed;
  top: var(--pointer-y, 50vh);
  left: var(--pointer-x, 50vw);
  width: var(--size);
  height: var(--size);
  border-radius: 999px;
  background: var(--color-primary);
  box-shadow:
    0 0 10px 2px color-mix(in srgb, var(--color-primary) 52%, transparent),
    0 0 24px 7px color-mix(in srgb, var(--color-secondary) 28%, transparent);
  opacity: 0.68;
  transform: translate(
    calc(-50% + var(--orbit-x) * var(--scatter, 1)),
    calc(-50% + var(--orbit-y) * var(--scatter, 1))
  );
  transition: top var(--lag) cubic-bezier(0.16, 1, 0.3, 1), left var(--lag) cubic-bezier(0.16, 1, 0.3, 1);
  animation: cursor-float 3.8s ease-in-out infinite alternate;
  animation-delay: var(--delay);
}
.liquid-lens {
  position: fixed;
  top: var(--wake-y, 50vh);
  left: var(--pointer-x, 50vw);
  width: 38rem;
  height: 27rem;
  transform: translate(-50%, -50%) rotate(calc(var(--wake-angle, 0deg) * 0.08));
  transform-origin: center;
  transition:
    top 820ms cubic-bezier(0.16, 1, 0.3, 1),
    left 820ms cubic-bezier(0.16, 1, 0.3, 1),
    transform 560ms ease-out;
}
.lens-core {
  position: absolute;
  top: 50%;
  left: 51%;
  width: 29rem;
  height: 14rem;
  border-radius: 50%;
  background: radial-gradient(ellipse, color-mix(in srgb, var(--color-secondary) 38%, transparent), color-mix(in srgb, var(--color-primary) 24%, transparent) 42%, transparent 73%);
  filter: blur(18px);
  opacity: calc(0.38 + var(--wake-speed, 0) * 0.26);
  transform: translate(-50%, -50%);
  animation: liquid-breathe 7s ease-in-out infinite;
}
.lens-sheen {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 31rem;
  height: 10rem;
  border-radius: 50%;
  background: linear-gradient(102deg, transparent 8%, color-mix(in srgb, white 32%, transparent) 39%, color-mix(in srgb, var(--color-secondary) 42%, transparent) 51%, transparent 75%);
  filter: blur(11px);
  mix-blend-mode: screen;
  opacity: calc(0.28 + var(--wake-speed, 0) * 0.22);
  transform: translate(-50%, -50%) rotate(-22deg);
  animation: liquid-sheen 9s ease-in-out infinite;
}
.lens-sheen-two {
  width: 25rem;
  height: 7rem;
  background: linear-gradient(74deg, transparent 16%, color-mix(in srgb, var(--color-primary) 44%, transparent) 45%, color-mix(in srgb, white 22%, transparent) 55%, transparent 78%);
  filter: blur(16px);
  opacity: calc(0.22 + var(--wake-speed, 0) * 0.18);
  transform: translate(-50%, -50%) rotate(31deg);
  animation-delay: -4s;
}
.lens-grain {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 34rem;
  height: 20rem;
  border-radius: 50%;
  background-image: radial-gradient(circle, color-mix(in srgb, white 42%, transparent) 0 1px, transparent 1.8px);
  background-size: 1.3rem 1.3rem;
  mask-image: radial-gradient(ellipse, transparent 0 42%, black 67%, transparent 76%);
  filter: blur(0.1px);
  opacity: calc(0.08 + var(--wake-speed, 0) * 0.2);
  transform: translate(-50%, -50%);
  animation: liquid-grain 12s linear infinite;
}
.water-caustics {
  position: absolute;
  inset: 0;
  opacity: 0.12;
  background-image:
    repeating-radial-gradient(ellipse at 18% 22%, transparent 0 12px, color-mix(in srgb, var(--color-secondary) 11%, transparent) 13px 14px, transparent 16px 32px),
    repeating-radial-gradient(ellipse at 75% 74%, transparent 0 18px, color-mix(in srgb, white 7%, transparent) 19px 20px, transparent 22px 42px);
  background-position: calc(var(--pointer-shift-x, 0px) * -0.45) calc(var(--scroll-shift, 0px) * -0.12);
  background-size: 15rem 12rem, 19rem 16rem;
  mask-image: linear-gradient(to bottom, transparent, black 14%, black 84%, transparent);
  animation: water-drift 18s linear infinite;
}
.particle-layer {
  position: absolute;
  inset: 0;
  will-change: transform;
}
.particle-layer-back {
  transform: translate3d(
    calc(var(--pointer-shift-x, 0px) * -0.2),
    calc(var(--scroll-shift, 0px) * -0.12),
    0
  );
}
.particle-layer-front {
  transform: translate3d(
    calc(var(--pointer-shift-x, 0px) * 0.55),
    calc(var(--scroll-shift, 0px) * -0.31),
    0
  );
}
.orb {
  position: absolute;
  width: var(--size);
  height: var(--size);
  border-radius: 9999px;
  background: var(--color-primary);
  box-shadow:
    0 0 10px 2px color-mix(in srgb, var(--color-primary) 52%, transparent),
    0 0 24px 7px color-mix(in srgb, var(--color-secondary) 28%, transparent);
  opacity: 0.68;
  animation: drift var(--duration) ease-in-out infinite alternate;
  animation-delay: var(--delay);
}
.orb-1 {
  top: 3%;
  left: 7%;
  --size: 4px;
  --duration: 9s;
  --delay: -2s;
}
.orb-2 {
  top: 7%;
  left: 29%;
  --size: 7px;
  --duration: 13s;
  --delay: -7s;
}
.orb-3 {
  top: 11%;
  right: 8%;
  --size: 4px;
  --duration: 10s;
  --delay: -4s;
}
.orb-4 {
  top: 16%;
  right: 32%;
  --size: 6px;
  --duration: 12s;
  --delay: -9s;
}
.orb-5 {
  top: 20%;
  left: 3%;
  --size: 3px;
  --duration: 8s;
  --delay: -1s;
}
.orb-6 {
  top: 25%;
  left: 46%;
  --size: 5px;
  --duration: 14s;
  --delay: -5s;
}
.orb-7 {
  top: 29%;
  right: 5%;
  --size: 7px;
  --duration: 11s;
  --delay: -6s;
}
.orb-8 {
  top: 34%;
  left: 16%;
  --size: 4px;
  --duration: 9s;
  --delay: -3s;
}
.orb-9 {
  top: 38%;
  right: 22%;
  --size: 5px;
  --duration: 13s;
  --delay: -8s;
}
.orb-10 {
  top: 42%;
  left: 35%;
  --size: 3px;
  --duration: 10s;
  --delay: -2s;
}
.orb-11 {
  top: 47%;
  right: 10%;
  --size: 6px;
  --duration: 12s;
  --delay: -5s;
}
.orb-12 {
  top: 51%;
  left: 6%;
  --size: 5px;
  --duration: 11s;
  --delay: -9s;
}
.orb-13 {
  top: 56%;
  left: 25%;
  --size: 7px;
  --duration: 14s;
  --delay: -4s;
}
.orb-14 {
  top: 60%;
  right: 37%;
  --size: 3px;
  --duration: 8s;
  --delay: -1s;
}
.orb-15 {
  top: 64%;
  right: 7%;
  --size: 5px;
  --duration: 10s;
  --delay: -6s;
}
.orb-16 {
  top: 68%;
  left: 13%;
  --size: 4px;
  --duration: 12s;
  --delay: -3s;
}
.orb-17 {
  top: 72%;
  left: 43%;
  --size: 6px;
  --duration: 11s;
  --delay: -7s;
}
.orb-18 {
  top: 76%;
  right: 19%;
  --size: 4px;
  --duration: 9s;
  --delay: -5s;
}
.orb-19 {
  top: 80%;
  left: 4%;
  --size: 6px;
  --duration: 13s;
  --delay: -8s;
}
.orb-20 {
  top: 84%;
  left: 31%;
  --size: 3px;
  --duration: 10s;
  --delay: -2s;
}
.orb-21 {
  top: 88%;
  right: 5%;
  --size: 7px;
  --duration: 12s;
  --delay: -6s;
}
.orb-22 {
  top: 92%;
  right: 29%;
  --size: 4px;
  --duration: 9s;
  --delay: -4s;
}
.orb-23 {
  top: 96%;
  left: 18%;
  --size: 5px;
  --duration: 11s;
  --delay: -7s;
}
.orb-24 {
  top: 98%;
  left: 48%;
  --size: 3px;
  --duration: 8s;
  --delay: -1s;
}
.orb-25 {
  top: 6%;
  left: 74%;
  --size: 5px;
  --duration: 12s;
  --delay: -5s;
}
.orb-26 {
  top: 22%;
  left: 68%;
  --size: 3px;
  --duration: 9s;
  --delay: -2s;
}
.orb-27 {
  top: 36%;
  left: 82%;
  --size: 6px;
  --duration: 15s;
  --delay: -8s;
}
.orb-28 {
  top: 48%;
  left: 19%;
  --size: 4px;
  --duration: 10s;
  --delay: -4s;
}
.orb-29 {
  top: 58%;
  left: 58%;
  --size: 7px;
  --duration: 13s;
  --delay: -6s;
}
.orb-30 {
  top: 70%;
  left: 77%;
  --size: 3px;
  --duration: 8s;
  --delay: -1s;
}
.orb-31 {
  top: 83%;
  left: 54%;
  --size: 5px;
  --duration: 14s;
  --delay: -9s;
}
.orb-32 {
  top: 94%;
  left: 72%;
  --size: 4px;
  --duration: 11s;
  --delay: -3s;
}
@keyframes drift {
  from {
    transform: translate3d(var(--repel-x, 0px), var(--repel-y, 0px), 0) scale(calc(0.8 * var(--repel-scale, 1)));
    opacity: 0.26;
  }
  to {
    transform: translate3d(calc(42px + var(--repel-x, 0px)), calc(-58px + var(--repel-y, 0px)), 0) scale(calc(1.3 * var(--repel-scale, 1)));
    opacity: 0.92;
  }
}
@keyframes cursor-float {
  from { margin: -3px 0 0 -2px; }
  to { margin: 5px 4px -5px 1px; }
}
@keyframes aurora {
  from {
    margin: 0;
    scale: 0.84;
  }
  to {
    margin: -3rem 7rem 0;
    scale: 1.17;
  }
}
@keyframes liquid-breathe {
  0%,
  100% {
    transform: translate(-50%, -50%) scale(0.88) rotate(-4deg);
  }
  50% {
    transform: translate(-50%, -50%) scale(1.12) rotate(4deg);
  }
}
@keyframes liquid-sheen {
  0%, 100% { transform: translate(-55%, -50%) rotate(-25deg) scaleX(0.9); }
  50% { transform: translate(-44%, -50%) rotate(-14deg) scaleX(1.12); }
}
@keyframes liquid-grain {
  to { background-position: 2.6rem 1.3rem; }
}
@keyframes water-drift {
  to { background-position: calc(var(--pointer-shift-x, 0px) * -0.45 + 15rem) calc(var(--scroll-shift, 0px) * -0.12 + 12rem), calc(19rem) 16rem; }
}
@media (max-width: 640px) {
  .water-caustics {
    opacity: 0.08;
  }
  .aurora {
    opacity: 0.13;
  }
}
@media (prefers-reduced-motion: reduce) {
  .aurora,
  .orb {
    animation: none;
  }
  .water-light,
  .liquid-lens {
    display: none;
  }
  .particle-layer {
    transform: none;
  }
}
</style>
