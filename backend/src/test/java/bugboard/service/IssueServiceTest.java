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
 * Unit test di IssueService (dipendenze mockate con Mockito, nessun DB).
 *
 * Per ogni metodo ci sono DUE sezioni, una per criterio di progettazione:
 *
 *   [BLACK-BOX] equivalence-class testing, copertura each-choice:
 *       per ogni parametro si individuano le classi di equivalenza, distinte
 *       in VALIDE (V) — l'input supera il relativo controllo — e NON VALIDE
 *       (NV) — l'input fa scattare un'eccezione. Each-choice: ogni classe
 *       (V o NV) compare in almeno un test.
 *       Limite noto: MASKING (il primo controllo che fallisce corto-circuita
 *       gli altri, quindi in un caso multi-NV si osserva solo la prima
 *       eccezione). E' la ragione per cui serve anche il white-box.
 *
 *   [WHITE-BOX] condition coverage completa:
 *       ogni singolo operando di ogni condizione composta (&&, ||) viene
 *       valutato sia a true sia a false almeno una volta.
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

    // ##############################################################
    //  METODO 1: assignIssueToUser(issueId, userEmail, expiringDate,
    //                              expectedVersion, adminSID)
    // ##############################################################

    /* ------------------------------------------------------------
     * [BLACK-BOX] equivalence-class testing — each-choice
     *
     * Classi di equivalenza dei parametri del metodo — (V) valida,
     *
     *   issueId:
     *      (V)  esiste                  -> findIssueOr404 supera
     *      (NV) non esiste              -> NotFound
     *   userEmail (destinatario):
     *      (V)  esiste, ruolo USER      -> destinatario lecito
     *      (NV) non esiste              -> NotFound
     *      (NV) esiste, ruolo non-USER  -> BadRequest (assegnabile solo a USER)
     *   expiringDate (scadenza, opzionale):
     *      (V)  null                    -> nessuna nuova scadenza
     *      (V)  valorizzata             -> imposta la scadenza
     *   expectedVersion (optimistic lock lato client):
     *      (V)  null                    -> confronto saltato, prosegue
     *      (V)  == corrente             -> confronto ok, prosegue
     *      (NV) != corrente             -> Conflict (409)
     *   adminSID (sessione che invoca):
     *      (V)  sessione admin          -> requireAdmin supera
     *      (NV) sessione nulla          -> Forbidden
     *      (NV) sessione non-admin      -> Forbidden
     *
     * Blocco massimo = 3 classi -> 3 test. Le classi NV di parametri diversi
     * sono COMBINATE in un caso solo; vale il MASKING (si osserva solo la
     * prima eccezione). I blocchi di version == e != compaiono in EC2/EC3 ma
     * sono mascherati dalla sessione: il loro effetto e' verificato dal
     * white-box (cc_assign_5 !=, cc_assign_6 ==).
     *
     *      | issueId      | userEmail    | expiringDate  | version       | adminSID      | esito atteso
     * EC1  | esiste  (V)  | USER    (V)  | valorizz. (V) | null      (V) | admin    (V)  | successo (scadenza impostata)
     * EC2  | inesist.(NV) | inesist.(NV) | null      (V) | ==corr.   (V) | nulla   (NV)  | Forbidden (resto MASCHERATO)
     * EC3  | esiste  (V)  | non-USER(NV) | null      (V) | !=corr.  (NV) | non-admin(NV) | Forbidden (resto MASCHERATO)
     * ------------------------------------------------------------ */

    @Test
    void ec_assign_1_tuttiValidi() {
        // PARAMETRI tutti su classe valida: issueId esiste, userEmail esiste-USER,
        // expiringDate valorizzata, version null, adminSID sessione admin.
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
        // PARAMETRI: issueId inesistente + userEmail inesistente + expiringDate null
        //            + version == corrente (nominale) + adminSID sessione nulla.
        // MASKING: la sessione nulla corto-circuita -> si osserva solo Forbidden.
        when(sessioneService.getUtenteBySessionId(adminSid)).thenReturn(null);

        assertThrows(ForbiddenException.class,
            () -> issueService.assignIssueToUser(999, "ghost@test.it", null, 0L, adminSid));
    }

    @Test
    void ec_assign_3_nonValideCombinate() {
        // PARAMETRI: issueId esistente + userEmail esiste-non-USER + expiringDate null
        //            + version != corrente (nominale) + adminSID sessione non-admin.
        // MASKING: il non-admin corto-circuita -> si osserva solo Forbidden.
        when(sessioneService.getUtenteBySessionId(adminSid)).thenReturn(user(5, "user@test.it"));

        assertThrows(ForbiddenException.class,
            () -> issueService.assignIssueToUser(10, "admin2@test.it", null, 1L, adminSid));
    }

    /* ------------------------------------------------------------
     * [WHITE-BOX] condition coverage completa
     *
     * Condizioni composte del metodo (e helper) e operando coperto:
     *   requireUser   : user == null                         -> T:CC2  F:CC1
     *   requireAdmin  : role != ADMIN                        -> T:CC3  F:CC1
     *   findIssueOr404: Optional vuoto                       -> T:CC4  F:CC1
     *   checkVersion  : expectedVersion != null   (C1)       -> T:CC5  F:CC1
     *                   !expectedVersion.equals() (C2)       -> T:CC5  F:CC6
     *   findByEmail   : Optional vuoto                       -> T:CC7  F:CC1
     *   ruolo dest.   : role != USER                         -> T:CC8  F:CC1
     *   stato         : stato == CLOSED           (C1)       -> T:CC9  F:CC1
     *                   stato == DONE             (C2)       -> T:CC10 F:CC1
     *   scadenza      : expiringDate != null                 -> T:CC6  F:CC1
     *                   eraScaduta (else-if)                 -> T:CC11 F:CC12
     *   riattivazione : stato == TODO             (C1)       -> T:CC1  F:CC11
     *                   eraScaduta                (C2)       -> T:CC11 F:CC12
     * ------------------------------------------------------------ */

    @Test
    void cc_assign_1_valida_TODO() { // lato "prosegue" di tutte le condizioni; TODO -> IN_PROGRESS
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
    void cc_assign_2_sessioneNulla_forbidden() { // user == null : TRUE
        when(sessioneService.getUtenteBySessionId(adminSid)).thenReturn(null);

        assertThrows(ForbiddenException.class,
            () -> issueService.assignIssueToUser(10, "mario@test.it", null, null, adminSid));
    }

    @Test
    void cc_assign_3_nonAdmin_forbidden() { // role != ADMIN : TRUE
        when(sessioneService.getUtenteBySessionId(adminSid)).thenReturn(user(5, "user@test.it"));

        assertThrows(ForbiddenException.class,
            () -> issueService.assignIssueToUser(10, "mario@test.it", null, null, adminSid));
    }

    @Test
    void cc_assign_4_issueInesistente_notFound() { // findById vuoto : TRUE
        when(sessioneService.getUtenteBySessionId(adminSid)).thenReturn(admin("admin@test.it"));
        when(issueRepository.findById(10)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
            () -> issueService.assignIssueToUser(10, "mario@test.it", null, null, adminSid));
    }

    @Test
    void cc_assign_5_versioneDiversa_conflict() { // checkVersion C1=T, C2=T
        when(sessioneService.getUtenteBySessionId(adminSid)).thenReturn(admin("admin@test.it"));
        when(issueRepository.findById(10)).thenReturn(Optional.of(issue(StatoIssue.TODO, 2L)));

        assertThrows(ConflictException.class,
            () -> issueService.assignIssueToUser(10, "mario@test.it", null, 1L, adminSid));
    }

    @Test
    void cc_assign_6_versioneUguale_conData() { // checkVersion C2=F ; expiringDate != null : TRUE
        Issue issue = issue(StatoIssue.TODO, 5L);
        when(sessioneService.getUtenteBySessionId(adminSid)).thenReturn(admin("admin@test.it"));
        when(issueRepository.findById(10)).thenReturn(Optional.of(issue));
        when(utenteRepository.findByEmail("mario@test.it")).thenReturn(Optional.of(user(2, "mario@test.it")));

        LocalDate scadenza = LocalDate.now().plusDays(7);
        issueService.assignIssueToUser(10, "mario@test.it", scadenza, 5L, adminSid); // expectedVersion == version

        assertEquals(scadenza.atTime(23, 59, 59), issue.getDataScadenza());
        assertEquals(StatoIssue.IN_PROGRESS, issue.getStato());
    }

    @Test
    void cc_assign_7_destinatarioInesistente_notFound() { // findByEmail vuoto : TRUE
        when(sessioneService.getUtenteBySessionId(adminSid)).thenReturn(admin("admin@test.it"));
        when(issueRepository.findById(10)).thenReturn(Optional.of(issue(StatoIssue.TODO, 0L)));
        when(utenteRepository.findByEmail("ghost@test.it")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
            () -> issueService.assignIssueToUser(10, "ghost@test.it", null, null, adminSid));
    }

    @Test
    void cc_assign_8_destinatarioNonUser_badRequest() { // role != USER : TRUE
        when(sessioneService.getUtenteBySessionId(adminSid)).thenReturn(admin("admin@test.it"));
        when(issueRepository.findById(10)).thenReturn(Optional.of(issue(StatoIssue.TODO, 0L)));
        when(utenteRepository.findByEmail("admin2@test.it")).thenReturn(Optional.of(admin("admin2@test.it")));

        assertThrows(BadRequestException.class,
            () -> issueService.assignIssueToUser(10, "admin2@test.it", null, null, adminSid));
    }

    @Test
    void cc_assign_9_statoClosed_badRequest() { // stato == CLOSED : TRUE (1° operando dell'OR)
        when(sessioneService.getUtenteBySessionId(adminSid)).thenReturn(admin("admin@test.it"));
        when(issueRepository.findById(10)).thenReturn(Optional.of(issue(StatoIssue.CLOSED, 0L)));
        when(utenteRepository.findByEmail("mario@test.it")).thenReturn(Optional.of(user(2, "mario@test.it")));

        assertThrows(BadRequestException.class,
            () -> issueService.assignIssueToUser(10, "mario@test.it", null, null, adminSid));
    }

    @Test
    void cc_assign_10_statoDone_badRequest() { // stato == DONE : TRUE (2° operando dell'OR)
        when(sessioneService.getUtenteBySessionId(adminSid)).thenReturn(admin("admin@test.it"));
        when(issueRepository.findById(10)).thenReturn(Optional.of(issue(StatoIssue.DONE, 0L)));
        when(utenteRepository.findByEmail("mario@test.it")).thenReturn(Optional.of(user(2, "mario@test.it")));

        assertThrows(BadRequestException.class,
            () -> issueService.assignIssueToUser(10, "mario@test.it", null, null, adminSid));
    }

    @Test
    void cc_assign_11_statoExpired_riportataInProgress() { // eraScaduta=T ; riattivazione C1=F, C2=T
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
    void cc_assign_12_riassegnaInProgress_restaInProgress() { // eraScaduta=F (else-if e riattivazione C2)
        Issue issue = issue(StatoIssue.IN_PROGRESS, 0L);
        when(sessioneService.getUtenteBySessionId(adminSid)).thenReturn(admin("admin@test.it"));
        when(issueRepository.findById(10)).thenReturn(Optional.of(issue));
        when(utenteRepository.findByEmail("mario@test.it")).thenReturn(Optional.of(user(2, "mario@test.it")));

        issueService.assignIssueToUser(10, "mario@test.it", null, null, adminSid);

        assertEquals(StatoIssue.IN_PROGRESS, issue.getStato()); // non-TODO e non-scaduta: lo stato non cambia
        verify(issueRepository).save(issue);
    }

    // ##############################################################
    //  METODO 2: aggiungiCommento(idIssue, testo, expectedVersion, sid)
    // ##############################################################

    /* ------------------------------------------------------------
     * [BLACK-BOX] equivalence-class testing — each-choice
     *
     * Classi di equivalenza dei SOLI PARAMETRI del metodo — (V)/(NV).
     *
     *   idIssue:
     *      (V)  esiste                  -> findIssueOr404 supera
     *      (NV) non esiste              -> NotFound
     *   testo:
     *      (V)  non vuoto               -> commento accettato
     *      (NV) null                    -> BadRequest
     *      (NV) vuoto / soli spazi      -> BadRequest
     *   expectedVersion (optimistic lock lato client):
     *      (V)  null                    -> confronto saltato, prosegue
     *      (V)  == corrente             -> confronto ok, prosegue
     *      (NV) != corrente             -> Conflict (409)
     *   sid (sessione che invoca):
     *      (V)  valida                  -> requireUser supera
     *      (NV) non valida (incl. null) -> Forbidden
     *
     * Blocco massimo = 3 classi -> 3 test. Le classi NV sono COMBINATE;
     * MASKING: si osserva solo la prima eccezione. I blocchi version == e !=
     * compaiono in ECc2/ECc3 ma sono mascherati (sessione/testo non validi
     * vengono prima): verificati dal white-box (cc_commento_7 ==, cc_commento_6 !=).
     *
     *      | idIssue     | testo       | version       | sid           | esito atteso
     * ECc1 | esiste (V)  | non-vuoto(V)| null      (V) | valida   (V)  | successo
     * ECc2 | inesist.(NV)| null    (NV)| ==corr.   (V) | non valida(NV)| Forbidden (resto MASCHERATO)
     * ECc3 | esiste (V)  | vuoto   (NV)| !=corr.  (NV) | valida   (V)  | BadRequest (resto MASCHERATO)
     * ------------------------------------------------------------ */

    @Test
    void ec_commento_1_tuttiValidi() {
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
        // PARAMETRI: idIssue inesistente + testo null + version == corrente (nominale)
        //            + sid sessione non valida (null).
        // MASKING: requireUser fallisce subito -> solo Forbidden.
        when(sessioneService.getUtenteBySessionId(userSid)).thenReturn(null);

        assertThrows(ForbiddenException.class,
            () -> issueService.aggiungiCommento(999, null, 0L, userSid));
    }

    @Test
    void ec_commento_3_nonValideCombinate() {
        // PARAMETRI: idIssue esistente + testo vuoto + version != corrente (nominale)
        //            + sid sessione valida.
        // MASKING: il controllo sul testo precede quello sulla versione -> solo BadRequest.
        when(sessioneService.getUtenteBySessionId(userSid)).thenReturn(user(5, "mario@test.it"));

        assertThrows(BadRequestException.class,
            () -> issueService.aggiungiCommento(10, "   ", 1L, userSid));
    }

    /* ------------------------------------------------------------
     * [WHITE-BOX] condition coverage completa
     *
     *   requireUser : user == null                  -> T:WC2  F:WC1
     *   testo       : testo == null        (C1)     -> T:WC3  F:WC1
     *                 trim().isEmpty()     (C2)     -> T:WC4  F:WC1
     *   findById    : Optional vuoto                -> T:WC5  F:WC1
     *   checkVersion: expectedVersion != null (C1)  -> T:WC6  F:WC1
     *                 !equals()            (C2)     -> T:WC6  F:WC7
     *   stato       : == DONE              (C1)     -> T:WC8  F:WC1
     *                 == EXPIRED           (C2)     -> T:WC9  F:WC1
     *                 == CLOSED            (C3)     -> T:WC10 F:WC1
     *   ownership   : assegnatoA == null   (C1)     -> T:WC11 F:WC1
     *                 !id.equals()         (C2)     -> T:WC12 F:WC1
     *   lista       : commento == null              -> T:WC13 F:WC1
     * ------------------------------------------------------------ */

    @Test
    void cc_commento_1_valido() { // lato "prosegue" di tutte le condizioni
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
    void cc_commento_2_sessioneNulla_forbidden() { // user == null : TRUE
        when(sessioneService.getUtenteBySessionId(userSid)).thenReturn(null);

        assertThrows(ForbiddenException.class,
            () -> issueService.aggiungiCommento(10, "ciao", null, userSid));
    }

    @Test
    void cc_commento_3_testoNull_badRequest() { // testo == null : TRUE
        when(sessioneService.getUtenteBySessionId(userSid)).thenReturn(user(5, "mario@test.it"));

        assertThrows(BadRequestException.class,
            () -> issueService.aggiungiCommento(10, null, null, userSid));
    }

    @Test
    void cc_commento_4_testoVuoto_badRequest() { // testo==null : FALSE, trim().isEmpty() : TRUE
        when(sessioneService.getUtenteBySessionId(userSid)).thenReturn(user(5, "mario@test.it"));

        assertThrows(BadRequestException.class,
            () -> issueService.aggiungiCommento(10, "   ", null, userSid));
    }

    @Test
    void cc_commento_5_issueInesistente_notFound() { // findById vuoto : TRUE
        when(sessioneService.getUtenteBySessionId(userSid)).thenReturn(user(5, "mario@test.it"));
        when(issueRepository.findById(10)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
            () -> issueService.aggiungiCommento(10, "ciao", null, userSid));
    }

    @Test
    void cc_commento_6_versioneDiversa_conflict() { // checkVersion C1=T, C2=T
        Issue issue = issue(StatoIssue.IN_PROGRESS, 2L);
        when(sessioneService.getUtenteBySessionId(userSid)).thenReturn(user(5, "mario@test.it"));
        when(issueRepository.findById(10)).thenReturn(Optional.of(issue));

        assertThrows(ConflictException.class,
            () -> issueService.aggiungiCommento(10, "ciao", 1L, userSid));
    }

    @Test
    void cc_commento_7_versioneUguale_ok() { // checkVersion C2=F
        Utente u = user(5, "mario@test.it");
        Issue issue = issue(StatoIssue.IN_PROGRESS, 5L);
        issue.setAssegnatoA(u);
        when(sessioneService.getUtenteBySessionId(userSid)).thenReturn(u);
        when(issueRepository.findById(10)).thenReturn(Optional.of(issue));

        issueService.aggiungiCommento(10, "ciao", 5L, userSid); // expectedVersion == version

        assertEquals(1, issue.getCommento().size());
    }

    @Test
    void cc_commento_8_statoDone_badRequest() { // stato == DONE : TRUE
        Utente u = user(5, "mario@test.it");
        Issue issue = issue(StatoIssue.DONE, 0L);
        issue.setAssegnatoA(u);
        when(sessioneService.getUtenteBySessionId(userSid)).thenReturn(u);
        when(issueRepository.findById(10)).thenReturn(Optional.of(issue));

        assertThrows(BadRequestException.class,
            () -> issueService.aggiungiCommento(10, "ciao", null, userSid));
    }

    @Test
    void cc_commento_9_statoExpired_badRequest() { // == DONE : F, == EXPIRED : TRUE
        Utente u = user(5, "mario@test.it");
        Issue issue = issue(StatoIssue.EXPIRED, 0L);
        issue.setAssegnatoA(u);
        when(sessioneService.getUtenteBySessionId(userSid)).thenReturn(u);
        when(issueRepository.findById(10)).thenReturn(Optional.of(issue));

        assertThrows(BadRequestException.class,
            () -> issueService.aggiungiCommento(10, "ciao", null, userSid));
    }

    @Test
    void cc_commento_10_statoClosed_badRequest() { // == DONE : F, == EXPIRED : F, == CLOSED : TRUE
        Utente u = user(5, "mario@test.it");
        Issue issue = issue(StatoIssue.CLOSED, 0L);
        issue.setAssegnatoA(u);
        when(sessioneService.getUtenteBySessionId(userSid)).thenReturn(u);
        when(issueRepository.findById(10)).thenReturn(Optional.of(issue));

        assertThrows(BadRequestException.class,
            () -> issueService.aggiungiCommento(10, "ciao", null, userSid));
    }

    @Test
    void cc_commento_11_assegnatoANull_forbidden() { // ownership: assegnatoA == null : TRUE
        Issue issue = issue(StatoIssue.IN_PROGRESS, 0L);
        issue.setAssegnatoA(null);
        when(sessioneService.getUtenteBySessionId(userSid)).thenReturn(user(5, "mario@test.it"));
        when(issueRepository.findById(10)).thenReturn(Optional.of(issue));

        assertThrows(ForbiddenException.class,
            () -> issueService.aggiungiCommento(10, "ciao", null, userSid));
    }

    @Test
    void cc_commento_12_altroAssegnatario_forbidden() { // assegnatoA != null : F, !id.equals() : TRUE
        Issue issue = issue(StatoIssue.IN_PROGRESS, 0L);
        issue.setAssegnatoA(user(2, "altro@test.it"));
        when(sessioneService.getUtenteBySessionId(userSid)).thenReturn(user(5, "mario@test.it"));
        when(issueRepository.findById(10)).thenReturn(Optional.of(issue));

        assertThrows(ForbiddenException.class,
            () -> issueService.aggiungiCommento(10, "ciao", null, userSid));
    }

    @Test
    void cc_commento_13_listaCommentiNull_ok() { // commento == null : TRUE (viene inizializzata)
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
