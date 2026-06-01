package bugboard.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import bugboard.dto.AuthResponse;
import bugboard.dto.LoginRequest;
import bugboard.dto.RegisterRequest;
import bugboard.model.Sessione;
import bugboard.model.Utente;
import bugboard.repository.UserRepository;

import java.util.Optional;

/*
 * SRP  — gestisce solo autenticazione e registrazione utenti.
 * DIP  — dipende da ISessioneService (astrazione), non da SessioneService.
 *        Il BCryptPasswordEncoder è iniettato via @Bean, non istanziato qui.
 * OCP  — il mapping ruolo→frontend è delegato a Utente.Role.toFrontendRole();
 *        aggiungere un ruolo non richiede modificare questo service.
 */
@Service
public class AuthService implements IAuthService {

    @Autowired
    private UserRepository utenteRepository;

    @Autowired
    private ISessioneService sessioneService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    public AuthResponse login(LoginRequest request) {
        Optional<Utente> utente = utenteRepository.findByEmail(request.getEmail());

        if (utente.isEmpty()) return null;

        Utente u = utente.get();

        if (!passwordEncoder.matches(request.getPassword(), u.getPassword())) return null;

        Sessione s = sessioneService.createSession(u);

        return new AuthResponse(
            s.getSid().toString(),
            u.getName() + " " + u.getSurname(),
            u.getRole().toFrontendRole()
        );
    }

    @Override
    public boolean register(RegisterRequest request) {
        if (utenteRepository.findByEmail(request.getEmail()).isPresent()) {
            return false;
        }

        Utente nuovo = new Utente();
        nuovo.setEmail(request.getEmail());
        nuovo.setPassword(passwordEncoder.encode(request.getPassword()));
        nuovo.setName(request.getName());
        nuovo.setSurname(request.getSurname());
        nuovo.setRole(Utente.Role.USER);

        utenteRepository.save(nuovo);
        return true;
    }
}
