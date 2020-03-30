package symphony.bm.bmlogicdevices.entities;

import org.bson.Document;
import symphony.bm.bmlogicdevices.adaptors.Adaptor;

import java.util.List;
import java.util.Vector;

public class Room {
    private String rid;
    private String name;
    private Vector<Device> devices = new Vector<>();
    private List<Adaptor> adaptors;

    public Room(String rid, String name, List<Adaptor> adaptors) {
        this.rid = rid;
        this.name = name;
        this.adaptors = adaptors;
    }

    public void createRoom() {
        for (Adaptor adaptor : adaptors) {
            adaptor.roomCreated(this);
        }
    }

    public Document convertToDocument() {
        return new Document()
                .append("RID", rid)
                .append("name", name);
    }

    public void addDevice(Device device) {
        devices.add(device);
    }

    public Vector<Device> getDevices() {
        return devices;
    }

    public String getRID() {
        return rid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
