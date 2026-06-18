package bugboard.controller;

import java.util.Map;

import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import bugboard.exception.ApiException;
import jakarta.persistence.OptimisticLockException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /*
     * Un unico handler per tutte le ApiException: lo status HTTP è portato
     * dall'eccezione stessa, quindi aggiungere un nuovo tipo di errore non
     * richiede modificare questo handler (OCP).
     */
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Map<String, String>> handleApi(ApiException ex) {
        return ResponseEntity.status(ex.getStatus())
            .body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler({
        ObjectOptimisticLockingFailureException.class,
        OptimisticLockingFailureException.class,
        OptimisticLockException.class
    })
    public ResponseEntity<Map<String, String>> handleOptimisticLock(Exception ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(Map.of("message", "La issue è stata aggiornata da un altro utente. Riprova."));
    }
}
