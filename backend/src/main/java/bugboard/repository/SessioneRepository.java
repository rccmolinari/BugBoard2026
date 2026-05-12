package bugboard.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import bugboard.model.Sessione;
import bugboard.model.Utente;
import java.util.UUID;
@Repository
public interface SessioneRepository extends JpaRepository<Sessione, UUID> {

    Sessione findBySid(UUID sid);
    
    Utente findUtenteBySid(UUID sid);

    void deleteBySid(UUID sid);
}