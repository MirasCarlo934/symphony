package symphony.bm.cache.devices.rest;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import symphony.bm.cache.devices.entities.Device;
import symphony.bm.cache.devices.entities.Room;
import symphony.bm.cache.devices.entities.SuperRoom;
import symphony.bm.cache.devices.entities.deviceproperty.DeviceProperty;
import symphony.bm.cache.devices.entities.deviceproperty.DevicePropertyValueSnapshot;
import symphony.bm.cache.devices.entities.deviceproperty.DevicePropertyValueSnapshotRepository;
import symphony.bm.cache.devices.rest.messages.MicroserviceMessage;
import symphony.bm.cache.devices.rest.messages.MicroserviceSuccessfulMessage;

import java.util.HashMap;
import java.util.List;
import java.util.Vector;

@RestController
@RequestMapping("/devices")
@AllArgsConstructor
public class DevicesRestController {
    private static final Logger LOG = LoggerFactory.getLogger(DevicesRestController.class);

    private final SuperRoom superRoom;
    private final DevicePropertyValueSnapshotRepository valueSnapshotRepository;

    @GetMapping("/newcid")
    public String getNewDeviceCID() {
        LOG.info("Generating new device CID...");
        String cid = generateNewCID();
        LOG.info("New CID " + cid + " generated");
        return cid;
    }

    @GetMapping("/{cid}")
    public Device getDevice(@PathVariable String cid) {
        LOG.info("Getting device " + cid);
        Device d = superRoom.getDevice(cid);
        if (d == null) {
            LOG.warn("No device found!");
        }
        return d;
    }

    @GetMapping("/{cid}/properties/{prop_index}")
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

    @PostMapping("/{cid}")
    public MicroserviceMessage addDevice(@PathVariable String cid, @RequestBody Device device) throws Exception {
        String rid = device.getRID();
        LOG.info("Adding device " + cid + " to room " + rid + "...");
        Room room = superRoom.getRoom(rid);
        room.addDeviceAndCreateInAdaptors(device);
        LOG.info("Device " + cid + " added to room " + rid);
        return new MicroserviceSuccessfulMessage();
    }

    @PatchMapping("/{cid}")
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

    @PatchMapping("/{cid}/properties/{prop_index}")
    public MicroserviceMessage updateDeviceProperty(@PathVariable String cid, @PathVariable int prop_index,
                                                    @RequestBody DeviceProperty property) throws Exception {
        LOG.info("Updating " + cid + "." + prop_index + "...");
        Device device = superRoom.getDevice(cid);
        if (device == null) {
            throw new NullPointerException("No device with CID " + cid + " exists");
        }
        DeviceProperty existing = device.getProperty(prop_index);
        if (existing == null) {
            throw new NullPointerException("Property " + cid + "." + prop_index + " does not exist");
        }
        if (!existing.getName().equals(property.getName())) {
            LOG.info("Updating name of " + existing.getID() + " from " + existing.getName() + " to "
                    + property.getName());
            existing.setName(property.getName());
        }
        if (!existing.getValue().equals(property.getValue())) {
            LOG.info("Updating value of " + existing.getID() + " from " + existing.getValue() + " to "
                    + property.getValue());
            existing.setValue(property.getValue());
            LOG.info("Inserting value snapshot of " + existing.getID() + " in DB...");
            DevicePropertyValueSnapshot valueSnapshot = new DevicePropertyValueSnapshot(property);
            valueSnapshotRepository.save(valueSnapshot);
            LOG.info(existing.getID() + " value snapshot inserted in DB");
        }
        return new MicroserviceSuccessfulMessage();
    }

    @DeleteMapping("/{cid}")
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

    private String generateNewCID() {
        String newCID;
        do {
            newCID = RandomStringUtils.random(8, true, true);
        } while (superRoom.getDevice(newCID) != null);
        return newCID;
    }
}
