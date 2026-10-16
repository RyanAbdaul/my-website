<script setup lang="ts">
import { onMounted } from 'vue'
import { RouterView } from 'vue-router'
import AmbientOrbs from './components/AmbientOrbs.vue'
import CursorGlow from './components/CursorGlow.vue'
import { verifyAdminAccess } from './lib/auth'

onMounted(() => {
  void verifyAdminAccess()
})
</script>

<template>
  <RouterView v-slot="{ Component, route }">
    <div :key="route.fullPath" class="app-shell relative isolate overflow-hidden">
      <AmbientOrbs />
      <CursorGlow />
      <component :is="Component" class="relative z-10" />
    </div>
  </RouterView>
</template>
