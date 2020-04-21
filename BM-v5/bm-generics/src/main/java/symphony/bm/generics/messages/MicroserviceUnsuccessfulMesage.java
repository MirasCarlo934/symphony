package symphony.bm.generics.messages;

public class MicroserviceUnsuccessfulMesage extends MicroserviceMessage {
    
    public MicroserviceUnsuccessfulMesage(String msg) {
        super(false, msg);
    }
}
