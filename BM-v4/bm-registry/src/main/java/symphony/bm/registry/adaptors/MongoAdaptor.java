package symphony.bm.registry.adaptors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoOperations;
import symphony.bm.registry.entities.Device;
import symphony.bm.registry.entities.DeviceProperty;
import symphony.bm.registry.entities.Room;

import java.util.HashMap;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;
import static org.springframework.data.mongodb.core.query.Update.update;

public class MongoAdaptor implements Adaptor {
    private static final Logger LOG = LoggerFactory.getLogger(MongoAdaptor.class);
    private MongoOperations mongo;
    
    public MongoAdaptor(MongoOperations mongo) {
        this.mongo = mongo;
    }
    
    @Override
    public void deviceCreated(Device device) {
    
    }
    
    @Override
    public void deviceDeleted(Device device) {
    
    }
    
    @Override
    public void deviceUpdated(Device device) {
    
    }
    
    @Override
    public void roomCreated(Room room) {
    
    }
    
    @Override
    public void propertyUpdated(DeviceProperty property) {
        LOG.info("Updating property " + property.getDevice().getCID() + "." + property.getIndex() + " in MongoDB...");
        mongo.updateFirst(query(where("properties")), update(String.valueOf(property.getIndex()), property),
                HashMap.class);
        LOG.info("Property " + property.getDevice().getCID() + "." + property.getIndex() + " updated in MongoDB");
    }
}
