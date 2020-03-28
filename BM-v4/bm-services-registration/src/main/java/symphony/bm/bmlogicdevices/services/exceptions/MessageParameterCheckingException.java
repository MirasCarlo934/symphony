package symphony.bm.bmlogicdevices.services.exceptions;

public class MessageParameterCheckingException extends Exception {
    public MessageParameterCheckingException(String msg) {
        super(msg);
    }
    public MessageParameterCheckingException(String msg, Exception e) {
        super(msg, e);
    }
}
