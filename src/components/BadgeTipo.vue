<!--
  BadgeTipo.vue
  Traduzione diretta della funzione badgeTipo() del vecchio dashboard.js.
  Identica logica di mapping tipo → classi/label.
-->
<script setup>
import { computed } from 'vue'

const props = defineProps({
  tipo: { type: String, required: true },
})

const stili = {
  bug:           'bg-red-50 text-red-600 border-red-100',
  feature:       'bg-blue-50 text-blue-600 border-blue-100',
  question:      'bg-amber-50 text-amber-600 border-amber-100',
  documentation: 'bg-violet-50 text-violet-600 border-violet-100',
}
const label = { bug: 'Bug', feature: 'Feature', question: 'Question', documentation: 'Docs' }

const cls = computed(() => stili[props.tipo] || 'bg-ink-50 text-ink-400 border-ink-200')
const testo = computed(() => label[props.tipo] || props.tipo)
</script>

<template>
  <span
    class="inline-flex px-2 py-0.5 rounded text-[11px] font-mono font-medium border"
    :class="cls"
  >
    {{ testo }}
  </span>
</template>
