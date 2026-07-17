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
 * Test di unita' per UserService, in particolare per deleteUser(sid, email).
 * Repository e servizio di sessione sono mockati con Mockito.
 *
 * Come per gli altri servizi ci sono i test black-box sulle classi di
 * equivalenza (criterio each-choice: ogni classe, indicata tra parentesi
 * graffe, compare in almeno un test) e i test white-box per la condition
 * coverage, in cui ogni condizione deve risultare sia vera che falsa.
 * Quando piu' classi non valide finiscono nello stesso test si osserva solo
 * la prima eccezione lanciata, e i controlli cosi' mascherati vengono
 * ripresi dal white-box.
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

    // ------------------- test di deleteUser -------------------

    /*
     * Parte black-box: classi di equivalenza, criterio each-choice.
     *
     * Per sid le classi sono {sessione di un admin}, valida, e {sessione di
     * un non admin o nulla}, non valida perche' porta a Forbidden. Per email
     * le classi sono tre: {utente esistente con ruolo USER o READONLY},
     * valida perche' e' un utente eliminabile, {utente inesistente} non
     * valida (NotFound) e {utente esistente con ruolo ADMIN} non valida
     * (BadRequest, un amministratore non si puo' eliminare).
     *
     * Il parametro con piu' classi e' email, che ne ha tre, quindi bastano
     * tre test. In ec_delete_2 le classi non valide dei due parametri sono
     * combinate: il controllo sull'admin viene eseguito per primo, quindi si
     * osserva solo Forbidden e la classe dell'email resta mascherata (viene
     * comunque ripresa dai test white-box).
     */

    @Test
    void ec_delete_1_adminEliminaUser() {
        // Entrambi i parametri su una classe valida: {sessione admin} e
        // {utente eliminabile}, qui di ruolo USER.
        Utente target = utenteCon(Role.USER, "mario@test.it");
        when(sessioneService.isAdmin(sid)).thenReturn(true);
        when(utenteRepository.findByEmail("mario@test.it")).thenReturn(Optional.of(target));

        userService.deleteUser(sid, "mario@test.it");

        verify(utenteRepository).delete(target);
    }

    @Test
    void ec_delete_2_nonValideCombinate() {
        // Classi non valide combinate: {sessione di un non admin} e {utente
        // inesistente}. Il controllo sull'admin fallisce per primo, quindi
        // si osserva solo Forbidden.
        when(sessioneService.isAdmin(sid)).thenReturn(false);

        assertThrows(ForbiddenException.class,
            () -> userService.deleteUser(sid, "ghost@test.it"));
        verify(utenteRepository, never()).delete(any());
    }

    @Test
    void ec_delete_3_targetAdmin_badRequest() {
        // Classe non valida di email: {utente esistente con ruolo ADMIN},
        // un admin non si puo' eliminare.
        Utente target = utenteCon(Role.ADMIN, "admin2@test.it");
        when(sessioneService.isAdmin(sid)).thenReturn(true);
        when(utenteRepository.findByEmail("admin2@test.it")).thenReturn(Optional.of(target));

        assertThrows(BadRequestException.class,
            () -> userService.deleteUser(sid, "admin2@test.it"));
        verify(utenteRepository, never()).delete(any());
    }

    /*
     * Parte white-box: condition coverage di deleteUser. cc_delete_1 e' il
     * caso valido che copre il lato "si prosegue" dei controlli; poi c'e'
     * un test per la sessione non admin, uno per l'email inesistente e due
     * per la condizione composta sul ruolo: con un ADMIN entrambi gli
     * operandi sono veri e scatta il BadRequest, con un READONLY il secondo
     * operando e' falso e l'eliminazione va a buon fine.
     */

    @Test
    void cc_delete_1_user_ok() { // caso valido: un admin elimina un utente USER
        Utente target = utenteCon(Role.USER, "mario@test.it");
        when(sessioneService.isAdmin(sid)).thenReturn(true);
        when(utenteRepository.findByEmail("mario@test.it")).thenReturn(Optional.of(target));

        userService.deleteUser(sid, "mario@test.it");

        verify(utenteRepository).delete(target);
    }

    @Test
    void cc_delete_2_nonAdmin_forbidden() { // la sessione non e' di un admin
        when(sessioneService.isAdmin(sid)).thenReturn(false);

        assertThrows(ForbiddenException.class,
            () -> userService.deleteUser(sid, "mario@test.it"));
        verify(utenteRepository, never()).delete(any());
    }

    @Test
    void cc_delete_3_inesistente_notFound() { // l'email non corrisponde a nessun utente
        when(sessioneService.isAdmin(sid)).thenReturn(true);
        when(utenteRepository.findByEmail("ghost@test.it")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
            () -> userService.deleteUser(sid, "ghost@test.it"));
    }

    @Test
    void cc_delete_4_admin_badRequest() { // il bersaglio e' un ADMIN, non eliminabile
        Utente target = utenteCon(Role.ADMIN, "admin@test.it");
        when(sessioneService.isAdmin(sid)).thenReturn(true);
        when(utenteRepository.findByEmail("admin@test.it")).thenReturn(Optional.of(target));

        assertThrows(BadRequestException.class,
            () -> userService.deleteUser(sid, "admin@test.it"));
        verify(utenteRepository, never()).delete(any());
    }

    @Test
    void cc_delete_5_readonly_ok() { // il bersaglio e' READONLY, eliminabile come un USER
        Utente target = utenteCon(Role.READONLY, "ro@test.it");
        when(sessioneService.isAdmin(sid)).thenReturn(true);
        when(utenteRepository.findByEmail("ro@test.it")).thenReturn(Optional.of(target));

        userService.deleteUser(sid, "ro@test.it");

        verify(utenteRepository).delete(target);
    }
}
