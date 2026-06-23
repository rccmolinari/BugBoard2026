<script setup>
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import api from '../api'
const router = useRouter()
const route = useRoute()


/* ── Stato del form ─────────────────────────────────────────── */
const email       = ref('')
const password    = ref('')
const showPassword = ref(false)

const emailError    = ref(false)
const passwordError = ref(false)
const loginError    = ref(false)
const isLoading     = ref(false)


function handleLogin(event) {
  event.preventDefault()

  api.post('/api/auth/login', {
    email: email.value,
    password: password.value
  })
    .then(response => {

      const { sessionId, nome, ruolo } = response.data
      sessionStorage.setItem(
        'bb_utente',
        JSON.stringify({ sessionId, nome, ruolo })
      )

      if (ruolo === 'admin') {
        router.push('/admin')
      } else if (ruolo === 'readonly') {
        router.push('/readonly')
      } else {
        router.push('/user')
      }
    })
    .catch(error => {
      console.error('Errore durante il login:', error)
      loginError.value = true
      router.push('/')
    })
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
          <p class="text-ink-400 text-[15px] leading-relaxed max-w-xs">
            Sviluppato da:
          </p>

          <ul class="mt-8 space-y-3">
            <li class="flex items-center gap-3 text-ink-400 text-sm">
              <span class="w-1.5 h-1.5 rounded-full bg-brand-500 flex-shrink-0"></span>
              Molinari Rocco
            </li>
            <li class="flex items-center gap-3 text-ink-400 text-sm">
              <span class="w-1.5 h-1.5 rounded-full bg-brand-500 flex-shrink-0"></span>
              Megna Daniele
            </li>
          </ul>
        </div>

        <div class="flex items-center gap-2">
          <span class="font-mono text-[11px] text-ink-600 tracking-wider uppercase">v1.0.0</span>
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

          <div class="mt-4 text-center">
            <p class="text-sm text-ink-500">
              Non hai un account?
              <button
                type="button"
                @click="router.push('/register')"
                class="text-brand-600 hover:text-brand-700 font-medium transition-colors"
              >
                Registrati
              </button>
            </p>
          </div>

        </form>

      </div>
    </div>

  </div>
</template>
