package bugboard.service;


import bugboard.dto.RegisterRequest;

import bugboard.model.Utente;

import bugboard.repository.UtenteRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Service
public class UserService {

    @Autowired
    private UtenteRepository utenteRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Transactional
    public Utente creaNuovoUtente(RegisterRequest request) {
    
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

}
