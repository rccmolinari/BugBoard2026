package bugboard.exception;

import org.springframework.http.HttpStatus;

/*
 * La radice di tutte le nostre eccezioni: ognuna si porta dietro lo status
 * HTTP che le corrisponde. Così il GlobalExceptionHandler ne gestisce un tipo
 * solo e lo status lo legge da qui; se domani serve un nuovo errore (tipo un
 * 422) basta fare la sottoclasse, senza mettere mano all'handler.
 */
public abstract class ApiException extends RuntimeException {

    private final HttpStatus status;

    protected ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
