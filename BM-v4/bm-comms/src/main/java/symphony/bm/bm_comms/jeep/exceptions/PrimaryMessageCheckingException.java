package symphony.bm.bm_comms.jeep.exceptions;

public class PrimaryMessageCheckingException extends Exception {
    public PrimaryMessageCheckingException(String message) {
        super(message);
    }

    public PrimaryMessageCheckingException(String message, Throwable cause) {
        super(message, cause);
    }

    public PrimaryMessageCheckingException(Throwable cause) {
        super(cause);
    }

    protected PrimaryMessageCheckingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
