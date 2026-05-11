package bugboard.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import bugboard.dto.AuthResponse;
import bugboard.dto.LoginRequest;
import bugboard.dto.RegisterRequest;
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
     * @param request payload di login
     * @return AuthResponse con id, nome, ruolo se autentico, altrimenti null
     */
    public AuthResponse login(LoginRequest request) {
        Optional<Utente> utente = utenteRepository.findByEmail(request.getEmail());

        if (utente.isEmpty()) {
            return null;
        }

        Utente u = utente.get();

        if (!passwordEncoder.matches(request.getPassword(), u.getPassword())) {
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
    
    // notifica errore registrazione per colpa dell'email già esistente
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
