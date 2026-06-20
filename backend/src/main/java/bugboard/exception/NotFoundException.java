package bugboard.exception;

import org.springframework.http.HttpStatus;

// Roba che non esiste: diventa un 404.
public class NotFoundException extends ApiException {
    public NotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }
}
