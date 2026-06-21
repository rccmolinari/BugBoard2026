package bugboard.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import bugboard.model.Utente;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<Utente, Integer> {
    
    // Cerca l'utente partendo dalla mail (la usiamo per login e vari controlli)
    Optional<Utente> findByEmail(String email);

    // Tutti gli utenti di un certo ruolo
    List<Utente> findByRole(Utente.Role role); 

    // Dice solo se esiste già un utente con quella mail
    boolean existsByEmail(String email);

}