package symphony.bm.bmlogicdevices.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import symphony.bm.bmlogicdevices.jeep.JeepMessage;
import symphony.bm.bmlogicdevices.rest.OutboundRestMicroserviceCommunicator;
import symphony.bm.bmlogicdevices.services.exceptions.SecondaryMessageParameterCheckingException;

public abstract class Service {
    protected Logger LOG;
    protected String logDomain;
    protected String serviceName;
    protected String messageServiceName;
    protected OutboundRestMicroserviceCommunicator restCommunicator;

    public Service(String logDomain, String serviceName, String messageServiceName,
                   OutboundRestMicroserviceCommunicator restCommunicator) {
        LOG = LoggerFactory.getLogger(logDomain + "." + serviceName);
        this.logDomain = logDomain;
        this.serviceName = serviceName;
        this.messageServiceName = messageServiceName;
        this.restCommunicator = restCommunicator;
    }

    public void processMessage(JeepMessage message) throws SecondaryMessageParameterCheckingException {
        LOG.debug("Checking secondary message parameters for MRN: " + message.getMRN());
        checkSecondaryMessageParameters(message);
        LOG.debug("Secondary message parameters checked! Processing...");
        process(message);
    }

    protected abstract void process(JeepMessage message);

    protected abstract boolean checkSecondaryMessageParameters(JeepMessage message)
            throws SecondaryMessageParameterCheckingException;
}
