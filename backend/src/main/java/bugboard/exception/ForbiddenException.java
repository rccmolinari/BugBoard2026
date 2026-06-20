package bugboard.exception;

import org.springframework.http.HttpStatus;

// Sessione mancante o non valida, oppure permessi che non bastano: 403.
public class ForbiddenException extends ApiException {
    public ForbiddenException(String message) {
        super(HttpStatus.FORBIDDEN, message);
    }
}
