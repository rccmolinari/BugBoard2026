package bugboard.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import bugboard.model.Utente;

import bugboard.repository.UtenteRepository;

import bugboard.dto.UserRegistrationRequest; 


@Service
public class AdminService {
       
        @Autowired
        private UtenteRepository utenteRepository;

        public Utente creaNuovoUtente(UserRegistrationRequest request) {
            Utente utente = new Utente();
            utente.setEmail(request.getEmail());
            utente.setPassword(request.getPassword());
            utente.setName(request.getName());
            utente.setSurname(request.getSurname());
            
            try {
                utente.setRole(Utente.Role.fromValue(request.getRole()));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Ruolo non valido: " + request.getRole());
            }

            return utenteRepository.save(utente);
        }


}
