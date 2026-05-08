package bugboard.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import bugboard.dto.AuthResponse;
import bugboard.model.Utente;
import bugboard.repository.UtenteRepository;

import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UtenteRepository utenteRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * Autentica un utente basandosi su email e password.
     * 
     * @param email email dell'utente
     * @param password password dell'utente (plain text)
     * @return AuthResponse con id, nome, ruolo se autentico, altrimenti null
     */
    public AuthResponse login(String email, String password) {
        Optional<Utente> utente = utenteRepository.findByEmail(email);

        if (utente.isEmpty()) {
            return null;
        }

        Utente u = utente.get();

        if (!passwordEncoder.matches(password, u.getPassword())) {
            return null;
        }

        // Costruisci la risposta con il ruolo mappato al formato frontend
        String ruolo = u.getRole().toString().toLowerCase();
        if (ruolo.equals("user")) {
            ruolo = "normal";
        }

        return new AuthResponse(
            u.getId(),
            u.getName() + " " + u.getSurname(),
            ruolo
        );
    }

    /**
     * Registra un nuovo utente.
     * La password viene hashata con BCrypt prima di salvare.
     * 
     * @return true se registrazione riuscita, false se email già esiste
     */
    public boolean register(String email, String password, String name, String surname) {
        if (utenteRepository.findByEmail(email).isPresent()) {
            return false;
        }

        Utente nuovo = new Utente();
        nuovo.setEmail(email);
        nuovo.setPassword(passwordEncoder.encode(password)); 
        nuovo.setName(name);
        nuovo.setSurname(surname);
        nuovo.setRole(Utente.Role.USER);

        utenteRepository.save(nuovo);
        return true;
    }
}
