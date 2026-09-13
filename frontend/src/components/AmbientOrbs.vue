<template>
  <div class="ambient-orbs" aria-hidden="true">
    <span v-for="orb in 24" :key="orb" class="orb" :class="`orb-${orb}`"></span>
  </div>
</template>

<style scoped>
.ambient-orbs { position: absolute; inset: 0; z-index: 0; pointer-events: none; overflow: hidden; }
.orb { position: absolute; width: var(--size); height: var(--size); border-radius: 9999px; background: var(--color-primary); box-shadow: 0 0 10px 2px color-mix(in srgb, var(--color-primary) 52%, transparent), 0 0 24px 7px color-mix(in srgb, var(--color-secondary) 28%, transparent); animation: drift var(--duration) ease-in-out infinite alternate; animation-delay: var(--delay); opacity: .42; }
.orb-1{top:3%;left:7%;--size:4px;--duration:9s;--delay:-2s}.orb-2{top:7%;left:29%;--size:7px;--duration:13s;--delay:-7s}.orb-3{top:11%;right:8%;--size:4px;--duration:10s;--delay:-4s}.orb-4{top:16%;right:32%;--size:6px;--duration:12s;--delay:-9s}.orb-5{top:20%;left:3%;--size:3px;--duration:8s;--delay:-1s}.orb-6{top:25%;left:46%;--size:5px;--duration:14s;--delay:-5s}.orb-7{top:29%;right:5%;--size:7px;--duration:11s;--delay:-6s}.orb-8{top:34%;left:16%;--size:4px;--duration:9s;--delay:-3s}.orb-9{top:38%;right:22%;--size:5px;--duration:13s;--delay:-8s}.orb-10{top:42%;left:35%;--size:3px;--duration:10s;--delay:-2s}.orb-11{top:47%;right:10%;--size:6px;--duration:12s;--delay:-5s}.orb-12{top:51%;left:6%;--size:5px;--duration:11s;--delay:-9s}.orb-13{top:56%;left:25%;--size:7px;--duration:14s;--delay:-4s}.orb-14{top:60%;right:37%;--size:3px;--duration:8s;--delay:-1s}.orb-15{top:64%;right:7%;--size:5px;--duration:10s;--delay:-6s}.orb-16{top:68%;left:13%;--size:4px;--duration:12s;--delay:-3s}.orb-17{top:72%;left:43%;--size:6px;--duration:11s;--delay:-7s}.orb-18{top:76%;right:19%;--size:4px;--duration:9s;--delay:-5s}.orb-19{top:80%;left:4%;--size:6px;--duration:13s;--delay:-8s}.orb-20{top:84%;left:31%;--size:3px;--duration:10s;--delay:-2s}.orb-21{top:88%;right:5%;--size:7px;--duration:12s;--delay:-6s}.orb-22{top:92%;right:29%;--size:4px;--duration:9s;--delay:-4s}.orb-23{top:96%;left:18%;--size:5px;--duration:11s;--delay:-7s}.orb-24{top:98%;left:48%;--size:3px;--duration:8s;--delay:-1s}
@keyframes drift { from { transform: translate3d(0, 0, 0) scale(.8); opacity: .26; } to { transform: translate3d(42px, -58px, 0) scale(1.3); opacity: .92; } }
@media (prefers-reduced-motion: reduce) { .orb { animation: none; } }
</style>
