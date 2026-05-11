package bugboard.repository;

import bugboard.model.Notifica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NotificaRepository extends JpaRepository<Notifica, Integer> {
    
    // conta tutte le notifiche non lette per un user specifico
    long countByAssegnatoAAndLettaFalse(Integer assegnatoA);

    // trova tutte le notifiche per un user specifico
    List<Notifica> findByAssegnatoA(Integer assegnatoA);

    // trova tutte le notifiche assegnate da un admin specifico
    List<Notifica> findByAssegnatario(Integer assegnatario);

    // trova tutte le notifiche per un issue specifico
    List<Notifica> findByIssue(Integer issue);
}
