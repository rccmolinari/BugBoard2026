<!--
  DashboardPage.vue
  Porting di dashboard.html + dashboard.js.

  Logica **identica** all'originale:
    - ISSUE_DEMO array immutato
    - Auth guard con sessionStorage + window.location.href = 'login.html'
    - Stats calcolate dagli stessi filtri
    - Tabella filtrata su titolo / tipo / stato
    - isScaduta(), formattaData(), apriIssue() con lo stesso comportamento
    - logout() svuota la sessionStorage e manda al login

  Rispetto al vecchio file:
    - innerHTML + getElementById → v-for e data binding reattivo
    - classList.add/remove('hidden') → ref booleane (sidebar mobile)
    - escapeHtml() non serve: Vue fa escape automatico nelle interpolazioni {{ }}
    - badge helpers → componenti <BadgeTipo>, <BadgeStato>, <BadgePriorita>
-->
<script setup>
import { ref, computed } from 'vue'
import Sidebar from '../components/Sidebar.vue'
import StatCard from '../components/StatCard.vue'
import BadgeTipo from '../components/BadgeTipo.vue'
import BadgeStato from '../components/BadgeStato.vue'
import BadgePriorita from '../components/BadgePriorita.vue'


/* ── Dati demo ────────────────────────────────────────────────
   Sostituire con GET /api/issues quando il backend è pronto.
   ─────────────────────────────────────────────────────────── */
const ISSUE_DEMO = [
  {
    id: 1,
    titolo:    'Login fallisce con caratteri speciali nella password',
    tipo:      'bug',
    stato:     'todo',
    priorita:  'critical',
    assegnata: 'Dev User',
    scadenza:  '2025-04-30',
    etichette: ['auth', 'sicurezza'],
  },
  {
    id: 2,
    titolo:    'Aggiungere dark mode all\'interfaccia utente',
    tipo:      'feature',
    stato:     'in-progress',
    priorita:  'medium',
    assegnata: null,
    scadenza:  null,
    etichette: ['frontend'],
  },
  {
    id: 3,
    titolo:    'Documentazione API mancante per endpoint /reports',
    tipo:      'documentation',
    stato:     'todo',
    priorita:  'low',
    assegnata: null,
    scadenza:  null,
    etichette: ['docs'],
  },
  {
    id: 4,
    titolo:    'Come funzionano i permessi per i ruoli annidati?',
    tipo:      'question',
    stato:     'done',
    priorita:  null,
    assegnata: 'Admin',
    scadenza:  null,
    etichette: ['sicurezza'],
  },
  {
    id: 5,
    titolo:    'Upload immagine causa crash su Safari mobile',
    tipo:      'bug',
    stato:     'todo',
    priorita:  'high',
    assegnata: 'Dev User',
    scadenza:  '2025-04-25',
    etichette: ['frontend', 'mobile'],
  },
  {
    id: 6,
    titolo:    'Notifiche email non vengono inviate correttamente',
    tipo:      'bug',
    stato:     'in-progress',
    priorita:  'high',
    assegnata: 'Dev User',
    scadenza:  '2025-05-05',
    etichette: ['backend'],
  },
  {
    id: 7,
    titolo:    'Aggiungere esportazione CSV per la lista issue',
    tipo:      'feature',
    stato:     'todo',
    priorita:  'low',
    assegnata: null,
    scadenza:  null,
    etichette: ['export'],
  },
]


/* ══════════════════════════════════════════════════════════════
   AUTH GUARD — identico all'originale
   ══════════════════════════════════════════════════════════════ */
function getUtente() {
  const raw = sessionStorage.getItem('bb_utente')
  return raw ? JSON.parse(raw) : null
}

const utente = getUtente()

if (!utente) {
  window.location.href = 'login.html'
}


/* ══════════════════════════════════════════════════════════════
   STATE REATTIVO — filtri tabella + sidebar mobile
   ══════════════════════════════════════════════════════════════ */
const cerca = ref('')
const filtroTipo = ref('')
const filtroStato = ref('')
const sidebarAperta = ref(false)


/* ══════════════════════════════════════════════════════════════
   COMPUTED — sostituiscono aggiornaStats() e renderTabella()
   ══════════════════════════════════════════════════════════════ */
const statTotale  = computed(() => ISSUE_DEMO.length)
const statTodo    = computed(() => ISSUE_DEMO.filter(i => i.stato === 'todo').length)
const statProgress = computed(() => ISSUE_DEMO.filter(i => i.stato === 'in-progress').length)
const statCritici = computed(() => ISSUE_DEMO.filter(i =>
  i.priorita === 'critical' &&
  i.stato !== 'done' &&
  i.stato !== 'closed'
).length)

const issueFiltrate = computed(() => {
  const q = cerca.value.toLowerCase()
  return ISSUE_DEMO.filter(issue => {
    const matchTitolo = issue.titolo.toLowerCase().includes(q)
    const matchTipo   = !filtroTipo.value  || issue.tipo  === filtroTipo.value
    const matchStato  = !filtroStato.value || issue.stato === filtroStato.value
    return matchTitolo && matchTipo && matchStato
  })
})

// Iniziali per l'avatar della topbar (la sidebar le calcola da sola)
const iniziali = computed(() => {
  if (!utente?.nome) return '?'
  return utente.nome
    .split(' ')
    .map(p => p[0])
    .join('')
    .toUpperCase()
    .slice(0, 2)
})

// Ruolo readonly: nasconde il bottone "Nuova issue" (come inizializzaUI() originale)
const puoCreareIssue = computed(() => utente?.ruolo !== 'readonly')


/* ══════════════════════════════════════════════════════════════
   UTILITY — identiche all'originale
   ══════════════════════════════════════════════════════════════ */
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

function apriIssue(id) {
  console.log('Apri issue', id)
  // TODO: window.location.href = `issue-detail.html?id=${id}`;
}

function inizialiDa(nome) {
  return nome.split(' ').map(p => p[0]).join('').toUpperCase().slice(0, 2)
}


/* ══════════════════════════════════════════════════════════════
   SIDEBAR MOBILE + LOGOUT
   ══════════════════════════════════════════════════════════════ */
function aprireSidebar()  { sidebarAperta.value = true  }
function chiudiSidebar() { sidebarAperta.value = false }

function logout() {
  sessionStorage.removeItem('bb_utente')
  window.location.href = 'login.html'
}
</script>

<template>
  <div v-if="utente">

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
          <span class="text-ink-400 text-sm hidden sm:inline">Panoramica</span>
        </div>

        <div class="flex items-center gap-2">

          <!-- Campana notifiche -->
          <button class="relative p-2 rounded-lg text-ink-500 hover:text-ink-800 hover:bg-ink-50 transition-colors">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none"
              stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/>
              <path d="M13.73 21a2 2 0 0 1-3.46 0"/>
            </svg>
            <!-- Badge numerico — nascosto di default (come nell'originale) -->
            <span class="hidden absolute top-1 right-1 w-4 h-4 rounded-full bg-red-500
                         text-white text-[9px] font-mono font-bold
                         flex items-center justify-center">
              3
            </span>
          </button>

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
      <main class="flex-1 p-6 lg:p-8 space-y-8">

        <!-- Intestazione sezione + bottone nuova issue -->
        <div class="flex items-start justify-between gap-4">
          <div>
            <h2 class="font-display text-ink-900 text-xl font-bold">Panoramica progetto</h2>
            <p class="text-ink-400 text-sm mt-0.5">Situazione attuale delle issue.</p>
          </div>
          <button
            v-if="puoCreareIssue"
            class="flex items-center gap-2 px-4 py-2.5 rounded-lg
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
        <div class="grid grid-cols-2 lg:grid-cols-4 gap-4">

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


        <!-- ── Sezione tabella issue ───────────────────────────── -->
        <div class="anim-fade-in delay-5">

          <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-3 mb-4">
            <div>
              <h3 class="font-display text-ink-900 text-base font-semibold">Issue recenti</h3>
              <p class="text-ink-400 text-xs mt-0.5">{{ issueFiltrate.length }} issue trovate</p>
            </div>

            <div class="flex flex-wrap items-center gap-2">

              <!-- Ricerca per titolo -->
              <div class="relative">
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
                              w-44 transition-colors" />
              </div>

              <select v-model="filtroTipo"
                      class="px-2.5 py-1.5 text-sm rounded-lg border border-ink-200
                             bg-white text-ink-600 cursor-pointer
                             focus:border-brand-500 focus:ring-2 focus:ring-brand-500/20
                             transition-colors">
                <option value="">Tutti i tipi</option>
                <option value="bug">Bug</option>
                <option value="feature">Feature</option>
                <option value="question">Question</option>
                <option value="documentation">Docs</option>
              </select>

              <select v-model="filtroStato"
                      class="px-2.5 py-1.5 text-sm rounded-lg border border-ink-200
                             bg-white text-ink-600 cursor-pointer
                             focus:border-brand-500 focus:ring-2 focus:ring-brand-500/20
                             transition-colors">
                <option value="">Tutti gli stati</option>
                <option value="todo">Todo</option>
                <option value="in-progress">In Progress</option>
                <option value="done">Done</option>
                <option value="closed">Closed</option>
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
                      <div v-if="issue.assegnata" class="flex items-center gap-2">
                        <div class="w-5 h-5 rounded-full bg-brand-500/20 flex items-center justify-center flex-shrink-0">
                          <span class="text-[9px] font-mono text-brand-700 font-medium">
                            {{ inizialiDa(issue.assegnata) }}
                          </span>
                        </div>
                        <span class="text-sm text-ink-600">{{ issue.assegnata }}</span>
                      </div>
                      <span v-else class="text-ink-300 text-sm">—</span>
                    </td>

                    <td class="px-4 py-3.5 whitespace-nowrap hidden lg:table-cell">
                      <span v-if="issue.scadenza"
                            class="font-mono text-[12px]"
                            :class="isScaduta(issue) ? 'text-red-500 font-medium' : 'text-ink-400'">
                        {{ formattaData(issue.scadenza) }}
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
                Mostrate {{ issueFiltrate.length }} di {{ ISSUE_DEMO.length }}
              </span>
              <a href="#" class="text-xs font-medium text-brand-600 hover:text-brand-700 transition-colors">
                Vedi tutte le issue →
              </a>
            </div>
          </div>

        </div>

      </main>
    </div>
  </div>
</template>
