package bugboard.controller;

import java.util.Map;

import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.persistence.OptimisticLockException;

@RestControllerAdvice
public class GlobalExceptionHandler {

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
