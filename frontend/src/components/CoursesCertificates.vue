<script setup lang="ts">
import { onMounted, ref } from 'vue'

type Certificate = { id: string; title: string; issuer: string | null; imagePath: string }

const certificates = ref<Certificate[]>([])

onMounted(async () => {
  const response = await fetch('/api/certificates')
  if (response.ok) {
    const items = await response.json()
    if (Array.isArray(items)) certificates.value = items
  }
})
</script>

<template>
  <section id="certificates" class="bg-canvas px-5 py-28 text-ink sm:px-10 sm:py-36 lg:px-20" aria-labelledby="certificates-heading">
    <div class="mx-auto max-w-350">
      <div class="max-w-2xl"><p class="text-xs font-bold tracking-[0.14em] text-accent uppercase">Learning</p><h2 id="certificates-heading" class="mt-5 font-serif text-5xl leading-[0.9] tracking-[-0.065em] sm:text-7xl">Certificates.</h2><p class="mt-6 max-w-xl text-base leading-relaxed text-muted-ink sm:text-lg">A record of the skills and achievements earned along the way.</p></div>
      <div class="mt-18 border-y border-ink/15 py-8 sm:py-10">
        <div class="mb-7 flex items-center justify-between"><h3 class="font-serif text-3xl tracking-[-0.05em] sm:text-4xl">Credentials</h3><span class="text-xs font-bold tracking-[0.12em] text-accent uppercase">{{ certificates.length }} earned</span></div>
        <div class="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
          <article v-for="certificate in certificates" :key="certificate.id" class="group overflow-hidden rounded-2xl border border-ink/15 bg-white shadow-sm"><img :src="certificate.imagePath" :alt="certificate.title" class="aspect-[1.55/1] w-full object-cover transition duration-500 group-hover:scale-[1.03]" /><div class="p-5"><h4 class="font-semibold">{{ certificate.title }}</h4><p v-if="certificate.issuer" class="mt-1 text-sm text-muted-ink">{{ certificate.issuer }}</p></div></article>
        </div>
        <p v-if="!certificates.length" class="py-6 text-muted-ink">Certificates are coming soon.</p>
      </div>
    </div>
  </section>
</template>
