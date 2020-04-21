package symphony.bm.generics.messages;

public class MicroserviceSuccessfulMessage extends MicroserviceMessage {

    public MicroserviceSuccessfulMessage(String message) {
        super(true, message);
    }
    
}
