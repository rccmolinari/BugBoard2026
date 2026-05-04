package bugboard.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import bugboard.model.Issue;

import java.util.List;

public interface IssueRepository extends JpaRepository<Issue, Integer> {
    // Trova tutte le issue create da un utente specifico usando l'ID dal dump
    List<Issue> findByCreatoreId(Integer idCreatore);

}