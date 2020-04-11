package symphony.bm.registry.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import symphony.bm.registry.adaptors.Adaptor;

import java.util.HashMap;
import java.util.List;
import java.util.Vector;

@Document(collection = "registry")
public class Room extends Entity {
    @Field("RID") @Getter private String rid;
    @Getter private String name;
    @Getter protected HashMap<String, Device> devices;
    @Getter protected HashMap<String, Room> rooms;
    
    @Transient @Setter @Getter(onMethod_ = {@JsonIgnore}) private Room parentRoom;

    public Room(String rid, String name, HashMap<String, Device> devices, HashMap<String, Room> rooms) {
        this.rid = rid;
        this.name = name;
        this.devices = devices;
        this.rooms = rooms;
    }
    
    public Room(String rid, String name) {
        this();
        this.rid = rid;
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

    public void createRoom() {
        for (Adaptor adaptor : adaptors) {
            adaptor.roomCreated(this);
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
    
    @Override
    void setAdaptorsToChildren(List<Adaptor> adaptors) {
        for (Device d : devices.values()) {
            d.setAdaptors(adaptors);
        }
        for (Room r : rooms.values()) {
            r.setAdaptors(adaptors);
        }
    }
    
    @Override
    void setSelfToChildren() {
        for (Device d : devices.values()) {
            d.setRoom(this);
            d.setSelfToChildren();
        }
        for (Room r : rooms.values()) {
            r.setParentRoom(this);
            r.setSelfToChildren();
        }
    }
    
//    public Document convertToDocument() {
//        return new Document()
//                .append("RID", rid)
//                .append("name", name);
//    }

//    public void addDevice(Device device) {
//        devices.add(device);
//    }

//    @JsonIgnore
//    public Vector<Device> getDevices() {
//        return devices;
//    }
//
//    @JsonProperty("RID")
//    public String getRID() {
//        return rid;
//    }
//
//    public String getName() {
//        return name;
//    }
//
//    public void setName(String name) {
//        this.name = name;
//    }
}
