package symphony.bm.cache.devices.rest;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import symphony.bm.cache.devices.entities.Device;
import symphony.bm.cache.devices.entities.Room;
import symphony.bm.cache.devices.entities.SuperRoom;
import symphony.bm.generics.messages.MicroserviceMessage;
import symphony.bm.generics.messages.MicroserviceSuccessfulMessage;
import symphony.bm.generics.exceptions.MicroserviceProcessingException;

@RestController()
@RequestMapping("/rooms")
@AllArgsConstructor
public class RoomsRestController {
    private static final Logger LOG = LoggerFactory.getLogger(RoomsRestController.class);

    private final SuperRoom superRoom;

    @GetMapping("/rooms/{rid}")
    public Room getRoom(@PathVariable String rid) {
        LOG.info("Getting room " + rid);
        Room r = superRoom.getRoom(rid);
        if (r == null) {
            LOG.warn("No room found!");
        }
        return r;
    }

    @PostMapping("/{rid}/devices/{cid}")
    public MicroserviceMessage addDevice(@PathVariable String rid, @PathVariable String cid,
                                         @RequestBody Device device) throws MicroserviceProcessingException {
        LOG.info("Adding device " + cid + " to room " + rid + "...");
        Room room = superRoom.getRoom(rid);
        try {
            room.addDeviceAndCreateInAdaptors(device);
        } catch (Exception e) {
            throw new MicroserviceProcessingException("Add device failed", e);
        }
        LOG.info("Device " + cid + " added to room " + rid);
        return new MicroserviceSuccessfulMessage("Device added");
    }

    @PostMapping("/{rid}")
    public MicroserviceMessage addRoomToSuperRoom(@PathVariable String rid, @RequestBody Room room)
            throws MicroserviceProcessingException {
        LOG.info("Adding room " + rid + "...");
        try {
            superRoom.addRoomAndCreateInAdaptors(room);
        } catch (Exception e) {
            throw new MicroserviceProcessingException("Add room failed", e);
        }
        LOG.info("Room " + rid + " added");
        return new MicroserviceSuccessfulMessage("Room added");
    }

    @PostMapping("/{parent_rid}/{new_rid}")
    public MicroserviceMessage addRoom(@PathVariable String parent_rid, @PathVariable String new_rid,
                                       @RequestBody Room room) throws MicroserviceProcessingException {
        LOG.info("Adding room " + new_rid + " to parent room " + parent_rid + "...");
        try {
            superRoom.getRoom(parent_rid).addRoomAndCreateInAdaptors(room);
        } catch (Exception e) {
            throw new MicroserviceProcessingException("Add room failed", e);
        }
        LOG.info("Room " + new_rid + " added to parent room " + parent_rid);
        return new MicroserviceSuccessfulMessage("Room added");
    }

    @DeleteMapping("/{rid}")
    public MicroserviceMessage deleteRoom(@PathVariable String rid) throws MicroserviceProcessingException {
        LOG.info("Deleting room " + rid + "...");
        Room r = superRoom.getRoom(rid);
        if (r == null) {
            throw new MicroserviceProcessingException("No room with RID " + rid + " exists");
        }
        try {
            r.getParentRoom().removeRoomAndDeleteInAdaptors(rid);
        } catch (Exception e) {
            throw new MicroserviceProcessingException("Delete room failed", e);
        }
        LOG.info("Room " + rid + " deleted");
        superRoom.printAllEntities();
        return new MicroserviceSuccessfulMessage("Room deleted");
    }
}
