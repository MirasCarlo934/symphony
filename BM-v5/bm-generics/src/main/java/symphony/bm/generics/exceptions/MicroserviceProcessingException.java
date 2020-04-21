package symphony.bm.generics.exceptions;

public class MicroserviceProcessingException extends Exception {
    public MicroserviceProcessingException(String message) {
        super(message);
    }

    public MicroserviceProcessingException(String message, Exception e) {
        super(message, e);
    }
}
