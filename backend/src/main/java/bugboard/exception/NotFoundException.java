package bugboard.exception;

import org.springframework.http.HttpStatus;

/** Risorsa inesistente → 404. */
public class NotFoundException extends ApiException {
    public NotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }
}
