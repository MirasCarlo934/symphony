package symphony.bm.cache.devices.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import symphony.bm.cache.devices.adaptors.AdaptorManager;

import java.util.List;
import java.util.Vector;

@Document(collection = "entities")
@RequiredArgsConstructor
public class Room extends Entity {
    @Id @JsonIgnore private String _id;
    @JsonProperty("RID") @NonNull @Getter(onMethod_ = {@JsonProperty("RID")}) private String RID;
    @Setter(AccessLevel.PRIVATE) @Getter private String parentRID;
    @NonNull @Getter private String name;
    @NonNull @Getter protected List<Device> devices;
    @NonNull @Getter protected List<Room> rooms;
    
    @Transient @Setter @Getter(onMethod_ = {@JsonIgnore}) private Room parentRoom;
    
    public Room(String RID, String name) {
        this();
        this.RID = RID;
        this.name = name;
    }
    
    public Room() {
        devices = new Vector<>();
        rooms = new Vector<>();
    }
    
    /**
     * Gets the Device object with the specified cid in this room and its subrooms.
     * @param cid The Device CID
     * @return A Device object or <i>null</i> if nonexistent.
     */
    public Device getDevice(String cid) {
        for (Device d : devices) {
            if (d.getCID().equals(cid)) return d;
        }
        if (!rooms.isEmpty()) {
            for (Room room : rooms) {
                Device d = room.getDevice(cid);
                if (d != null) return d;
            }
        }
        return null;
    }

    private void addDevice(Device device) {
        devices.add(device);
        device.setRID(RID);
    }

    /**
     * Adds a Device in this Room and <b>registers</b> it in the adaptors.
     * @param device
     * @throws Exception
     */
    public void addDeviceAndCreateInAdaptors(Device device) throws Exception {
        addDevice(device);
        device.setAdaptorManager(adaptorManager);
        device.setRoom(this);
        adaptorManager.deviceCreated(device);
    }

    private Device removeDevice(String cid) {
        Device device = getDevice(cid);
        devices.remove(device);
        return device;
    }

    /**
     * Removes a Device from this Room and <b>unregisters</b> it in the adaptors.
     * @param cid
     * @return
     * @throws Exception
     */
    public Device removeDeviceAndDeleteInAdaptors(String cid) throws Exception {
        Device device = removeDevice(cid);
        if (device != null) {
            adaptorManager.deviceDeleted(device);
        }
        return device;
    }

    /**
     * Transfers a Device from this Room to another specified Room and triggers a <b>device transfer<b/> in the
     * adaptors.
     * @param cid
     * @param room
     * @return
     * @throws Exception
     */
    public void transferDevice(String cid, Room room) throws Exception {
        Device device = removeDevice(cid);
        if (device != null) {
            devices.remove(device);
            room.addDevice(device);
            adaptorManager.deviceTransferredRoom(device, this, room);
        }
    }
    
    public Room getRoom(String rid) {
        for (Room r : rooms) {
            if (r.getRID().equals(rid)) return r;
        }
        if (!rooms.isEmpty()) {
            for (Room room : rooms) {
                Room r = room.getRoom(rid);
                if (r != null) return r;
            }
        }
        return null;
    }

    private void addRoom(Room room) {
        rooms.add(room);
        room.setParentRID(RID);
    }

    /**
     * Adds a Room in this Room and <b>creates</b> it in the adaptors.
     * @param room
     * @throws Exception
     */
    public void addRoomAndCreateInAdaptors(Room room) throws Exception {
        addRoom(room);
        room.setParentRoom(this);
        room.setAdaptorManager(adaptorManager);
        adaptorManager.roomCreated(room);
    }

    private Room removeRoom(String rid) {
        Room room = getRoom(rid);
        rooms.remove(room);
        return room;
    }
    
    /**
     * Removes a Room from this Room and <b>deletes<b/> it in the adaptors.
     *
     * @param rid
     * @return Room object of removed room, <i>null</i> if room does not exist in this room and its subrooms
     */
    public Room removeRoomAndDeleteInAdaptors(String rid) throws Exception {
        Room room = removeRoom(rid);
        if (room != null) {
            adaptorManager.roomDeleted(room);
        }
        return room;
    }

    /**
     * Transfers a Room from this Room to another specified Room and triggers a <b>room transfer<b/> in the
     * adaptors.
     * @param rid
     * @param room
     * @return
     * @throws Exception
     */
    public void transferRoom(String rid, Room room) throws Exception {
        Room r = getRoom(rid);
        if (r != null) {
            rooms.remove(r);
            r.addRoomAndCreateInAdaptors(r);
            adaptorManager.roomTransferredRoom(r, this, r);
        }
    }
    
    public int countAllDevices() {
        int count = 0;
        count += devices.size();
        for (Room room : rooms) {
            count += room.countAllDevices();
        }
        return count;
    }
    
    public int countAllRooms() {
        int count = 0;
        count += rooms.size();
        for (Room room : rooms) {
            count += room.countAllRooms();
        }
        return count;
    }
    
    @JsonIgnore
    public Room getFirstAncestorRoom() {
        if (parentRoom.getClass().equals(SuperRoom.class)) {
            return this;
        } else {
            return parentRoom.getFirstAncestorRoom();
        }
    }
    
    @Override
    protected void setAdaptorManagerToChildren(AdaptorManager adaptors) {
        for (Device d : devices) {
            d.setAdaptorManager(adaptors);
        }
        for (Room r : rooms) {
            r.setAdaptorManager(adaptors);
        }
    }
    
    @Override
    protected void setSelfToChildren() {
        for (Device d : devices) {
            try {
                d.setRoom(this);
            } catch (Exception e) {
                e.printStackTrace();
            }
            d.setSelfToChildren();
        }
        for (Room r : rooms) {
            r.setParentRoom(this);
            r.setSelfToChildren();
        }
    }
}
