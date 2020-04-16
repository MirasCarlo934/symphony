package symphony.bm.cache.devices.rest;

import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import symphony.bm.cache.devices.entities.SuperRoom;
import symphony.bm.cache.devices.entities.Device;
import symphony.bm.cache.devices.entities.deviceproperty.DeviceProperty;
import symphony.bm.cache.devices.entities.Room;
import symphony.bm.cache.devices.rest.messages.MicroserviceMessage;
import symphony.bm.cache.devices.rest.messages.MicroserviceSuccessfulMessage;

import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Vector;

@RestController
public class RestMicroserviceCommunicator {
    private static final Logger LOG = LoggerFactory.getLogger(RestMicroserviceCommunicator.class);
    private SuperRoom superRoom;

    public RestMicroserviceCommunicator(SuperRoom superRoom) {
        this.superRoom = superRoom;
    }
    
    @GetMapping("/")
    public SuperRoom getAll() {
        LOG.info("Returning all entities in the Symphony Network...");
        return superRoom;
    }
    
    @GetMapping("/devices/newcid")
    public String getNewDeviceCID() {
        LOG.info("Generating new device CID...");
        String cid = generateNewCID();
        LOG.info("New CID " + cid + " generated");
        return cid;
    }
    
    @GetMapping("/devices/{cid}")
    public Device getDevice(@PathVariable String cid) {
        LOG.info("Getting device " + cid);
        Device d = superRoom.getDevice(cid);
        if (d == null) {
            LOG.warn("No device found!");
        }
        return d;
    }
    
    @GetMapping("/devices/{cid}/{prop_index}")
    public DeviceProperty getDeviceProperty(@PathVariable String cid, @PathVariable int prop_index) {
        LOG.info("Getting device property " + cid + "." + prop_index);
        Device d = getDevice(cid);
        if (d == null) {
            return null;
        }
        DeviceProperty p = d.getProperty(prop_index);
        if (p == null) {
            LOG.warn("No property " + prop_index + " found in device " + cid + "!");
        }
        return p;
    }
    
    @PostMapping("/query/propertylist")
    public HashMap<String, List<DeviceProperty>> getDeviceProperties(@RequestBody HashMap<String, List<Integer>> requestBody) {
        LOG.info("Getting device properties...");
        HashMap<String, List<DeviceProperty>> response = new HashMap<>();
        int n = 0;
        for (String cid : requestBody.keySet()) {
            response.put(cid, new Vector<>());
            Device d = superRoom.getDevice(cid);
            for (int prop_index : requestBody.get(cid)) {
                response.get(cid).add(d.getProperty(prop_index));
                n++;
                LOG.info(cid + "." + prop_index + " retrieved");
            }
        }
        LOG.info(n + " properties retrieved");
        return response;
    }
    
    @GetMapping("/rooms/{rid}")
    public Room getRoom(@PathVariable String rid) {
        LOG.info("Getting room " + rid);
        Room r = superRoom.getRoom(rid);
        if (r == null) {
            LOG.warn("No room found!");
        }
        return r;
    }
    
    @PatchMapping("/devices/{cid}")
    public MicroserviceMessage updateDevice(@PathVariable String cid, @RequestBody Device device) throws Exception {
        LOG.info("Updating device " + cid + "...");
        Device existing = superRoom.getDevice(cid);
        Room oldRoom = superRoom.getRoom(existing.getRID());
        Room newRoom = superRoom.getRoom(device.getRID());
        LOG.error(oldRoom.getRID() + " - " + newRoom.getRID());
        if (!existing.getName().equals(device.getName())) {
            LOG.info("Updating name of device " + cid + " from " + existing.getName() + " to " + device.getName());
            existing.setName(device.getName());
        }
        if (!existing.getRID().equals(device.getRID())) {
            LOG.info("Device " + cid + " transferring from room " + oldRoom.getRID() + " to "
                    + newRoom.getRID());
            oldRoom.transferDevice(cid, newRoom);
        }
        return new MicroserviceSuccessfulMessage();
    }
    
    @PatchMapping("/devices/{cid}/{prop_index}")
    public MicroserviceMessage updateDeviceProperty(@PathVariable String cid, @PathVariable String prop_index,
                                                    @RequestBody DeviceProperty property) {
        LOG.info("Updating " + cid + "." + prop_index + "...");
        return new MicroserviceSuccessfulMessage();
    }

    @PostMapping("/rooms/{rid}/devices/{cid}")
    public MicroserviceMessage addDevice(@PathVariable String rid, @PathVariable String cid,
                                         @RequestBody Device device) throws Exception {
        LOG.info("Adding device " + cid + " to room " + rid + "...");
        Room room = superRoom.getRoom(rid);
        room.addDeviceAndCreateInAdaptors(device);
        LOG.info("Device " + cid + " added to room " + rid);
        return new MicroserviceSuccessfulMessage();
    }

    @PostMapping("/rooms/{rid}")
    public MicroserviceMessage addRoomToSuperRoom(@PathVariable String rid, @RequestBody Room room) throws Exception {
        LOG.info("Adding room " + rid + "...");
        superRoom.addRoomAndCreateInAdaptors(room);
        LOG.info("Room " + rid + " added");
        return new MicroserviceSuccessfulMessage();
    }

    @PostMapping("/rooms/{parent_rid}/{new_rid}")
    public MicroserviceMessage addRoom(@PathVariable String parent_rid, @PathVariable String new_rid,
                                       @RequestBody Room room) throws Exception {
        LOG.info("Adding room " + new_rid + " to parent room " + parent_rid + "...");
        superRoom.getRoom(parent_rid).addRoomAndCreateInAdaptors(room);
        LOG.info("Room " + new_rid + " added to parent room " + parent_rid);
        return new MicroserviceSuccessfulMessage();
    }

    @DeleteMapping("/devices/{cid}")
    public MicroserviceMessage deleteDevice(@PathVariable String cid) throws Exception {
        LOG.info("Deleting device " + cid + "...");
        Device d = superRoom.getDevice(cid);
        if (d == null) {
            throw new NullPointerException("No device with CID " + cid + " exists");
        }
        d.getRoom().removeDeviceAndDeleteInAdaptors(cid);
        LOG.info("Device " + cid + " deleted");
        superRoom.printAllEntities();
        return new MicroserviceSuccessfulMessage();
    }
    
    @DeleteMapping("/rooms/{rid}")
    public MicroserviceMessage deleteRoom(@PathVariable String rid) throws Exception {
        LOG.info("Deleting room " + rid + "...");
        Room r = superRoom.getRoom(rid);
        if (r == null) {
            throw new NullPointerException("No room with RID " + rid + " exists");
        }
        r.getParentRoom().removeRoomAndDeleteInAdaptors(rid);
        LOG.info("Room " + rid + " deleted");
        superRoom.printAllEntities();
        return new MicroserviceSuccessfulMessage();
    }
    
    private String generateNewCID() {
        String newCID;
        do {
            newCID = RandomStringUtils.random(8, true, true);
        } while (superRoom.getDevice(newCID) != null);
        return newCID;
    }

//    @RequestMapping("internal/reload")
//    public boolean reload(@RequestParam(value = "cid", required = false) String cid,
//                          @RequestParam(value = "propindex", required = false) Integer prop_index) {
//        if (cid != null) {
//            if (prop_index != null) {
//                LOG.info("Reloading device " + cid + " property " + prop_index + " from DB...");
//                registry.reloadDevicePropertyFromDB(cid, prop_index);
//                Device d = registry.getDeviceObject(cid);
//                LOG.error(String.valueOf(d.getPropertyValue(prop_index)));
//                LOG.info("Device reloaded successfully");
//            }
//        }
//        return true;
//    }
//
//    private String receiveJeepMessage(String msgStr, AbstService service) {
//        LOG.debug("Message arrived: " + msgStr);
//        JeepMessage msg = new JeepMessage(msgStr);
//        try {
//            JeepMessage m = service.processMessage(msg);
//            return m.toString();
//        }
//        catch (MessageParameterCheckingException e) {
//            LOG.error("Unable to process message!", e);
//            JeepResponse error = new JeepResponse(msg, e.getMessage());
//            return error.toString();
//        }
//    }
}
