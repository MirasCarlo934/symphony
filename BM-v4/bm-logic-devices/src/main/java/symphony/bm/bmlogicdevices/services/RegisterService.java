package symphony.bm.bmlogicdevices.services;

import org.json.JSONException;
import org.json.JSONObject;
import symphony.bm.bmlogicdevices.jeep.JeepMessage;
import symphony.bm.bmlogicdevices.mongodb.MongoDBManager;
import symphony.bm.bmlogicdevices.rest.OutboundRestMicroserviceCommunicator;
import symphony.bm.bmlogicdevices.services.exceptions.SecondaryMessageParameterCheckingException;

public class RegisterService extends Service {
    private MongoDBManager mongo;
    private String devColl;

    public RegisterService(String logDomain, String serviceName, String messageServiceName,
                           OutboundRestMicroserviceCommunicator restCommunicator, MongoDBManager mongoDBManager,
                           String devicesMongoDBCollection) {
        super(logDomain, serviceName, messageServiceName, restCommunicator);
        this.mongo = mongoDBManager;
        this.devColl = devicesMongoDBCollection;
    }

    @Override
    protected void process(JeepMessage message) {

    }

    @Override
    protected boolean checkSecondaryMessageParameters(JeepMessage message) throws SecondaryMessageParameterCheckingException {
        // Check if message contains proper parameters
        try { message.getString("PID"); }
        catch (JSONException e) {
            String errorMsg = "PID parameter not found!";
            LOG.error(errorMsg);
            throw new SecondaryMessageParameterCheckingException(errorMsg);
        }
        try {
            Object room = message.get("room");
            if (room.getClass().equals(JSONObject.class)) { // for type 2 registration
                JSONObject roomJSON = (JSONObject) room;
                try { roomJSON.getString("ID"); }
                catch (JSONException e) {
                    String errorMsg = "\"roomID\" parameter not found!";
                    LOG.error(errorMsg);
                    throw new SecondaryMessageParameterCheckingException(errorMsg);
                }
                try { roomJSON.getString("name"); }
                catch (JSONException e) {
                    String errorMsg = "\"roomName\" parameter not found!";
                    LOG.error(errorMsg);
                    throw new SecondaryMessageParameterCheckingException(errorMsg);
                }
            }
        }
        catch (JSONException e) {
            String errorMsg = "\"room\" parameter not found!";
            LOG.error(errorMsg);
            throw new SecondaryMessageParameterCheckingException(errorMsg);
        }
        return false;
    }
}
