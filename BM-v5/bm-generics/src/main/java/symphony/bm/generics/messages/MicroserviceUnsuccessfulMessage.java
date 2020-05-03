package symphony.bm.generics.messages;

public class MicroserviceUnsuccessfulMessage extends MicroserviceMessage {
    
    public MicroserviceUnsuccessfulMessage(String msg) {
        super(false, msg);
    }
}
