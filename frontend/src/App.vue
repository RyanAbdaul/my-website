<script setup lang="ts">
import { onMounted } from 'vue'
import { RouterView } from 'vue-router'
import AmbientOrbs from './components/AmbientOrbs.vue'
import { verifyAdminAccess } from './lib/auth'

onMounted(() => {
  void verifyAdminAccess()
})
</script>

<template>
  <RouterView v-slot="{ Component, route }">
    <div :key="route.fullPath" class="relative isolate overflow-hidden">
      <AmbientOrbs />
      <component :is="Component" class="relative z-10" />
    </div>
  </RouterView>
</template>
