package symphony.bm.bmlogicdevices.entities;

import symphony.bm.bmlogicdevices.adaptors.Adaptor;

import java.util.HashMap;
import java.util.List;

public class Device {
    private String cid;
    private String pid;
    private String name;
    private Room room;
    private HashMap<Integer, DeviceProperty> properties = new HashMap<>();
    private List<Adaptor> adaptors;

    public Device(String cid, String pid, String name, Room room, DeviceProperty[] properties, List<Adaptor> adaptors) {
        this.cid = cid;
        this.pid = pid;
        this.name = name;
        this.room = room;
        this.adaptors = adaptors;

        room.addDevice(this);

        for (int i = 0; i < properties.length; i++) {
            this.properties.put(properties[i].getIndex(), properties[i]);
        }
    }

    public void registerDevice() {
        for (Adaptor adaptor : adaptors) {
            adaptor.deviceRegistered(this);
        }
    }


    public String getCID() {
        return cid;
    }

    public String getPID() {
        return pid;
    }

    public String getName() {
        return name;
    }

    public Room getRoom() {
        return room;
    }
}
