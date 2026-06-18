package bugboard.service;

import bugboard.dto.RegisterRequest;
import bugboard.dto.AllUserResponse;

import bugboard.exception.BadRequestException;
import bugboard.exception.ConflictException;
import bugboard.exception.ForbiddenException;
import bugboard.exception.NotFoundException;

import bugboard.model.Utente;
import bugboard.model.Utente.Role;

import bugboard.repository.UserRepository;

import java.util.List;
import java.util.UUID;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/*
 * SRP — gestisce solo operazioni CRUD sugli utenti (lato admin).
 * DIP — dipende da ISessioneService (astrazione); tutte le collaborazioni
 *       arrivano via costruttore (BCryptPasswordEncoder via @Bean).
 * I fallimenti sono segnalati con ApiException (403/404/400/409) e i dati
 *       restituiti sono DTO: l'entity Utente (con la password) non esce mai.
 */
@Service
public class UserService implements IUserService {

    private final UserRepository utenteRepository;
    private final ISessioneService sessioneService;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository utenteRepository,
                       ISessioneService sessioneService,
                       BCryptPasswordEncoder passwordEncoder) {
        this.utenteRepository = utenteRepository;
        this.sessioneService = sessioneService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public AllUserResponse creaNuovoUtente(UUID sid, RegisterRequest request) {
        requireAdmin(sid);

        if (utenteRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ConflictException("Email già presente: " + request.getEmail());
        }

        Utente nuovo = new Utente();
        nuovo.setName(request.getName());
        nuovo.setSurname(request.getSurname());
        nuovo.setEmail(request.getEmail());
        nuovo.setPassword(passwordEncoder.encode(request.getPassword()));

        try {
            nuovo.setRole(Role.fromValue(request.getRole()));
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Ruolo non valido: " + request.getRole());
        }

        return toDto(utenteRepository.save(nuovo));
    }

    @Override
    public List<AllUserResponse> getAllUsers(UUID sid) {
        requireAdmin(sid);
        return utenteRepository.findAll().stream().map(this::toDto).toList();
    }

    @Override
    @Transactional
    public void deleteUser(UUID sid, String email) {
        requireAdmin(sid);

        Utente utenteDaEliminare = utenteRepository.findByEmail(email)
            .orElseThrow(() -> new NotFoundException("Utente non trovato: " + email));

        Role ruolo = utenteDaEliminare.getRole();
        if (ruolo != Role.USER && ruolo != Role.READONLY) {
            throw new BadRequestException("È possibile eliminare solo utenti USER o READONLY");
        }

        utenteRepository.delete(utenteDaEliminare);
    }

    /* ────────────────────────── HELPER ─────────────────────────── */

    private void requireAdmin(UUID sid) {
        if (!sessioneService.isAdmin(sid)) {
            throw new ForbiddenException("Permessi insufficienti");
        }
    }

    private AllUserResponse toDto(Utente u) {
        return new AllUserResponse(
            u.getId(),
            u.getName(),
            u.getSurname(),
            u.getEmail(),
            u.getRole().toString()
        );
    }
}
