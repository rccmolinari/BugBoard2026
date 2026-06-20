package bugboard.repository;

import bugboard.model.Notifica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NotificaRepository extends JpaRepository<Notifica, Integer> {

    // Le notifiche arrivate a un certo utente
    List<Notifica> findByAssegnatoAId(Integer assegnatoA);

    // Le notifiche generate da un certo admin
    List<Notifica> findByAssegnatarioId(Integer assegnatario);

    // Le notifiche legate a una certa issue
    List<Notifica> findByIssueId(Integer issue);
    
    // Quante notifiche non lette ha un utente (per il pallino col numero)
    int countByAssegnatoAIdAndLettaFalse(Integer assegnatoA);


}
