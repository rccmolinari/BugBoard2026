<!--
  BadgeStato.vue
  Traduzione diretta della funzione badgeStato() del vecchio dashboard.js.
-->
<script setup>
import { computed } from 'vue'

const props = defineProps({
  stato: { type: String, required: true },
})

const stili = {
  'todo':        'bg-ink-100 text-ink-500 border-ink-200',
  'in-progress': 'bg-blue-50 text-blue-600 border-blue-100',
  'done':        'bg-green-50 text-green-600 border-green-100',
  'closed':      'bg-ink-50 text-ink-400 border-ink-200',
}
const label = { 'todo': 'Todo', 'in-progress': 'In Progress', 'done': 'Done', 'closed': 'Closed' }

const cls = computed(() => stili[props.stato] || 'bg-ink-50 text-ink-400 border-ink-200')
const testo = computed(() => label[props.stato] || props.stato)
</script>

<template>
  <span
    class="inline-flex px-2 py-0.5 rounded text-[11px] font-mono font-medium border"
    :class="cls"
  >
    {{ testo }}
  </span>
</template>
