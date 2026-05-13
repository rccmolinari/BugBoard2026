package bugboard.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import bugboard.model.Sessione;
import bugboard.model.Utente;
import jakarta.transaction.Transactional;

import java.util.UUID;
@Repository
public interface SessioneRepository extends JpaRepository<Sessione, UUID> {

    Sessione findBySid(UUID sid);
    
    @Query("SELECT s.utente FROM Sessione s WHERE s.sid = :sid")
        Utente findUtenteBySid(@Param("sid") UUID sid);

    @Modifying
    @Transactional // Fondamentale per le operazioni di DELETE/UPDATE
    void deleteBySid(UUID sid);
}