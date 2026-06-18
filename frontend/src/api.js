import axios from 'axios'

/*
 * Client API centralizzato.
 *
 * Un unico interceptor inietta la sessione come header X-Session-Id leggendola
 * da sessionStorage: così nessuna pagina deve più passare il sid negli URL
 * (che finirebbe in log, history del browser e header Referer).
 *
 * Tutte le pagine importano questa istanza invece di axios "nudo".
 */
const api = axios.create()

api.interceptors.request.use((config) => {
  const raw = sessionStorage.getItem('bb_utente')
  if (raw) {
    try {
      const { sessionId } = JSON.parse(raw)
      if (sessionId) {
        config.headers['X-Session-Id'] = sessionId
      }
    } catch {
      // sessione non valida in storage: la gestione è demandata al router guard
    }
  }
  return config
})

export default api
