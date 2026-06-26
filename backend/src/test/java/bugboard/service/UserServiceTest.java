package bugboard.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import bugboard.exception.BadRequestException;
import bugboard.exception.ForbiddenException;
import bugboard.exception.NotFoundException;
import bugboard.model.Utente;
import bugboard.model.Utente.Role;
import bugboard.repository.UserRepository;

/*
 * Unit test di UserService, metodo deleteUser(sid, email).
 *
 *   [BLACK-BOX] equivalence-class testing, copertura each-choice: per ogni
 *       parametro si individuano le classi VALIDE (V) e NON VALIDE (NV); ogni
 *       classe compare in almeno un test e le NV vengono combinate in un solo
 *       caso. Limite di MASKING: nel caso multi-NV si osserva solo la prima
 *       eccezione, perciò la copertura piena delle NV sta nel white-box.
 *   [WHITE-BOX] condition coverage completa -> ogni operando a true e a false.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository utenteRepository;
    @Mock private ISessioneService sessioneService;
    @Mock private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks private UserService userService;

    private final UUID sid = UUID.randomUUID();

    private Utente utenteCon(Role ruolo, String email) {
        Utente u = new Utente();
        u.setId(2);
        u.setEmail(email);
        u.setRole(ruolo);
        return u;
    }

    // ##############################################################
    //  METODO 3: deleteUser(sid, email)
    // ##############################################################

    /* ------------------------------------------------------------
     * [BLACK-BOX] equivalence-class testing — each-choice
     *
     * Classi di equivalenza dei SOLI PARAMETRI del metodo — (V)/(NV).
     *   sid (sessione che invoca):
     *      (V)  sessione admin            -> requireAdmin supera
     *      (NV) sessione non-admin        -> Forbidden  (null incluso qui)
     *   email (utente da eliminare):
     *      (V)  esiste, eliminabile       -> ruolo USER o READONLY -> delete
     *      (NV) non esiste                -> NotFound
     *      (NV) esiste, ruolo ADMIN       -> BadRequest (un admin non si elimina)
     *
     * Blocco massimo = 3 classi -> 3 test.
     *
     *      | sid           | email             | esito atteso
     * ECd1 | admin    (V)  | eliminabile  (V)  | delete eseguita (target USER)
     * ECd2 | non-admin(NV) | non-esiste  (NV)  | Forbidden (email MASCHERATA)
     * ECd3 | admin    (V)  | esiste-ADMIN (NV) | BadRequest
     * ------------------------------------------------------------ */

    @Test
    void ec_delete_1_adminEliminaUser() {
        Utente target = utenteCon(Role.USER, "mario@test.it");
        when(sessioneService.isAdmin(sid)).thenReturn(true);
        when(utenteRepository.findByEmail("mario@test.it")).thenReturn(Optional.of(target));

        userService.deleteUser(sid, "mario@test.it");

        verify(utenteRepository).delete(target);
    }

    @Test
    void ec_delete_2_nonValideCombinate() {
        // non-admin + email inesistente + ruolo non eliminabile.
        // MASKING: il controllo admin fallisce subito -> solo Forbidden.
        when(sessioneService.isAdmin(sid)).thenReturn(false);

        assertThrows(ForbiddenException.class,
            () -> userService.deleteUser(sid, "ghost@test.it"));
        verify(utenteRepository, never()).delete(any());
    }

    @Test
    void ec_delete_3_targetAdmin_badRequest() {
        // email -> referente ADMIN: classe NV del parametro email (un admin non si elimina).
        Utente target = utenteCon(Role.ADMIN, "admin2@test.it");
        when(sessioneService.isAdmin(sid)).thenReturn(true);
        when(utenteRepository.findByEmail("admin2@test.it")).thenReturn(Optional.of(target));

        assertThrows(BadRequestException.class,
            () -> userService.deleteUser(sid, "admin2@test.it"));
        verify(utenteRepository, never()).delete(any());
    }

    /* ------------------------------------------------------------
     * [WHITE-BOX] condition coverage completa
     *
     *   requireAdmin : !isAdmin(sid)                 -> T:WD2  F:WD1
     *   findByEmail  : Optional vuoto                -> T:WD3  F:WD1
     *   ruolo        : ruolo != USER      (C1)       -> T:WD4  F:WD1
     *                  ruolo != READONLY  (C2)       -> T:WD4  F:WD5
     * ------------------------------------------------------------ */

    @Test
    void cc_delete_1_user_ok() { // !isAdmin=F, ruolo!=USER (C1)=F  -> elimina
        Utente target = utenteCon(Role.USER, "mario@test.it");
        when(sessioneService.isAdmin(sid)).thenReturn(true);
        when(utenteRepository.findByEmail("mario@test.it")).thenReturn(Optional.of(target));

        userService.deleteUser(sid, "mario@test.it");

        verify(utenteRepository).delete(target);
    }

    @Test
    void cc_delete_2_nonAdmin_forbidden() { // !isAdmin : TRUE
        when(sessioneService.isAdmin(sid)).thenReturn(false);

        assertThrows(ForbiddenException.class,
            () -> userService.deleteUser(sid, "mario@test.it"));
        verify(utenteRepository, never()).delete(any());
    }

    @Test
    void cc_delete_3_inesistente_notFound() { // findByEmail vuoto : TRUE
        when(sessioneService.isAdmin(sid)).thenReturn(true);
        when(utenteRepository.findByEmail("ghost@test.it")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
            () -> userService.deleteUser(sid, "ghost@test.it"));
    }

    @Test
    void cc_delete_4_admin_badRequest() { // ruolo != USER (C1)=T, ruolo != READONLY (C2)=T
        Utente target = utenteCon(Role.ADMIN, "admin@test.it");
        when(sessioneService.isAdmin(sid)).thenReturn(true);
        when(utenteRepository.findByEmail("admin@test.it")).thenReturn(Optional.of(target));

        assertThrows(BadRequestException.class,
            () -> userService.deleteUser(sid, "admin@test.it"));
        verify(utenteRepository, never()).delete(any());
    }

    @Test
    void cc_delete_5_readonly_ok() { // ruolo != USER (C1)=T, ruolo != READONLY (C2)=F  -> elimina
        Utente target = utenteCon(Role.READONLY, "ro@test.it");
        when(sessioneService.isAdmin(sid)).thenReturn(true);
        when(utenteRepository.findByEmail("ro@test.it")).thenReturn(Optional.of(target));

        userService.deleteUser(sid, "ro@test.it");

        verify(utenteRepository).delete(target);
    }
}
