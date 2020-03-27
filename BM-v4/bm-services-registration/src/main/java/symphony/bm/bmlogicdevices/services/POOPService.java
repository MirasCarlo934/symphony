package symphony.bm.bmlogicdevices.services;

import symphony.bm.bmlogicdevices.SymphonyEnvironment;
import symphony.bm.bmlogicdevices.entities.Device;
import symphony.bm.bmlogicdevices.jeep.JeepMessage;
import symphony.bm.bmlogicdevices.mongodb.MongoDBManager;
import symphony.bm.bmlogicdevices.rest.OutboundRestMicroserviceCommunicator;
import symphony.bm.bmlogicdevices.services.exceptions.SecondaryMessageParameterCheckingException;

public class POOPService extends Service {
    private MongoDBManager mongo;
    private SymphonyEnvironment env;

    public POOPService(String logDomain, String serviceName, String messageServiceName,
                       OutboundRestMicroserviceCommunicator restCommunicator, MongoDBManager mongoDBManager,
                       SymphonyEnvironment symphonyEnvironment) {
        super(logDomain, serviceName, messageServiceName, restCommunicator);
        this.mongo = mongoDBManager;
        this.env = symphonyEnvironment;
    }

    @Override
    protected void process(JeepMessage message) {
        int index = message.getInt("prop-index");
        int value = message.getInt("prop-value");
        Device device = env.getDeviceObject(message.getCID());

        LOG.info("Updating value of device property " + device.getCID() + "->" + index + " from "
                + device.getPropertyValue(index) + " to " + value);
        device.setPropertyValue(index, value);
        LOG.info("Device property " + device.getCID() + "->" + index + " updated from " + device.getPropertyValue(index)
                + " to " + value);
    }

    @Override
    protected void checkSecondaryMessageParameters(JeepMessage message) throws SecondaryMessageParameterCheckingException {
        Device device = env.getDeviceObject(message.getCID());
        if (!message.has("prop-index"))
            throw secondaryMessageCheckingException("\"prop-index\" parameter not found!");
        if (!message.has("prop-value"))
            throw secondaryMessageCheckingException("\"prop-value\" parameter not found!");

        int index = message.getInt("prop-index");
        int value = message.getInt("prop-value");
        if (!device.hasPropertyIndex(index))
            throw secondaryMessageCheckingException("device " + device.getCID() + " does not contain property with " +
                    "index " + index);
        if (!device.checkValueValidity(index, value))
            throw secondaryMessageCheckingException("value " + value + " is outside the allowable value range of the " +
                    "property " + index + " of device " + device.getCID());
    }
}
