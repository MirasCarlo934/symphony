package symphony.bm.bmlogicdevices.services;

import com.mongodb.client.MongoCollection;
import org.json.JSONException;
import org.json.JSONObject;
import org.bson.Document;
import symphony.bm.bmlogicdevices.SymphonyEnvironment;
import symphony.bm.bmlogicdevices.entities.Device;
import symphony.bm.bmlogicdevices.entities.DeviceProperty;
import symphony.bm.bmlogicdevices.entities.Room;
import symphony.bm.bmlogicdevices.jeep.JeepMessage;
import symphony.bm.bmlogicdevices.mongodb.MongoDBManager;
import symphony.bm.bmlogicdevices.rest.OutboundRestMicroserviceCommunicator;
import symphony.bm.bmlogicdevices.services.exceptions.SecondaryMessageParameterCheckingException;

import java.util.List;
import java.util.Vector;

import static com.mongodb.client.model.Filters.eq;

public class RegisterService extends Service {
    private SymphonyEnvironment environment;
    private MongoDBManager mongo;
    private String devicesCollection;
    private String roomsCollection;
    private String productsCollection;

    public RegisterService(String logDomain, String serviceName, String messageServiceName,
                           OutboundRestMicroserviceCommunicator restCommunicator, SymphonyEnvironment environment,
                           MongoDBManager mongoDBManager, String devicesCollectionName, String roomsCollectionName,
                           String productsCollectionName) {
        super(logDomain, serviceName, messageServiceName, restCommunicator);
        this.environment = environment;
        this.mongo = mongoDBManager;
        this.devicesCollection = devicesCollectionName;
        this.roomsCollection = roomsCollectionName;
        this.productsCollection = productsCollectionName;
    }

    @Override
    protected void process(JeepMessage message) {
        String cid = message.getCID();
        MongoCollection<Document> devices = mongo.getCollection(devicesCollection);
        MongoCollection<Document> products = mongo.getCollection(productsCollection);
        Document product = products.find(eq("PID", message.getString("PID"))).first();
        Document device = devices.find(eq("CID", cid)).first();
        assert product != null;

        if (device == null) {
            LOG.info("Registering device " + cid + "...");
            Room roomObj;
            if (message.get("room").getClass().equals(String.class)) { // for type 1 registration
                roomObj = environment.getRoomObject(message.getString("room"));
            } else { // for type 2 registration
                JSONObject roomDoc = (JSONObject) message.get("room");
                roomObj = environment.createRoomObject(roomDoc.getString("RID"), roomDoc.getString("name"));
            }
            List<Document> productProps = product.getList("properties", Document.class);
            Vector<DeviceProperty> properties = new Vector<>();
            for (Document propDoc : productProps) {
                Document propType = (Document) propDoc.get("type");
                properties.add(new DeviceProperty(propDoc.getInteger("index"),
                        propDoc.getString("name"), propType.getString("ID"),
                        propType.getInteger("minValue"), propType.getInteger("maxValue")));
            }
            environment.createDeviceObject(message.getCID(), message.getString("PID"),
                    message.getString("name"), roomObj, properties.toArray(new DeviceProperty[0]));
            LOG.info("Device registered successfully!");
        } else {
            LOG.info("Updating device " + cid + "...");
        }
    }

    @Override
    protected boolean checkSecondaryMessageParameters(JeepMessage message) throws SecondaryMessageParameterCheckingException {
        // Check if message contains proper parameters
        try { message.getString("name"); }
        catch (JSONException e) {
            throw secondaryMessageCheckingException("\"name\" parameter not found!");
        }
        try {
            String pid = message.getString("PID");
            MongoCollection<Document> products = mongo.getCollection(productsCollection);
            Document result = products.find(eq("PID", pid)).first();
            if (result == null) {
                throw secondaryMessageCheckingException("Invalid PID!");
            }
        } catch (JSONException e) {
            throw secondaryMessageCheckingException("PID parameter not found!");
        }
        try {
            Object room = message.get("room");
            if (room.getClass().equals(String.class)) { // for type 1 registration
                MongoCollection<Document> rooms = mongo.getCollection(roomsCollection);
                Document result = rooms.find(eq("room", room)).first();
                if (result == null) {
                    throw secondaryMessageCheckingException("Invalid roomID!");
                }
            } else { // for type 2 registration
                JSONObject roomJSON = (JSONObject) room;
                try { roomJSON.getString("RID"); }
                catch (JSONException e) {
                    throw secondaryMessageCheckingException("\"roomID\" parameter not found!");
                }
                try { roomJSON.getString("name"); }
                catch (JSONException e) {
                    throw secondaryMessageCheckingException("\"roomName\" parameter not found!");
                }
            }
        } catch (JSONException e) {
            String errorMsg = "\"room\" parameter not found!";
            LOG.error(errorMsg);
            throw secondaryMessageCheckingException("\"roomID\" parameter not found!");
        }
        return false;
    }

    private SecondaryMessageParameterCheckingException secondaryMessageCheckingException(String errorMsg) {
        LOG.error(errorMsg);
        return new SecondaryMessageParameterCheckingException(errorMsg);
    }
}
