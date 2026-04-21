<!--
  StatCard.vue
  Card delle statistiche. Struttura HTML identica a quella ripetuta 4 volte
  in dashboard.html e in admin.html — qui semplicemente parametrizzata.

  Props:
    - label         (testo piccolo in alto, es. "Totale")
    - value         (numero grande, es. "12")
    - subtitle      (testo piccolo in basso, es. "issue nel sistema")
    - iconBgClass   (classe di sfondo del quadretto dell'icona, es. "bg-ink-50")
    - valueColorClass (colore del numero, es. "text-ink-900")
    - animDelayClass (classe di ritardo animazione, es. "delay-1")
  Slot:
    - icon → l'SVG dell'icona
-->
<script setup>
defineProps({
  label: { type: String, required: true },
  value: { type: [String, Number], default: '—' },
  subtitle: { type: String, default: '' },
  iconBgClass: { type: String, default: 'bg-ink-50' },
  valueColorClass: { type: String, default: 'text-ink-900' },
  animDelayClass: { type: String, default: 'delay-1' },
})
</script>

<template>
  <div
    class="anim-fade-in bg-white rounded-xl border border-ink-100 p-5 shadow-sm"
    :class="animDelayClass"
  >
    <div class="flex items-center justify-between mb-3">
      <p class="text-xs font-mono text-ink-400 uppercase tracking-wider">{{ label }}</p>
      <div class="w-7 h-7 rounded-lg flex items-center justify-center" :class="iconBgClass">
        <slot name="icon" />
      </div>
    </div>
    <p class="font-display text-3xl font-bold" :class="valueColorClass">{{ value }}</p>
    <p class="text-xs text-ink-400 mt-1">{{ subtitle }}</p>
  </div>
</template>
