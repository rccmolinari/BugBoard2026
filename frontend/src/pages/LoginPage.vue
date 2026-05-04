<!--
  LoginPage.vue
  Porting di login.html + login.js.

  La logica è **identica** all'originale:
    - UTENTI_DEMO hardcoded come prima
    - handleLogin() con validazione email/password
    - 600ms di setTimeout per simulare latenza
    - sessionStorage.setItem('bb_utente', ...)
    - window.location.href per il redirect (sarà sostituito dal routing)
    - togglePassword() per mostrare/nascondere
    - setCaricamento() diventa isLoading ref

  Rispetto al DOM originale:
    - i .classList.add/remove('hidden') diventano v-show con ref booleane
    - gli id="..." non servono più, si usano le ref reattive
-->
<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
/* ── Credenziali demo ──────────────────────────────────────────
   Solo per sviluppo — da rimuovere quando il backend è pronto.
   ─────────────────────────────────────────────────────────── */
const UTENTI_DEMO = [
  { email: 'admin@bugboard.io', password: 'admin123', ruolo: 'admin',    nome: 'Admin' },
  { email: 'dev@bugboard.io',   password: 'dev123',   ruolo: 'normal',   nome: 'Dev User' },
  { email: 'guest@bugboard.io', password: 'guest123', ruolo: 'readonly', nome: 'Guest' },
]


/* ── Stato del form ─────────────────────────────────────────── */
const email       = ref('')
const password    = ref('')
const rememberMe  = ref(false)
const showPassword = ref(false)

const emailError    = ref(false)
const passwordError = ref(false)
const loginError    = ref(false)
const isLoading     = ref(false)


/* ── handleLogin ───────────────────────────────────────────────
   Valida i campi, simula una chiamata API, poi fa il redirect.
   ─────────────────────────────────────────────────────────── */
function handleLogin(event) {
  event.preventDefault()

  const emailVal = email.value.trim().toLowerCase()
  const passwordVal = password.value

  // Valido prima di fare qualsiasi cosa
  let tuttoOk = true

  if (!emailVal || !emailVal.includes('@')) {
    emailError.value = true
    tuttoOk = false
  } else {
    emailError.value = false
  }

  if (!passwordVal) {
    passwordError.value = true
    tuttoOk = false
  } else {
    passwordError.value = false
  }

  if (!tuttoOk) return

  // Avvio il loader e nascondo eventuali errori precedenti
  isLoading.value = true
  loginError.value = false

  /*
   * Simulo 600ms di latenza di rete.
   */
  setTimeout(() => {
    const utente = UTENTI_DEMO.find(u => u.email === emailVal && u.password === passwordVal)

    if (utente) {
      sessionStorage.setItem('bb_utente', JSON.stringify(utente))
      if (utente.ruolo === 'admin') {
        router.push('/admin')
      } else if (utente.ruolo === 'normal') {
        router.push('/user')
      } else {
        router.push('/user')
      }
    } else {
      isLoading.value = false
      loginError.value = true
      router.push('/')
    }
  }, 600)
}


/* ── togglePassword ────────────────────────────────────────────
   Alterna la visibilità della password.
   ─────────────────────────────────────────────────────────── */
function togglePassword() {
  showPassword.value = !showPassword.value
}
</script>

<template>
  <!--
    LAYOUT LOGIN: due colonne affiancate.
    Sinistra = pannello brand (nascosto su mobile).
    Destra   = form di login.
  -->
  <div class="min-h-screen flex">

    <!-- ═══════════════════════════════════════════════════════
         PANNELLO SINISTRO — branding
         Nascosto su mobile (hidden md:flex) per non sprecare spazio.
         ═══════════════════════════════════════════════════════ -->
    <div class="hidden md:flex md:w-[52%] lg:w-[55%] bg-ink-900 flex-col relative overflow-hidden dot-pattern">

      <!-- Aloni luminosi decorativi — puri CSS, niente immagini -->
      <div class="absolute -top-16 -right-16 w-64 h-64 rounded-full bg-brand-500/10 blur-3xl pointer-events-none"></div>
      <div class="absolute bottom-32 -left-24 w-80 h-80 rounded-full bg-brand-500/5 blur-3xl pointer-events-none"></div>

      <div class="relative z-10 flex flex-col justify-between h-full p-10 lg:p-14">

        <!-- Logo -->
        <div class="flex items-center gap-3">
          <div class="w-9 h-9 rounded-lg bg-brand-500 flex items-center justify-center flex-shrink-0">
            <span class="font-mono text-[13px] font-medium text-ink-900 tracking-tight">B2</span>
          </div>
          <span class="font-display text-white text-[17px] font-semibold tracking-tight">BugBoard26</span>
        </div>

        <!-- Claim centrale -->
        <div>
          <h1 class="font-display text-white text-3xl lg:text-4xl font-bold leading-tight mb-4">
            Traccia.<br />Assegna.<br />Risolvi.
          </h1>
          <p class="text-ink-400 text-[15px] leading-relaxed max-w-xs">
            La piattaforma collaborativa per gestire issue software,
            dal bug critico alla richiesta di documentazione.
          </p>

          <ul class="mt-8 space-y-3">
            <li class="flex items-center gap-3 text-ink-400 text-sm">
              <span class="w-1.5 h-1.5 rounded-full bg-brand-500 flex-shrink-0"></span>
              Segnalazione rapida con priorità e allegati
            </li>
            <li class="flex items-center gap-3 text-ink-400 text-sm">
              <span class="w-1.5 h-1.5 rounded-full bg-brand-500 flex-shrink-0"></span>
              Assegnazione e notifiche in tempo reale
            </li>
            <li class="flex items-center gap-3 text-ink-400 text-sm">
              <span class="w-1.5 h-1.5 rounded-full bg-brand-500 flex-shrink-0"></span>
              Ruoli differenziati: admin, normale, readonly
            </li>
          </ul>
        </div>

        <div class="flex items-center gap-2">
          <span class="font-mono text-[11px] text-ink-600 tracking-wider uppercase">v1.0.0</span>
          <span class="text-ink-700">·</span>
          <span class="font-mono text-[11px] text-ink-600 tracking-wider uppercase">dev</span>
        </div>

      </div>
    </div>


    <!-- ═══════════════════════════════════════════════════════
         PANNELLO DESTRO — il form
         ═══════════════════════════════════════════════════════ -->
    <div class="flex-1 flex flex-col items-center justify-center px-6 py-12 sm:px-10 bg-white">

      <!-- Logo visibile solo su mobile -->
      <div class="flex items-center gap-3 mb-10 md:hidden">
        <div class="w-9 h-9 rounded-lg bg-brand-500 flex items-center justify-center">
          <span class="font-mono text-[13px] font-medium text-ink-900">B2</span>
        </div>
        <span class="font-display text-ink-900 text-[17px] font-semibold">BugBoard26</span>
      </div>

      <div class="w-full max-w-sm">

        <!-- Intestazione form -->
        <div class="mb-8 anim-fade-up delay-1">
          <h2 class="font-display text-ink-900 text-2xl font-bold mb-1">Bentornato</h2>
          <p class="text-ink-400 text-sm">Inserisci le tue credenziali per accedere.</p>
        </div>

        <form @submit="handleLogin" novalidate>

          <!-- Email -->
          <div class="anim-fade-up delay-2 mb-5">
            <label for="email" class="block text-xs font-mono font-medium text-ink-600 uppercase tracking-wider mb-2">
              Email
            </label>
            <input
              v-model="email"
              type="email"
              id="email"
              name="email"
              autocomplete="email"
              placeholder="mario@esempio.it"
              required
              class="w-full px-4 py-2.5 rounded-lg border border-ink-200 bg-ink-50
                     text-ink-800 text-sm placeholder-ink-300
                     transition-colors duration-150
                     focus:border-brand-500 focus:bg-white focus:ring-2 focus:ring-brand-500/20"
            />
            <p v-show="emailError" class="text-red-500 text-xs mt-1.5">
              Inserisci un indirizzo email valido.
            </p>
          </div>

          <!-- Password -->
          <div class="anim-fade-up delay-3 mb-5">
            <div class="flex items-center justify-between mb-2">
              <label for="password" class="block text-xs font-mono font-medium text-ink-600 uppercase tracking-wider">
                Password
              </label>
              <button type="button"
                      class="text-xs text-brand-600 hover:text-brand-700 transition-colors font-medium">
                Password dimenticata?
              </button>
            </div>
            <div class="relative">
              <input
                v-model="password"
                :type="showPassword ? 'text' : 'password'"
                id="password"
                name="password"
                autocomplete="current-password"
                placeholder="••••••••"
                required
                class="w-full px-4 py-2.5 pr-11 rounded-lg border border-ink-200 bg-ink-50
                       text-ink-800 text-sm placeholder-ink-300
                       transition-colors duration-150
                       focus:border-brand-500 focus:bg-white focus:ring-2 focus:ring-brand-500/20"
              />
              <button type="button"
                      @click="togglePassword"
                      aria-label="Mostra/nascondi password"
                      class="absolute right-3 top-1/2 -translate-y-1/2 text-ink-400 hover:text-ink-600 transition-colors">
                <svg xmlns="http://www.w3.org/2000/svg"
                     width="16" height="16" viewBox="0 0 24 24"
                     fill="none" stroke="currentColor" stroke-width="2"
                     stroke-linecap="round" stroke-linejoin="round">
                  <!-- Icona "occhio aperto" se password nascosta, "occhio barrato" se visibile -->
                  <template v-if="!showPassword">
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
            <p v-show="passwordError" class="text-red-500 text-xs mt-1.5">
              La password è obbligatoria.
            </p>
          </div>

          <!-- Ricordami -->
          <div class="anim-fade-up delay-4 mb-7 flex items-center gap-2.5">
            <input
              v-model="rememberMe"
              type="checkbox"
              id="rememberMe"
              name="rememberMe"
              class="w-4 h-4 rounded border-ink-300 accent-brand-500 cursor-pointer"
            />
            <label for="rememberMe" class="text-sm text-ink-500 cursor-pointer select-none">
              Ricordami per 30 giorni
            </label>
          </div>

          <!-- Errore credenziali errate -->
          <div v-show="loginError"
               class="mb-5 px-4 py-3 rounded-lg bg-red-50 border border-red-200 text-red-600 text-sm">
            Email o password non corretti. Riprova.
          </div>

          <!-- Bottone submit -->
          <div class="anim-fade-up delay-5">
            <button type="submit"
                    :disabled="isLoading"
                    class="w-full py-2.5 px-6 rounded-lg bg-brand-500 hover:bg-brand-600
                           text-white font-semibold text-sm
                           transition-all duration-150 active:scale-[0.99]
                           disabled:opacity-60 disabled:cursor-not-allowed
                           flex items-center justify-center gap-2">
              <svg v-show="isLoading"
                   class="animate-spin w-4 h-4"
                   xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/>
                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8H4z"/>
              </svg>
              <span>{{ isLoading ? 'Accesso in corso…' : 'Accedi' }}</span>
            </button>
          </div>

        </form>

        <!--
          Hint credenziali di test.
          DA RIMUOVERE in produzione.
        -->
        <div class="anim-fade-up delay-6 mt-8 p-4 rounded-lg bg-ink-50 border border-ink-100">
          <p class="text-xs font-mono text-ink-400 uppercase tracking-wider mb-2">Credenziali di test</p>
          <div class="space-y-1">
            <p class="text-xs text-ink-500">
              <span class="font-mono text-ink-700">admin@bugboard.io</span>
              <span class="mx-1 text-ink-300">/</span>
              <span class="font-mono text-ink-700">admin123</span>
              <span class="ml-2 px-1.5 py-0.5 bg-brand-100 text-brand-700 text-[10px] font-mono rounded">admin</span>
            </p>
            <p class="text-xs text-ink-500">
              <span class="font-mono text-ink-700">dev@bugboard.io</span>
              <span class="mx-1 text-ink-300">/</span>
              <span class="font-mono text-ink-700">dev123</span>
            </p>
          </div>
        </div>

      </div>
    </div>

  </div>
</template>
