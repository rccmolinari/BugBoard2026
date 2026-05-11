package bugboard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import bugboard.model.Utente;

import java.util.List;
import java.util.Optional;

@Repository
public interface UtenteRepository extends JpaRepository<Utente, Integer> {
    
    // Trova un utente specifico in base all'email
    Optional<Utente> findByEmail(String email);

    Optional<Utente> findById(Integer id);
    
   
    // Trova tutti gli utenti in base al ruolo
    List<Utente> findByRole(Utente.Role role); 

    // Controlla se email esiste
    boolean existsByEmail(String email);

}