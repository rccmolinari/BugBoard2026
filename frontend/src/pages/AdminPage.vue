<!--
  AdminPage.vue
  Dashboard principale dell'admin orientata alle issue.
-->
<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import Sidebar from '../components/Sidebar.vue'
import StatCard from '../components/StatCard.vue'
import BadgeTipo from '../components/BadgeTipo.vue'
import BadgeStato from '../components/BadgeStato.vue'
import BadgePriorita from '../components/BadgePriorita.vue'
import axios from 'axios'

function getUtente() {
  const raw = sessionStorage.getItem('bb_utente')
  if (!raw) return null

  try {
    return JSON.parse(raw)
  } catch (error) {
    console.error('Sessione utente non valida in sessionStorage:', error)
    sessionStorage.removeItem('bb_utente')
    return null
  }
}

const utente = getUtente() ?? { nome: '', sessionId: null }
const router = useRouter()

const issues = ref([])

if (utente.sessionId) {
  axios.get('/api/issues/' + utente.sessionId)
    .then(response => {
      issues.value = response.data
    })
    .catch(error => {
      console.error('Errore durante il caricamento delle issue:', error)
    })
} else {
  router.replace('/')
}



const sidebarAperta = ref(false)
const popupAssegnaAperto = ref(false)
const issueSelezionata = ref(null)
const dataAssegnazione = ref('')
const route = useRoute()

const iniziali = computed(() => {
  if (!utente?.nome) return '?'
  return utente.nome
    .split(' ')
    .map(p => p[0])
    .join('')
    .toUpperCase()
    .slice(0, 2)
})

const statTotale = computed(() => issues.value.length)
const statTodo = computed(() => issues.value.filter(i => i.stato === 'todo').length)
const statInCorso = computed(() => issues.value.filter(i => i.stato === 'in-progress').length)
const statRisolte = computed(() => issues.value.filter(i => i.stato === 'done' || i.stato === 'closed').length)

const tutteIssue = computed(() => (
  [...issues.value]
    .sort((a, b) => new Date(b.dataCreazione) - new Date(a.dataCreazione))
))

const ultimeIssue = computed(() => (
  tutteIssue.value
    .slice(0, 6)
))

const soloIssue = computed(() => route.hash === '#ultime-issue')
const issueDaMostrare = computed(() => (soloIssue.value ? tutteIssue.value : ultimeIssue.value))

function formattaData(iso) {
  if (!iso) return '—'
  return new Date(iso).toLocaleDateString('it-IT', {
    day: '2-digit', month: 'short', year: 'numeric',
  })
}

function apriPopupAssegna(issue) {
  issueSelezionata.value = issue
  dataAssegnazione.value = ''
  popupAssegnaAperto.value = true
}

function chiudiPopupAssegna() {
  popupAssegnaAperto.value = false
  issueSelezionata.value = null
}

function submitAssegnazione() {
  console.log('Assegna issue:', issueSelezionata.value?.id, 'data:', dataAssegnazione.value)
  chiudiPopupAssegna()
}

function aprireSidebar() { sidebarAperta.value = true }
function chiudiSidebar() { sidebarAperta.value = false }

function logout() {
  const sessionId = utente?.sessionId

  if (!sessionId) {
    sessionStorage.removeItem('bb_utente')
    router.replace('/')
    return
  }

  axios.post('/api/auth/logout', { sessionId })
    .catch(error => {
      console.error('Errore durante il logout:', error)
    })
    .finally(() => {
      sessionStorage.removeItem('bb_utente')
      router.replace('/')
    })
}
</script>

<template>
  <div>
    <Sidebar pagina="admin" :utente="utente" :is-open="sidebarAperta" @logout="logout" />

    <div
      v-show="sidebarAperta"
      @click="chiudiSidebar"
      class="fixed inset-0 bg-ink-900/40 z-20"
    ></div>

    <div class="app-main-content min-h-screen flex flex-col">
      <header class="sticky top-0 z-10 bg-white border-b border-ink-100 flex items-center justify-between h-14 px-6 flex-shrink-0">
        <button
          @click="aprireSidebar"
          class="mr-3 p-1.5 rounded-lg text-ink-500 hover:bg-ink-50 md:hidden"
        >
          <svg class="w-5 h-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <line x1="3" y1="6" x2="21" y2="6" />
            <line x1="3" y1="12" x2="21" y2="12" />
            <line x1="3" y1="18" x2="21" y2="18" />
          </svg>
        </button>

        <div class="flex items-center gap-2">
          <h1 class="font-display text-ink-900 text-[15px] font-semibold">Dashboard Admin</h1>
          <span class="text-ink-300 text-sm hidden sm:inline">/</span>
          <span class="text-ink-400 text-sm hidden sm:inline">{{ soloIssue ? 'Tutte le issue' : 'Issue' }}</span>
        </div>

        <div class="flex items-center gap-2">
          <div class="w-px h-5 bg-ink-200 mx-1"></div>
          <button class="flex items-center gap-2.5 pl-2 pr-3 py-1.5 rounded-lg hover:bg-ink-50 transition-colors group">
            <div class="w-7 h-7 rounded-full bg-brand-500 flex items-center justify-center">
              <span class="font-mono text-[11px] font-medium text-ink-900">{{ iniziali }}</span>
            </div>
            <span class="text-sm font-medium text-ink-700 hidden sm:inline">{{ utente.nome }}</span>
          </button>
        </div>
      </header>

      <main class="flex-1 p-4 sm:p-6 lg:p-8 space-y-6 sm:space-y-8">
        <div v-if="!soloIssue">
          <h2 class="font-display text-ink-900 text-xl font-bold">Resoconto issue</h2>
          <p class="text-ink-400 text-sm mt-0.5">Panoramica generale e ultime issue aggiunte.</p>
        </div>

        <div v-if="!soloIssue" class="grid grid-cols-2 lg:grid-cols-4 gap-4">
          <StatCard label="Totale" :value="statTotale" subtitle="issue nel sistema" icon-bg-class="bg-ink-50" value-color-class="text-ink-900" anim-delay-class="delay-1">
            <template #icon>
              <svg class="w-3.5 h-3.5 text-ink-400" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <line x1="8" y1="6" x2="21" y2="6" /><line x1="8" y1="12" x2="21" y2="12" />
                <line x1="8" y1="18" x2="21" y2="18" /><line x1="3" y1="6" x2="3.01" y2="6" />
              </svg>
            </template>
          </StatCard>

          <StatCard label="Da fare" :value="statTodo" subtitle="issue da prendere in carico" icon-bg-class="bg-amber-50" value-color-class="text-amber-500" anim-delay-class="delay-2">
            <template #icon>
              <svg class="w-3.5 h-3.5 text-amber-400" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="12" cy="12" r="10" />
                <polyline points="12 6 12 12 16 14" />
              </svg>
            </template>
          </StatCard>

          <StatCard label="In corso" :value="statInCorso" subtitle="attualmente in lavorazione" icon-bg-class="bg-blue-50" value-color-class="text-blue-500" anim-delay-class="delay-3">
            <template #icon>
              <svg class="w-3.5 h-3.5 text-blue-400" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <polyline points="23 4 23 10 17 10" />
                <path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10" />
              </svg>
            </template>
          </StatCard>

          <StatCard label="Risolte" :value="statRisolte" subtitle="chiuse o completate" icon-bg-class="bg-green-50" value-color-class="text-green-600" anim-delay-class="delay-4">
            <template #icon>
              <svg class="w-3.5 h-3.5 text-green-500" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <polyline points="20 6 9 17 4 12" />
              </svg>
            </template>
          </StatCard>
        </div>

        <div id="ultime-issue" class="anim-fade-in delay-5">
          <div class="mb-4">
            <h3 class="font-display text-ink-900 text-base font-semibold">
              {{ soloIssue ? 'Tutte le issue' : 'Ultime issue aggiunte' }}
            </h3>
            <p class="text-ink-400 text-xs mt-0.5">
              {{ soloIssue ? 'Elenco completo delle issue.' : 'Con dettaglio di chi le ha inserite.' }}
            </p>
          </div>

          <div class="bg-white rounded-xl border border-ink-100 shadow-sm overflow-hidden">
            <div class="overflow-x-auto">
              <table class="w-full text-sm">
                <thead>
                  <tr class="border-b border-ink-100">
                    <th class="text-left px-5 py-3 text-[10px] font-mono text-ink-400 uppercase tracking-wider whitespace-nowrap">#</th>
                    <th class="text-left px-4 py-3 text-[10px] font-mono text-ink-400 uppercase tracking-wider">Titolo</th>
                    <th class="text-left px-4 py-3 text-[10px] font-mono text-ink-400 uppercase tracking-wider whitespace-nowrap">Tipo</th>
                    <th class="text-left px-4 py-3 text-[10px] font-mono text-ink-400 uppercase tracking-wider whitespace-nowrap">Priorità</th>
                    <th class="text-left px-4 py-3 text-[10px] font-mono text-ink-400 uppercase tracking-wider whitespace-nowrap">Stato</th>
                    <th class="text-left px-4 py-3 text-[10px] font-mono text-ink-400 uppercase tracking-wider whitespace-nowrap">Aggiunta da</th>
                    <th class="text-left px-4 py-3 text-[10px] font-mono text-ink-400 uppercase tracking-wider whitespace-nowrap hidden lg:table-cell">Data Scadenza</th>
                    <th class="px-4 py-3"></th>
                  </tr>
                </thead>
                <tbody class="divide-y divide-ink-50">
                  <tr v-for="issue in issueDaMostrare" :key="issue.id" class="transition-colors hover:bg-ink-50/60">
                    <td class="px-5 py-3.5 whitespace-nowrap">
                      <span class="font-mono text-[12px] text-ink-400">#{{ issue.id }}</span>
                    </td>
                    <td class="px-4 py-3.5">
                      <span class="text-ink-800 font-medium max-w-xs truncate block">{{ issue.titolo }}</span>
                    </td>
                    <td class="px-4 py-3.5 whitespace-nowrap">
                      <BadgeTipo :tipo="issue.tipo" />
                    </td>
                    <td class="px-4 py-3.5 whitespace-nowrap">
                      <BadgePriorita :priorita="issue.priorita" />
                    </td>
                    <td class="px-4 py-3.5 whitespace-nowrap">
                      <BadgeStato :stato="issue.stato" />
                    </td>
                    <td class="px-4 py-3.5 whitespace-nowrap">
                      <span class="text-sm text-ink-600">{{ issue.creatore.email }}</span>
                    </td>
                    <td class="px-4 py-3.5 whitespace-nowrap hidden lg:table-cell">
                      <span class="font-mono text-[12px] text-ink-400">{{ formattaData(issue.dataScadenza) }}</span>
                    </td>
                    <td class="px-4 py-3.5 text-right">
                      <button
                        type="button"
                        @click="apriPopupAssegna(issue)"
                        class="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-semibold
                               text-brand-700 bg-brand-50 hover:bg-brand-100 border border-brand-100 transition-colors"
                      >
                        Assegna
                      </button>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </main>
    </div>

    <div
      v-if="popupAssegnaAperto"
      @click="chiudiPopupAssegna"
      class="fixed inset-0 z-50 bg-ink-900/50 flex items-end sm:items-center justify-center p-0 sm:p-4"
    >
      <div
        @click.stop
        class="w-full max-w-md rounded-none sm:rounded-xl bg-white border border-ink-100 shadow-xl p-5 space-y-4"
      >
        <h3 class="font-display text-lg font-semibold text-ink-900">
          Assegna issue <span class="font-mono text-sm text-ink-400">#{{ issueSelezionata?.id }}</span>
        </h3>

        <input
          v-model="dataAssegnazione"
          type="date"
          class="w-full rounded-lg border border-ink-200 px-3 py-2 text-sm text-ink-800
                 focus:border-brand-500 focus:ring-2 focus:ring-brand-500/20"
        />

        <div class="flex flex-col-reverse sm:flex-row justify-end gap-2">
          <button
            type="button"
            @click="chiudiPopupAssegna"
            class="px-3 py-2 rounded-lg text-sm font-medium text-ink-600 hover:bg-ink-50 transition-colors w-full sm:w-auto"
          >
            Annulla
          </button>
          <button
            type="button"
            @click="submitAssegnazione"
            class="px-4 py-2 rounded-lg text-sm font-semibold text-white bg-brand-500 hover:bg-brand-600 transition-colors w-full sm:w-auto"
          >
            Submit
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
