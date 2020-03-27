package symphony.bm.bmlogicdevices.services;

import com.mongodb.client.MongoCollection;
import org.json.JSONArray;
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
    private SymphonyEnvironment env;
    private MongoDBManager mongo;
    private String productsCollection;

    public RegisterService(String logDomain, String serviceName, String messageServiceName,
                           OutboundRestMicroserviceCommunicator restCommunicator, SymphonyEnvironment symphonyEnvironment,
                           MongoDBManager mongoDBManager, String productsCollectionName) {
        super(logDomain, serviceName, messageServiceName, restCommunicator);
        this.env = symphonyEnvironment;
        this.mongo = mongoDBManager;
        this.productsCollection = productsCollectionName;
    }

    @Override
    protected void process(JeepMessage message) {
        String cid = message.getCID();
        MongoCollection<Document> products = mongo.getCollection(productsCollection);
        Device device = env.getDeviceObject(cid);

        if (device == null) {
            LOG.info("Registering device " + cid + " to Symphony network...");
            String pid;
            Vector<DeviceProperty> properties = new Vector<>();
            Room roomObj;
            if (message.get("room").getClass().equals(String.class)) { // for RID registration
                roomObj = env.getRoomObject(message.getString("room"));
            } else { // for new room registration
                JSONObject roomDoc = (JSONObject) message.get("room");
                roomObj = env.getRoomObject(roomDoc.getString("RID"));
                if (roomObj == null) {
                    roomObj = env.createRoomObject(roomDoc.getString("RID"),
                            roomDoc.getString("name"));
                }
            }

            if (message.get("product").getClass().equals(String.class)) { // for PID registration
                Document product = products.find(eq("PID", message.getString("product"))).first();
                assert product != null;
                List<Document> productProps = product.getList("properties", Document.class);
                pid = message.getString("product");
                for (Document propDoc : productProps) {
                    properties.add(new DeviceProperty(propDoc.getInteger("index"),
                            propDoc.getString("name"), propDoc.getString("type"),
                            propDoc.getInteger("minValue"), propDoc.getInteger("maxValue")));
                }
            } else { // for with-product registration
                JSONObject prodJSON = message.getJSONObject("product");
                JSONArray propsJSON = prodJSON.getJSONArray("properties");
                pid = prodJSON.getString("PID");
                for (Object p : propsJSON) {
                    JSONObject propJSON = (JSONObject) p;
                    String name = propJSON.getString("name");
                    int index = propJSON.getInt("index");
                    String type = propJSON.getString("ID");
                    int minVal = propJSON.getInt("minValue");
                    int maxVal = propJSON.getInt("maxValue");
                    properties.add(new DeviceProperty(index, name, type, minVal, maxVal));
                }
            }
            env.createDeviceObject(message.getCID(), pid,
                    message.getString("name"), roomObj, properties);
            LOG.info("Device " + cid + " registered successfully!");
        } else {
            LOG.info("Updating device " + cid + "...");
        }
    }

    @Override
    protected void checkSecondaryMessageParameters(JeepMessage message) throws SecondaryMessageParameterCheckingException {
        // Check if message contains proper parameters
        if (!message.has("name"))
            throw secondaryMessageCheckingException("\"name\" parameter not found!");
        try {
            Object product = message.get("product");
            if (product.getClass().equals(String.class)) { // for PID registration
                MongoCollection<Document> products = mongo.getCollection(productsCollection);
                Document result = products.find(eq("PID", product)).first();
                if (result == null) {
                    throw secondaryMessageCheckingException("Invalid product!");
                }
            } else { // for with-product registration
                JSONObject productJSON = (JSONObject) product;
                if (!productJSON.has("PID"))
                    throw secondaryMessageCheckingException("product.PID does not exist!");
                if (!productJSON.has("properties"))
                    throw secondaryMessageCheckingException("product.properties does not exist!");
                JSONArray properties = productJSON.getJSONArray("properties");
                Vector<Integer> indices = new Vector<>();
                int maxIndex = 0;
                for (Object p : properties) {
                    JSONObject prop = (JSONObject) p;
                    if (!prop.has("name"))
                        throw secondaryMessageCheckingException("a declared property has no specified name!");
                    if (!prop.has("type"))
                        throw secondaryMessageCheckingException("a declared property has no specified type!");
                    if (!prop.has("index"))
                        throw secondaryMessageCheckingException("a declared property has no specified index!");
                    if (!prop.has("minValue"))
                        throw secondaryMessageCheckingException("a declared property has no specified minValue!");
                    if (!prop.has("maxValue"))
                        throw secondaryMessageCheckingException("a declared property has no specified typeID!");
                    int index = prop.getInt("index");
                    if (indices.contains(index))
                        throw secondaryMessageCheckingException("multiple properties declared for the same index!");
                    if (index < 0)
                        throw secondaryMessageCheckingException("property indices must start at 0!");
                    else {
                        indices.add(index);
                        if (index > maxIndex) maxIndex = index;
                    }
                }
                if (!indices.contains(0))
                    throw secondaryMessageCheckingException("property indices must start at 0!");
                if (indices.size()-1 != maxIndex)
                    throw secondaryMessageCheckingException("property indices must be continuous!");
            }
        } catch (JSONException e) {
            throw secondaryMessageCheckingException("\"product\" parameter not found!");
        }
        try {
            Object room = message.get("room");
            if (room.getClass().equals(String.class)) { // for RID registration
                Room r = env.getRoomObject((String) room);
                if (r == null) {
                    throw secondaryMessageCheckingException("Nonexistent roomID!");
                }
            } else { // for new room registration
                JSONObject roomJSON = (JSONObject) room;
                if (!roomJSON.has("RID"))
                    throw secondaryMessageCheckingException("\"roomID\" parameter not found!");
                if (!roomJSON.has("name"))
                    throw secondaryMessageCheckingException("\"roomName\" parameter not found!");
            }
        } catch (JSONException e) {
            String errorMsg = "\"room\" parameter not found!";
            LOG.error(errorMsg);
            throw secondaryMessageCheckingException("\"roomID\" parameter not found!");
        }
    }
}
