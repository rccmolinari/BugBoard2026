package bugboard.exception;

import org.springframework.http.HttpStatus;

/** Input non valido o violazione di una regola di business → 400. */
public class BadRequestException extends ApiException {
    public BadRequestException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
