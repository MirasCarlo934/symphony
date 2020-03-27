package symphony.bm.bmlogicdevices.services;

public abstract class ServiceManager {
    private String msn;

    public ServiceManager(String msn) {
        this.msn = msn;
    }

    public abstract Service createService();

    public String getMSN() {
        return msn;
    }
}
