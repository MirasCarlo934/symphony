package symphony.bm.bmlogicdevices.entities;

import symphony.bm.bmlogicdevices.adaptors.Adaptor;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Vector;

public class Device {
    private String cid;
    private String pid;
    private String name;
    private Room room;
    private HashMap<Integer, DeviceProperty> properties = new HashMap<>();
    private List<Adaptor> adaptors;

    public Device(String cid, String pid, String name, Room room, List<DeviceProperty> properties, List<Adaptor> adaptors) {
        this.cid = cid;
        this.pid = pid;
        this.name = name;
        this.room = room;
        this.adaptors = adaptors;

        room.addDevice(this);

        for (DeviceProperty property : properties) {
            this.properties.put(property.getIndex(), property);
        }
    }

    public void registerDevice() {
        for (Adaptor adaptor : adaptors) {
            adaptor.deviceRegistered(this);
        }
    }

    public void unregisterDevice() {
        for (Adaptor adaptor : adaptors) {
            adaptor.deviceUnregistered(this);
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

    public boolean hasPropertyIndex(int index) {
        return properties.containsKey(index);
    }

    public boolean checkValueValidity(int propIndex, int value) {
        return properties.get(propIndex).checkValue(value);
    }

    public void setPropertyValue(int propIndex, int value) {
        DeviceProperty prop = properties.get(propIndex);
        for (Adaptor adaptor : adaptors) {
            adaptor.propertyValueUpdated(this, prop);
        }
        prop.setValue(value);
    }

    public int getPropertyValue(int propIndex) {
        return properties.get(propIndex).getValue();
    }

    public List<DeviceProperty> getPropertiesList() {
        Vector<DeviceProperty> props = new Vector<>();
        for (int i = 0; i < properties.size(); i++) {
            props.add(properties.get(i));
        }
        return props;
    }
}
