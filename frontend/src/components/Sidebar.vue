<!--
  Sidebar.vue
  Sidebar condivisa tra dashboard e admin.
  Accetta `pagina` per sapere qual è la voce attiva, e `utente` per il profilo.

  Props:
    - pagina     : 'dashboard' | 'admin' | 'admin-users'  (voce attiva)
    - utente     : oggetto utente con { nome, ruolo }
    - isOpen     : boolean — apertura su mobile (classe .is-open)
  Emits:
    - @logout    : click sul bottone di uscita

  NOTA: il colore del badge ruolo deriva dalla stessa mappa usata in dashboard.js.
-->
<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const props = defineProps({
  pagina: { type: String, required: true },
  utente: { type: Object, required: true },
  isOpen: { type: Boolean, default: false },
})

defineEmits(['logout'])
const route = useRoute()

const iniziali = computed(() => {
  if (!props.utente?.nome) return '?'
  return props.utente.nome
    .split(' ')
    .map(p => p[0])
    .join('')
    .toUpperCase()
    .slice(0, 2)
})

// Stessa mappa usata in dashboard.js (coloriRuolo)
const coloriRuolo = {
  admin:    'text-brand-600',
  normal:   'text-blue-500',
  readonly: 'text-ink-400',
}
const classeRuolo = computed(() =>
  `text-[11px] font-mono ${coloriRuolo[props.utente?.ruolo] || 'text-ink-400'}`
)

const isDashboardAttiva = computed(() => (
  (props.pagina === 'dashboard' && route.hash !== '#tutte-issue') ||
  (props.pagina === 'admin' && route.hash !== '#ultime-issue')
))
const isIssueAttiva = computed(() => (
  (props.pagina === 'dashboard' && route.hash === '#tutte-issue') ||
  (props.pagina === 'admin' && route.hash === '#ultime-issue')
))
const isGestioneUtentiAttiva = computed(() => props.pagina === 'admin-users')

// Visibilità legata al ruolo (come nel vecchio inizializzaUI() del dashboard.js)
const mostraAdminLink = computed(() => props.utente?.ruolo === 'admin')
const dashboardPath = computed(() => {
  if (props.utente?.ruolo === 'admin') return '/admin'
  if (props.utente?.ruolo === 'readonly') return '/readonly'
  return '/user'
})
const issuesPath = computed(() => {
  if (props.utente?.ruolo === 'admin') return '/admin#ultime-issue'
  if (props.utente?.ruolo === 'readonly') return '/readonly#tutte-issue'
  return '/user#tutte-issue'
})
const gestioneUtentiPath = '/admin/users'
</script>

<template>
  <aside
    class="app-sidebar bg-white border-r border-ink-100 flex flex-col"
    :class="{ 'is-open': isOpen }"
  >
    <!-- Logo -->
    <div class="flex items-center gap-3 px-5 py-5 border-b border-ink-100 flex-shrink-0">
      <div class="w-8 h-8 rounded-lg bg-brand-500 flex items-center justify-center">
        <span class="font-mono text-[12px] font-medium text-ink-900">B2</span>
      </div>
      <span class="font-display text-ink-900 text-[16px] font-semibold tracking-tight">BugBoard26</span>
    </div>

    <!-- Navigazione -->
    <nav class="flex-1 px-3 py-4 space-y-0.5 overflow-y-auto">
      <p class="px-3 pt-1 pb-2 text-[10px] font-mono text-ink-300 uppercase tracking-[0.12em]">Menu</p>

      <!-- Dashboard -->
      <RouterLink
        :to="dashboardPath"
        class="flex items-center gap-3 px-3 py-2 rounded-lg text-sm font-medium transition-colors duration-100"
        :class="isDashboardAttiva
          ? 'text-ink-800 bg-ink-50'
          : 'text-ink-500 hover:text-ink-800 hover:bg-ink-50'"
      >
        <svg class="w-4 h-4 flex-shrink-0" viewBox="0 0 24 24" fill="none"
          stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/>
          <rect x="14" y="14" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/>
        </svg>
        Dashboard
      </RouterLink>

      <!-- Tutte le issue -->
      <RouterLink
        :to="issuesPath"
        class="flex items-center gap-3 px-3 py-2 rounded-lg text-sm font-medium transition-colors duration-100"
        :class="isIssueAttiva
          ? 'text-ink-800 bg-ink-50'
          : 'text-ink-500 hover:text-ink-800 hover:bg-ink-50'"
      >
        <svg class="w-4 h-4 flex-shrink-0" viewBox="0 0 24 24" fill="none"
          stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <line x1="8" y1="6" x2="21" y2="6"/><line x1="8" y1="12" x2="21" y2="12"/>
          <line x1="8" y1="18" x2="21" y2="18"/>
          <line x1="3" y1="6" x2="3.01" y2="6"/>
          <line x1="3" y1="12" x2="3.01" y2="12"/>
          <line x1="3" y1="18" x2="3.01" y2="18"/>
        </svg>
        Tutte le issue
      </RouterLink>

      <RouterLink
        v-if="mostraAdminLink"
        :to="gestioneUtentiPath"
        class="flex items-center gap-3 px-3 py-2 rounded-lg text-sm font-medium transition-colors duration-100"
        :class="isGestioneUtentiAttiva
          ? 'text-ink-800 bg-ink-50'
          : 'text-ink-500 hover:text-ink-800 hover:bg-ink-50'"
      >
        <svg class="w-4 h-4 flex-shrink-0" viewBox="0 0 24 24" fill="none"
          stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/>
          <circle cx="9" cy="7" r="4"/>
          <path d="M22 11h-6"/>
          <path d="M19 8v6"/>
        </svg>
        Gestione utenti
      </RouterLink>

    </nav>

    <!-- Profilo utente in fondo -->
    <div class="flex-shrink-0 border-t border-ink-100 px-4 py-4">
      <div class="flex items-center gap-3">
        <div class="w-8 h-8 rounded-full bg-brand-500 flex items-center justify-center flex-shrink-0">
          <span class="font-mono text-[11px] font-medium text-ink-900">{{ iniziali }}</span>
        </div>
        <div class="flex-1 min-w-0">
          <p class="text-sm font-medium text-ink-800 truncate">{{ utente?.nome || '—' }}</p>
          <p :class="classeRuolo">{{ utente?.ruolo || '—' }}</p>
        </div>
        <button
          @click="$emit('logout')"
          title="Esci"
          class="p-1.5 rounded-lg text-ink-400 hover:text-red-500 hover:bg-red-50 transition-colors"
        >
          <svg class="w-4 h-4" viewBox="0 0 24 24" fill="none"
            stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/>
            <polyline points="16 17 21 12 16 7"/>
            <line x1="21" y1="12" x2="9" y2="12"/>
          </svg>
        </button>
      </div>
    </div>
  </aside>
</template>
