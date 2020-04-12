package symphony.bm.cache.devices.entities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;
import symphony.bm.cache.devices.adaptors.Adaptor;
import symphony.bm.cache.devices.adaptors.AdaptorManager;

import java.util.*;

@Component
public class SuperRoom extends Room {
    private static final Logger LOG = LoggerFactory.getLogger(SuperRoom.class);
    private MongoOperations mongo;

    public SuperRoom(AdaptorManager adaptorManager, MongoTemplate mongoTemplate) {
        super("_super", "Super Room");
        this.mongo = mongoTemplate;

        reloadAllEntities();
        setAdaptorManager(adaptorManager);
        setSelfToChildren();
    }
    
    public void reloadAllEntities() {
        LOG.info("Reloading all entities in super room from DB...");
        List<Room> roomsList = mongo.findAll(Room.class);
    
        for (Room room : roomsList) {
            room.setParentRoom(this);
            rooms.add(room);
        }

        LOG.info(countAllDevices() + " devices and " + countAllRooms() + " rooms reloaded from DB");
    }
}
