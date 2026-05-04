package bugboard.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import bugboard.model.Utente;

import java.util.Optional;

public interface UtenteRepository extends JpaRepository<Utente, Integer> {
    Optional<Utente> findByEmail(String email);
}