package symphony.bm.cache.devices.rest.messages;

public abstract class MicroserviceMessage {
    private boolean success;
    
    public MicroserviceMessage(boolean success) {
        this.success = success;
    }
    
    public boolean isSuccess() {
        return success;
    }
}
