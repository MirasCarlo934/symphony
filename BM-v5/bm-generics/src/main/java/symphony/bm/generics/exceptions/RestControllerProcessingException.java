package symphony.bm.generics.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

public class RestControllerProcessingException extends Exception {
    @Getter private HttpStatus httpStatus;

    public RestControllerProcessingException(String msg, HttpStatus httpStatus, Throwable cause) {
        super(msg, cause);
        this.httpStatus = httpStatus;
    }

    public RestControllerProcessingException(String msg, HttpStatus httpStatus) {
        super(msg);
        this.httpStatus = httpStatus;
    }
}
