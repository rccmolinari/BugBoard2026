<!--
  BadgeRuolo.vue
  Traduzione diretta della funzione badgeRuolo() del vecchio admin.js.
-->
<script setup>
import { computed } from 'vue'

const props = defineProps({
  ruolo: { type: String, required: true },
})

const stili = {
  admin:    'bg-brand-50 text-brand-700 border-brand-100',
  normal:   'bg-blue-50 text-blue-600 border-blue-100',
  readonly: 'bg-ink-100 text-ink-500 border-ink-200',
}
const label = { admin: 'Admin', normal: 'Normale', readonly: 'Readonly' }

const cls = computed(() => stili[props.ruolo] || stili.readonly)
const testo = computed(() => label[props.ruolo] || props.ruolo)
</script>

<template>
  <span
    class="inline-flex px-2 py-0.5 rounded text-[11px] font-mono font-medium border"
    :class="cls"
  >
    {{ testo }}
  </span>
</template>
