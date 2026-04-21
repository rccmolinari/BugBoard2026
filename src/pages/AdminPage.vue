<!--
  AdminPage.vue
  Porting di admin.html + admin.js.

  Logica **identica** all'originale:
    - Auth guard: se non sei loggato vai al login, se non sei admin vai alla dashboard
    - Store utenti su localStorage (chiave 'bb_utenti') con seed di UTENTI_DEFAULT
    - nuovoId() calcola il prossimo ID
    - creaUtente(): stesse validazioni (nome, email con @, password >= 6, no duplicati)
    - eliminaUtente(): window.confirm + filter + salvataggio
    - mostraMessaggio/nascondiMessaggio: setTimeout di 4000ms per i successi
    - togglePasswordForm alterna type tra password/text
    - formattaData, coloreAvatar: identiche

  Rispetto al vecchio file:
    - Le scritture su localStorage restano, ma lo stato reattivo (`utenti`) viene
      aggiornato in parallelo così la UI si rinfresca da sola — nessuna chiamata
      manuale tipo renderUtenti()/aggiornaStats() in giro per le funzioni.
-->
<script setup>
import { ref, computed } from 'vue'
import Sidebar from '../components/Sidebar.vue'
import StatCard from '../components/StatCard.vue'
import BadgeRuolo from '../components/BadgeRuolo.vue'


/* ══════════════════════════════════════════════════════════════
   AUTH GUARD — solo gli amministratori possono vedere la pagina.
   ══════════════════════════════════════════════════════════════ */
function getUtente() {
  const raw = sessionStorage.getItem('bb_utente')
  return raw ? JSON.parse(raw) : null
}

const utente = getUtente()

if (!utente) {
  window.location.href = 'login.html'
} else if (utente.ruolo !== 'admin') {
  window.location.href = 'dashboard.html'
}


/* ══════════════════════════════════════════════════════════════
   STORE UTENTI (localStorage) — stesso meccanismo dell'originale
   ══════════════════════════════════════════════════════════════ */
const STORAGE_KEY = 'bb_utenti'

const UTENTI_DEFAULT = [
  { id: 1, nome: 'Admin',    email: 'admin@bugboard.io', password: 'admin123', ruolo: 'admin',    default: true },
  { id: 2, nome: 'Dev User', email: 'dev@bugboard.io',   password: 'dev123',   ruolo: 'normal',   default: false },
  { id: 3, nome: 'Guest',    email: 'guest@bugboard.io', password: 'guest123', ruolo: 'readonly', default: false },
]

function getUtenti() {
  const raw = localStorage.getItem(STORAGE_KEY)
  if (!raw) {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(UTENTI_DEFAULT))
    return UTENTI_DEFAULT
  }
  return JSON.parse(raw)
}

function salvaUtenti(lista) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(lista))
}

function nuovoId() {
  const lista = utenti.value
  return lista.length ? Math.max(...lista.map(u => u.id)) + 1 : 1
}


/* ══════════════════════════════════════════════════════════════
   STATE REATTIVO
   ══════════════════════════════════════════════════════════════ */
const utenti = ref(getUtenti())

// Filtri tabella
const cerca = ref('')
const filtroRuolo = ref('')

// Form nuovo utente
const inputNome = ref('')
const inputEmail = ref('')
const inputPassword = ref('')
const inputRuolo = ref('normal')
const showPasswordForm = ref(false)

// Messaggio di feedback del form
const messaggioForm = ref({ testo: '', tipo: '', visibile: false })

// Sidebar mobile
const sidebarAperta = ref(false)


/* ══════════════════════════════════════════════════════════════
   COMPUTED
   ══════════════════════════════════════════════════════════════ */
const iniziali = computed(() => {
  if (!utente?.nome) return '?'
  return utente.nome
    .split(' ')
    .map(p => p[0])
    .join('')
    .toUpperCase()
    .slice(0, 2)
})

// Stats
const statTotale   = computed(() => utenti.value.length)
const statAdmin    = computed(() => utenti.value.filter(u => u.ruolo === 'admin').length)
const statNormal   = computed(() => utenti.value.filter(u => u.ruolo === 'normal').length)
const statReadonly = computed(() => utenti.value.filter(u => u.ruolo === 'readonly').length)

// Lista filtrata (sostituisce renderUtenti())
const utentiFiltrati = computed(() => {
  const q = cerca.value.toLowerCase()
  let lista = utenti.value
  if (q) {
    lista = lista.filter(u =>
      u.nome.toLowerCase().includes(q) ||
      u.email.toLowerCase().includes(q)
    )
  }
  if (filtroRuolo.value) {
    lista = lista.filter(u => u.ruolo === filtroRuolo.value)
  }
  return lista
})

const etichettaConteggio = computed(() => {
  const n = utentiFiltrati.value.length
  return `${n} ${n === 1 ? 'utente trovato' : 'utenti trovati'}`
})

const classeMessaggio = computed(() => {
  const stili = {
    successo: 'bg-green-50 border-green-200 text-green-700',
    errore:   'bg-red-50 border-red-200 text-red-600',
  }
  return `mt-4 px-4 py-3 rounded-lg border text-sm ${stili[messaggioForm.value.tipo] || ''}`
})


/* ══════════════════════════════════════════════════════════════
   CREA UTENTE — stesse validazioni dell'originale
   ══════════════════════════════════════════════════════════════ */
function creaUtente() {
  const nome     = inputNome.value.trim()
  const email    = inputEmail.value.trim().toLowerCase()
  const password = inputPassword.value
  const ruolo    = inputRuolo.value

  nascondiMessaggio()

  if (!nome) { mostraMessaggio('Il nome è obbligatorio.', 'errore'); return }
  if (!email || !email.includes('@')) { mostraMessaggio('Inserisci un\'email valida.', 'errore'); return }
  if (!password || password.length < 6) { mostraMessaggio('La password deve avere almeno 6 caratteri.', 'errore'); return }

  if (utenti.value.find(u => u.email === email)) {
    mostraMessaggio('Esiste già un utente con questa email.', 'errore')
    return
  }

  const nuovo = { id: nuovoId(), nome, email, password, ruolo, default: false }
  const aggiornati = [...utenti.value, nuovo]
  salvaUtenti(aggiornati)
  utenti.value = aggiornati

  mostraMessaggio(`Utente "${nome}" creato con successo.`, 'successo')
  resetForm()
}


/* ══════════════════════════════════════════════════════════════
   ELIMINA UTENTE — identico all'originale (window.confirm)
   ══════════════════════════════════════════════════════════════ */
function eliminaUtente(id, nome) {
  if (!confirm(`Eliminare l'utente "${nome}"?\nQuesta azione non può essere annullata.`)) return
  const aggiornati = utenti.value.filter(u => u.id !== id)
  salvaUtenti(aggiornati)
  utenti.value = aggiornati
}


/* ══════════════════════════════════════════════════════════════
   TOGGLE VISIBILITÀ PASSWORD
   ══════════════════════════════════════════════════════════════ */
function togglePasswordForm() {
  showPasswordForm.value = !showPasswordForm.value
}


/* ── Messaggi di feedback ────────────────────────────────────── */
function mostraMessaggio(testo, tipo) {
  messaggioForm.value = { testo, tipo, visibile: true }
  if (tipo === 'successo') setTimeout(nascondiMessaggio, 4000)
}
function nascondiMessaggio() {
  messaggioForm.value = { ...messaggioForm.value, visibile: false }
}
function resetForm() {
  inputNome.value = ''
  inputEmail.value = ''
  inputPassword.value = ''
  inputRuolo.value = 'normal'
}


/* ── Utility ──────────────────────────────────────────────────── */
function formattaData(iso) {
  return new Date(iso).toLocaleDateString('it-IT', {
    day: '2-digit', month: 'short', year: 'numeric',
  })
}

function coloreAvatar(ruolo) {
  return { admin: 'bg-brand-500', normal: 'bg-blue-400', readonly: 'bg-ink-400' }[ruolo] || 'bg-ink-400'
}

function inizialiDa(nome) {
  return nome.split(' ').map(p => p[0]).join('').toUpperCase().slice(0, 2)
}

function dataCreazione(u) {
  return u.default ? '01 gen 2025' : formattaData(new Date().toISOString())
}


/* ── Sidebar mobile + Logout ─────────────────────────────────── */
function aprireSidebar()  { sidebarAperta.value = true  }
function chiudiSidebar() { sidebarAperta.value = false }

function logout() {
  sessionStorage.removeItem('bb_utente')
  window.location.href = 'login.html'
}
</script>

<template>
  <div v-if="utente && utente.ruolo === 'admin'">

    <!-- Sidebar condivisa (pagina = admin) -->
    <Sidebar pagina="admin" :utente="utente" :is-open="sidebarAperta" @logout="logout" />

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

      <!-- Topbar -->
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
          <h1 class="font-display text-ink-900 text-[15px] font-semibold">Amministrazione</h1>
          <span class="text-ink-300 text-sm hidden sm:inline">/</span>
          <span class="text-ink-400 text-sm hidden sm:inline">Gestione utenti</span>
        </div>

        <div class="flex items-center gap-2">
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


      <!-- Contenuto principale -->
      <main class="flex-1 p-6 lg:p-8 space-y-8">

        <!-- Intestazione -->
        <div>
          <h2 class="font-display text-ink-900 text-xl font-bold">Gestione utenti</h2>
          <p class="text-ink-400 text-sm mt-0.5">
            Crea, visualizza ed elimina gli account della piattaforma.
          </p>
        </div>


        <!-- ── Stat cards ─────────────────────────────────────── -->
        <div class="grid grid-cols-2 lg:grid-cols-4 gap-4">

          <StatCard label="Totale" :value="statTotale" subtitle="utenti registrati"
                    icon-bg-class="bg-ink-50" value-color-class="text-ink-900" anim-delay-class="delay-1">
            <template #icon>
              <svg class="w-3.5 h-3.5 text-ink-400" viewBox="0 0 24 24" fill="none"
                stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
                <circle cx="9" cy="7" r="4"/>
                <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
                <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
              </svg>
            </template>
          </StatCard>

          <StatCard label="Admin" :value="statAdmin" subtitle="con accesso completo"
                    icon-bg-class="bg-brand-50" value-color-class="text-brand-600" anim-delay-class="delay-2">
            <template #icon>
              <svg class="w-3.5 h-3.5 text-brand-500" viewBox="0 0 24 24" fill="none"
                stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>
              </svg>
            </template>
          </StatCard>

          <StatCard label="Normali" :value="statNormal" subtitle="utenti del team"
                    icon-bg-class="bg-blue-50" value-color-class="text-blue-500" anim-delay-class="delay-3">
            <template #icon>
              <svg class="w-3.5 h-3.5 text-blue-400" viewBox="0 0 24 24" fill="none"
                stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                <circle cx="12" cy="7" r="4"/>
              </svg>
            </template>
          </StatCard>

          <StatCard label="Readonly" :value="statReadonly" subtitle="osservatori esterni"
                    icon-bg-class="bg-ink-100" value-color-class="text-ink-500" anim-delay-class="delay-4">
            <template #icon>
              <svg class="w-3.5 h-3.5 text-ink-400" viewBox="0 0 24 24" fill="none"
                stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
                <circle cx="12" cy="12" r="3"/>
              </svg>
            </template>
          </StatCard>

        </div>


        <!-- ── Layout a due colonne: tabella + form ─────────── -->
        <div class="anim-fade-in delay-5 grid grid-cols-1 xl:grid-cols-[1fr_340px] gap-6 items-start">


          <!-- ── Tabella utenti ──────────────────────────────── -->
          <div>
            <!-- Header + filtri -->
            <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-3 mb-4">
              <div>
                <h3 class="font-display text-ink-900 text-base font-semibold">Utenti del sistema</h3>
                <p class="text-ink-400 text-xs mt-0.5">{{ etichettaConteggio }}</p>
              </div>

              <div class="flex flex-wrap items-center gap-2">
                <div class="relative">
                  <svg class="absolute left-2.5 top-1/2 -translate-y-1/2 w-3.5 h-3.5 text-ink-300 pointer-events-none"
                       viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                       stroke-linecap="round" stroke-linejoin="round">
                    <circle cx="11" cy="11" r="8"/>
                    <line x1="21" y1="21" x2="16.65" y2="16.65"/>
                  </svg>
                  <input v-model="cerca"
                         type="search"
                         placeholder="Cerca per nome o email…"
                         class="pl-8 pr-3 py-1.5 text-sm rounded-lg border border-ink-200
                                bg-white text-ink-800 placeholder-ink-300
                                focus:border-brand-500 focus:ring-2 focus:ring-brand-500/20
                                w-52 transition-colors" />
                </div>

                <select v-model="filtroRuolo"
                        class="px-2.5 py-1.5 text-sm rounded-lg border border-ink-200
                               bg-white text-ink-600 cursor-pointer
                               focus:border-brand-500 focus:ring-2 focus:ring-brand-500/20
                               transition-colors">
                  <option value="">Tutti i ruoli</option>
                  <option value="admin">Admin</option>
                  <option value="normal">Normale</option>
                  <option value="readonly">Readonly</option>
                </select>
              </div>
            </div>

            <!-- Tabella -->
            <div class="bg-white rounded-xl border border-ink-100 shadow-sm overflow-hidden">
              <div class="overflow-x-auto">
                <table class="w-full text-sm">
                  <thead>
                    <tr class="border-b border-ink-100">
                      <th class="text-left px-5 py-3 text-[10px] font-mono text-ink-400 uppercase tracking-wider">Utente</th>
                      <th class="text-left px-4 py-3 text-[10px] font-mono text-ink-400 uppercase tracking-wider">Email</th>
                      <th class="text-left px-4 py-3 text-[10px] font-mono text-ink-400 uppercase tracking-wider whitespace-nowrap">Ruolo</th>
                      <th class="text-left px-4 py-3 text-[10px] font-mono text-ink-400 uppercase tracking-wider whitespace-nowrap hidden lg:table-cell">Creato il</th>
                      <th class="px-4 py-3"></th>
                    </tr>
                  </thead>
                  <tbody class="divide-y divide-ink-50">

                    <!-- Empty state -->
                    <tr v-if="utentiFiltrati.length === 0">
                      <td colspan="5" class="px-5 py-12 text-center">
                        <div class="flex flex-col items-center gap-2 text-ink-300">
                          <svg class="w-8 h-8" viewBox="0 0 24 24" fill="none"
                            stroke="currentColor" stroke-width="1.5"
                            stroke-linecap="round" stroke-linejoin="round">
                            <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
                            <circle cx="9" cy="7" r="4"/>
                            <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
                            <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
                          </svg>
                          <p class="text-sm">Nessun utente corrisponde ai filtri</p>
                        </div>
                      </td>
                    </tr>

                    <!-- Righe utenti -->
                    <tr v-for="u in utentiFiltrati" :key="u.id"
                        class="transition-colors hover:bg-ink-50/60 group">

                      <!-- Avatar + nome -->
                      <td class="px-5 py-4">
                        <div class="flex items-center gap-3">
                          <div class="w-8 h-8 rounded-full flex items-center justify-center flex-shrink-0"
                               :class="coloreAvatar(u.ruolo)">
                            <span class="font-mono text-[11px] font-medium text-white">
                              {{ inizialiDa(u.nome) }}
                            </span>
                          </div>
                          <div>
                            <p class="text-sm font-medium text-ink-800">{{ u.nome }}</p>
                            <span v-if="u.default" class="text-[10px] font-mono text-ink-400">
                              account di sistema
                            </span>
                          </div>
                        </div>
                      </td>

                      <!-- Email -->
                      <td class="px-4 py-4">
                        <span class="font-mono text-[12px] text-ink-500">{{ u.email }}</span>
                      </td>

                      <!-- Badge ruolo -->
                      <td class="px-4 py-4">
                        <BadgeRuolo :ruolo="u.ruolo" />
                      </td>

                      <!-- Data creazione (mock) -->
                      <td class="px-4 py-4 hidden lg:table-cell">
                        <span class="font-mono text-[12px] text-ink-400">
                          {{ dataCreazione(u) }}
                        </span>
                      </td>

                      <!-- Azioni -->
                      <td class="px-4 py-4 text-right">
                        <span v-if="u.default" class="text-[11px] font-mono text-ink-300 pr-1">protetto</span>
                        <button
                          v-else
                          @click="eliminaUtente(u.id, u.nome)"
                          class="opacity-0 group-hover:opacity-100 transition-opacity
                                 inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg
                                 text-xs font-medium text-red-600
                                 hover:bg-red-50 border border-transparent hover:border-red-100"
                        >
                          <svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none"
                            stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                            <polyline points="3 6 5 6 21 6"/>
                            <path d="M19 6l-1 14H6L5 6"/>
                            <path d="M10 11v6"/><path d="M14 11v6"/><path d="M9 6V4h6v2"/>
                          </svg>
                          Elimina
                        </button>
                      </td>

                    </tr>

                  </tbody>
                </table>
              </div>
            </div>
          </div>


          <!-- ── Form crea nuovo utente ──────────────────────── -->
          <div class="bg-white rounded-xl border border-ink-100 shadow-sm p-6">

            <div class="mb-5">
              <h3 class="font-display text-ink-900 text-base font-semibold">Crea nuovo utente</h3>
              <p class="text-ink-400 text-xs mt-0.5">
                Il nuovo utente potrà accedere subito con le credenziali impostate.
              </p>
            </div>

            <form @submit.prevent="creaUtente" novalidate>

              <!-- Nome -->
              <div class="mb-4">
                <label for="inputNome"
                       class="block text-xs font-mono font-medium text-ink-600 uppercase tracking-wider mb-2">
                  Nome
                </label>
                <input v-model="inputNome"
                       type="text"
                       id="inputNome"
                       placeholder="Mario Rossi"
                       autocomplete="off"
                       class="w-full px-4 py-2.5 rounded-lg border border-ink-200 bg-ink-50
                              text-ink-800 text-sm placeholder-ink-300
                              transition-colors duration-150
                              focus:border-brand-500 focus:bg-white focus:ring-2 focus:ring-brand-500/20" />
              </div>

              <!-- Email -->
              <div class="mb-4">
                <label for="inputEmail"
                       class="block text-xs font-mono font-medium text-ink-600 uppercase tracking-wider mb-2">
                  Email
                </label>
                <input v-model="inputEmail"
                       type="email"
                       id="inputEmail"
                       placeholder="mario@esempio.it"
                       autocomplete="off"
                       class="w-full px-4 py-2.5 rounded-lg border border-ink-200 bg-ink-50
                              text-ink-800 text-sm placeholder-ink-300
                              transition-colors duration-150
                              focus:border-brand-500 focus:bg-white focus:ring-2 focus:ring-brand-500/20" />
              </div>

              <!-- Password con toggle -->
              <div class="mb-4">
                <label for="inputPassword"
                       class="block text-xs font-mono font-medium text-ink-600 uppercase tracking-wider mb-2">
                  Password
                </label>
                <div class="relative">
                  <input v-model="inputPassword"
                         :type="showPasswordForm ? 'text' : 'password'"
                         id="inputPassword"
                         placeholder="Minimo 6 caratteri"
                         autocomplete="new-password"
                         class="w-full px-4 py-2.5 pr-11 rounded-lg border border-ink-200 bg-ink-50
                                text-ink-800 text-sm placeholder-ink-300
                                transition-colors duration-150
                                focus:border-brand-500 focus:bg-white focus:ring-2 focus:ring-brand-500/20" />
                  <button type="button"
                          @click="togglePasswordForm"
                          class="absolute right-3 top-1/2 -translate-y-1/2 text-ink-400 hover:text-ink-600 transition-colors">
                    <svg xmlns="http://www.w3.org/2000/svg"
                         width="16" height="16" viewBox="0 0 24 24"
                         fill="none" stroke="currentColor" stroke-width="2"
                         stroke-linecap="round" stroke-linejoin="round">
                      <template v-if="!showPasswordForm">
                        <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
                        <circle cx="12" cy="12" r="3"/>
                      </template>
                      <template v-else>
                        <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8
                                 a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4
                                 c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07
                                 a3 3 0 1 1-4.24-4.24"/>
                        <line x1="1" y1="1" x2="23" y2="23"/>
                      </template>
                    </svg>
                  </button>
                </div>
              </div>

              <!-- Ruolo -->
              <div class="mb-6">
                <label for="inputRuolo"
                       class="block text-xs font-mono font-medium text-ink-600 uppercase tracking-wider mb-2">
                  Ruolo
                </label>
                <select v-model="inputRuolo"
                        id="inputRuolo"
                        class="w-full px-4 py-2.5 rounded-lg border border-ink-200 bg-ink-50
                               text-ink-800 text-sm cursor-pointer
                               transition-colors duration-150
                               focus:border-brand-500 focus:bg-white focus:ring-2 focus:ring-brand-500/20">
                  <option value="normal">Normale — accesso standard</option>
                  <option value="admin">Admin — accesso completo</option>
                  <option value="readonly">Readonly — solo lettura</option>
                </select>
                <p class="text-xs text-ink-400 mt-2 leading-relaxed">
                  <strong class="text-ink-500">Normale:</strong> può creare e commentare issue.
                  <strong class="text-ink-500">Admin:</strong> gestisce utenti e assegna issue.
                  <strong class="text-ink-500">Readonly:</strong> solo visualizzazione.
                </p>
              </div>

              <!-- Messaggio di feedback -->
              <div v-show="messaggioForm.visibile" :class="classeMessaggio">
                {{ messaggioForm.testo }}
              </div>

              <!-- Submit -->
              <button type="submit"
                      class="w-full mt-2 py-2.5 px-6 rounded-lg
                             bg-brand-500 hover:bg-brand-600
                             text-white font-semibold text-sm
                             transition-colors duration-150 active:scale-[0.99]
                             flex items-center justify-center gap-2">
                <svg class="w-4 h-4" viewBox="0 0 24 24" fill="none"
                  stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/>
                  <circle cx="9" cy="7" r="4"/>
                  <line x1="19" y1="8" x2="19" y2="14"/>
                  <line x1="22" y1="11" x2="16" y2="11"/>
                </svg>
                Crea utente
              </button>

            </form>
          </div>

        </div><!-- fine grid a due colonne -->

      </main>
    </div>
  </div>
</template>
