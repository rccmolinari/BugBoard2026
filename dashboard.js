/*
 * dashboard.js
 *
 * Logica della dashboard principale.
 *
 * NOTA PER ANGULAR:
 * Questo file si distribuirà così:
 *   - ISSUE_DEMO        → IssueService.getIssues() con HTTP GET /api/issues
 *   - getUtente()       → AuthService.utenteCorrente$ (Observable)
 *   - inizializzaUI()   → DashboardComponent.ngOnInit()
 *   - aggiornaStats()   → DashboardService.getStats() o computed dal component
 *   - renderTabella()   → *ngFor su un array filtrato, con un pipe custom
 *   - badge helpers     → BadgePipe o un componente <app-badge tipo="bug">
 *   - logout()          → AuthService.logout() + Router.navigate(['/login'])
 */


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
];


/* ══════════════════════════════════════════════════════════════
   AUTH GUARD
   Controlla che ci sia una sessione attiva; altrimenti manda
   al login. Con Angular sarà un AuthGuard.canActivate().
   ══════════════════════════════════════════════════════════════ */
function getUtente() {
  const raw = sessionStorage.getItem('bb_utente');
  return raw ? JSON.parse(raw) : null;
}

const utente = getUtente();

if (!utente) {
  window.location.href = 'login.html';
}


/* ══════════════════════════════════════════════════════════════
   INIZIALIZZAZIONE UI
   Popola sidebar e topbar con i dati dell'utente,
   e mostra/nasconde gli elementi in base al ruolo.
   ══════════════════════════════════════════════════════════════ */
function inizializzaUI() {
  // Calcolo le iniziali per gli avatar (max 2 caratteri)
  const iniziali = utente.nome
    .split(' ')
    .map(p => p[0])
    .join('')
    .toUpperCase()
    .slice(0, 2);

  // Popolo gli elementi del DOM
  document.getElementById('sidebarAvatarText').textContent = iniziali;
  document.getElementById('topAvatarText').textContent     = iniziali;
  document.getElementById('sidebarNome').textContent       = utente.nome;
  document.getElementById('topNome').textContent           = utente.nome;

  // Badge del ruolo: colore diverso per ogni livello
  const coloriRuolo = {
    admin:    'text-brand-600',
    normal:   'text-blue-500',
    readonly: 'text-ink-400',
  };
  const badgeRuolo = document.getElementById('sidebarRuoloBadge');
  badgeRuolo.textContent = utente.ruolo;
  badgeRuolo.className   = `text-[11px] font-mono ${coloriRuolo[utente.ruolo] || 'text-ink-400'}`;

  // Mostro il link Amministrazione solo per gli admin
  if (utente.ruolo === 'admin') {
    document.getElementById('navAdmin').classList.remove('hidden');
  }

  // Per i readonly nascondo tutto quello che riguarda la modifica
  if (utente.ruolo === 'readonly') {
    document.getElementById('navNuovaIssue').classList.add('hidden');
    document.getElementById('btnNuovaIssue').classList.add('hidden');
  }
}


/* ══════════════════════════════════════════════════════════════
   STATISTICHE
   Conta le issue per stato/priorità e aggiorna i card.
   Con Angular: DashboardService.getStats() → Observable<Stats>
   ══════════════════════════════════════════════════════════════ */
function aggiornaStats() {
  const totale   = ISSUE_DEMO.length;
  const todo     = ISSUE_DEMO.filter(i => i.stato === 'todo').length;
  const inCorso  = ISSUE_DEMO.filter(i => i.stato === 'in-progress').length;
  // Bug critici = priorità critical E non ancora chiusi
  const critici  = ISSUE_DEMO.filter(i =>
    i.priorita === 'critical' &&
    i.stato !== 'done' &&
    i.stato !== 'closed'
  ).length;

  document.getElementById('statTotale').textContent   = totale;
  document.getElementById('statTodo').textContent     = todo;
  document.getElementById('statProgress').textContent = inCorso;
  document.getElementById('statCritici').textContent  = critici;
}


/* ══════════════════════════════════════════════════════════════
   TABELLA ISSUE
   Legge i filtri dal DOM, filtra le issue e le renderizza.
   Con Angular: *ngFor su filteredIssues, aggiornato da un setter
   o da un pipe puro sul template.
   ══════════════════════════════════════════════════════════════ */
function renderTabella() {
  const cerca = document.getElementById('cercaTitolo').value.toLowerCase();
  const tipo  = document.getElementById('filtroTipo').value;
  const stato = document.getElementById('filtroStato').value;

  const filtrate = ISSUE_DEMO.filter(issue => {
    const matchTitolo = issue.titolo.toLowerCase().includes(cerca);
    const matchTipo   = !tipo  || issue.tipo  === tipo;
    const matchStato  = !stato || issue.stato === stato;
    return matchTitolo && matchTipo && matchStato;
  });

  // Aggiorno i contatori
  document.getElementById('issueCountLabel').textContent =
    `${filtrate.length} issue trovate`;
  document.getElementById('issueTableFooter').textContent =
    `Mostrate ${filtrate.length} di ${ISSUE_DEMO.length}`;

  const tbody = document.getElementById('issueTableBody');

  // Empty state se i filtri non producono risultati
  if (filtrate.length === 0) {
    tbody.innerHTML = `
      <tr>
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
      </tr>`;
    return;
  }

  tbody.innerHTML = filtrate.map(issue => `
    <tr class="issue-row transition-colors" onclick="apriIssue(${issue.id})">

      <!-- ID in monospace -->
      <td class="px-5 py-3.5 whitespace-nowrap">
        <span class="font-mono text-[12px] text-ink-400">#${issue.id}</span>
      </td>

      <!-- Titolo + etichette -->
      <td class="px-4 py-3.5">
        <div class="flex items-center gap-2">
          <span class="text-ink-800 font-medium max-w-xs truncate block">
            ${escapeHtml(issue.titolo)}
          </span>
          <div class="hidden lg:flex items-center gap-1">
            ${(issue.etichette || []).slice(0, 2).map(e => `
              <span class="px-1.5 py-0.5 text-[10px] font-mono rounded
                           bg-ink-100 text-ink-500 border border-ink-200">
                ${escapeHtml(e)}
              </span>`).join('')}
            ${issue.etichette?.length > 2
              ? `<span class="text-[11px] text-ink-400">+${issue.etichette.length - 2}</span>`
              : ''}
          </div>
        </div>
      </td>

      <td class="px-4 py-3.5 whitespace-nowrap">${badgeTipo(issue.tipo)}</td>
      <td class="px-4 py-3.5 whitespace-nowrap">${badgePriorita(issue.priorita)}</td>
      <td class="px-4 py-3.5 whitespace-nowrap">${badgeStato(issue.stato)}</td>

      <!-- Assegnatario con mini-avatar -->
      <td class="px-4 py-3.5 whitespace-nowrap">
        ${issue.assegnata
          ? `<div class="flex items-center gap-2">
               <div class="w-5 h-5 rounded-full bg-brand-500/20 flex items-center justify-center flex-shrink-0">
                 <span class="text-[9px] font-mono text-brand-700 font-medium">
                   ${issue.assegnata.split(' ').map(p => p[0]).join('').toUpperCase().slice(0, 2)}
                 </span>
               </div>
               <span class="text-sm text-ink-600">${escapeHtml(issue.assegnata)}</span>
             </div>`
          : `<span class="text-ink-300 text-sm">—</span>`
        }
      </td>

      <!-- Scadenza — rossa se è già passata e l'issue non è chiusa -->
      <td class="px-4 py-3.5 whitespace-nowrap hidden lg:table-cell">
        ${issue.scadenza
          ? `<span class="font-mono text-[12px] ${isScaduta(issue) ? 'text-red-500 font-medium' : 'text-ink-400'}">
               ${formattaData(issue.scadenza)}
             </span>`
          : `<span class="text-ink-200 text-sm">—</span>`
        }
      </td>

    </tr>
  `).join('');
}


/* ══════════════════════════════════════════════════════════════
   BADGE HELPERS
   Piccole funzioni che restituiscono HTML con le classi Tailwind
   giuste per tipo, stato e priorità.
   Con Angular diventano un BadgePipe o <app-badge> component.
   ══════════════════════════════════════════════════════════════ */
function badgeTipo(tipo) {
  const stili = {
    bug:           'bg-red-50 text-red-600 border-red-100',
    feature:       'bg-blue-50 text-blue-600 border-blue-100',
    question:      'bg-amber-50 text-amber-600 border-amber-100',
    documentation: 'bg-violet-50 text-violet-600 border-violet-100',
  };
  const label = { bug: 'Bug', feature: 'Feature', question: 'Question', documentation: 'Docs' };
  const cls   = stili[tipo] || 'bg-ink-50 text-ink-400 border-ink-200';
  return `<span class="inline-flex px-2 py-0.5 rounded text-[11px] font-mono font-medium border ${cls}">
            ${label[tipo] || tipo}
          </span>`;
}

function badgeStato(stato) {
  const stili = {
    'todo':        'bg-ink-100 text-ink-500 border-ink-200',
    'in-progress': 'bg-blue-50 text-blue-600 border-blue-100',
    'done':        'bg-green-50 text-green-600 border-green-100',
    'closed':      'bg-ink-50 text-ink-400 border-ink-200',
  };
  const label = { 'todo': 'Todo', 'in-progress': 'In Progress', 'done': 'Done', 'closed': 'Closed' };
  const cls   = stili[stato] || 'bg-ink-50 text-ink-400 border-ink-200';
  return `<span class="inline-flex px-2 py-0.5 rounded text-[11px] font-mono font-medium border ${cls}">
            ${label[stato] || stato}
          </span>`;
}

function badgePriorita(p) {
  if (!p) return `<span class="text-ink-200 text-sm">—</span>`;
  const stili = {
    critical: 'bg-red-50 text-red-600 border-red-100',
    high:     'bg-orange-50 text-orange-600 border-orange-100',
    medium:   'bg-amber-50 text-amber-500 border-amber-100',
    low:      'bg-green-50 text-green-600 border-green-100',
  };
  const cls = stili[p] || 'bg-ink-50 text-ink-400 border-ink-200';
  return `<span class="inline-flex px-2 py-0.5 rounded text-[11px] font-mono font-medium border ${cls}">
            ${p.charAt(0).toUpperCase() + p.slice(1)}
          </span>`;
}


/* ══════════════════════════════════════════════════════════════
   UTILITY
   ══════════════════════════════════════════════════════════════ */

// Formatta una data ISO in "15 apr 2025" con la locale italiana
function formattaData(iso) {
  if (!iso) return '—';
  return new Date(iso).toLocaleDateString('it-IT', {
    day: '2-digit', month: 'short', year: 'numeric',
  });
}

// Controlla se la scadenza è passata e l'issue è ancora aperta
function isScaduta(issue) {
  if (!issue.scadenza) return false;
  if (issue.stato === 'done' || issue.stato === 'closed') return false;
  return new Date(issue.scadenza) < new Date();
}

// Escape HTML per evitare XSS quando inserisco testo utente nel DOM
function escapeHtml(str) {
  const div = document.createElement('div');
  div.textContent = str;
  return div.innerHTML;
}

// Navigazione al dettaglio issue — con Angular sarà Router.navigate(['/issues', id])
function apriIssue(id) {
  console.log('Apri issue', id);
  // TODO: window.location.href = `issue-detail.html?id=${id}`;
}


/* ── Sidebar mobile ──────────────────────────────────────────── */
function aprireSidebar() {
  document.getElementById('sidebar').classList.add('open');
  document.getElementById('overlay').classList.remove('hidden');
}

function chiudiSidebar() {
  document.getElementById('sidebar').classList.remove('open');
  document.getElementById('overlay').classList.add('hidden');
}


/* ── Logout ──────────────────────────────────────────────────── */
function logout() {
  // Con Angular: AuthService.logout() invalida il token sul server, poi naviga al login
  sessionStorage.removeItem('bb_utente');
  window.location.href = 'login.html';
}


/* ══════════════════════════════════════════════════════════════
   AVVIO
   Eseguito appena il file viene caricato (il DOM è già pronto
   perché questo script è in fondo al body).
   ══════════════════════════════════════════════════════════════ */
inizializzaUI();
aggiornaStats();
renderTabella();
