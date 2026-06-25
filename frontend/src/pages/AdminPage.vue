<!--
  AdminPage.vue
  Dashboard principale dell'admin orientata alle issue.
-->
<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import Sidebar from '../components/Sidebar.vue'
import StatCard from '../components/StatCard.vue'
import BadgeTipo from '../components/BadgeTipo.vue'
import BadgeStato from '../components/BadgeStato.vue'
import BadgePriorita from '../components/BadgePriorita.vue'
import api from '../api'
import IssueDetailPanel from '../components/IssueDetailPanel.vue'
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
const issueDettaglio = ref(null)
const caricamentoDettaglio = ref(false)

async function apriIssue(id) {
  caricamentoDettaglio.value = true
  issueDettaglio.value = null
  try {
  const res = await api.get(`/api/issues/dettagliAdmin/${id}`)
    issueDettaglio.value = res.data
  } catch (e) {
    console.error('Errore caricamento dettagli:', e)
  } finally {
    caricamentoDettaglio.value = false
  }
}
const issues = ref([])

// Stato noto al client: id -> version. A ogni giro di polling lo confronto con
// quello del server per capire quali issue sono nuove o cambiate.
let versioniNote = new Map()

function indicizzaVersioni(lista) {
  versioniNote = new Map(lista.map(i => [i.id, i.version]))
}

// Caricamento completo: load iniziale e dopo assegna/chiudi. Riallinea anche
// l'indice delle version.
async function caricaIssue() {
  try {
    const response = await api.get('/api/issues')
    issues.value = response.data
    // più recenti prima
    issues.value.sort((a, b) => new Date(b.dataCreazione) - new Date(a.dataCreazione))
    indicizzaVersioni(issues.value)
  } catch (error) {
    console.error('Errore durante il caricamento delle issue:', error)
  }
}

// Polling leggero: chiedo solo [{id, version}] di tutte le issue, confronto con
// quello che ho, e mi riprendo via /righe solo le issue nuove o cambiate.
async function controllaAggiornamenti() {
  try {
    const { data: versioni } = await api.get('/api/issues/versions')
    const idsServer = new Set(versioni.map(v => v.id))

    const idsDaAggiornare = versioni
      .filter(v => versioniNote.get(v.id) !== v.version)
      .map(v => v.id)
    const ciSonoRimozioni = [...versioniNote.keys()].some(id => !idsServer.has(id))

    if (!idsDaAggiornare.length && !ciSonoRimozioni) return

    // parto dalla lista attuale, togliendo eventuali issue sparite dal server
    let lista = ciSonoRimozioni
      ? issues.value.filter(i => idsServer.has(i.id))
      : [...issues.value]

    if (idsDaAggiornare.length) {
      const { data: righe } = await api.get('/api/issues/righe', {
        params: { ids: idsDaAggiornare.join(',') },
      })
      const perId = new Map(righe.map(r => [r.id, r]))
      const idsEsistenti = new Set(lista.map(i => i.id))
      // sostituisco le righe cambiate...
      lista = lista.map(i => perId.get(i.id) ?? i)
      // ...e aggiungo quelle nuove
      for (const riga of righe) {
        if (!idsEsistenti.has(riga.id)) lista.push(riga)
      }
      lista.sort((a, b) => new Date(b.dataCreazione) - new Date(a.dataCreazione))
    }

    issues.value = lista
    indicizzaVersioni(issues.value)
  } catch (error) {
    console.error('Errore durante il controllo aggiornamenti:', error)
  }
}

if (utente.sessionId) {
  caricaIssue()
} else {
  router.replace('/')
}

// Polling: l'admin rivede le issue aggiornate ogni 5 secondi
let pollingId = null
onMounted(() => {
  if (utente.sessionId) pollingId = setInterval(controllaAggiornamenti, 5000)
})
onUnmounted(() => clearInterval(pollingId))

const sidebarAperta = ref(false)
const popupAssegnaAperto = ref(false)
const issueSelezionata = ref(null)
const dataAssegnazione = ref('')
const emailAssegnatario = ref('')
const route = useRoute()
const oggi = new Date().toISOString().split('T')[0]
const assignError = ref('')
const isAssigning = ref(false)
const assignSuccess = ref(false)
const dataScadenza = ref('')
const iniziali = computed(() => {
  if (!utente?.nome) return '?'
  return utente.nome
    .split(' ')
    .map(p => p[0])
    .join('')
    .toUpperCase()
    .slice(0, 2)
})

const cerca = ref('')
const filtroTipo = ref('')
const filtroStato = ref('')
const cercaId = ref('')

const statTotale = computed(() => issues.value.length)
const statTodo = computed(() => issues.value.filter(i => i.stato === 'TODO').length)
const statInCorso = computed(() => issues.value.filter(i => i.stato === 'IN_PROGRESS').length)
const statRisolte = computed(() => issues.value.filter(i => i.stato === 'DONE').length)
const statScadute = computed(() => issues.value.filter(i => i.stato === 'EXPIRED').length)
const statDaAssegnare = computed(() => issues.value.filter(i => i.stato === 'TODO' && !i.assegnatoA).length)
const statClosed = computed(() => issues.value.filter(i => i.stato === 'CLOSED').length)

function rankIssue(i) {
  if (i.stato === 'EXPIRED') return 0
  if (i.stato === 'TODO' && !i.assegnatoA) return 1
  return 2
}

const ultimeIssue = computed(() => (
  [...issues.value]
    .filter(i => i.stato !== 'DONE' && i.stato !== 'CLOSED' && i.stato !== 'IN_PROGRESS')
    .sort((a, b) => {
      const d = rankIssue(a) - rankIssue(b)
      if (d !== 0) return d
      return new Date(b.dataCreazione) - new Date(a.dataCreazione)
    })
    .slice(0, 6)
))

const tutteIssue = computed(() => {
  const idQ = cercaId.value.trim()
  const q = cerca.value.toLowerCase()
  return [...issues.value]
    .filter(i => {
      const matchId     = !idQ || String(i.id).includes(idQ)
      const matchTitolo = !q   || i.titolo.toLowerCase().includes(q)
      const matchTipo   = !filtroTipo.value  || i.tipo  === filtroTipo.value
      const matchStato  = !filtroStato.value || i.stato === filtroStato.value
      return matchId && matchTitolo && matchTipo && matchStato
    })
    .sort((a, b) => new Date(b.dataCreazione) - new Date(a.dataCreazione))
})

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
  dataScadenza.value = ''
  // Riparto pulito a ogni apertura: senza questo, l'errore (o il successo) di
  // un'assegnazione precedente resterebbe a video anche su un'altra issue.
  emailAssegnatario.value = ''
  assignError.value = ''
  assignSuccess.value = false
  popupAssegnaAperto.value = true
}

function chiudiPopupAssegna() {
  popupAssegnaAperto.value = false
  issueSelezionata.value = null
  assignError.value = ''
  assignSuccess.value = false
}

async function submitAssegnazione() {
  isAssigning.value = true
  assignError.value = ''
  assignSuccess.value = false

  try {
    if(dataScadenza.value && dataScadenza.value <= oggi) {
      assignError.value = 'La data di scadenza non deve essere inferiore ad oggi'
      isAssigning.value = false
      return
    }
    const payload = {
      issueId: issueSelezionata.value?.id,
      userEmail: emailAssegnatario.value,
      dataScadenza: dataScadenza.value || null,
      version: issueSelezionata.value?.version ?? null,
    }
    await api.put('/api/issues/assign', payload)

    // 2xx = assegnazione riuscita; gli errori arrivano nel catch
    assignSuccess.value = true

    setTimeout(() => {
      chiudiPopupAssegna()
      emailAssegnatario.value = ''
      dataAssegnazione.value = ''
      dataScadenza.value = ''
      assignSuccess.value = false
    }, 900)

    await caricaIssue()

  } catch (error) {
    assignError.value =
      error?.response?.data?.message || 'Errore durante assegnazione'
  } finally {
    isAssigning.value = false
  }
}

async function onIssueChiusa() {
  issueDettaglio.value = null
  try {
    await caricaIssue()
  } catch (e) {
    console.error('Errore ricaricamento issue:', e)
  }
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

  api.post('/api/auth/logout')
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
          <button
            class="flex items-center gap-2.5 pl-2 pr-3 py-1.5 rounded-lg pointer-events-none">
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

        <div v-if="!soloIssue" class="grid grid-cols-2 sm:grid-cols-3 gap-4">
          <StatCard label="Totale" :value="statTotale" subtitle="issue nel sistema" icon-bg-class="bg-ink-50" value-color-class="text-ink-900" anim-delay-class="delay-1">
            <template #icon>
              <svg class="w-3.5 h-3.5 text-ink-400" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <line x1="8" y1="6" x2="21" y2="6" /><line x1="8" y1="12" x2="21" y2="12" />
                <line x1="8" y1="18" x2="21" y2="18" /><line x1="3" y1="6" x2="3.01" y2="6" />
              </svg>
            </template>
          </StatCard>

          <StatCard label="In corso" :value="statInCorso" subtitle="attualmente in lavorazione" icon-bg-class="bg-blue-50" value-color-class="text-blue-500" anim-delay-class="delay-2">
            <template #icon>
              <svg class="w-3.5 h-3.5 text-blue-400" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <polyline points="23 4 23 10 17 10" />
                <path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10" />
              </svg>
            </template>
          </StatCard>

          <StatCard label="Risolte" :value="statRisolte" subtitle="completate" icon-bg-class="bg-green-50" value-color-class="text-green-600" anim-delay-class="delay-3">
            <template #icon>
              <svg class="w-3.5 h-3.5 text-green-500" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <polyline points="20 6 9 17 4 12" />
              </svg>
            </template>
          </StatCard>

          <StatCard label="Scadute" :value="statScadute" subtitle="issue scadute" icon-bg-class="bg-red-50" value-color-class="text-red-600" anim-delay-class="delay-4">
            <template #icon>
              <svg class="w-3.5 h-3.5 text-red-400" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="12" cy="12" r="10" />
                <line x1="12" y1="8" x2="12" y2="12" />
                <line x1="12" y1="16" x2="12.01" y2="16" />
              </svg>
            </template>
          </StatCard>

          <StatCard label="Da assegnare" :value="statDaAssegnare" subtitle="TODO senza assegnatario" icon-bg-class="bg-orange-50" value-color-class="text-orange-500" anim-delay-class="delay-5">
            <template #icon>
              <svg class="w-3.5 h-3.5 text-orange-400" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="12" cy="8" r="4" />
                <path d="M20 21a8 8 0 1 0-16 0" />
              </svg>
            </template>
          </StatCard>

          <StatCard label="Chiuse" :value="statClosed" subtitle="issue chiuse definitivamente" icon-bg-class="bg-slate-50" value-color-class="text-slate-500" anim-delay-class="delay-6">
            <template #icon>
              <svg class="w-3.5 h-3.5 text-slate-400" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
                <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
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

          <div v-if="soloIssue" class="flex flex-wrap items-center gap-2 mb-4">
            <div class="relative">
              <svg class="absolute left-2.5 top-1/2 -translate-y-1/2 w-3.5 h-3.5 text-ink-300 pointer-events-none"
                   viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                   stroke-linecap="round" stroke-linejoin="round">
                <circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/>
              </svg>
              <input v-model="cerca" type="search" placeholder="Cerca per nome…"
                     class="pl-8 pr-3 py-1.5 text-sm rounded-lg border border-ink-200
                            bg-white text-ink-800 placeholder-ink-300
                            focus:border-brand-500 focus:ring-2 focus:ring-brand-500/20
                            w-44 transition-colors" />
            </div>

            <div class="relative">
              <svg class="absolute left-2.5 top-1/2 -translate-y-1/2 w-3.5 h-3.5 text-ink-300 pointer-events-none"
                   viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                   stroke-linecap="round" stroke-linejoin="round">
                <line x1="4" y1="9" x2="20" y2="9"/><line x1="4" y1="15" x2="20" y2="15"/>
                <line x1="10" y1="3" x2="8" y2="21"/><line x1="16" y1="3" x2="14" y2="21"/>
              </svg>
              <input v-model="cercaId" type="search" placeholder="Cerca per ID…"
                     class="pl-8 pr-3 py-1.5 text-sm rounded-lg border border-ink-200
                            bg-white text-ink-800 placeholder-ink-300
                            focus:border-brand-500 focus:ring-2 focus:ring-brand-500/20
                            w-36 transition-colors" />
            </div>

            <select v-model="filtroTipo"
                    class="px-2.5 py-1.5 text-sm rounded-lg border border-ink-200
                           bg-white text-ink-600 cursor-pointer
                           focus:border-brand-500 focus:ring-2 focus:ring-brand-500/20 transition-colors">
              <option value="">Tutti i tipi</option>
              <option value="BUG">Bug</option>
              <option value="FEATURE">Feature</option>
              <option value="QUESTION">Question</option>
              <option value="DOCUMENTATION">Docs</option>
            </select>

            <select v-model="filtroStato"
                    class="px-2.5 py-1.5 text-sm rounded-lg border border-ink-200
                           bg-white text-ink-600 cursor-pointer
                           focus:border-brand-500 focus:ring-2 focus:ring-brand-500/20 transition-colors">
              <option value="">Tutti gli stati</option>
              <option value="TODO">Todo</option>
              <option value="IN_PROGRESS">In Progress</option>
              <option value="DONE">Done</option>
              <option value="CLOSED">Closed</option>
              <option value="EXPIRED">Expired</option>
            </select>
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
                                        <th class="text-left px-4 py-3 text-[10px] font-mono text-ink-400 uppercase tracking-wider whitespace-nowrap">Assegnata a</th>
                    <th class="text-left px-4 py-3 text-[10px] font-mono text-ink-400 uppercase tracking-wider whitespace-nowrap hidden lg:table-cell">Data Scadenza</th>
                    <th class="px-4 py-3"></th>
                  </tr>
                </thead>
                <tbody class="divide-y divide-ink-50">
                <tr v-for="issue in issueDaMostrare" :key="issue.id"
                    class="transition-colors hover:bg-ink-50/60 cursor-pointer"
                    @click="apriIssue(issue.id)">
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
                      <span class="text-sm text-ink-600">{{ issue.creatore}}</span>
                    </td>
                    <td class="px-4 py-3.5 whitespace-nowrap">
                      <span class="text-sm text-ink-600">{{ issue.assegnatoA }}</span>
                    </td>
                    <td class="px-4 py-3.5 whitespace-nowrap hidden lg:table-cell">
                      <span class="font-mono text-[12px] text-ink-400">{{ formattaData(issue.dataScadenza) }}</span>
                    </td>
                    <td class="px-4 py-3.5 text-right">
                    <button
                      v-if="issue.stato !== 'CLOSED' && issue.stato !== 'DONE'"
                      type="button"
                      @click.stop="apriPopupAssegna(issue)"
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
      <div>
      <label for="assign-scadenza" class="text-xs font-medium text-ink-500 uppercase tracking-wide">
       Data di scadenza (opzionale)
      </label>
        <input
          id="assign-scadenza"
          v-model="dataScadenza"
          type="date"
          :min="oggi"
          class="w-full rounded-lg border border-ink-200 px-3 py-2 text-sm text-ink-800
                 focus:border-brand-500 focus:ring-2 focus:ring-brand-500/20"
         /> 
      </div>
      <div>
      <label for="assign-email" class="text-xs font-medium text-ink-500 uppercase tracking-wide">
        Email utente a cui assegnare
      </label>

      <input
        id="assign-email"
        v-model="emailAssegnatario"
        type="email"
        placeholder="utente@email.com"
        class="w-full rounded-lg border border-ink-200 px-3 py-2 text-sm text-ink-800
              focus:border-brand-500 focus:ring-2 focus:ring-brand-500/20"
      />
    </div>

      <div v-if="assignError"
      class="px-4 py-3 rounded-lg bg-red-50 border border-red-200 text-red-600 text-sm">
    {{ assignError }}
    </div>
    <div v-show="assignSuccess"
        class="px-4 py-3 rounded-lg bg-green-50 border border-green-100 text-green-700 text-sm">
      Issue assegnata con successo!
    </div>
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
    :disabled="isAssigning"
    class="px-4 py-2 rounded-lg text-sm font-semibold text-white bg-brand-500 hover:bg-brand-600
          transition-colors w-full sm:w-auto flex items-center justify-center gap-2"
  >
    <svg v-if="isAssigning"
        class="animate-spin w-4 h-4"
        xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
      <circle class="opacity-25" cx="12" cy="12" r="10"
              stroke="currentColor" stroke-width="4"/>
      <path class="opacity-75" fill="currentColor"
            d="M4 12a8 8 0 018-8v8H4z"/>
    </svg>

    <span>
      {{ isAssigning ? 'Assegnazione…' : 'Submit' }}
    </span>
  </button>
        </div>
      </div>
    </div>
    <IssueDetailPanel
      :issue="issueDettaglio"
      :caricamento="caricamentoDettaglio"
      :session-id="utente.sessionId"
      :ruolo="utente.ruolo"
      :isAdmin="true"
      @close="issueDettaglio = null"
      @assegna="(issue) => { issueDettaglio = null; apriPopupAssegna(issue) }"
      @chiusa="onIssueChiusa"
    />
  </div>
</template>
