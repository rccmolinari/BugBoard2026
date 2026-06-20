package bugboard.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import bugboard.exception.ApiException;

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
}
