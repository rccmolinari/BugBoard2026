package bugboard.service;


import bugboard.dto.RegisterRequest;

import bugboard.model.Utente;

import bugboard.repository.UserRepository;
import bugboard.dto.AllUserResponse;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;
import bugboard.service.SessioneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Service
public class UserService {

    @Autowired
    private UserRepository utenteRepository;

    @Autowired
    private SessioneService sessioneService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Transactional
    public Utente creaNuovoUtente(UUID sid, RegisterRequest request) {
        
        Utente admin = sessioneService.getUtenteBySessionId(sid);

        if (admin == null || !admin.getRole().name().equalsIgnoreCase("ADMIN")) {
                return null;
        }
            //controllo se email già esiste
        if(utenteRepository.findByEmail(request.getEmail()).isPresent()){
            throw new RuntimeException("Email già presente " + request.getEmail());
        }

    
        Utente nuovo = new Utente();
        nuovo.setName(request.getName());
        nuovo.setSurname(request.getSurname());
        nuovo.setEmail(request.getEmail());
        nuovo.setPassword(passwordEncoder.encode(request.getPassword()));
        
        try {
            nuovo.setRole(Utente.Role.fromValue(request.getRole()));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Ruolo non valido: " + request.getRole());
        }
        
        return utenteRepository.save(nuovo);
}

    public List<AllUserResponse> getAllUsers(UUID sid) {
        if(sessioneService.getUtenteBySessionId(sid).getRole() != Utente.Role.ADMIN) {
            return null;
        }
        List<AllUserResponse> response = new ArrayList<>();
        List<Utente> utenti = utenteRepository.findAll();
        for(Utente u : utenti) {
            AllUserResponse userResponse = new AllUserResponse();
            userResponse.setId(u.getId());
            userResponse.setName(u.getName());
            userResponse.setSurname(u.getSurname());
            userResponse.setEmail(u.getEmail());
            userResponse.setRole(u.getRole().toString());
            response.add(userResponse);
        }
        return response;
    }
}
