package symphony.bm.services.registry.exceptions;

public class RequestProcessingException extends Exception {
    public RequestProcessingException(String message) {
        super(message);
    }
    
    public RequestProcessingException(String message, Exception e) {
        super(message, e);
    }
}
