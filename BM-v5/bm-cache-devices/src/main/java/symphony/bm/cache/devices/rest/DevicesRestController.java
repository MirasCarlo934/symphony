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
import symphony.bm.generics.messages.MicroserviceMessage;
import symphony.bm.generics.messages.MicroserviceSuccessfulMessage;
import symphony.bm.generics.exceptions.MicroserviceProcessingException;

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
    public MicroserviceMessage addDevice(@PathVariable String cid, @RequestBody Device device)
            throws MicroserviceProcessingException {
        String rid = device.getRID();
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

    @PatchMapping("/{cid}")
    public MicroserviceMessage updateDevice(@PathVariable String cid, @RequestBody Device device)
            throws MicroserviceProcessingException {
        LOG.info("Updating device " + cid + "...");
        Device existing = superRoom.getDevice(cid);
        Room oldRoom = superRoom.getRoom(existing.getRID());
        Room newRoom = superRoom.getRoom(device.getRID());
        try {
            if (!existing.getName().equals(device.getName())) {
                LOG.info("Updating name of device " + cid + " from " + existing.getName() + " to " + device.getName());
                existing.setName(device.getName());
            }
            if (!existing.getRID().equals(device.getRID())) {
                LOG.info("Device " + cid + " transferring from room " + oldRoom.getRID() + " to "
                        + newRoom.getRID());
                oldRoom.transferDevice(cid, newRoom);
            }
        } catch (Exception e) {
            throw new MicroserviceProcessingException("Update device failed", e);
        }
        return new MicroserviceSuccessfulMessage("Device updated");
    }

    @PatchMapping("/{cid}/properties/{prop_index}")
    public MicroserviceMessage updateDeviceProperty(@PathVariable String cid, @PathVariable int prop_index,
                                                    @RequestBody DeviceProperty property)
            throws MicroserviceProcessingException {
        LOG.info("Updating " + cid + "." + prop_index + "...");
        Device device = superRoom.getDevice(cid);
        if (device == null) {
            throw new MicroserviceProcessingException("No device with CID " + cid + " exists");
        }
        DeviceProperty existing = device.getProperty(prop_index);
        if (existing == null) {
            throw new MicroserviceProcessingException("Property " + cid + "." + prop_index + " does not exist");
        }
        try {
            boolean changed = false;
            if (!existing.getName().equals(property.getName())) {
                LOG.info("Updating name of " + existing.getID() + " from " + existing.getName() + " to "
                        + property.getName());
                existing.setName(property.getName());
                changed = true;
            }
            if (!existing.getValue().equals(property.getValue())) {
                LOG.info("Updating value of " + existing.getID() + " from " + existing.getValue() + " to "
                        + property.getValue());
                existing.setValue(property.getValue());
                LOG.info("Inserting value snapshot of " + existing.getID() + " in DB...");
                DevicePropertyValueSnapshot valueSnapshot = new DevicePropertyValueSnapshot(property);
                valueSnapshotRepository.save(valueSnapshot);
                LOG.info(existing.getID() + " value snapshot inserted in DB");
                changed = true;
            }
            if (!changed) {
                LOG.info("Nothing new to update");
            }
        } catch (Exception e) {
            throw new MicroserviceProcessingException("Update device property failed", e);
        }
        return new MicroserviceSuccessfulMessage("Device property updated");
    }

    @DeleteMapping("/{cid}")
    public MicroserviceMessage deleteDevice(@PathVariable String cid)
            throws MicroserviceProcessingException {
        LOG.info("Deleting device " + cid + "...");
        Device d = superRoom.getDevice(cid);
        if (d == null) {
            throw new MicroserviceProcessingException("No device with CID " + cid + " exists");
        }
        try {
            d.getRoom().removeDeviceAndDeleteInAdaptors(cid);
        } catch (Exception e) {
            throw new MicroserviceProcessingException("Delete device failed", e);
        }
        LOG.info("Device " + cid + " deleted");
        superRoom.printAllEntities();
        return new MicroserviceSuccessfulMessage("Device deleted");
    }

    private String generateNewCID() {
        String newCID;
        do {
            newCID = RandomStringUtils.random(8, true, true);
        } while (superRoom.getDevice(newCID) != null);
        return newCID;
    }
}
