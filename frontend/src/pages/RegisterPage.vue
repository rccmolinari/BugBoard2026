<!--
  RegisterPage.vue
  Form di registrazione collegato al backend.
-->
<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const router = useRouter()

const nome = ref('')
const cognome = ref('')
const email = ref('')
const password = ref('')
const showPassword = ref(false)

const nomeError = ref(false)
const cognomeError = ref(false)
const emailError = ref(false)
const passwordError = ref(false)
const registerInfo = ref(false)
const registerError = ref(false)
const registerErrorText = ref('')
const isLoading = ref(false)

function handleRegister(event) {
  event.preventDefault()

  const nomeVal = nome.value.trim()
  const cognomeVal = cognome.value.trim()
  const emailVal = email.value.trim().toLowerCase()
  const passwordVal = password.value

  nomeError.value = !nomeVal
  cognomeError.value = !cognomeVal
  emailError.value = !emailVal || !emailVal.includes('@')
  passwordError.value = !passwordVal || passwordVal.length < 6

  if (nomeError.value || cognomeError.value || emailError.value || passwordError.value) return

  isLoading.value = true
  registerInfo.value = false
  registerError.value = false
  registerErrorText.value = ''

  axios.post('/api/register', {
    email: emailVal,
    password: passwordVal,
    name: nomeVal,
    surname: cognomeVal,
  })
    .then(response => {
      if (response.data === true) {
        registerInfo.value = true
        setTimeout(() => {
          router.push('/')
        }, 900)
        return
      }

      registerError.value = true
      registerErrorText.value = 'Email gia\' registrata.'
    })
    .catch(error => {
      registerError.value = true
      if (error?.response?.status === 409) {
        registerErrorText.value = 'Email gia\' registrata.'
      } else if (error?.response?.status === 400) {
        registerErrorText.value = 'Compila correttamente tutti i campi.'
      } else {
        registerErrorText.value = 'Errore durante la registrazione. Riprova.'
      }
    })
    .finally(() => {
      isLoading.value = false
    })
}

function togglePassword() {
  showPassword.value = !showPassword.value
}
</script>

<template>
  <div class="min-h-screen flex">
    <div class="hidden md:flex md:w-[52%] lg:w-[55%] bg-ink-900 flex-col relative overflow-hidden dot-pattern">
      <div class="absolute -top-16 -right-16 w-64 h-64 rounded-full bg-brand-500/10 blur-3xl pointer-events-none"></div>
      <div class="absolute bottom-32 -left-24 w-80 h-80 rounded-full bg-brand-500/5 blur-3xl pointer-events-none"></div>

      <div class="relative z-10 flex flex-col justify-between h-full p-10 lg:p-14">
        <div class="flex items-center gap-3">
          <div class="w-9 h-9 rounded-lg bg-brand-500 flex items-center justify-center flex-shrink-0">
            <span class="font-mono text-[13px] font-medium text-ink-900 tracking-tight">B2</span>
          </div>
          <span class="font-display text-white text-[17px] font-semibold tracking-tight">BugBoard26</span>
        </div>

        <div>
          <h1 class="font-display text-white text-3xl lg:text-4xl font-bold leading-tight mb-4">
            Crea il tuo<br />account.
          </h1>
          <p class="text-ink-400 text-[15px] leading-relaxed max-w-xs">
            Compila i dati per registrarti. Il collegamento al database lo configurerai tu.
          </p>
        </div>

        <div class="flex items-center gap-2">
          <span class="font-mono text-[11px] text-ink-600 tracking-wider uppercase">v1.0.0</span>
          <span class="text-ink-700">·</span>
          <span class="font-mono text-[11px] text-ink-600 tracking-wider uppercase">dev</span>
        </div>
      </div>
    </div>

    <div class="flex-1 flex flex-col items-center justify-center px-6 py-12 sm:px-10 bg-white">
      <div class="flex items-center gap-3 mb-10 md:hidden">
        <div class="w-9 h-9 rounded-lg bg-brand-500 flex items-center justify-center">
          <span class="font-mono text-[13px] font-medium text-ink-900">B2</span>
        </div>
        <span class="font-display text-ink-900 text-[17px] font-semibold">BugBoard26</span>
      </div>

      <div class="w-full max-w-sm">
        <div class="mb-8 anim-fade-up delay-1">
          <h2 class="font-display text-ink-900 text-2xl font-bold mb-1">Registrati</h2>
          <p class="text-ink-400 text-sm">Inserisci i tuoi dati per creare il profilo.</p>
        </div>

        <form @submit="handleRegister" novalidate>
          <div class="anim-fade-up delay-2 mb-4">
            <label for="nome" class="block text-xs font-mono font-medium text-ink-600 uppercase tracking-wider mb-2">
              Nome
            </label>
            <input
              v-model="nome"
              type="text"
              id="nome"
              name="nome"
              autocomplete="given-name"
              placeholder="Mario"
              class="w-full px-4 py-2.5 rounded-lg border border-ink-200 bg-ink-50
                     text-ink-800 text-sm placeholder-ink-300
                     transition-colors duration-150
                     focus:border-brand-500 focus:bg-white focus:ring-2 focus:ring-brand-500/20"
            />
            <p v-show="nomeError" class="text-red-500 text-xs mt-1.5">Il nome e' obbligatorio.</p>
          </div>

          <div class="anim-fade-up delay-3 mb-4">
            <label for="cognome" class="block text-xs font-mono font-medium text-ink-600 uppercase tracking-wider mb-2">
              Cognome
            </label>
            <input
              v-model="cognome"
              type="text"
              id="cognome"
              name="cognome"
              autocomplete="family-name"
              placeholder="Rossi"
              class="w-full px-4 py-2.5 rounded-lg border border-ink-200 bg-ink-50
                     text-ink-800 text-sm placeholder-ink-300
                     transition-colors duration-150
                     focus:border-brand-500 focus:bg-white focus:ring-2 focus:ring-brand-500/20"
            />
            <p v-show="cognomeError" class="text-red-500 text-xs mt-1.5">Il cognome e' obbligatorio.</p>
          </div>

          <div class="anim-fade-up delay-4 mb-4">
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
              class="w-full px-4 py-2.5 rounded-lg border border-ink-200 bg-ink-50
                     text-ink-800 text-sm placeholder-ink-300
                     transition-colors duration-150
                     focus:border-brand-500 focus:bg-white focus:ring-2 focus:ring-brand-500/20"
            />
            <p v-show="emailError" class="text-red-500 text-xs mt-1.5">Inserisci una email valida.</p>
          </div>

          <div class="anim-fade-up delay-5 mb-5">
            <label for="password" class="block text-xs font-mono font-medium text-ink-600 uppercase tracking-wider mb-2">
              Password
            </label>
            <div class="relative">
              <input
                v-model="password"
                :type="showPassword ? 'text' : 'password'"
                id="password"
                name="password"
                autocomplete="new-password"
                placeholder="••••••••"
                class="w-full px-4 py-2.5 pr-11 rounded-lg border border-ink-200 bg-ink-50
                       text-ink-800 text-sm placeholder-ink-300
                       transition-colors duration-150
                       focus:border-brand-500 focus:bg-white focus:ring-2 focus:ring-brand-500/20"
              />
              <button
                type="button"
                @click="togglePassword"
                class="absolute right-3 top-1/2 -translate-y-1/2 text-ink-400 hover:text-ink-600 transition-colors"
                aria-label="Mostra/nascondi password"
              >
                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24"
                     fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <template v-if="!showPassword">
                    <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
                    <circle cx="12" cy="12" r="3" />
                  </template>
                  <template v-else>
                    <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8
                             a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4
                             c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07
                             a3 3 0 1 1-4.24-4.24" />
                    <line x1="1" y1="1" x2="23" y2="23" />
                  </template>
                </svg>
              </button>
            </div>
            <p v-show="passwordError" class="text-red-500 text-xs mt-1.5">La password deve avere almeno 6 caratteri.</p>
          </div>

          <div v-show="registerInfo" class="mb-5 px-4 py-3 rounded-lg bg-green-50 border border-green-100 text-green-700 text-sm">
            Registrazione completata. Reindirizzamento al login...
          </div>

          <div v-show="registerError" class="mb-5 px-4 py-3 rounded-lg bg-red-50 border border-red-200 text-red-600 text-sm">
            {{ registerErrorText }}
          </div>

          <div class="anim-fade-up delay-6">
            <button
              type="submit"
              :disabled="isLoading"
              class="w-full py-2.5 px-6 rounded-lg bg-brand-500 hover:bg-brand-600
                      text-white font-semibold text-sm
                      transition-all duration-150 active:scale-[0.99]
                      disabled:opacity-60 disabled:cursor-not-allowed"
            >
              {{ isLoading ? 'Registrazione...' : 'Registrati' }}
            </button>
          </div>
        </form>

        <div class="mt-6 text-center">
          <p class="text-sm text-ink-500">
            Hai gia' un account?
            <button
              type="button"
              @click="router.push('/')"
              class="text-brand-600 hover:text-brand-700 font-medium transition-colors"
            >
              Accedi
            </button>
          </p>
        </div>
      </div>
    </div>
  </div>
</template>
