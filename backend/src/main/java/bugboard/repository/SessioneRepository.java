package bugboard.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import jakarta.transaction.Transactional; 

import bugboard.model.Sessione;
import bugboard.model.Utente;

import java.util.UUID;

@Repository
public interface SessioneRepository extends JpaRepository<Sessione, UUID> {

    Sessione findBySid(UUID sid);
    
    @Query("SELECT s.utente FROM Sessione s WHERE s.sid = :sid")
    Utente findUtenteBySid(@Param("sid")UUID sid);


    @Modifying
    @Transactional
    void deleteBySid(UUID sid);
}