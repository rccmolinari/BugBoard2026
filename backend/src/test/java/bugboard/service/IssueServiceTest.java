package bugboard.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import bugboard.event.IssueAssegnataEvent;
import bugboard.exception.BadRequestException;
import bugboard.exception.ConflictException;
import bugboard.exception.ForbiddenException;
import bugboard.exception.NotFoundException;
import bugboard.mapper.IssueMapper;
import bugboard.model.Issue;
import bugboard.model.Issue.StatoIssue;
import bugboard.model.Utente;
import bugboard.repository.IssueRepository;
import bugboard.repository.UserRepository;

    /*
    * Test di unita' per IssueService: le dipendenze sono mockate con Mockito,
    * quindi non serve un database.
    *
    * Per ogni metodo ci sono due gruppi di test. I test "ec_" seguono l'approccio
    * black-box delle classi di equivalenza con criterio each-choice: per ogni
    * parametro individuiamo le classi, indicate tra parentesi graffe, e ogni
    * classe (valida o non valida) compare in almeno un test. I test "cc_" sono
    * invece white-box e puntano alla condition coverage, cioe' ogni operando
    * delle condizioni composte del codice deve risultare sia vero che falso
    * almeno una volta. Servono entrambi: nei test black-box in cui combiniamo
    * piu' classi non valide si osserva solo la prima eccezione lanciata, perche'
    * il primo controllo che fallisce corto-circuita gli altri, quindi il
    * black-box da solo non basterebbe a esercitare tutti i controlli.
    */
    @ExtendWith(MockitoExtension.class)
    class IssueServiceTest {

        @Mock private IssueRepository issueRepository;
        @Mock private UserRepository utenteRepository;
        @Mock private ISessioneService sessioneService;
        @Mock private IssueMapper issueMapper;
        @Mock private ApplicationEventPublisher eventPublisher;

        @InjectMocks private IssueService issueService;

        private final UUID adminSid = UUID.randomUUID();
        private final UUID userSid = UUID.randomUUID();

        // ---------- helper ----------

        private Utente admin(String email) {
            Utente u = new Utente();
            u.setId(1);
            u.setEmail(email);
            u.setRole(Utente.Role.ADMIN);
            return u;
        }

        private Utente user(int id, String email) {
            Utente u = new Utente();
            u.setId(id);
            u.setEmail(email);
            u.setRole(Utente.Role.USER);
            return u;
        }

        private Issue issue(StatoIssue stato, long version) {
            Issue i = new Issue();
            i.setId(10);
            i.setStato(stato);
            i.setVersion(version);
            return i;
        }

        // ------------------- test di assignIssueToUser -------------------

        /*
        * Parte black-box: classi di equivalenza, criterio each-choice.
        *
        * Per issueId le classi sono {issue esistente}, valida, e {issue
        * inesistente}, non valida perche' porta a NotFound. Per userEmail:
        * {utente esistente con ruolo USER} valida, {utente inesistente} non
        * valida (NotFound), {utente esistente ma di ruolo diverso da USER} non
        * valida (BadRequest, una issue si puo' assegnare solo a un USER). Per
        * expiringDate, che e' opzionale, sono valide entrambe le classi {null}
        * e {data valorizzata}. Per expectedVersion: {null} e {uguale alla
        * versione corrente} valide, {diversa dalla versione corrente} non
        * valida (Conflict). Per adminSID: {sessione di un admin} valida,
        * {sessione nulla} e {sessione di un non admin} non valide (Forbidden).
        *
        * Con l'each-choice bastano tanti test quante sono le classi del
        * parametro che ne ha di piu', cioe' tre. Nel primo tutti i parametri
        * stanno su una classe valida; negli altri due le classi non valide dei
        * vari parametri sono combinate tra loro, quindi si osserva solo la
        * prima eccezione: il controllo sulla sessione viene eseguito per primo
        * e maschera gli altri. Le classi sulla versione, che qui restano
        * mascherate, vengono comunque esercitate dai test white-box cc_assign_5
        * e cc_assign_6.
        */

        @Test
        void ec_assign_1_tuttiValidi() {
            // Tutti i parametri su una classe valida: {issue esistente},
            // {destinatario con ruolo USER}, {scadenza valorizzata},
            // {versione null}, {sessione admin}.
            Issue issue = issue(StatoIssue.TODO, 0L);
            when(sessioneService.getUtenteBySessionId(adminSid)).thenReturn(admin("admin@test.it"));
            when(issueRepository.findById(10)).thenReturn(Optional.of(issue));
            when(utenteRepository.findByEmail("mario@test.it")).thenReturn(Optional.of(user(2, "mario@test.it")));

            LocalDate scadenza = LocalDate.now().plusDays(7);
            issueService.assignIssueToUser(10, "mario@test.it", scadenza, null, adminSid);

            assertEquals(scadenza.atTime(23, 59, 59), issue.getDataScadenza());
            assertEquals(StatoIssue.IN_PROGRESS, issue.getStato());
        }

        @Test
        void ec_assign_2_nonValideCombinate() {
            // Classi non valide combinate: {issue inesistente}, {destinatario
            // inesistente} e {sessione nulla}; la versione sta sulla classe valida
            // {uguale alla corrente}. Il controllo sulla sessione fallisce per
            // primo e maschera gli altri, quindi si osserva solo Forbidden.
            when(sessioneService.getUtenteBySessionId(adminSid)).thenReturn(null);

            assertThrows(ForbiddenException.class,
                () -> issueService.assignIssueToUser(999, "ghost@test.it", null, 0L, adminSid));
        }

        @Test
        void ec_assign_3_nonValideCombinate() {
            // Classi non valide combinate: {destinatario di ruolo non USER},
            // {versione diversa dalla corrente} e {sessione di un non admin}.
            // Anche qui la sessione viene controllata per prima e maschera il
            // resto: si osserva solo Forbidden.
            when(sessioneService.getUtenteBySessionId(adminSid)).thenReturn(user(5, "user@test.it"));

            assertThrows(ForbiddenException.class,
                () -> issueService.assignIssueToUser(10, "admin2@test.it", null, 1L, adminSid));
        }

        /*
        * Parte white-box: condition coverage.
        *
        * L'obiettivo e' far valutare ogni operando di ogni condizione del
        * metodo, compresi gli helper requireAdmin, findIssueOr404 e
        * checkVersion, sia a vero che a falso. cc_assign_1 e' il caso in cui
        * tutti i controlli passano, quindi copre da solo il lato "si prosegue"
        * di quasi tutte le condizioni; ognuno degli altri test fa scattare un
        * controllo specifico: sessione nulla, utente non admin, issue
        * inesistente, versione diversa e versione uguale, destinatario
        * inesistente o di ruolo sbagliato, i due stati (CLOSED e DONE) in cui
        * la issue non e' assegnabile e infine i due rami della riassegnazione,
        * cioe' issue scaduta che torna in lavorazione e issue gia' in
        * lavorazione che resta dov'e'.
        */

        @Test
        void cc_assign_1_valida_TODO() { // caso valido: tutti i controlli passano e da TODO si va in IN_PROGRESS
            Utente dest = user(2, "mario@test.it");
            Issue issue = issue(StatoIssue.TODO, 0L);
            when(sessioneService.getUtenteBySessionId(adminSid)).thenReturn(admin("admin@test.it"));
            when(issueRepository.findById(10)).thenReturn(Optional.of(issue));
            when(utenteRepository.findByEmail("mario@test.it")).thenReturn(Optional.of(dest));

            issueService.assignIssueToUser(10, "mario@test.it", null, null, adminSid);

            assertEquals(StatoIssue.IN_PROGRESS, issue.getStato());
            assertEquals(dest, issue.getAssegnatoA());
            verify(issueRepository).save(issue);
            verify(eventPublisher).publishEvent(any(IssueAssegnataEvent.class));
        }

        @Test
        void cc_assign_2_sessioneNulla_forbidden() { // la sessione non corrisponde a nessun utente
            when(sessioneService.getUtenteBySessionId(adminSid)).thenReturn(null);

            assertThrows(ForbiddenException.class,
                () -> issueService.assignIssueToUser(10, "mario@test.it", null, null, adminSid));
        }

        @Test
        void cc_assign_3_nonAdmin_forbidden() { // sessione valida ma di un utente semplice, non admin
            when(sessioneService.getUtenteBySessionId(adminSid)).thenReturn(user(5, "user@test.it"));

            assertThrows(ForbiddenException.class,
                () -> issueService.assignIssueToUser(10, "mario@test.it", null, null, adminSid));
        }

        @Test
        void cc_assign_4_issueInesistente_notFound() { // la issue cercata non esiste
            when(sessioneService.getUtenteBySessionId(adminSid)).thenReturn(admin("admin@test.it"));
            when(issueRepository.findById(10)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class,
                () -> issueService.assignIssueToUser(10, "mario@test.it", null, null, adminSid));
        }

        @Test
        void cc_assign_5_versioneDiversa_conflict() { // il client manda una versione diversa da quella corrente
            when(sessioneService.getUtenteBySessionId(adminSid)).thenReturn(admin("admin@test.it"));
            when(issueRepository.findById(10)).thenReturn(Optional.of(issue(StatoIssue.TODO, 2L)));

            assertThrows(ConflictException.class,
                () -> issueService.assignIssueToUser(10, "mario@test.it", null, 1L, adminSid));
        }

        @Test
        void cc_assign_6_versioneUguale_conData() { // versione uguale a quella corrente e scadenza impostata
            Issue issue = issue(StatoIssue.TODO, 5L);
            when(sessioneService.getUtenteBySessionId(adminSid)).thenReturn(admin("admin@test.it"));
            when(issueRepository.findById(10)).thenReturn(Optional.of(issue));
            when(utenteRepository.findByEmail("mario@test.it")).thenReturn(Optional.of(user(2, "mario@test.it")));

            LocalDate scadenza = LocalDate.now().plusDays(7);
            issueService.assignIssueToUser(10, "mario@test.it", scadenza, 5L, adminSid);

            assertEquals(scadenza.atTime(23, 59, 59), issue.getDataScadenza());
            assertEquals(StatoIssue.IN_PROGRESS, issue.getStato());
        }

        @Test
        void cc_assign_7_destinatarioInesistente_notFound() { // l'email indicata non corrisponde a nessun utente
            when(sessioneService.getUtenteBySessionId(adminSid)).thenReturn(admin("admin@test.it"));
            when(issueRepository.findById(10)).thenReturn(Optional.of(issue(StatoIssue.TODO, 0L)));
            when(utenteRepository.findByEmail("ghost@test.it")).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class,
                () -> issueService.assignIssueToUser(10, "ghost@test.it", null, null, adminSid));
        }

        @Test
        void cc_assign_8_destinatarioNonUser_badRequest() { // il destinatario esiste ma e' un admin, non assegnabile
            when(sessioneService.getUtenteBySessionId(adminSid)).thenReturn(admin("admin@test.it"));
            when(issueRepository.findById(10)).thenReturn(Optional.of(issue(StatoIssue.TODO, 0L)));
            when(utenteRepository.findByEmail("admin2@test.it")).thenReturn(Optional.of(admin("admin2@test.it")));

            assertThrows(BadRequestException.class,
                () -> issueService.assignIssueToUser(10, "admin2@test.it", null, null, adminSid));
        }

        @Test
        void cc_assign_9_statoClosed_badRequest() { // issue chiusa, primo operando dell'or sullo stato
            when(sessioneService.getUtenteBySessionId(adminSid)).thenReturn(admin("admin@test.it"));
            when(issueRepository.findById(10)).thenReturn(Optional.of(issue(StatoIssue.CLOSED, 0L)));
            when(utenteRepository.findByEmail("mario@test.it")).thenReturn(Optional.of(user(2, "mario@test.it")));

            assertThrows(BadRequestException.class,
                () -> issueService.assignIssueToUser(10, "mario@test.it", null, null, adminSid));
        }

        @Test
        void cc_assign_10_statoDone_badRequest() { // issue completata, secondo operando dell'or sullo stato
            when(sessioneService.getUtenteBySessionId(adminSid)).thenReturn(admin("admin@test.it"));
            when(issueRepository.findById(10)).thenReturn(Optional.of(issue(StatoIssue.DONE, 0L)));
            when(utenteRepository.findByEmail("mario@test.it")).thenReturn(Optional.of(user(2, "mario@test.it")));

            assertThrows(BadRequestException.class,
                () -> issueService.assignIssueToUser(10, "mario@test.it", null, null, adminSid));
        }

        @Test
        void cc_assign_11_statoExpired_riportataInProgress() { // issue scaduta: la riassegnazione azzera la scadenza e la rimette in lavorazione
            Issue issue = issue(StatoIssue.EXPIRED, 0L);
            issue.setDataScadenza(LocalDateTime.now().minusDays(1));
            when(sessioneService.getUtenteBySessionId(adminSid)).thenReturn(admin("admin@test.it"));
            when(issueRepository.findById(10)).thenReturn(Optional.of(issue));
            when(utenteRepository.findByEmail("mario@test.it")).thenReturn(Optional.of(user(2, "mario@test.it")));

            issueService.assignIssueToUser(10, "mario@test.it", null, null, adminSid);

            assertNull(issue.getDataScadenza());
            assertEquals(StatoIssue.IN_PROGRESS, issue.getStato());
        }

        @Test
        void cc_assign_12_riassegnaInProgress_restaInProgress() { // issue gia' in lavorazione e non scaduta
            Issue issue = issue(StatoIssue.IN_PROGRESS, 0L);
            when(sessioneService.getUtenteBySessionId(adminSid)).thenReturn(admin("admin@test.it"));
            when(issueRepository.findById(10)).thenReturn(Optional.of(issue));
            when(utenteRepository.findByEmail("mario@test.it")).thenReturn(Optional.of(user(2, "mario@test.it")));

            issueService.assignIssueToUser(10, "mario@test.it", null, null, adminSid);

            assertEquals(StatoIssue.IN_PROGRESS, issue.getStato()); // non-TODO e non-scaduta: lo stato non cambia
            verify(issueRepository).save(issue);
        }

        // ------------------- test di aggiungiCommento -------------------

        /*
        * Parte black-box: classi di equivalenza, criterio each-choice.
        *
        * Per idIssue le classi sono {issue esistente}, valida, e {issue
        * inesistente}, non valida (NotFound). Per testo: {testo non vuoto}
        * valida, {null} e {vuoto o di soli spazi} non valide, entrambe
        * BadRequest. Per expectedVersion: {null} e {uguale alla versione
        * corrente} valide, {diversa dalla versione corrente} non valida
        * (Conflict). Per sid: {sessione valida} valida, {sessione non valida o
        * nulla} non valida (Forbidden).
        *
        * Il parametro con piu' classi e' il testo, che ne ha tre, quindi
        * bastano tre test. Le classi non valide sono combinate: in
        * ec_commento_2 fallisce per primo il controllo sulla sessione e si
        * osserva solo Forbidden, in ec_commento_3 il controllo sul testo
        * precede quello sulla versione e si osserva solo BadRequest. I due casi
        * sulla versione, qui mascherati, sono coperti dai white-box
        * cc_commento_6 e cc_commento_7.
        */

        @Test
        void ec_commento_1_tuttiValidi() {
            // Tutti i parametri su una classe valida: {issue esistente},
            // {testo non vuoto}, {versione null}, {sessione valida}.
            Utente u = user(5, "mario@test.it");
            Issue issue = issue(StatoIssue.IN_PROGRESS, 0L);
            issue.setAssegnatoA(u);
            when(sessioneService.getUtenteBySessionId(userSid)).thenReturn(u);
            when(issueRepository.findById(10)).thenReturn(Optional.of(issue));

            issueService.aggiungiCommento(10, "ok", null, userSid);

            assertEquals(1, issue.getCommento().size());
        }

        @Test
        void ec_commento_2_nonValideCombinate() {
            // Classi non valide combinate: {issue inesistente}, {testo null} e
            // {sessione non valida}; la versione sta su {uguale alla corrente}.
            // Il controllo sulla sessione fallisce per primo, quindi si osserva
            // solo Forbidden.
            when(sessioneService.getUtenteBySessionId(userSid)).thenReturn(null);

            assertThrows(ForbiddenException.class,
                () -> issueService.aggiungiCommento(999, null, 0L, userSid));
        }

        @Test
        void ec_commento_3_nonValideCombinate() {
            // Classi non valide combinate: {testo vuoto} e {versione diversa
            // dalla corrente}, con sessione e issue valide. Il controllo sul
            // testo precede quello sulla versione, quindi si osserva solo
            // BadRequest.
            when(sessioneService.getUtenteBySessionId(userSid)).thenReturn(user(5, "mario@test.it"));

            assertThrows(BadRequestException.class,
                () -> issueService.aggiungiCommento(10, "   ", 1L, userSid));
        }

        /*
        * Parte white-box: condition coverage di aggiungiCommento.
        *
        * Come sopra, cc_commento_1 e' il caso valido che copre il lato "si
        * prosegue" di tutti i controlli, mentre gli altri test li fanno fallire
        * uno alla volta: sessione nulla, testo null, testo di soli spazi, issue
        * inesistente, versione diversa e versione uguale, i tre stati in cui
        * non si puo' commentare (DONE, EXPIRED e CLOSED, un operando dell'or
        * ciascuno), i due casi sull'assegnatario (issue senza assegnatario e
        * issue assegnata a qualcun altro) e la lista commenti null che il
        * metodo deve inizializzare.
        */

        @Test
        void cc_commento_1_valido() { // caso valido: il commento viene aggiunto con testo e autore giusti
            Utente u = user(5, "mario@test.it");
            Issue issue = issue(StatoIssue.IN_PROGRESS, 0L);
            issue.setAssegnatoA(u);
            when(sessioneService.getUtenteBySessionId(userSid)).thenReturn(u);
            when(issueRepository.findById(10)).thenReturn(Optional.of(issue));

            issueService.aggiungiCommento(10, "Ci sto lavorando", null, userSid);

            assertEquals(1, issue.getCommento().size());
            assertEquals("Ci sto lavorando", issue.getCommento().get(0).getTesto());
            assertEquals("mario@test.it", issue.getCommento().get(0).getAutore());
            verify(issueRepository).save(issue);
        }

        @Test
        void cc_commento_2_sessioneNulla_forbidden() { // la sessione non corrisponde a nessun utente
            when(sessioneService.getUtenteBySessionId(userSid)).thenReturn(null);

            assertThrows(ForbiddenException.class,
                () -> issueService.aggiungiCommento(10, "ciao", null, userSid));
        }

        @Test
        void cc_commento_3_testoNull_badRequest() { // testo null
            when(sessioneService.getUtenteBySessionId(userSid)).thenReturn(user(5, "mario@test.it"));

            assertThrows(BadRequestException.class,
                () -> issueService.aggiungiCommento(10, null, null, userSid));
        }

        @Test
        void cc_commento_4_testoVuoto_badRequest() { // testo presente ma di soli spazi
            when(sessioneService.getUtenteBySessionId(userSid)).thenReturn(user(5, "mario@test.it"));

            assertThrows(BadRequestException.class,
                () -> issueService.aggiungiCommento(10, "   ", null, userSid));
        }

        @Test
        void cc_commento_5_issueInesistente_notFound() { // la issue non esiste
            when(sessioneService.getUtenteBySessionId(userSid)).thenReturn(user(5, "mario@test.it"));
            when(issueRepository.findById(10)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class,
                () -> issueService.aggiungiCommento(10, "ciao", null, userSid));
        }

        @Test
        void cc_commento_6_versioneDiversa_conflict() { // versione diversa da quella corrente
            Issue issue = issue(StatoIssue.IN_PROGRESS, 2L);
            when(sessioneService.getUtenteBySessionId(userSid)).thenReturn(user(5, "mario@test.it"));
            when(issueRepository.findById(10)).thenReturn(Optional.of(issue));

            assertThrows(ConflictException.class,
                () -> issueService.aggiungiCommento(10, "ciao", 1L, userSid));
        }

        @Test
        void cc_commento_7_versioneUguale_ok() { // versione uguale a quella corrente: il confronto passa
            Utente u = user(5, "mario@test.it");
            Issue issue = issue(StatoIssue.IN_PROGRESS, 5L);
            issue.setAssegnatoA(u);
            when(sessioneService.getUtenteBySessionId(userSid)).thenReturn(u);
            when(issueRepository.findById(10)).thenReturn(Optional.of(issue));

            issueService.aggiungiCommento(10, "ciao", 5L, userSid);

            assertEquals(1, issue.getCommento().size());
        }

        @Test
        void cc_commento_8_statoDone_badRequest() { // non si commenta una issue DONE
            Utente u = user(5, "mario@test.it");
            Issue issue = issue(StatoIssue.DONE, 0L);
            issue.setAssegnatoA(u);
            when(sessioneService.getUtenteBySessionId(userSid)).thenReturn(u);
            when(issueRepository.findById(10)).thenReturn(Optional.of(issue));

            assertThrows(BadRequestException.class,
                () -> issueService.aggiungiCommento(10, "ciao", null, userSid));
        }

        @Test
        void cc_commento_9_statoExpired_badRequest() { // non si commenta una issue EXPIRED
            Utente u = user(5, "mario@test.it");
            Issue issue = issue(StatoIssue.EXPIRED, 0L);
            issue.setAssegnatoA(u);
            when(sessioneService.getUtenteBySessionId(userSid)).thenReturn(u);
            when(issueRepository.findById(10)).thenReturn(Optional.of(issue));

            assertThrows(BadRequestException.class,
                () -> issueService.aggiungiCommento(10, "ciao", null, userSid));
        }

        @Test
        void cc_commento_10_statoClosed_badRequest() { // non si commenta una issue CLOSED
            Utente u = user(5, "mario@test.it");
            Issue issue = issue(StatoIssue.CLOSED, 0L);
            issue.setAssegnatoA(u);
            when(sessioneService.getUtenteBySessionId(userSid)).thenReturn(u);
            when(issueRepository.findById(10)).thenReturn(Optional.of(issue));

            assertThrows(BadRequestException.class,
                () -> issueService.aggiungiCommento(10, "ciao", null, userSid));
        }

        @Test
        void cc_commento_11_assegnatoANull_forbidden() { // la issue non ha nessun assegnatario
            Issue issue = issue(StatoIssue.IN_PROGRESS, 0L);
            issue.setAssegnatoA(null);
            when(sessioneService.getUtenteBySessionId(userSid)).thenReturn(user(5, "mario@test.it"));
            when(issueRepository.findById(10)).thenReturn(Optional.of(issue));

            assertThrows(ForbiddenException.class,
                () -> issueService.aggiungiCommento(10, "ciao", null, userSid));
        }

        @Test
        void cc_commento_12_altroAssegnatario_forbidden() { // la issue e' assegnata a un altro utente
            Issue issue = issue(StatoIssue.IN_PROGRESS, 0L);
            issue.setAssegnatoA(user(2, "altro@test.it"));
            when(sessioneService.getUtenteBySessionId(userSid)).thenReturn(user(5, "mario@test.it"));
            when(issueRepository.findById(10)).thenReturn(Optional.of(issue));

            assertThrows(ForbiddenException.class,
                () -> issueService.aggiungiCommento(10, "ciao", null, userSid));
        }

        @Test
        void cc_commento_13_listaCommentiNull_ok() { // lista commenti null: viene inizializzata dal metodo
            Utente u = user(5, "mario@test.it");
            Issue issue = issue(StatoIssue.IN_PROGRESS, 0L);
            issue.setAssegnatoA(u);
            issue.setCommento(null);
            when(sessioneService.getUtenteBySessionId(userSid)).thenReturn(u);
            when(issueRepository.findById(10)).thenReturn(Optional.of(issue));

            issueService.aggiungiCommento(10, "ciao", null, userSid);

            assertEquals(1, issue.getCommento().size());
        }
    }
