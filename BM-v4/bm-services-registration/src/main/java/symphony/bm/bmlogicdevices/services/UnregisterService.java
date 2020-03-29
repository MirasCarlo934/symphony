package symphony.bm.bmlogicdevices.services;

import symphony.bm.bmlogicdevices.SymphonyEnvironment;
import symphony.bm.bmlogicdevices.jeep.JeepMessage;
import symphony.bm.bmlogicdevices.jeep.JeepResponse;
import symphony.bm.bmlogicdevices.rest.OutboundRestMicroserviceCommunicator;
import symphony.bm.bmlogicdevices.services.exceptions.MessageParameterCheckingException;

public class UnregisterService extends Service {
    private SymphonyEnvironment env;

    public UnregisterService(String logDomain, String serviceName, String messageServiceName,
                             OutboundRestMicroserviceCommunicator restCommunicator,
                             SymphonyEnvironment symphonyEnvironment) {
        super(logDomain, serviceName, messageServiceName, restCommunicator);
        this.env = symphonyEnvironment;
    }

    @Override
    protected JeepResponse process(JeepMessage message) {
        LOG.info("Unregistering device " + message.getCID() + " from Symphony network");
        env.deleteDeviceObject(message.getCID());
        LOG.info("Device " + message.getCID() + " unregistered successfully!");
        return new JeepResponse(message);
    }

    @Override
    protected void checkSecondaryMessageParameters(JeepMessage message) throws MessageParameterCheckingException {
        if (!env.containsDeviceObject(message.getCID()))
            throw secondaryMessageCheckingException("CID does not exist!");
    }
}
