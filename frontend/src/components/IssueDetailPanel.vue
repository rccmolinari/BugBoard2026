<!-- components/IssueDetailPanel.vue -->
<template>
  <!-- Overlay -->
  <Transition name="fade">
    <div
      v-if="issue || caricamento"
      class="fixed inset-0 z-40 bg-black/20 backdrop-blur-[1px]"
      @click="$emit('close')"
    />
  </Transition>

  <!-- Panel -->
  <Transition name="slide">
    <aside
      v-if="issue || caricamento"
      class="fixed right-0 top-0 z-50 h-full w-full max-w-md
             bg-white shadow-xl border-l border-ink-100
             flex flex-col overflow-hidden"
    >
      <!-- Header -->
      <div class="flex items-center justify-between px-5 py-4 border-b border-ink-100 flex-shrink-0">
        <div class="flex items-center gap-2">
          <span class="font-mono text-[11px] text-ink-400">#{{ issue?.id }}</span>
          <span class="text-[10px] font-mono text-ink-300">·</span>
          <span class="text-[10px] font-mono text-ink-400 uppercase tracking-wider">Dettaglio issue</span>
        </div>
        <button
          @click="$emit('close')"
          class="w-7 h-7 rounded-lg flex items-center justify-center
                 text-ink-400 hover:text-ink-600 hover:bg-ink-100 transition-colors"
        >
          <svg class="w-4 h-4" viewBox="0 0 24 24" fill="none"
               stroke="currentColor" stroke-width="2"
               stroke-linecap="round" stroke-linejoin="round">
            <line x1="18" y1="6" x2="6" y2="18"/>
            <line x1="6" y1="6" x2="18" y2="18"/>
          </svg>
        </button>
      </div>

      <!-- Loading -->
      <div v-if="caricamento" class="flex-1 flex items-center justify-center">
        <svg class="w-6 h-6 animate-spin text-ink-300" viewBox="0 0 24 24"
             fill="none" stroke="currentColor" stroke-width="2">
          <path d="M21 12a9 9 0 1 1-6.219-8.56"/>
        </svg>
      </div>

      <!-- Corpo scrollabile -->
      <div v-else-if="issue" class="flex-1 overflow-y-auto">
        <div class="px-5 py-4 space-y-5">

          <!-- Titolo -->
          <div class="space-y-0.5">
            <span class="text-[10px] font-mono text-ink-400 uppercase tracking-wider">Titolo</span>
            <p class="text-base text-ink-800 font-semibold leading-snug">{{ issue.titolo }}</p>
          </div>

          <!-- Descrizione -->
          <div v-if="issue.descrizione" class="space-y-0.5">
            <span class="text-[10px] font-mono text-ink-400 uppercase tracking-wider">Descrizione</span>
            <p class="text-sm text-ink-600 leading-relaxed">{{ issue.descrizione }}</p>
          </div>

          <!-- Badges -->
          <div class="grid grid-cols-3 gap-3">
            <div class="space-y-1">
              <span class="text-[10px] font-mono text-ink-400 uppercase tracking-wider">Tipo</span>
              <BadgeTipo :tipo="issue.tipo" />
            </div>
            <div class="space-y-1">
              <span class="text-[10px] font-mono text-ink-400 uppercase tracking-wider">Priorità</span>
              <BadgePriorita :priorita="issue.priorita" />
            </div>
            <div class="space-y-1">
              <span class="text-[10px] font-mono text-ink-400 uppercase tracking-wider">Stato</span>
              <BadgeStato :stato="issue.stato" />
            </div>
          </div>

          <!-- Etichette -->
          <div v-if="issue.etichetta?.length" class="space-y-1.5">
            <span class="text-[10px] font-mono text-ink-400 uppercase tracking-wider">Etichette</span>
            <div class="flex flex-wrap gap-1.5">
              <span
                v-for="e in issue.etichetta" :key="e"
                class="px-2 py-0.5 text-[11px] font-mono rounded
                       bg-ink-100 text-ink-500 border border-ink-200"
              >
                {{ e }}
              </span>
            </div>
          </div>

                    <!-- Creatore (solo admin e readonly ) -->
        <div v-if="(isAdmin || ruolo === 'readonly') && issue.emailCreatore" class="space-y-1">
            <span class="text-[10px] font-mono text-ink-400 uppercase tracking-wider">Creata da</span>
            <div class="flex items-center gap-2">
              <div class="w-6 h-6 rounded-full bg-ink-200 flex items-center justify-center flex-shrink-0">
                <span class="text-[9px] font-mono text-ink-600 font-medium">
                  {{ iniziali(issue.emailCreatore) }}
                </span>
              </div>
              <span class="text-sm text-ink-600">{{ issue.emailCreatore }}</span>
            </div>
          </div>

          <!-- Chi ha assegnato (solo admin) -->
          <div v-if="issue.emailAssegnatario" class="space-y-1">
            <span class="text-[10px] font-mono text-ink-400 uppercase tracking-wider">Assegnata da</span>
            <div class="flex items-center gap-2">
              <div class="w-6 h-6 rounded-full bg-brand-500/20 flex items-center justify-center flex-shrink-0">
                <span class="text-[9px] font-mono text-brand-700 font-medium">
                  {{ iniziali(issue.emailAssegnatario) }}
                </span>
              </div>
              <span class="text-sm text-ink-600">{{ issue.emailAssegnatario }}</span>
            </div>
          </div>

          <!-- A chi è assegnata (admin → emailAssegnatoA, user → emailAssegnatario) -->
        <div v-if="(isAdmin || ruolo === 'readonly') && issue.emailAssegnatoA" class="space-y-1">
            <span class="text-[10px] font-mono text-ink-400 uppercase tracking-wider">Assegnata a</span>
            <div class="flex items-center gap-2">
              <div class="w-6 h-6 rounded-full bg-green-500/20 flex items-center justify-center flex-shrink-0">
                <span class="text-[9px] font-mono text-green-700 font-medium">
                  {{ iniziali(assegnatoA) }}
                </span>
              </div>
              <span class="text-sm text-ink-600">{{ assegnatoA }}</span>
            </div>
          </div>

          <!-- Immagine allegata -->
          <div v-if="issue.immagineContentType && ruolo !== 'readonly'" class="space-y-1.5">
            <span class="text-[10px] font-mono text-ink-400 uppercase tracking-wider block">Immagine allegata</span>
            <img
              :src="`/api/issues/${issue.id}/immagine`"
              alt="Screenshot allegato"
              class="w-full h-auto max-h-64 object-contain rounded-lg
                     border border-ink-100 bg-ink-900/5 shadow-sm"
            />
          </div>

          <!-- Scadenza -->
          <div v-if="issue.dataScadenza" class="space-y-0.5">
            <span class="text-[10px] font-mono text-ink-400 uppercase tracking-wider">Scadenza</span>
            <p class="font-mono text-[13px]"
               :class="isScaduta(issue) ? 'text-red-500 font-semibold' : 'text-ink-500'">
              {{ formattaData(issue.dataScadenza) }}
            </p>
          </div>

          <!-- Commenti -->
        <div class="space-y-3">
        <span class="text-[10px] font-mono text-ink-400 uppercase tracking-wider">
            Commenti ({{ commentiLocali.length }})
        </span>

        <!-- Lista commenti -->
        <div v-if="commentiLocali.length" class="space-y-1.5">
        <div
            v-for="(c, i) in commentiOrdinati" :key="i"
            class="bg-ink-50 rounded-lg px-3 py-2.5 border border-ink-100 space-y-1.5"
        >
        <div class="flex items-center gap-2">
        <div class="w-6 h-6 rounded-full bg-brand-500/20 flex items-center justify-center flex-shrink-0">
            <span class="text-[9px] font-mono text-brand-700 font-medium">
            {{ iniziali(c.autore) }}
            </span>
        </div>
        <span class="font-mono text-[11px] text-ink-400">{{ formattaDataOra(c.timestamp) }}</span>
        <span class="text-[11px] text-ink-500">{{ c.autore }}</span>
        </div>
            <p class="text-sm text-ink-600 leading-relaxed pl-8">{{ c.testo }}</p>
        </div>
        </div>
        <p v-else class="text-sm text-ink-300">Nessun commento.</p>
        </div>

        </div>
      </div>
        
        <!-- Footer user — scrivi commento + chiudi -->
        <div v-if="!isAdmin && issue && !caricamento"
            class="flex-shrink-0 border-t border-ink-100 px-5 py-4 space-y-2">
        <template v-if="allowComment">
        <textarea
            v-model="nuovoCommento"
            rows="3"
            placeholder="Scrivi un commento…"
            class="w-full rounded-lg border border-ink-200 px-3 py-2 text-sm text-ink-800
                placeholder-ink-300 focus:border-brand-500 focus:ring-2 focus:ring-brand-500/20
                resize-none transition-colors"
        />
        <p v-if="erroreCommento" class="text-xs text-red-500">{{ erroreCommento }}</p>
        <button
            @click="inviaCommento"
            :disabled="invioCommento || !nuovoCommento.trim()"
            class="w-full inline-flex items-center justify-center gap-2
                px-3 py-2 rounded-lg text-sm font-semibold
                text-white bg-brand-500 hover:bg-brand-600
                disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
        >
            <svg v-if="invioCommento" class="w-4 h-4 animate-spin" viewBox="0 0 24 24"
                fill="none" stroke="currentColor" stroke-width="2">
            <path d="M21 12a9 9 0 1 1-6.219-8.56"/>
            </svg>
            {{ invioCommento ? 'Invio…' : 'Invia commento' }}
        </button>
        </template>
        <div v-if="ruolo !== 'readonly' && issue.stato !== 'DONE' && issue.stato !== 'CLOSED'" class="pt-1">
          <p v-if="erroreChiusura" class="text-xs text-red-500 mb-1">{{ erroreChiusura }}</p>
          <button
            @click="chiudiIssue"
            :disabled="chiusaInCorso"
            class="w-full inline-flex items-center justify-center gap-2
                   px-3 py-2 rounded-lg text-sm font-semibold
                   text-emerald-700 bg-emerald-50 hover:bg-emerald-100
                   border border-emerald-100
                   disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
          >
            <svg v-if="chiusaInCorso" class="w-4 h-4 animate-spin" viewBox="0 0 24 24"
                 fill="none" stroke="currentColor" stroke-width="2">
              <path d="M21 12a9 9 0 1 1-6.219-8.56"/>
            </svg>
            <svg v-else class="w-4 h-4" viewBox="0 0 24 24" fill="none"
                 stroke="currentColor" stroke-width="2"
                 stroke-linecap="round" stroke-linejoin="round">
              <polyline points="20 6 9 17 4 12"/>
            </svg>
            {{ chiusaInCorso ? 'Chiusura…' : 'Chiudi Issue' }}
          </button>
        </div>
        </div>

      <!-- Footer (solo admin) -->
      <div v-if="isAdmin && issue && !caricamento" class="flex-shrink-0 border-t border-ink-100 px-5 py-4 space-y-2">
        <button
          @click="$emit('assegna', issue)"
          class="w-full inline-flex items-center justify-center gap-2
                 px-4 py-2.5 rounded-lg text-sm font-semibold
                 text-brand-700 bg-brand-50 hover:bg-brand-100
                 border border-brand-100 transition-colors"
        >
          <svg class="w-4 h-4" viewBox="0 0 24 24" fill="none"
               stroke="currentColor" stroke-width="2"
               stroke-linecap="round" stroke-linejoin="round">
            <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
            <circle cx="12" cy="7" r="4"/>
          </svg>
          Assegna issue
        </button>
        <div v-if="issue.stato !== 'CLOSED'">
          <p v-if="erroreChiusura" class="text-xs text-red-500 mb-1">{{ erroreChiusura }}</p>
          <button
            @click="chiudiIssueAdmin"
            :disabled="chiusaInCorso"
            class="w-full inline-flex items-center justify-center gap-2
                   px-4 py-2.5 rounded-lg text-sm font-semibold
                   text-red-700 bg-red-50 hover:bg-red-100
                   border border-red-100
                   disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
          >
            <svg v-if="chiusaInCorso" class="w-4 h-4 animate-spin" viewBox="0 0 24 24"
                 fill="none" stroke="currentColor" stroke-width="2">
              <path d="M21 12a9 9 0 1 1-6.219-8.56"/>
            </svg>
            <svg v-else class="w-4 h-4" viewBox="0 0 24 24" fill="none"
                 stroke="currentColor" stroke-width="2"
                 stroke-linecap="round" stroke-linejoin="round">
              <path d="M18 6 6 18M6 6l12 12"/>
            </svg>
            {{ chiusaInCorso ? 'Chiusura…' : 'Chiudi Issue' }}
          </button>
        </div>
      </div>

    </aside>
  </Transition>
</template>
<script setup>
import { computed, ref, watch } from 'vue'
import axios from 'axios'
import BadgeTipo from './BadgeTipo.vue'
import BadgeStato from './BadgeStato.vue'
import BadgePriorita from './BadgePriorita.vue'

const props = defineProps({
  issue:       { type: Object,  default: null  },
  caricamento: { type: Boolean, default: false },
  isAdmin:     { type: Boolean, default: false },
  ruolo:      { type: String,  default: false },
  allowComment: { type: Boolean, default: true },
  sessionId:   { type: String,  default: null  },
})

const emit = defineEmits(['close', 'assegna', 'chiusa'])

const commentiLocali = ref([])

watch(() => props.issue, (nuova) => {
  commentiLocali.value = nuova?.commento ? [...nuova.commento] : []
}, { immediate: true })

const commentiOrdinati = computed(() => {
  return [...commentiLocali.value].sort((a, b) => new Date(b.timestamp) - new Date(a.timestamp))
})

const nuovoCommento = ref('')
const invioCommento = ref(false)
const erroreCommento = ref('')

const chiusaInCorso = ref(false)
const erroreChiusura = ref('')

async function chiudiIssue() {
  chiusaInCorso.value = true
  erroreChiusura.value = ''
  try {
    const res = await axios.post(`/api/issues/chiudi/${props.issue.id}/${props.sessionId}`)
    if (res.data === true) {
      emit('chiusa')
    } else {
      erroreChiusura.value = 'Operazione fallita. Verifica di avere i permessi.'
    }
  } catch (e) {
    erroreChiusura.value = 'Errore durante la chiusura.'
  } finally {
    chiusaInCorso.value = false
  }
}

async function chiudiIssueAdmin() {
  chiusaInCorso.value = true
  erroreChiusura.value = ''
  try {
    const res = await axios.post(`/api/issues/chiudi-admin/${props.issue.id}/${props.sessionId}`)
    if (res.data === true) {
      emit('chiusa')
    } else {
      erroreChiusura.value = 'Operazione fallita.'
    }
  } catch (e) {
    erroreChiusura.value = 'Errore durante la chiusura.'
  } finally {
    chiusaInCorso.value = false
  }
}

const assegnatoA = computed(() => {
  if (!props.issue) return null
  return props.ruolo === 'admin'
    ? props.issue.emailAssegnatoA
    : props.issue.emailAssegnatario
})

async function inviaCommento() {
  if (!nuovoCommento.value.trim()) return
  invioCommento.value = true
  erroreCommento.value = ''
  try {
    const ok = await axios.post(
      `/api/issues/${props.issue.id}/commento/${props.sessionId}`,
      nuovoCommento.value,
      { headers: { 'Content-Type': 'text/plain' } }
    )
    if (ok.data === true) {
      nuovoCommento.value = ''

      // Ricarica i commenti aggiornati dal backend
      const endpoint = props.ruolo === 'admin' || props.ruolo === 'readonly'
        ? `/api/issues/dettagliAdmin/${props.issue.id}/${props.sessionId}`
        : `/api/issues/dettagli/${props.issue.id}/${props.sessionId}`

      const res = await axios.get(endpoint)
      commentiLocali.value = res.data.commento ?? []
    } else {
      erroreCommento.value = 'Invio fallito, riprova.'
    }
  } catch (e) {
    erroreCommento.value = 'Errore durante l\'invio.'
  } finally {
    invioCommento.value = false
  }
}

function iniziali(email) {
  if (!email) return '?'
  const local = email.split('@')[0]
  return local.split(/[._]/).map(p => p[0]).join('').toUpperCase().slice(0, 2)
}

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
    day: '2-digit', month: 'short', year: 'numeric'
  })
}
function isScaduta(issue) {
  if (!issue?.dataScadenza) return false
  if (issue.stato === 'done' || issue.stato === 'closed') return false
  return new Date(issue.dataScadenza) < new Date()
}
</script>

<style scoped>
.fade-enter-active, .fade-leave-active { transition: opacity 0.2s ease; }
.fade-enter-from, .fade-leave-to       { opacity: 0; }

.slide-enter-active, .slide-leave-active { transition: transform 0.25s ease; }
.slide-enter-from, .slide-leave-to       { transform: translateX(100%); }
</style>
