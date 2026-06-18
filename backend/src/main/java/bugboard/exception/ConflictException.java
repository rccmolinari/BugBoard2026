package bugboard.exception;

import org.springframework.http.HttpStatus;

/** Conflitto con lo stato attuale (es. email già registrata) → 409. */
public class ConflictException extends ApiException {
    public ConflictException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}
