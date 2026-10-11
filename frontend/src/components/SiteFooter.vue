<template>
  <footer class="border-t border-ink/15 bg-canvas px-5 py-10 text-ink sm:px-10 lg:px-20">
    <div class="mx-auto flex max-w-350 flex-col gap-10">
      <div class="flex flex-col justify-between gap-8 sm:flex-row sm:items-end">
        <div>
          <a href="#about" class="inline-flex items-center gap-3" aria-label="Back to top">
            <img class="size-11 rounded-xl object-cover" src="@/assets/logo.png" alt="Ryan Abdulaziz logo" />
            <span class="font-serif text-2xl font-bold tracking-[-0.05em]">BashMobarmeg <span class="text-accent">Academy</span></span>
          </a>
          <p class="mt-4 max-w-sm text-sm leading-relaxed text-muted-ink">Building useful ideas, learning in public, and sharing the process.</p>
          <div class="mt-4 space-y-1 text-sm text-muted-ink">
            <a class="block transition hover:text-accent" href="mailto:ryanabdaul@gmail.com">ryanabdaul@gmail.com</a>
            <a class="block transition hover:text-accent" href="tel:+201040828330">+20 104 082 8330</a>
          </div>
        </div>

        <div class="flex flex-wrap gap-x-7 gap-y-3 text-sm font-semibold text-muted-ink">
          <a class="transition hover:text-accent" href="#about">About</a>
          <a class="transition hover:text-accent" href="#experience">Experience</a>
          <a class="transition hover:text-accent" href="#certificates">Certificates</a>
          <a class="transition hover:text-accent" href="mailto:ryanabdaul@gmail.com">Email ↗</a>
        </div>
      </div>

      <div class="flex flex-col justify-between gap-3 border-t border-ink/10 pt-5 text-xs font-bold tracking-[0.1em] text-muted-ink uppercase sm:flex-row">
        <p>© {{ new Date().getFullYear() }} BashMobarmeg Academy</p>
        <p>Made with curiosity <span class="text-accent">✦</span></p>
      </div>
    </div>
  </footer>
</template>
