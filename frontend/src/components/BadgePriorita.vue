<!--
  BadgePriorita.vue
  Traduzione diretta della funzione badgePriorita() del vecchio dashboard.js.
  Se la priorità è null/undefined mostra il trattino "—" (come l'originale).
-->
<script setup>
import { computed } from 'vue'

const props = defineProps({
  priorita: { type: Number, default: null },
})

const stili = {
  1: 'bg-red-50 text-red-600 border-red-100',
  2: 'bg-orange-50 text-orange-600 border-orange-100',
  3: 'bg-amber-50 text-amber-500 border-amber-100',
  4: 'bg-green-50 text-green-600 border-green-100',
}

const cls = computed(() => stili[props.priorita] || 'bg-ink-50 text-ink-400 border-ink-200')
const testo = computed(() =>
  props.priorita ? props.priorita.charAt(0).toUpperCase() + props.priorita.toString() : ''
)
</script>

<template>
  <span v-if="!priorita" class="text-ink-200 text-sm">—</span>
  <span
    v-else
    class="inline-flex px-2 py-0.5 rounded text-[11px] font-mono font-medium border"
    :class="cls"
  >
    {{ testo }}
  </span>
</template>
