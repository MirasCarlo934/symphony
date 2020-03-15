package symphony.bm.bm_logic_devices.modules.exceptions;

public class RequestProcessingException extends Exception {
    public RequestProcessingException(String message) {
        super(message);
    }

    public RequestProcessingException(String message, Throwable cause) {
        super(message, cause);
    }

    public RequestProcessingException(Throwable cause) {
        super(cause);
    }

    public RequestProcessingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
