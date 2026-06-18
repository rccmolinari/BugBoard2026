package bugboard.exception;

import org.springframework.http.HttpStatus;

/** Sessione assente/non valida o permessi insufficienti → 403. */
public class ForbiddenException extends ApiException {
    public ForbiddenException(String message) {
        super(HttpStatus.FORBIDDEN, message);
    }
}
