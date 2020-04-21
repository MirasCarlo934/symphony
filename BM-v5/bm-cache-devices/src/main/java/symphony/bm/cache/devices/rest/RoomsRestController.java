package symphony.bm.cache.devices.rest;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import symphony.bm.cache.devices.entities.Device;
import symphony.bm.cache.devices.entities.Room;
import symphony.bm.cache.devices.entities.SuperRoom;
import symphony.bm.cache.devices.rest.messages.MicroserviceMessage;
import symphony.bm.cache.devices.rest.messages.MicroserviceSuccessfulMessage;

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
                                         @RequestBody Device device) throws Exception {
        LOG.info("Adding device " + cid + " to room " + rid + "...");
        Room room = superRoom.getRoom(rid);
        room.addDeviceAndCreateInAdaptors(device);
        LOG.info("Device " + cid + " added to room " + rid);
        return new MicroserviceSuccessfulMessage();
    }

    @PostMapping("/{rid}")
    public MicroserviceMessage addRoomToSuperRoom(@PathVariable String rid, @RequestBody Room room) throws Exception {
        LOG.info("Adding room " + rid + "...");
        superRoom.addRoomAndCreateInAdaptors(room);
        LOG.info("Room " + rid + " added");
        return new MicroserviceSuccessfulMessage();
    }

    @PostMapping("/{parent_rid}/{new_rid}")
    public MicroserviceMessage addRoom(@PathVariable String parent_rid, @PathVariable String new_rid,
                                       @RequestBody Room room) throws Exception {
        LOG.info("Adding room " + new_rid + " to parent room " + parent_rid + "...");
        superRoom.getRoom(parent_rid).addRoomAndCreateInAdaptors(room);
        LOG.info("Room " + new_rid + " added to parent room " + parent_rid);
        return new MicroserviceSuccessfulMessage();
    }

    @DeleteMapping("/{rid}")
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
}
