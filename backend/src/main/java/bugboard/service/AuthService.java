package bugboard.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import bugboard.dto.AuthResponse;
import bugboard.dto.LoginRequest;
import bugboard.dto.RegisterRequest;
import bugboard.exception.ConflictException;
import bugboard.model.Sessione;
import bugboard.model.Utente;
import bugboard.repository.UserRepository;

import java.util.Optional;

/*
 * Si occupa solo di login e registrazione. Le password non le tengo mai in
 * chiaro: quando registro le cifro col BCryptPasswordEncoder e al login
 * confronto l'hash. La sessione vera e propria la faccio creare a
 * SessioneService, qui mi limito a chiedergliela.
 */
@Service
public class AuthService implements IAuthService {

    private final UserRepository utenteRepository;
    private final ISessioneService sessioneService;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthService(UserRepository utenteRepository,
                       ISessioneService sessioneService,
                       BCryptPasswordEncoder passwordEncoder) {
        this.utenteRepository = utenteRepository;
        this.sessioneService = sessioneService;
        this.passwordEncoder = passwordEncoder;
    }

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
    public void register(RegisterRequest request) {
        if (utenteRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ConflictException("Email già registrata: " + request.getEmail());
        }

        Utente nuovo = new Utente();
        nuovo.setEmail(request.getEmail());
        nuovo.setPassword(passwordEncoder.encode(request.getPassword()));
        nuovo.setName(request.getName());
        nuovo.setSurname(request.getSurname());
        nuovo.setRole(Utente.Role.USER);

        utenteRepository.save(nuovo);
    }
}
