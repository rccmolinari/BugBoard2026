package bugboard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import bugboard.model.Issue;

import java.util.List;
import java.time.LocalDateTime;

@Repository
public interface IssueRepository extends JpaRepository<Issue, Integer> {
    // Trova tutte le issue create da un utente specifico usando l'ID dal dump
    List<Issue> findByCreatoreId(Integer idCreatore);
    
    // Trova tutte le issue assegnate a un utente specifico usando l'ID dal dump
    List<Issue> findByAssegnatarioId(Integer idAssegnatario);

    // Trova tutte le issue con un certo stato
    List<Issue> findByStato(Issue.StatoIssue stato);

    // Trova tutte le issue in base alla priorità
    List<Issue> findByPrioritaGreaterThan(Integer priorita);

    // Trova issue in base a stato e priorità
    List<Issue> findByStatoAndPrioritaGreaterThan(Issue.StatoIssue stato, Integer priorita);
    
    // Cerca una issue in base al titolo
    List<Issue> findByTitoloContainingIgnoreCase(String titolo);
    
    // Trova le issue in base alla data di scadenza
    List<Issue> findByDataScadenzaBefore(LocalDateTime dataScadenza);

    // Cerca tutte le issue in base ad una etichetta specifica
    @Query(value = "SELECT * FROM issue WHERE ?1= ANY(etichetta)", nativeQuery = true)
    List<Issue> findBySpecificEtichetta(String etichetta);
}
