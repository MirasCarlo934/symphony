package bm.main.modules.exceptions;

public class ResponseProcessingException extends Exception {
    public ResponseProcessingException(String message) {
        super(message);
    }

    public ResponseProcessingException(String message, Throwable cause) {
        super(message, cause);
    }

    public ResponseProcessingException(Throwable cause) {
        super(cause);
    }

    public ResponseProcessingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
