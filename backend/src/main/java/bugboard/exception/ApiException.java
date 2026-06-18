package bugboard.exception;

import org.springframework.http.HttpStatus;

/*
 * Base per tutte le eccezioni applicative che mappano su un preciso
 * stato HTTP. Il GlobalExceptionHandler gestisce un solo tipo (ApiException)
 * e legge lo status da qui: aggiungere una nuova eccezione (es. 422) non
 * richiede toccare l'handler (OCP).
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
