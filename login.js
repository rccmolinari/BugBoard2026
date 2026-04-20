/*
 * login.js
 *
 * Logica della pagina di login.
 *
 * NOTA PER ANGULAR:
 * Questo file sparirà. La sua logica si distribuirà così:
 *   - UTENTI_DEMO       → non serve più, ci sarà un vero backend
 *   - handleLogin()     → LoginComponent + AuthService.login()
 *   - setCaricamento()  → stato locale del componente (isLoading: boolean)
 *   - togglePassword()  → metodo del componente, o direttiva custom
 */


/* ── Credenziali demo ──────────────────────────────────────────
   Solo per sviluppo — da rimuovere quando il backend è pronto.
   In Angular questo blocco non esisterà: ci sarà una POST reale.
   ─────────────────────────────────────────────────────────── */
const UTENTI_DEMO = [
  { email: 'admin@bugboard.io', password: 'admin123', ruolo: 'admin',    nome: 'Admin' },
  { email: 'dev@bugboard.io',   password: 'dev123',   ruolo: 'normal',   nome: 'Dev User' },
  { email: 'guest@bugboard.io', password: 'guest123', ruolo: 'readonly', nome: 'Guest' },
];


/* ── handleLogin ───────────────────────────────────────────────
   Chiamato dal form tramite onsubmit.
   Valida i campi, simula una chiamata API, poi fa il redirect.
   ─────────────────────────────────────────────────────────── */
function handleLogin(event) {
  event.preventDefault();

  const email    = document.getElementById('email').value.trim().toLowerCase();
  const password = document.getElementById('password').value;

  // Valido prima di fare qualsiasi cosa
  let tuttoOk = true;

  if (!email || !email.includes('@')) {
    document.getElementById('emailError').classList.remove('hidden');
    tuttoOk = false;
  } else {
    document.getElementById('emailError').classList.add('hidden');
  }

  if (!password) {
    document.getElementById('passwordError').classList.remove('hidden');
    tuttoOk = false;
  } else {
    document.getElementById('passwordError').classList.add('hidden');
  }

  if (!tuttoOk) return;

  // Avvio il loader e nascondo eventuali errori precedenti
  setCaricamento(true);
  document.getElementById('loginError').classList.add('hidden');

  /*
   * Simulo 600ms di latenza di rete.
   * Con Angular: AuthService.login(email, password).subscribe(...)
   * che fa POST /api/auth/login e riceve un JWT.
   */
  setTimeout(() => {
    const utente = UTENTI_DEMO.find(u => u.email === email && u.password === password);

    if (utente) {
      sessionStorage.setItem('bb_utente', JSON.stringify(utente));
      // Salvo la sessione — in Angular ci sarà un token JWT nel localStorage
      if(utente.ruolo === 'admin') {
        window.location.href = 'admin.html';
      } else if(utente.ruolo === 'normal') {
        window.location.href = 'dashboard.html';
      } else {
        window.location.href = 'readonly.html';
      }

    } else {
      setCaricamento(false);
      document.getElementById('loginError').classList.remove('hidden');
    }
  }, 600);
}


/* ── setCaricamento ────────────────────────────────────────────
   Attiva/disattiva il pulsante e lo spinner durante la chiamata.
   ─────────────────────────────────────────────────────────── */
function setCaricamento(attivo) {
  const btn     = document.getElementById('submitBtn');
  const label   = document.getElementById('submitLabel');
  const spinner = document.getElementById('spinner');

  btn.disabled = attivo;
  label.textContent = attivo ? 'Accesso in corso…' : 'Accedi';
  spinner.classList.toggle('hidden', !attivo);
}


/* ── togglePassword ────────────────────────────────────────────
   Alterna la visibilità della password e aggiorna l'icona.
   ─────────────────────────────────────────────────────────── */
function togglePassword() {
  const input    = document.getElementById('password');
  const icona    = document.getElementById('eyeIcon');
  const visibile = input.type === 'text';

  input.type = visibile ? 'password' : 'text';

  // Icona "occhio aperto" se la password è nascosta, "occhio barrato" se è visibile
  icona.innerHTML = visibile
    ? `<path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
       <circle cx="12" cy="12" r="3"/>`
    : `<path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8
                a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4
                c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07
                a3 3 0 1 1-4.24-4.24"/>
       <line x1="1" y1="1" x2="23" y2="23"/>`;
}
