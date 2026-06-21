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

    // Un solo punto dove le ApiException diventano risposte HTTP: lo status se
    // lo porta dietro l'eccezione stessa, quindi se un domani aggiungo un nuovo
    // tipo di errore qui non devo cambiare niente.
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Map<String, String>> handleApi(ApiException ex) {
        return ResponseEntity.status(ex.getStatus())
            .body(Map.of("message", ex.getMessage()));
    }

    // Quando @Version becca due salvataggi concorrenti sulla stessa issue,
    // Hibernate alza un'eccezione di lock: la trasformo in un 409 con un
    // messaggio chiaro, così il client sa che deve ricaricare e riprovare.
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
