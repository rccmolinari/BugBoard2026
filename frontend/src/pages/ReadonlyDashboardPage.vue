<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import Sidebar from '../components/Sidebar.vue'
import StatCard from '../components/StatCard.vue'
import BadgeTipo from '../components/BadgeTipo.vue'
import BadgeStato from '../components/BadgeStato.vue'
import BadgePriorita from '../components/BadgePriorita.vue'
import IssueDetailPanel from '../components/IssueDetailPanel.vue'
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

const router = useRouter()
const route = useRoute()
const utente = getUtente() ?? { nome: '', sessionId: null, ruolo: '' }
const issues = ref([])
const sidebarAperta = ref(false)
const issueDettaglio = ref(null)
const caricamentoDettaglio = ref(false)

if (utente.sessionId) {
  axios.get('/api/issues/stakeholder/' + utente.sessionId)
    .then(response => {
      issues.value = response.data
      //sortami le issue
      issues.value.sort((a, b) => new Date(b.dataCreazione) - new Date(a.dataCreazione))
      console.log('Issue caricate:', issues.value)
    })
    .catch(error => {
      console.error('Errore durante il caricamento delle issue:', error)
    })
} else {
  router.replace('/')
}

const cerca = ref('')
const cercaId = ref('')
const filtroStato = ref('')

const statTotale   = computed(() => issues.value.length)
const statTodo     = computed(() => issues.value.filter(i => i.stato === 'TODO').length)
const statProgress = computed(() => issues.value.filter(i => i.stato === 'IN_PROGRESS').length)
const statCritici  = computed(() => issues.value.filter(i =>
  i.priorita === 4 &&
  i.stato !== 'DONE' &&
  i.stato !== 'CLOSED'
).length)

const issueFiltrate = computed(() => {
  const q = cerca.value.toLowerCase()
  const idQ = cercaId.value.trim()
  return issues.value.filter(issue => {
    const matchTitolo = issue.titolo.toLowerCase().includes(q)
    const matchId     = !idQ || String(issue.id).includes(idQ)
    const matchStato  = !filtroStato.value || issue.stato === filtroStato.value
    return matchTitolo && matchId && matchStato
  })
})

const soloIssue = computed(() => route.hash === '#tutte-issue')

const iniziali = computed(() => {
  if (!utente?.nome) return '?'
  return utente.nome
    .split(' ')
    .map(p => p[0])
    .join('')
    .toUpperCase()
    .slice(0, 2)
})

function formattaData(iso) {
  if (!iso) return '—'
  return new Date(iso).toLocaleDateString('it-IT', {
    day: '2-digit', month: 'short', year: 'numeric'
  })
}

function isScaduta(issue) {
  if (!issue.scadenza) return false
  if (issue.stato === 'done' || issue.stato === 'closed') return false
  return new Date(issue.scadenza) < new Date()
}

async function apriIssue(id) {
  caricamentoDettaglio.value = true
  issueDettaglio.value = null
  try {
    const res = await axios.get(`/api/issues/dettagliStakeholder/${id}/${utente.sessionId}`)
    issueDettaglio.value = res.data
  } catch (e) {
    console.error('Errore caricamento dettagli:', e)
  } finally {
    caricamentoDettaglio.value = false
  }
}
function inizialiDa(nome) {
  if (!nome) return '?'
  return nome.split(' ').map(p => p[0]).join('').toUpperCase().slice(0, 2)
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
    <Sidebar pagina="dashboard" :utente="utente" :is-open="sidebarAperta" @logout="logout" />

    <div
      v-show="sidebarAperta"
      @click="chiudiSidebar"
      class="fixed inset-0 bg-ink-900/40 z-20"
    ></div>

    <div class="app-main-content min-h-screen flex flex-col">
      <header class="sticky top-0 z-10 bg-white border-b border-ink-100
                     flex items-center justify-between h-14 px-6 flex-shrink-0">

        <button @click="aprireSidebar"
                class="mr-3 p-1.5 rounded-lg text-ink-500 hover:bg-ink-50 md:hidden">
          <svg class="w-5 h-5" viewBox="0 0 24 24" fill="none"
            stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <line x1="3" y1="6" x2="21" y2="6"/>
            <line x1="3" y1="12" x2="21" y2="12"/>
            <line x1="3" y1="18" x2="21" y2="18"/>
          </svg>
        </button>

        <div class="flex items-center gap-2">
          <h1 class="font-display text-ink-900 text-[15px] font-semibold">Dashboard</h1>
          <span class="text-ink-300 text-sm hidden sm:inline">/</span>
          <span class="text-ink-400 text-sm hidden sm:inline">{{ soloIssue ? 'Tutte le issue' : 'Panoramica' }}</span>
        </div>

        <div class="flex items-center gap-2">
          <button class="flex items-center gap-2.5 pl-2 pr-3 py-1.5 rounded-lg
                         hover:bg-ink-50 transition-colors group">
            <div class="w-7 h-7 rounded-full bg-brand-500 flex items-center justify-center">
              <span class="font-mono text-[11px] font-medium text-ink-900">{{ iniziali }}</span>
            </div>
            <span class="text-sm font-medium text-ink-700 hidden sm:inline">{{ utente.nome }}</span>
            <svg class="w-3 h-3 text-ink-300 group-hover:text-ink-500 transition-colors hidden sm:block"
                 viewBox="0 0 24 24" fill="none" stroke="currentColor"
                 stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
              <polyline points="6 9 12 15 18 9"/>
            </svg>
          </button>
        </div>
      </header>

      <main class="flex-1 p-4 sm:p-6 lg:p-8 space-y-6 sm:space-y-8">
        <div v-if="!soloIssue" class="flex flex-col sm:flex-row sm:items-start justify-between gap-4">
          <div>
            <h2 class="font-display text-ink-900 text-xl font-bold">Panoramica progetto</h2>
            <p class="text-ink-400 text-sm mt-0.5">Situazione attuale delle issue.</p>
          </div>
        </div>

        <div v-if="!soloIssue" class="grid grid-cols-2 lg:grid-cols-4 gap-4">
          <StatCard label="Totale" :value="statTotale" subtitle="issue nel sistema"
                    icon-bg-class="bg-ink-50" value-color-class="text-ink-900" anim-delay-class="delay-1">
            <template #icon>
              <svg class="w-3.5 h-3.5 text-ink-400" viewBox="0 0 24 24" fill="none"
                stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <line x1="8" y1="6" x2="21" y2="6"/><line x1="8" y1="12" x2="21" y2="12"/>
                <line x1="8" y1="18" x2="21" y2="18"/>
                <line x1="3" y1="6" x2="3.01" y2="6"/>
              </svg>
            </template>
          </StatCard>

          <StatCard label="Da fare" :value="statTodo" subtitle="in attesa di lavorazione"
                    icon-bg-class="bg-amber-50" value-color-class="text-amber-500" anim-delay-class="delay-2">
            <template #icon>
              <svg class="w-3.5 h-3.5 text-amber-400" viewBox="0 0 24 24" fill="none"
                stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="12" cy="12" r="10"/>
                <polyline points="12 6 12 12 16 14"/>
              </svg>
            </template>
          </StatCard>

          <StatCard label="In corso" :value="statProgress" subtitle="in lavorazione"
                    icon-bg-class="bg-blue-50" value-color-class="text-blue-500" anim-delay-class="delay-3">
            <template #icon>
              <svg class="w-3.5 h-3.5 text-blue-400" viewBox="0 0 24 24" fill="none"
                stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <polyline points="23 4 23 10 17 10"/>
                <path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10"/>
              </svg>
            </template>
          </StatCard>

          <StatCard label="Critici" :value="statCritici" subtitle="bug ad alta priorità aperti"
                    icon-bg-class="bg-red-50" value-color-class="text-red-500" anim-delay-class="delay-4">
            <template #icon>
              <svg class="w-3.5 h-3.5 text-red-400" viewBox="0 0 24 24" fill="none"
                stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/>
                <line x1="12" y1="9" x2="12" y2="13"/>
                <line x1="12" y1="17" x2="12.01" y2="17"/>
              </svg>
            </template>
          </StatCard>
        </div>

        <div id="tutte-issue" class="anim-fade-in delay-5">
          <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-3 mb-4">
            <div>
              <h3 class="font-display text-ink-900 text-base font-semibold">
                {{ soloIssue ? 'Tutte le issue' : 'Issue recenti' }}
              </h3>
              <p class="text-ink-400 text-xs mt-0.5">
                {{ soloIssue ? 'Elenco completo delle issue.' : `${issueFiltrate.length} issue trovate` }}
              </p>
            </div>

            <div class="flex flex-wrap items-center gap-2 w-full sm:w-auto">
              <div class="relative w-full sm:w-auto">
                <svg class="absolute left-2.5 top-1/2 -translate-y-1/2 w-3.5 h-3.5 text-ink-300 pointer-events-none"
                     viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                     stroke-linecap="round" stroke-linejoin="round">
                  <circle cx="11" cy="11" r="8"/>
                  <line x1="21" y1="21" x2="16.65" y2="16.65"/>
                </svg>
                <input v-model="cerca"
                       type="search"
                       placeholder="Cerca per titolo…"
                       class="pl-8 pr-3 py-1.5 text-sm rounded-lg border border-ink-200
                              bg-white text-ink-800 placeholder-ink-300
                              focus:border-brand-500 focus:ring-2 focus:ring-brand-500/20
                              w-full sm:w-44 transition-colors" />
              </div>

              <div class="relative w-full sm:w-auto">
                <svg class="absolute left-2.5 top-1/2 -translate-y-1/2 w-3.5 h-3.5 text-ink-300 pointer-events-none"
                     viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                     stroke-linecap="round" stroke-linejoin="round">
                  <line x1="4" y1="9" x2="20" y2="9"/><line x1="4" y1="15" x2="20" y2="15"/>
                  <line x1="10" y1="3" x2="8" y2="21"/><line x1="16" y1="3" x2="14" y2="21"/>
                </svg>
                <input v-model="cercaId"
                       type="search"
                       placeholder="Cerca per ID…"
                       class="pl-8 pr-3 py-1.5 text-sm rounded-lg border border-ink-200
                              bg-white text-ink-800 placeholder-ink-300
                              focus:border-brand-500 focus:ring-2 focus:ring-brand-500/20
                              w-full sm:w-32 transition-colors" />
              </div>

              <select v-model="filtroStato"
                      class="px-2.5 py-1.5 text-sm rounded-lg border border-ink-200
                             bg-white text-ink-600 cursor-pointer
                             focus:border-brand-500 focus:ring-2 focus:ring-brand-500/20
                             transition-colors w-full sm:w-auto">
                <option value="">Tutti gli stati</option>
                <option value="TODO">Todo</option>
                <option value="IN_PROGRESS">In Progress</option>
                <option value="DONE">Done</option>
                <option value="CLOSED">Closed</option>
                <option value="EXPIRED">Expired</option>
              </select>
            </div>
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
                    <th class="text-left px-4 py-3 text-[10px] font-mono text-ink-400 uppercase tracking-wider whitespace-nowrap">Assegnata Da</th>
                    <th class="text-left px-4 py-3 text-[10px] font-mono text-ink-400 uppercase tracking-wider whitespace-nowrap hidden lg:table-cell">Scadenza</th>
                  </tr>
                </thead>
                <tbody class="divide-y divide-ink-50">
                  <tr v-if="issueFiltrate.length === 0">
                    <td colspan="7" class="px-5 py-12 text-center">
                      <div class="flex flex-col items-center gap-2 text-ink-300">
                        <svg class="w-8 h-8" viewBox="0 0 24 24" fill="none"
                          stroke="currentColor" stroke-width="1.5"
                          stroke-linecap="round" stroke-linejoin="round">
                          <circle cx="11" cy="11" r="8"/>
                          <line x1="21" y1="21" x2="16.65" y2="16.65"/>
                        </svg>
                        <p class="text-sm">Nessuna issue corrisponde ai filtri</p>
                      </div>
                    </td>
                  </tr>

                  <tr v-for="issue in issueFiltrate" :key="issue.id"
                      class="issue-row transition-colors"
                      @click="apriIssue(issue.id)">

                    <td class="px-5 py-3.5 whitespace-nowrap">
                      <span class="font-mono text-[12px] text-ink-400">#{{ issue.id }}</span>
                    </td>

                    <td class="px-4 py-3.5">
                      <div class="flex items-center gap-2">
                        <span class="text-ink-800 font-medium max-w-xs truncate block">
                          {{ issue.titolo }}
                        </span>
                        <div class="hidden lg:flex items-center gap-1">
                          <span v-for="e in (issue.etichette || []).slice(0, 2)" :key="e"
                                class="px-1.5 py-0.5 text-[10px] font-mono rounded
                                       bg-ink-100 text-ink-500 border border-ink-200">
                            {{ e }}
                          </span>
                          <span v-if="issue.etichette && issue.etichette.length > 2"
                                class="text-[11px] text-ink-400">
                            +{{ issue.etichette.length - 2 }}
                          </span>
                        </div>
                      </div>
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
                      <div v-if="issue.assegnatoDa" class="flex items-center gap-2">
                        <div class="w-5 h-5 rounded-full bg-brand-500/20 flex items-center justify-center flex-shrink-0">
                          <span class="text-[9px] font-mono text-brand-700 font-medium">
                            {{ inizialiDa(issue.assegnatoDa) }}
                          </span>
                        </div>
                        <span class="text-sm text-ink-600">{{ issue.assegnatoDa }}</span>
                      </div>
                      <span v-else class="text-ink-300 text-sm">—</span>
                    </td>

                    <td class="px-4 py-3.5 whitespace-nowrap hidden lg:table-cell">
                      <span v-if="issue.dataScadenza"
                            class="font-mono text-[12px]"
                            :class="isScaduta(issue) ? 'text-red-500 font-medium' : 'text-ink-400'">
                        {{ formattaData(issue.dataScadenza) }}
                      </span>
                      <span v-else class="text-ink-200 text-sm">—</span>
                    </td>

                  </tr>

                </tbody>
              </table>
            </div>

            <div class="border-t border-ink-100 px-5 py-3 flex items-center justify-between">
              <span class="text-xs text-ink-400">
                Mostrate {{ issueFiltrate.length }} di {{ issues.length }}
              </span>
              <a href="#" class="text-xs font-medium text-brand-600 hover:text-brand-700 transition-colors">
                Vedi tutte le issue →
              </a>
            </div>
          </div>
        </div>
      </main>
    </div>

    <IssueDetailPanel
      :issue="issueDettaglio"
      :caricamento="caricamentoDettaglio"
      :session-id="utente.sessionId"
      :allow-comment="false"
      :ruolo="utente.ruolo"
      @close="issueDettaglio = null"
    />
  </div>
</template>
