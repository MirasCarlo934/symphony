package symphony.bm.cache.devices.entities;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import symphony.bm.cache.devices.adaptors.Adaptor;

import java.util.HashMap;
import java.util.List;

@Document(collection = "entities")
@RequiredArgsConstructor
public class Room extends Entity {
    @Id @JsonIgnore private String _id;
    @JsonProperty("RID") @NonNull @Getter(onMethod_ = {@JsonProperty("RID")}) private String RID;
    @NonNull @Getter private String name;
    @NonNull @Getter protected HashMap<String, Device> devices;
    @NonNull @Getter protected HashMap<String, Room> rooms;
    
    @Transient @Setter @Getter(onMethod_ = {@JsonIgnore}) private Room parentRoom;
    
    public Room(String RID, String name) {
        this();
        this.RID = RID;
        this.name = name;
    }
    
    public Room() {
        devices = new HashMap<>();
        rooms = new HashMap<>();
    }
    
    public Device getDevice(String cid) {
        if (devices.containsKey(cid)) {
            return devices.get(cid);
        } else if (!rooms.isEmpty()) {
            for (Room room : rooms.values()) {
                Device d = room.getDevice(cid);
                if (d != null) return d;
            }
        }
        return null;
    }
    
    public void addDevice(Device device) throws Exception {
        devices.put(device.getCID(), device);
        device.setAdaptors(adaptors);
        device.setRoom(this);
        device.registerDeviceInAdaptors();
    }
    
    public Device removeDevice(String cid) throws Exception {
        Device device = null;
        if (devices.containsKey(cid)) {
            device = devices.remove(cid);
        } else {
            for (Room room : rooms.values()) {
                device = room.removeDevice(cid);
                if (device != null) break;
            }
        }
        if (device != null) {
            device.unregisterDeviceInAdaptors();
        }
        return device;
    }
    
    public Device transferDevice(String cid, Room room) throws Exception {
        Device device = devices.remove(cid);
        if (device != null) {
            room.addDevice(device);
        }
        return device;
    }
    
    public Room getRoom(String rid) {
        if (rooms.containsKey(rid)) {
            return rooms.get(rid);
        } else if (!rooms.isEmpty()) {
            for (Room room : rooms.values()) {
                Room r = room.getRoom(rid);
                if (r != null) return r;
            }
        }
        return null;
    }
    
    public void addRoom(Room room) throws Exception {
        rooms.put(room.getRID(), room);
        room.setParentRoom(this);
        room.setAdaptors(adaptors);
        room.createRoomInAdaptors();
    }
    
    /**
     * Removes a room from this room or its subrooms.
     *
     * @param rid
     * @return Room object of removed room, <i>null</i> if room does not exist in this room and its subrooms
     */
    public Room removeRoom(String rid) throws Exception {
        Room room = null;
        if (rooms.containsKey(rid)) {
            room = rooms.remove(rid);
        } else {
            for (Room r : rooms.values()) {
                room = r.removeRoom(rid);
                if (room != null) break;
            }
        }
        if (room != null) {
            room.removeRoomInAdaptors();
        }
        return room;
    }

    public void createRoomInAdaptors() throws Exception {
        for (Adaptor adaptor : adaptors) {
            adaptor.roomCreated(this);
        }
    }
    
    public void removeRoomInAdaptors() throws Exception {
        for (Adaptor adaptor : adaptors) {
            adaptor.roomDeleted(this);
        }
    }
    
    public void test() {
        for (String s : devices.keySet()) {
            System.out.print(s + ",");
        }
        System.out.println();
        for (String s : rooms.keySet()) {
            System.out.print(s + ",");
            rooms.get(s).test();
        }
    }
    
    public int countAllDevices() {
        int count = 0;
        count += devices.size();
        for (Room room : rooms.values()) {
            count += room.countAllDevices();
        }
        return count;
    }
    
    public int countAllRooms() {
        int count = 0;
        count += rooms.size();
        for (Room room : rooms.values()) {
            count += room.countAllRooms();
        }
        return count;
    }
    
    @JsonIgnore
    public Room getFirstAncestorRoom() {
//        System.out.println(parentRoom.getClass());
//        System.out.println(SuperRoom.class + " - " + name);
        if (parentRoom.getClass().equals(SuperRoom.class)) {
            return this;
        } else {
            return parentRoom.getFirstAncestorRoom();
        }
    }
    
    @Override
    protected void setAdaptorsToChildren(List<Adaptor> adaptors) {
        for (Device d : devices.values()) {
            d.setAdaptors(adaptors);
        }
        for (Room r : rooms.values()) {
            r.setAdaptors(adaptors);
        }
    }
    
    @Override
    protected void setSelfToChildren() {
        for (Device d : devices.values()) {
            try {
                d.setRoom(this);
            } catch (Exception e) {
                e.printStackTrace();
            }
            d.setSelfToChildren();
        }
        for (Room r : rooms.values()) {
            r.setParentRoom(this);
            r.setSelfToChildren();
        }
    }
}
