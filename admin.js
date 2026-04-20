/*
 * admin.js
 *
 * Logica della pagina di amministrazione.
 * Gestisce la lista utenti, la creazione e l'eliminazione.
 *
 * NOTA PER ANGULAR:
 *   - UTENTI_DEFAULT / getUtenti() / salvaUtenti()  → UserService con HTTP /api/users
 *   - inizializzaUI()                               → AdminComponent.ngOnInit()
 *   - renderUtenti()                                → *ngFor su users$ (Observable)
 *   - creaUtente()                                  → UserService.create() con ReactiveForm
 *   - eliminaUtente()                               → UserService.delete() + MatDialog conferma
 *   - aggiornaStats()                               → computed dal componente o AdminService
 */


/* ══════════════════════════════════════════════════════════════
   AUTH GUARD — solo gli amministratori possono vedere questa pagina.
   Con Angular sarà un RoleGuard.canActivate() che legge il ruolo dal token JWT.
   ══════════════════════════════════════════════════════════════ */
function getUtente() {
  const raw = sessionStorage.getItem('bb_utente');
  return raw ? JSON.parse(raw) : null;
}

const utente = getUtente();

if (!utente) {
  window.location.href = 'login.html';
} else if (utente.ruolo !== 'admin') {
  window.location.href = 'dashboard.html';
}


/* ══════════════════════════════════════════════════════════════
   STORE UTENTI (localStorage)
   Sostituire con un UserService + chiamate HTTP quando il backend è pronto.
   ══════════════════════════════════════════════════════════════ */
const STORAGE_KEY = 'bb_utenti';

const UTENTI_DEFAULT = [
  { id: 1, nome: 'Admin',    email: 'admin@bugboard.io', password: 'admin123', ruolo: 'admin',    default: true },
  { id: 2, nome: 'Dev User', email: 'dev@bugboard.io',   password: 'dev123',   ruolo: 'normal',   default: false },
  { id: 3, nome: 'Guest',    email: 'guest@bugboard.io', password: 'guest123', ruolo: 'readonly', default: false },
];

function getUtenti() {
  const raw = localStorage.getItem(STORAGE_KEY);
  if (!raw) {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(UTENTI_DEFAULT));
    return UTENTI_DEFAULT;
  }
  return JSON.parse(raw);
}

function salvaUtenti(lista) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(lista));
}

function nuovoId() {
  const utenti = getUtenti();
  return utenti.length ? Math.max(...utenti.map(u => u.id)) + 1 : 1;
}


/* ══════════════════════════════════════════════════════════════
   INIZIALIZZAZIONE UI
   ══════════════════════════════════════════════════════════════ */
function inizializzaUI() {
  const iniziali = utente.nome
    .split(' ')
    .map(p => p[0])
    .join('')
    .toUpperCase()
    .slice(0, 2);

  document.getElementById('sidebarAvatarText').textContent = iniziali;
  document.getElementById('topAvatarText').textContent     = iniziali;
  document.getElementById('sidebarNome').textContent       = utente.nome;
  document.getElementById('topNome').textContent           = utente.nome;

  const badgeRuoloEl = document.getElementById('sidebarRuoloBadge');
  badgeRuoloEl.textContent = utente.ruolo;
  badgeRuoloEl.className   = 'text-[11px] font-mono text-brand-600';
}


/* ══════════════════════════════════════════════════════════════
   STATS — conta gli utenti per ruolo
   ══════════════════════════════════════════════════════════════ */
function aggiornaStats() {
  const tutti = getUtenti();
  document.getElementById('statTotale').textContent   = tutti.length;
  document.getElementById('statAdmin').textContent    = tutti.filter(u => u.ruolo === 'admin').length;
  document.getElementById('statNormal').textContent   = tutti.filter(u => u.ruolo === 'normal').length;
  document.getElementById('statReadonly').textContent = tutti.filter(u => u.ruolo === 'readonly').length;
}


/* ══════════════════════════════════════════════════════════════
   RENDER TABELLA UTENTI
   Con Angular: *ngFor con pipe di filtro, triggered da FormControl.valueChanges.
   ══════════════════════════════════════════════════════════════ */
function renderUtenti() {
  const cerca = document.getElementById('cercaUtente').value.toLowerCase();
  const ruolo = document.getElementById('filtroRuolo').value;

  let utenti = getUtenti();

  if (cerca) {
    utenti = utenti.filter(u =>
      u.nome.toLowerCase().includes(cerca) ||
      u.email.toLowerCase().includes(cerca)
    );
  }
  if (ruolo) {
    utenti = utenti.filter(u => u.ruolo === ruolo);
  }

  document.getElementById('utentiCountLabel').textContent =
    `${utenti.length} ${utenti.length === 1 ? 'utente trovato' : 'utenti trovati'}`;

  const tbody = document.getElementById('utentiTableBody');

  if (utenti.length === 0) {
    tbody.innerHTML = `
      <tr>
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
      </tr>`;
    return;
  }

  tbody.innerHTML = utenti.map(u => `
    <tr class="transition-colors hover:bg-ink-50/60 group">

      <!-- Avatar + nome -->
      <td class="px-5 py-4">
        <div class="flex items-center gap-3">
          <div class="w-8 h-8 rounded-full ${coloreAvatar(u.ruolo)}
                      flex items-center justify-center flex-shrink-0">
            <span class="font-mono text-[11px] font-medium text-white">
              ${u.nome.split(' ').map(p => p[0]).join('').toUpperCase().slice(0, 2)}
            </span>
          </div>
          <div>
            <p class="text-sm font-medium text-ink-800">${escapeHtml(u.nome)}</p>
            ${u.default ? `<span class="text-[10px] font-mono text-ink-400">account di sistema</span>` : ''}
          </div>
        </div>
      </td>

      <!-- Email -->
      <td class="px-4 py-4">
        <span class="font-mono text-[12px] text-ink-500">${escapeHtml(u.email)}</span>
      </td>

      <!-- Badge ruolo -->
      <td class="px-4 py-4">${badgeRuolo(u.ruolo)}</td>

      <!-- Data creazione (mock) -->
      <td class="px-4 py-4 hidden lg:table-cell">
        <span class="font-mono text-[12px] text-ink-400">
          ${u.default ? '01 gen 2025' : formattaData(new Date().toISOString())}
        </span>
      </td>

      <!-- Azioni: il pulsante appare solo all'hover e non per gli account di sistema -->
      <td class="px-4 py-4 text-right">
        ${u.default
          ? `<span class="text-[11px] font-mono text-ink-300 pr-1">protetto</span>`
          : `<button onclick="eliminaUtente(${u.id}, '${escapeHtml(u.nome)}')"
                     class="opacity-0 group-hover:opacity-100 transition-opacity
                            inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg
                            text-xs font-medium text-red-600
                            hover:bg-red-50 border border-transparent hover:border-red-100
                            transition-colors">
               <svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none"
                 stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                 <polyline points="3 6 5 6 21 6"/>
                 <path d="M19 6l-1 14H6L5 6"/>
                 <path d="M10 11v6"/><path d="M14 11v6"/><path d="M9 6V4h6v2"/>
               </svg>
               Elimina
             </button>`
        }
      </td>

    </tr>
  `).join('');
}


/* ══════════════════════════════════════════════════════════════
   CREA UTENTE
   Con Angular: ReactiveForm + UserService.create(dto).subscribe()
   ══════════════════════════════════════════════════════════════ */
function creaUtente() {
  const nome     = document.getElementById('inputNome').value.trim();
  const email    = document.getElementById('inputEmail').value.trim().toLowerCase();
  const password = document.getElementById('inputPassword').value;
  const ruolo    = document.getElementById('inputRuolo').value;

  nascondiMessaggio();

  // Validazione lato client — con Angular useremmo Validators di ReactiveForm
  if (!nome) { mostraMessaggio('Il nome è obbligatorio.', 'errore'); return; }
  if (!email || !email.includes('@')) { mostraMessaggio('Inserisci un\'email valida.', 'errore'); return; }
  if (!password || password.length < 6) { mostraMessaggio('La password deve avere almeno 6 caratteri.', 'errore'); return; }

  // Controllo duplicati — lato server sarà gestito dal backend con un 409 Conflict
  if (getUtenti().find(u => u.email === email)) {
    mostraMessaggio('Esiste già un utente con questa email.', 'errore');
    return;
  }

  const nuovo = { id: nuovoId(), nome, email, password, ruolo, default: false };
  const utenti = getUtenti();
  utenti.push(nuovo);
  salvaUtenti(utenti);

  mostraMessaggio(`Utente "${nome}" creato con successo.`, 'successo');
  resetForm();
  renderUtenti();
  aggiornaStats();
}


/* ══════════════════════════════════════════════════════════════
   ELIMINA UTENTE
   Con Angular: MatDialog di conferma + UserService.delete(id).subscribe()
   ══════════════════════════════════════════════════════════════ */
function eliminaUtente(id, nome) {
  if (!confirm(`Eliminare l'utente "${nome}"?\nQuesta azione non può essere annullata.`)) return;
  salvaUtenti(getUtenti().filter(u => u.id !== id));
  renderUtenti();
  aggiornaStats();
}


/* ══════════════════════════════════════════════════════════════
   TOGGLE VISIBILITÀ PASSWORD nel form di creazione
   ══════════════════════════════════════════════════════════════ */
function togglePasswordForm() {
  const input  = document.getElementById('inputPassword');
  const icona  = document.getElementById('eyeIconForm');
  const ora    = input.type === 'text';
  input.type   = ora ? 'password' : 'text';
  icona.innerHTML = ora
    ? `<path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
       <circle cx="12" cy="12" r="3"/>`
    : `<path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8
                a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4
                c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07
                a3 3 0 1 1-4.24-4.24"/>
       <line x1="1" y1="1" x2="23" y2="23"/>`;
}


/* ── Messaggi di feedback ────────────────────────────────────── */
function mostraMessaggio(testo, tipo) {
  const el = document.getElementById('formMessaggio');
  const stili = {
    successo: 'bg-green-50 border-green-200 text-green-700',
    errore:   'bg-red-50 border-red-200 text-red-600',
  };
  el.className   = `mt-4 px-4 py-3 rounded-lg border text-sm ${stili[tipo]}`;
  el.textContent = testo;
  el.classList.remove('hidden');
  if (tipo === 'successo') setTimeout(nascondiMessaggio, 4000);
}
function nascondiMessaggio() {
  document.getElementById('formMessaggio').classList.add('hidden');
}
function resetForm() {
  ['inputNome', 'inputEmail', 'inputPassword'].forEach(id => {
    document.getElementById(id).value = '';
  });
  document.getElementById('inputRuolo').value = 'normal';
}


/* ── Utility ──────────────────────────────────────────────────── */
function escapeHtml(str) {
  const div = document.createElement('div');
  div.textContent = str;
  return div.innerHTML;
}

function formattaData(iso) {
  return new Date(iso).toLocaleDateString('it-IT', {
    day: '2-digit', month: 'short', year: 'numeric',
  });
}

function coloreAvatar(ruolo) {
  return { admin: 'bg-brand-500', normal: 'bg-blue-400', readonly: 'bg-ink-400' }[ruolo] || 'bg-ink-400';
}

function badgeRuolo(ruolo) {
  const stili = {
    admin:    'bg-brand-50 text-brand-700 border-brand-100',
    normal:   'bg-blue-50 text-blue-600 border-blue-100',
    readonly: 'bg-ink-100 text-ink-500 border-ink-200',
  };
  const label = { admin: 'Admin', normal: 'Normale', readonly: 'Readonly' };
  return `<span class="inline-flex px-2 py-0.5 rounded text-[11px] font-mono font-medium border ${stili[ruolo] || stili.readonly}">
            ${label[ruolo] || ruolo}
          </span>`;
}


/* ── Sidebar mobile ──────────────────────────────────────────── */
function aprireSidebar()  {
  document.getElementById('sidebar').classList.add('open');
  document.getElementById('overlay').classList.remove('hidden');
}
function chiudiSidebar() {
  document.getElementById('sidebar').classList.remove('open');
  document.getElementById('overlay').classList.add('hidden');
}

function logout() {
  sessionStorage.removeItem('bb_utente');
  window.location.href = 'login.html';
}


/* ── Avvio ──────────────────────────────────────────────────── */
inizializzaUI();
aggiornaStats();
renderUtenti();
