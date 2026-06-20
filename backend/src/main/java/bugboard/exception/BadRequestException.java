package bugboard.exception;

import org.springframework.http.HttpStatus;

// Input sbagliato o una regola di business non rispettata: 400.
public class BadRequestException extends ApiException {
    public BadRequestException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
