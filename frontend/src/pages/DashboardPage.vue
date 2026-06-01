<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import Sidebar from '../components/Sidebar.vue'
import StatCard from '../components/StatCard.vue'
import BadgeTipo from '../components/BadgeTipo.vue'
import BadgeStato from '../components/BadgeStato.vue'
import BadgePriorita from '../components/BadgePriorita.vue'
import axios from 'axios'
import { onMounted, onUnmounted } from 'vue'
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

const router = useRouter()
const route = useRoute()
const utente = getUtente() ?? { nome: '', sessionId: null, ruolo: '' }
const issues = ref([])
const dataScadenza = ref(null)
const assegnatoDa = ref('')

if (utente.sessionId) {
  axios.get('/api/issues/user/' + utente.sessionId)
    .then(response => {
      issues.value = response.data
      //ordina le issue in base alla data creazione
      issues.value.sort((a, b) => new Date(b.dataCreazione) - new Date(a.dataCreazione))
      console.log('Issue caricate:', issues.value)
    })
    .catch(error => {
      console.error('Errore durante il caricamento delle issue:', error)
    })
} else {
  router.replace('/')
}

const nIssues = ref(0)

let intervalId = null

onMounted(() => {
  intervalId = setInterval(async () => {
    try {
      const response = await axios.get('/api/notifies/number/' + utente.sessionId)
      const nuovoValore = response.data

      // Se sono arrivate nuove notifiche rispetto a prima → ricarica le issue
      if (nuovoValore > nIssues.value) {
        const res = await axios.get('/api/issues/user/' + utente.sessionId)
        issues.value = res.data
        issues.value.sort((a, b) => new Date(b.dataCreazione) - new Date(a.dataCreazione))
      }

      nIssues.value = nuovoValore

      if (popupNotificheAperto.value) {
        const res = await axios.get('/api/notifies/list/' + utente.sessionId)
        notifiche.value = res.data
      }
    } catch (e) {
      console.error(e)
    }
  }, 1000)
})
onUnmounted(() => {
  clearInterval(intervalId)
})


/* ══════════════════════════════════════════════════════════════
   STATE REATTIVO — filtri tabella + sidebar mobile
   ══════════════════════════════════════════════════════════════ */
const cerca = ref('')
const cercaId = ref('')
const filtroTipo = ref('')
const filtroStato = ref('')
const sidebarAperta = ref(false)

// Popup segnalazione
const popupSegnalazioneAperto = ref(false)
const segnalazione = ref({
  titolo: '',
  descrizione: '',
  immagine: null,
  tipo: '',
  priorita: '',
  etichette: []
})
const nuovaEtichetta = ref('')

function aggiungiEtichetta() {
  const e = nuovaEtichetta.value.trim()
  if (e && !segnalazione.value.etichette.includes(e)) {
    segnalazione.value.etichette.push(e)
  }
  nuovaEtichetta.value = ''
}

function rimuoviEtichetta(idx) {
  segnalazione.value.etichette.splice(idx, 1)
}


const handleImageUpload = (event) => {
  segnalazione.value.immagine = event.target.files[0] ?? null
}
const erroreInvio = ref('')
const confermaInvio = ref('')
const invioInCorso = ref(false)

// Popup notifiche
const popupNotificheAperto = ref(false)
const notifiche = ref([])
const caricamentoNotifiche = ref(false)

// Popup dettaglio notifica
const notificaSelezionata = ref(null)   // oggetto { id, titolo, ... } della notifica
const issueNotifica = ref(null)         // oggetto Issue restituito da /apri
const caricamentoDettaglio = ref(false)
const erroreDettaglio = ref('')


/* ══════════════════════════════════════════════════════════════
   COMPUTED 
   ══════════════════════════════════════════════════════════════ */
const statTotale   = computed(() => issues.value.length)
const statTodo     = computed(() => issues.value.filter(i => i.stato === 'TODO').length)
const statDone     = computed(() => issues.value.filter(i => i.stato === 'DONE').length)
const statProssimeScadenza = computed(() => {
  const limite = new Date()
  limite.setDate(limite.getDate() + 2)
  return issues.value.filter(i => {
    if (!i.dataScadenza) return false
    if (i.stato === 'DONE' || i.stato === 'CLOSED') return false
    return new Date(i.dataScadenza) <= limite
  }).length
})
const notificheOrdinate = computed(() =>
  [...notifiche.value].sort((a, b) =>
    new Date(b.dataCreazione) - new Date(a.dataCreazione)
  )
)
const issueFiltrate = computed(() => {
  const q = cerca.value.toLowerCase()
  const idQ = cercaId.value.trim()
  return issues.value.filter(issue => {
    const matchTitolo = issue.titolo.toLowerCase().includes(q)
    const matchId     = !idQ || String(issue.id).includes(idQ)
    const matchTipo   = !filtroTipo.value  || issue.tipo  === filtroTipo.value
    const matchStato  = !filtroStato.value || issue.stato === filtroStato.value
    return matchTitolo && matchId && matchTipo && matchStato
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

const puoCreareIssue = computed(() => utente?.ruolo !== 'readonly')


/* ══════════════════════════════════════════════════════════════
   UTILITY
   ══════════════════════════════════════════════════════════════ */

function formattaDataOra(iso) {
  if (!iso) return '—'
  return new Date(iso).toLocaleString('it-IT', {
    day: '2-digit', month: 'short', year: 'numeric',
    hour: '2-digit', minute: '2-digit'
  })
}

function formattaData(iso) {
  if (!iso) return '—'
  return new Date(iso).toLocaleDateString('it-IT', {
    day: '2-digit', month: 'short', year: 'numeric',
  })
}

function isScaduta(issue) {
  if (!issue.scadenza) return false
  if (issue.stato === 'done' || issue.stato === 'closed') return false
  return new Date(issue.scadenza) < new Date()
}


const issueDettaglio = ref(null)

async function apriIssue(id) {
  caricamentoDettaglio.value = true
  issueDettaglio.value = null
  try {
    const res = await axios.get(`/api/issues/dettagli/${id}/${utente.sessionId}`)
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

async function onIssueChiusa() {
  issueDettaglio.value = null
  try {
    const res = await axios.get('/api/issues/user/' + utente.sessionId)
    issues.value = res.data
    issues.value.sort((a, b) => new Date(b.dataCreazione) - new Date(a.dataCreazione))
  } catch (e) {
    console.error('Errore ricaricamento issue:', e)
  }
}




/* ══════════════════════════════════════════════════════════════
   NOTIFICHE
   ══════════════════════════════════════════════════════════════ */

// GET /api/notifies/list/{sid} — apre/chiude il dropdown e carica la lista
async function getNotifiche() {
  if (popupNotificheAperto.value) {
    popupNotificheAperto.value = false
    return
  }
  popupNotificheAperto.value = true
  caricamentoNotifiche.value = true
  try {
    const response = await axios.get('/api/notifies/list/' + utente.sessionId)
    notifiche.value = response.data
  } catch (error) {
    console.error('Errore durante il caricamento delle notifiche:', error)
  } finally {
    caricamentoNotifiche.value = false
  }
}

// GET /api/notifies/apri/{id}/{sid}
// Il backend segna già la notifica come letta e ritorna l'Issue associata.
async function apriDettaglioNotifica(n) {
  notificaSelezionata.value = n
  issueNotifica.value = null
  erroreDettaglio.value = ''
  caricamentoDettaglio.value = true

  try {
    const response = await axios.get(`/api/notifies/apri/${n.id}/${utente.sessionId}`)
    issueNotifica.value = response.data

    // Aggiorna subito lista e contatore: il backend ha già segnato come letta
    notifiche.value = notifiche.value.filter(item => item.id !== n.id)
    nIssues.value = Math.max(0, nIssues.value - 1)
  } catch (error) {
    console.error('Errore durante l\'apertura della notifica:', error)
    erroreDettaglio.value = 'Impossibile caricare i dettagli. Riprova.'
  } finally {
    caricamentoDettaglio.value = false
  }
}

function chiudiDettaglioNotifica() {
  notificaSelezionata.value = null
  issueNotifica.value = null
  erroreDettaglio.value = ''
}


/* ══════════════════════════════════════════════════════════════
   SIDEBAR MOBILE + LOGOUT
   ══════════════════════════════════════════════════════════════ */
function aprireSidebar() { sidebarAperta.value = true  }
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


/* ══════════════════════════════════════════════════════════════
   POPUP NUOVA SEGNALAZIONE
   ══════════════════════════════════════════════════════════════ */
function apriPopupSegnalazione()  {
  erroreInvio.value = ''
  confermaInvio.value = ''
  popupSegnalazioneAperto.value = true
}

function chiudiPopupSegnalazione() {
  popupSegnalazioneAperto.value = false
  erroreInvio.value = ''
  confermaInvio.value = ''
}

async function submitSegnalazione() {
  erroreInvio.value = ''
  confermaInvio.value = ''

  if (!segnalazione.value.titolo.trim()) {
    erroreInvio.value = 'Il titolo è obbligatorio.'
    return
  }
  if (!segnalazione.value.tipo) {
    erroreInvio.value = 'Seleziona un tipo.'
    return
  }
  if (!segnalazione.value.priorita) {
    erroreInvio.value = 'Seleziona una priorità.'
    return
  }

  invioInCorso.value = true
  try {
    const formData = new FormData()
    formData.append('titolo',      segnalazione.value.titolo)
    formData.append('descrizione', segnalazione.value.descrizione ?? '')
    formData.append('tipo',        segnalazione.value.tipo)
    formData.append('priorita',    segnalazione.value.priorita)
    formData.append('stato',       'todo')
    if (segnalazione.value.immagine) {
      formData.append('immagine', segnalazione.value.immagine)
    }
    for (const e of segnalazione.value.etichette) {
      formData.append('etichetta', e)
    }

    await axios.put('/api/issues/create/' + utente.sessionId, formData)

    const response = await axios.get('/api/issues/user/' + utente.sessionId)
    issues.value = response.data
    issues.value.sort((a, b) => new Date(b.dataCreazione) - new Date(a.dataCreazione))

    confermaInvio.value = 'Segnalazione inviata con successo.'
    segnalazione.value = { titolo: '', descrizione: '', immagine: null, tipo: '', priorita: '', etichette: [] }
    nuovaEtichetta.value = ''
    setTimeout(() => {
      chiudiPopupSegnalazione()
    }, 900)
  } catch (error) {
    console.error('Errore durante la creazione dell\'issue:', error)
    erroreInvio.value = 'Errore durante l\'invio. Riprova.'
  } finally {
    invioInCorso.value = false
  }
}



</script>

<template>
  <div>
    <!--  
<img
  v-if="issueNotifica.hasImmagine"
  :src="`/api/issues/${issueNotifica.id}/immagine`"
  alt="Immagine allegata"
  class="w-full rounded-lg border border-ink-100 object-contain max-h-64"
/>
-->
    <!-- Sidebar condivisa -->
    <Sidebar pagina="dashboard" :utente="utente" :is-open="sidebarAperta" @logout="logout" />

    <!-- Overlay mobile -->
    <div
      v-show="sidebarAperta"
      @click="chiudiSidebar"
      class="fixed inset-0 bg-ink-900/40 z-20"
    ></div>


    <!-- ═══════════════════════════════════════════════════════════
         MAIN CONTENT
         ═══════════════════════════════════════════════════════════ -->
    <div class="app-main-content min-h-screen flex flex-col">

      <!-- ── Topbar ──────────────────────────────────────────── -->
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

          <!-- ── Campana notifiche ──────────────────────────── -->
          <div class="relative">
            <button
              @click="getNotifiche"
              class="relative p-2 rounded-lg text-ink-500 hover:text-ink-800 hover:bg-ink-50 transition-colors"
            >
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none"
                stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/>
                <path d="M13.73 21a2 2 0 0 1-3.46 0"/>
              </svg>
              <span v-if="nIssues > 0"
                    class="absolute top-1 right-1 w-4 h-4 rounded-full bg-red-500
                           text-white text-[9px] font-mono font-bold
                           flex items-center justify-center">
                {{ nIssues }}
              </span>
            </button>

            <!-- Dropdown notifiche -->
            <transition name="fade-drop">
              <div
                v-if="popupNotificheAperto"
                class="absolute right-0 top-full mt-2 w-72 bg-white rounded-xl
                       border border-ink-100 shadow-xl z-50 overflow-hidden"
              >
                <!-- Header dropdown -->
                <div class="flex items-center justify-between px-4 py-3 border-b border-ink-100">
                  <span class="text-[10px] font-mono font-semibold text-ink-500 uppercase tracking-wider">
                    Notifiche
                  </span>
                  <button
                    @click="popupNotificheAperto = false"
                    class="text-ink-300 hover:text-ink-600 transition-colors"
                  >
                    <svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none"
                         stroke="currentColor" stroke-width="2.5"
                         stroke-linecap="round" stroke-linejoin="round">
                      <line x1="18" y1="6" x2="6" y2="18"/>
                      <line x1="6" y1="6" x2="18" y2="18"/>
                    </svg>
                  </button>
                </div>

                <!-- Corpo scrollabile -->
                <div class="max-h-72 overflow-y-auto divide-y divide-ink-50">

                  <!-- Caricamento -->
                  <div v-if="caricamentoNotifiche" class="flex items-center justify-center py-8">
                    <svg class="w-5 h-5 animate-spin text-ink-300" viewBox="0 0 24 24"
                         fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M21 12a9 9 0 1 1-6.219-8.56"/>
                    </svg>
                  </div>

                  <!-- Empty state -->
                  <div
                    v-else-if="notifiche.length === 0"
                    class="flex flex-col items-center gap-2 py-8 text-ink-300"
                  >
                    <svg class="w-7 h-7" viewBox="0 0 24 24" fill="none"
                         stroke="currentColor" stroke-width="1.5"
                         stroke-linecap="round" stroke-linejoin="round">
                      <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/>
                      <path d="M13.73 21a2 2 0 0 1-3.46 0"/>
                    </svg>
                    <p class="text-xs">Nessuna notifica</p>
                  </div>

                  <!-- Lista notifiche -->
                  <div
                    v-else
                    v-for="n in notificheOrdinate"
                    :key="n.id"
                    @click="apriDettaglioNotifica(n)"
                    class="flex items-start gap-3 px-4 py-3 hover:bg-ink-50 transition-colors cursor-pointer"
                  >
                    <div class="w-1.5 h-1.5 rounded-full bg-brand-500 mt-1.5 flex-shrink-0"></div>
                    <span class="text-sm text-ink-700 leading-snug">{{formattaDataOra(n.dataCreazione)}} : {{ n.messaggio }}</span>
                    <svg class="w-3.5 h-3.5 text-ink-200 ml-auto mt-0.5 flex-shrink-0"
                         viewBox="0 0 24 24" fill="none" stroke="currentColor"
                         stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                      <polyline points="9 18 15 12 9 6"/>
                    </svg>
                  </div>

                </div>
              </div>
            </transition>
          </div>
          <!-- ── Fine campana notifiche ─────────────────────── -->

          <div class="w-px h-5 bg-ink-200 mx-1"></div>

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


      <!-- ── Contenuto principale ────────────────────────────── -->
      <main class="flex-1 p-4 sm:p-6 lg:p-8 space-y-6 sm:space-y-8">

        <!-- Intestazione sezione + bottone nuova issue -->
        <div v-if="!soloIssue" class="flex flex-col sm:flex-row sm:items-start justify-between gap-4">
          <div>
            <h2 class="font-display text-ink-900 text-xl font-bold">Panoramica progetto</h2>
            <p class="text-ink-400 text-sm mt-0.5">Situazione attuale delle issue.</p>
          </div>
          <button
            v-if="puoCreareIssue"
            @click="apriPopupSegnalazione"
            class="flex items-center justify-center gap-2 px-4 py-2.5 rounded-lg w-full sm:w-auto
                   bg-brand-500 hover:bg-brand-600 text-white text-sm font-semibold
                   transition-colors duration-150 flex-shrink-0 active:scale-[0.98]"
          >
            <svg class="w-4 h-4" viewBox="0 0 24 24" fill="none"
              stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
              <line x1="12" y1="5" x2="12" y2="19"/>
              <line x1="5" y1="12" x2="19" y2="12"/>
            </svg>
            Segnala un problema
          </button>
        </div>


        <!-- ── Stat cards ────────────────────────────────────── -->
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

          <StatCard label="In scadenza" :value="statProssimeScadenza" subtitle="scadono entro 2 giorni"
                    icon-bg-class="bg-amber-50" value-color-class="text-amber-500" anim-delay-class="delay-3">
            <template #icon>
              <svg class="w-3.5 h-3.5 text-amber-400" viewBox="0 0 24 24" fill="none"
                stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="12" cy="12" r="10"/>
                <polyline points="12 6 12 12 16 14"/>
              </svg>
            </template>
          </StatCard>

          <StatCard label="Completate" :value="statDone" subtitle="issue risolte"
                    icon-bg-class="bg-green-50" value-color-class="text-green-600" anim-delay-class="delay-4">
            <template #icon>
              <svg class="w-3.5 h-3.5 text-green-500" viewBox="0 0 24 24" fill="none"
                stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <polyline points="20 6 9 17 4 12"/>
              </svg>
            </template>
          </StatCard>

        </div>


        <!-- ── Sezione tabella issue ───────────────────────────── -->
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

              <!-- Ricerca per titolo -->
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

              <!-- Ricerca per ID -->
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

              <select v-model="filtroTipo"
                      class="px-2.5 py-1.5 text-sm rounded-lg border border-ink-200
                             bg-white text-ink-600 cursor-pointer
                             focus:border-brand-500 focus:ring-2 focus:ring-brand-500/20
                             transition-colors w-full sm:w-auto">
                <option value="">Tutti i tipi</option>
                <option value="BUG">Bug</option>
                <option value="FEATURE">Feature</option>
                <option value="QUESTION">Question</option>
                <option value="DOCUMENTATION">Docs</option>
              </select>

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

          <!-- Tabella -->
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

                  <!-- Empty state -->
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

                  <!-- Righe issue -->
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

            <!-- Footer tabella -->
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


    <!-- ═══════════════════════════════════════════════════════════
         POPUP NUOVA SEGNALAZIONE
         ═══════════════════════════════════════════════════════════ -->
    <div
      v-if="popupSegnalazioneAperto"
      @click="chiudiPopupSegnalazione"
      class="fixed inset-0 z-50 bg-ink-900/50 flex items-end sm:items-center justify-center p-0 sm:p-4"
    >
      <div
        @click.stop
        class="w-full max-w-lg h-full sm:h-auto sm:max-h-[85vh] rounded-none sm:rounded-xl
               bg-white border border-ink-100 shadow-xl p-5 space-y-4 overflow-y-auto"
      >
        <h3 class="font-display text-lg font-semibold text-ink-900">Segnala un problema</h3>

        <!-- Titolo -->
        <div class="space-y-1">
          <label class="text-xs font-medium text-ink-500 uppercase tracking-wide">Titolo *</label>
          <input
            v-model="segnalazione.titolo"
            type="text"
            placeholder="Descrivi brevemente il problema…"
            class="w-full rounded-lg border border-ink-200 px-3 py-2 text-sm text-ink-800
                   placeholder-ink-300 focus:border-brand-500 focus:ring-2 focus:ring-brand-500/20"
          />
        </div>

        <!-- Descrizione -->
        <div class="space-y-1">
          <label class="text-xs font-medium text-ink-500 uppercase tracking-wide">Descrizione</label>
          <textarea
            v-model="segnalazione.descrizione"
            rows="4"
            placeholder="Aggiungi dettagli, passi per riprodurre, screenshot…"
            class="w-full rounded-lg border border-ink-200 px-3 py-2 text-sm text-ink-800
                   placeholder-ink-300 focus:border-brand-500 focus:ring-2 focus:ring-brand-500/20
                   resize-none"
          ></textarea>
        </div>
        <!-- Immagine -->
        <div class="space-y-1">
          <label class="text-xs font-medium text-ink-500 uppercase tracking-wide">
            Immagine
          </label>

          <input
            type="file"
            accept="image/*"
            @change="handleImageUpload"
            class="w-full rounded-lg border border-ink-200 px-3 py-2 text-sm text-ink-800
                  file:mr-4 file:rounded-md file:border-0
                  file:bg-brand-500 file:px-4 file:py-2
                  file:text-white hover:file:bg-brand-600
                  focus:border-brand-500 focus:ring-2 focus:ring-brand-500/20"
          />
        </div>
        <!-- Etichette -->
        <div class="space-y-1.5">
          <label class="text-xs font-medium text-ink-500 uppercase tracking-wide">Etichette</label>
          <div class="flex gap-2">
            <input
              v-model="nuovaEtichetta"
              type="text"
              placeholder="Aggiungi etichetta…"
              @keyup.enter="aggiungiEtichetta"
              class="flex-1 rounded-lg border border-ink-200 px-3 py-2 text-sm text-ink-800
                     placeholder-ink-300 focus:border-brand-500 focus:ring-2 focus:ring-brand-500/20"
            />
            <button
              type="button"
              @click="aggiungiEtichetta"
              class="px-3 py-2 rounded-lg border border-ink-200 bg-ink-50 hover:bg-ink-100
                     text-ink-700 font-bold text-base transition-colors"
            >+</button>
          </div>
          <div v-if="segnalazione.etichette.length" class="flex flex-wrap gap-1.5 pt-1">
            <span
              v-for="(e, i) in segnalazione.etichette" :key="i"
              class="flex items-center gap-1 px-2 py-0.5 text-[11px] font-mono rounded
                     bg-ink-100 text-ink-600 border border-ink-200"
            >
              {{ e }}
              <button type="button" @click="rimuoviEtichetta(i)"
                      class="text-ink-400 hover:text-red-500 leading-none ml-0.5">×</button>
            </span>
          </div>
        </div>

        <!-- Tipo + Priorità -->
        <div class="grid grid-cols-2 gap-3">
          <div class="space-y-1">
            <label class="text-xs font-medium text-ink-500 uppercase tracking-wide">Tipo *</label>
            <select
              v-model="segnalazione.tipo"
              class="w-full rounded-lg border border-ink-200 px-3 py-2 text-sm text-ink-700
                     focus:border-brand-500 focus:ring-2 focus:ring-brand-500/20 bg-white"
            >
              <option value="">Seleziona…</option>
              <option value="bug">Bug</option>
              <option value="feature">Feature</option>
              <option value="question">Question</option>
              <option value="documentation">Documentation</option>
            </select>
          </div>

          <div class="space-y-1">
            <label class="text-xs font-medium text-ink-500 uppercase tracking-wide">Priorità *</label>
            <select
              v-model="segnalazione.priorita"
              class="w-full rounded-lg border border-ink-200 px-3 py-2 text-sm text-ink-700
                     focus:border-brand-500 focus:ring-2 focus:ring-brand-500/20 bg-white"
            >
              <option value="">Seleziona…</option>
              <option value="0">Minimal</option>
              <option value="1">Low</option>
              <option value="2">Medium</option>
              <option value="3">High</option>
              <option value="4">Critical</option>
            </select>
          </div>
        </div>

        <!-- Errore -->
        <p v-if="erroreInvio" class="mb-5 px-4 py-3 rounded-lg bg-red-50 border border-red-200 text-red-600 text-sm">{{ erroreInvio }}</p>
        <p v-if="confermaInvio" class="mb-5 px-4 py-3 rounded-lg bg-emerald-50 border border-emerald-100 text-emerald-700 text-sm">{{ confermaInvio }}</p>

        <!-- Azioni -->
        <div class="flex flex-col-reverse sm:flex-row justify-end gap-2 pt-1">
          <button
            type="button"
            @click="chiudiPopupSegnalazione"
            :disabled="invioInCorso"
            class="px-3 py-2 rounded-lg text-sm font-medium text-ink-600
                   hover:bg-ink-50 transition-colors w-full sm:w-auto disabled:opacity-50"
          >
            Annulla
          </button>
          <button
            type="button"
            @click="submitSegnalazione"
            :disabled="invioInCorso"
            class="flex items-center justify-center gap-2 px-4 py-2 rounded-lg text-sm font-semibold
                   text-white bg-brand-500 hover:bg-brand-600 transition-colors w-full sm:w-auto
                   disabled:opacity-60 disabled:cursor-not-allowed"
          >
            <svg v-if="invioInCorso" class="w-4 h-4 animate-spin" viewBox="0 0 24 24"
                 fill="none" stroke="currentColor" stroke-width="2">
              <path d="M21 12a9 9 0 1 1-6.219-8.56"/>
            </svg>
            {{ invioInCorso ? 'Invio…' : 'Invia segnalazione' }}
          </button>
        </div>
      </div>
    </div>


    <!-- ═══════════════════════════════════════════════════════════
         POPUP DETTAGLIO NOTIFICA
         La notifica viene segnata come letta dal backend 
         quindi qui mostriamo solo i dati e un bottone di chiusura.
         ═══════════════════════════════════════════════════════════ -->
    <div
      v-if="notificaSelezionata"
      @click.self="chiudiDettaglioNotifica"
      class="fixed inset-0 z-[60] bg-ink-900/50 flex items-end sm:items-center justify-center p-0 sm:p-4"
    >
      <div
        class="w-full max-w-lg h-full sm:h-auto sm:max-h-[85vh] rounded-none sm:rounded-xl
               bg-white border border-ink-100 shadow-xl flex flex-col overflow-hidden"
      >
        <!-- Header -->
        <div class="flex items-center justify-between px-5 py-4 border-b border-ink-100 flex-shrink-0">
          <div class="flex items-center gap-2">
            <div class="w-2 h-2 rounded-full bg-brand-500"></div>
            <h3 class="font-display text-base font-semibold text-ink-900">
              Dettaglio notifica
            </h3>
          </div>
          <button
            @click="chiudiDettaglioNotifica"
            class="p-1.5 rounded-lg text-ink-300 hover:text-ink-600 hover:bg-ink-50 transition-colors"
          >
            <svg class="w-4 h-4" viewBox="0 0 24 24" fill="none"
                 stroke="currentColor" stroke-width="2.5"
                 stroke-linecap="round" stroke-linejoin="round">
              <line x1="18" y1="6" x2="6" y2="18"/>
              <line x1="6" y1="6" x2="18" y2="18"/>
            </svg>
          </button>
        </div>

        <!-- Corpo scrollabile -->
        <div class="flex-1 overflow-y-auto p-5 space-y-5">

          <!-- Titolo notifica -->
          <div class="space-y-1">
            <span class="text-[10px] font-mono text-ink-400 uppercase tracking-wider">Notifica</span>
            <p class="text-sm font-medium text-ink-800">{{ notificaSelezionata.titolo }}</p>
          </div>

          <!-- Caricamento issue -->
          <div v-if="caricamentoDettaglio" class="flex items-center justify-center py-10">
            <svg class="w-6 h-6 animate-spin text-ink-300" viewBox="0 0 24 24"
                 fill="none" stroke="currentColor" stroke-width="2">
              <path d="M21 12a9 9 0 1 1-6.219-8.56"/>
            </svg>
          </div>

          <!-- Errore caricamento -->
          <div v-else-if="erroreDettaglio"
               class="rounded-xl border border-red-100 bg-red-50 px-4 py-4 text-center">
            <p class="text-sm text-red-500">{{ erroreDettaglio }}</p>
          </div>

          <!-- Dati issue associata -->
          <div
            v-else-if="issueNotifica"
            class="rounded-xl border border-ink-100 bg-ink-50/50 divide-y divide-ink-100"
          >
            <!-- Sub-header issue -->
            <div class="px-4 py-2.5 flex items-center justify-between">
              <span class="text-[10px] font-mono text-ink-400 uppercase tracking-wider">Issue associata</span>
              <span class="font-mono text-[11px] text-ink-400">#{{ issueNotifica.id }}</span>
            </div>

            <div class="px-4 py-3 space-y-3">

              <div class="space-y-0.5">
                <span class="text-[10px] font-mono text-ink-400 uppercase tracking-wider">Titolo</span>
                <p class="text-sm text-ink-800 font-medium">{{ issueNotifica.titolo }}</p>
              </div>

              <div v-if="issueNotifica.descrizione" class="space-y-0.5">
                <span class="text-[10px] font-mono text-ink-400 uppercase tracking-wider">Descrizione</span>
                <p class="text-sm text-ink-600 leading-relaxed">{{ issueNotifica.descrizione }}</p>
              </div>

              <div class="grid grid-cols-3 gap-3">
                <div class="space-y-1">
                  <span class="text-[10px] font-mono text-ink-400 uppercase tracking-wider">Tipo</span>
                  <BadgeTipo :tipo="issueNotifica.tipo" />
                </div>
                <div class="space-y-1">
                  <span class="text-[10px] font-mono text-ink-400 uppercase tracking-wider">Priorità</span>
                  <BadgePriorita :priorita="issueNotifica.priorita" />
                </div>
                <div class="space-y-1">
                  <span class="text-[10px] font-mono text-ink-400 uppercase tracking-wider">Stato</span>
                  <BadgeStato :stato="issueNotifica.stato" />
                </div>
              </div>

              <div v-if="issueNotifica.assegnatoDa" class="space-y-0.5">
                <span class="text-[10px] font-mono text-ink-400 uppercase tracking-wider">Assegnata da</span>
                <div class="flex items-center gap-2 mt-1">
                  <div class="w-5 h-5 rounded-full bg-brand-500/20 flex items-center justify-center flex-shrink-0">
                    <span class="text-[9px] font-mono text-brand-700 font-medium">
                      {{ inizialiDa(issueNotifica.assegnatoDa) }}
                    </span>
                  </div>
                  <span class="text-sm text-ink-600">{{ issueNotifica.assegnatoDa }}</span>
                </div>
              </div>
              <div v-if="issueNotifica.immagineContentType" class="space-y-1">
                <span class="text-[10px] font-mono text-ink-400 uppercase tracking-wider block">Immagine allegata</span>
                <img 
                  :src="`/api/issues/${issueNotifica.id}/immagine`" 
                  alt="Screenshot allegato" 
                  class="w-full h-auto max-h-64 object-contain rounded-lg border border-ink-100 bg-ink-900/5 shadow-sm"
                />
              </div>
              <div v-if="issueNotifica.dataScadenza" class="space-y-0.5">
                <span class="text-[10px] font-mono text-ink-400 uppercase tracking-wider">Scadenza</span>
                <p class="font-mono text-[12px]"
                   :class="isScaduta(issueNotifica) ? 'text-red-500 font-medium' : 'text-ink-500'">
                  {{ formattaData(issueNotifica.dataScadenza) }}
                </p>
              </div>

            </div>
          </div>

          <!-- Fallback: /apri ha ritornato null -->
          <div v-else class="rounded-xl border border-ink-100 bg-ink-50/50 px-4 py-6 text-center">
            <p class="text-sm text-ink-300">Nessuna issue associata a questa notifica.</p>
          </div>

        </div>

        <!-- Footer -->
        <div class="flex-shrink-0 border-t border-ink-100 px-5 py-4 flex items-center justify-between">
          <!-- Badge "già letta" — appare non appena il caricamento finisce -->
          <span v-if="!caricamentoDettaglio && !erroreDettaglio"
                class="flex items-center gap-1.5 text-xs text-emerald-600 font-medium">
            <svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none"
                 stroke="currentColor" stroke-width="2.5"
                 stroke-linecap="round" stroke-linejoin="round">
              <polyline points="20 6 9 17 4 12"/>
            </svg>
            Segnata come letta
          </span>
          <span v-else class="flex-1"></span>

          <button
            @click="chiudiDettaglioNotifica"
            class="px-4 py-2 rounded-lg text-sm font-semibold text-white
                   bg-brand-500 hover:bg-brand-600 transition-colors"
          >
            Chiudi
          </button>
        </div>
      </div>
    </div>
    <IssueDetailPanel
      :issue="issueDettaglio"
      :caricamento="caricamentoDettaglio"
      :session-id="utente.sessionId"
      :ruolo="utente.ruolo"
      @close="issueDettaglio = null"
      @chiusa="onIssueChiusa"
    />

  </div>
</template>

<style scoped>
.fade-drop-enter-active,
.fade-drop-leave-active {
  transition: opacity 0.15s ease, transform 0.15s ease;
}
.fade-drop-enter-from,
.fade-drop-leave-to {
  opacity: 0;
  transform: translateY(-6px);
}
</style>