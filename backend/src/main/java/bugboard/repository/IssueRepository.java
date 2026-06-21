package bugboard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import bugboard.dto.IssueResponse;
import bugboard.model.Issue;

import java.util.List;
import java.time.LocalDateTime;

@Repository
public interface IssueRepository extends JpaRepository<Issue, Integer> {

    
    // Tutte le issue create da un certo utente
    List<Issue> findByCreatoreId(Integer idCreatore);
    
    // Tutte le issue che un certo utente ha assegnato
    List<Issue> findByAssegnatarioId(Integer idAssegnatario);

    // Tutte le issue assegnate a un certo utente
    List<Issue> findByAssegnatoAId(Integer assegnatoAId);

    // Tutte le issue in un certo stato
    List<Issue> findByStato(Issue.StatoIssue stato);

    // Le issue sopra una certa priorità
    List<Issue> findByPrioritaGreaterThan(Integer priorita);

    // Filtra per stato e priorità insieme
    List<Issue> findByStatoAndPrioritaGreaterThan(Issue.StatoIssue stato, Integer priorita);
    
    // Cerca per titolo, senza distinguere maiuscole e minuscole
    List<Issue> findByTitoloContainingIgnoreCase(String titolo);
    
    // Le issue scadute, cioè con scadenza prima di una certa data
    List<Issue> findByDataScadenzaBefore(LocalDateTime dataScadenza);

    List<Issue> findByTipo(Issue.TipoIssue tipo);
    // Solo i bug, con creatore e assegnatario già caricati (li guardano gli stakeholder)
    @Query("SELECT i FROM Issue i " +
        "LEFT JOIN FETCH i.creatore " +
        "LEFT JOIN FETCH i.assegnatoA " +
        "WHERE i.tipo = 'BUG'")
    List<Issue> findOnlyBugWithCreatoreAndDataScadenzaAndDataCreazione();

    // Le issue che hanno una certa etichetta dentro l'array
    @Query(value = "SELECT * FROM issue WHERE ?1= ANY(etichetta)", nativeQuery = true)
    List<Issue> findBySpecificEtichetta(String etichetta);

    // Carico le issue tirandomi dietro anche il creatore in un colpo solo
    @Query("SELECT i FROM Issue i LEFT JOIN FETCH i.creatore")
    List<Issue> findAllWithCreatoreAndDataScadenzaAndDataCreazione();
    
    
    // Mi costruisco già qui il DTO per la lista admin, senza portarmi dietro l'immagine
    @Query("SELECT new bugboard.dto.IssueResponse(" +
        "i.id, " +
        "i.titolo, " +
        "CAST(i.tipo AS String), " +
        "i.priorita, " +
        "CAST(i.stato AS String), " +
        "c.email, " +
        "a.email, " +
        "i.dataScadenza, " +
        "i.dataCreazione, " +
        "i.version) " +
        "FROM Issue i " +
        "LEFT JOIN i.creatore c " +
        "LEFT JOIN i.assegnatoA a")
    List<IssueResponse> findAllIssuesSenzaImmagine();

}
