<script setup>
import { computed } from 'vue'

const props = defineProps({
  priorita: { type: Number, default: null },
})

const stili = {
  0: 'bg-blue-50 text-blue-600 border-blue-100',
  1: 'bg-green-50 text-green-600 border-green-100',
  2: 'bg-amber-50 text-amber-600 border-amber-100',
  3: 'bg-orange-50 text-orange-500 border-orange-100',
  4: 'bg-red-50 text-red-600 border-red-100'
}

const etichette = {
  0: 'MINIMAL',
  1: 'LOW',
  2: 'MEDIUM',
  3: 'HIGH',
  4: 'CRITICAL',
}

const cls = computed(() =>
  stili[props.priorita] || 'bg-ink-50 text-ink-400 border-ink-200'
)

// CORREZIONE 1: Usiamo il controllo di nullità esplicito per ammettere lo 0
const testo = computed(() =>
  props.priorita !== null && props.priorita !== undefined ? etichette[props.priorita] : ''
)
</script>

<template>
  <span v-if="props.priorita === null || props.priorita === undefined" class="text-ink-200 text-sm">—</span>

  <span
    v-else
    class="inline-flex px-2 py-0.5 rounded text-[11px] font-mono font-medium border"
    :class="cls"
  >
    {{ testo }}
  </span>
</template>