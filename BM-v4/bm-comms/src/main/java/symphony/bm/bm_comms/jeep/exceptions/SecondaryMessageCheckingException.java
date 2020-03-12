package symphony.bm.bm_comms.jeep.exceptions;

public class SecondaryMessageCheckingException extends Exception {
    public SecondaryMessageCheckingException(String message) {
        super(message);
    }

    public SecondaryMessageCheckingException(String message, Throwable cause) {
        super(message, cause);
    }

    public SecondaryMessageCheckingException(Throwable cause) {
        super(cause);
    }

    protected SecondaryMessageCheckingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
