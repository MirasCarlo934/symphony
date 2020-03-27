package symphony.bm.bmlogicdevices.entities;

import symphony.bm.bmlogicdevices.adaptors.Adaptor;

import java.util.List;
import java.util.Vector;

public class Room {
    private String RID;
    private String name;
    private Vector<Device> devices = new Vector<>();
    private List<Adaptor> adaptors;

    public Room(String RID, String name, List<Adaptor> adaptors) {
        this.RID = RID;
        this.name = name;
        this.adaptors = adaptors;
    }

    public void createRoom() {
        for (Adaptor adaptor : adaptors) {
            adaptor.roomCreated(this);
        }
    }

    public void addDevice(Device device) {
        devices.add(device);
    }

    public Vector<Device> getDevices() {
        return devices;
    }

    public String getRID() {
        return RID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
