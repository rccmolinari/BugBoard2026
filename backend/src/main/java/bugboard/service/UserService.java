package bugboard.service;

import bugboard.dto.RegisterRequest;
import bugboard.dto.AllUserResponse;

import bugboard.model.Utente;

import bugboard.repository.UserRepository;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/*
 * SRP  — gestisce solo operazioni CRUD sugli utenti (admin).
 * DIP  — dipende da ISessioneService (astrazione).
 *        Il BCryptPasswordEncoder è iniettato via @Bean, non istanziato qui.
 */
@Service
public class UserService implements IUserService {

    @Autowired
    private UserRepository utenteRepository;

    @Autowired
    private ISessioneService sessioneService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public Utente creaNuovoUtente(UUID sid, RegisterRequest request) {
        if (!sessioneService.isAdmin(sid)) return null;

        if (utenteRepository.findByEmail(request.getEmail()).isPresent()) {
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

    @Override
    public List<AllUserResponse> getAllUsers(UUID sid) {
        if (!sessioneService.isAdmin(sid)) return Collections.emptyList();

        List<AllUserResponse> response = new ArrayList<>();
        for (Utente u : utenteRepository.findAll()) {
            AllUserResponse dto = new AllUserResponse();
            dto.setId(u.getId());
            dto.setName(u.getName());
            dto.setSurname(u.getSurname());
            dto.setEmail(u.getEmail());
            dto.setRole(u.getRole().toString());
            response.add(dto);
        }
        return response;
    }
}
