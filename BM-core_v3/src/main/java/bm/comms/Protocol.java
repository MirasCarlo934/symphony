package bm.comms;

public class Protocol {
    private String protocolName;
    private Listener listener;
    private Sender sender;

    public Protocol(String protocolName, Listener listener, Sender sender) {
        this.protocolName = protocolName;
        this.listener = listener;
        this.sender = sender;

        this.listener.setProtocol(this);
    }

    public String getProtocolName() {
        return protocolName;
    }

    public Listener getListener() {
        return listener;
    }

    public Sender getSender() {
        return sender;
    }
}
