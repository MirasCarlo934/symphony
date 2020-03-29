package symphony.bm.bmlogicdevices.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import symphony.bm.bmlogicdevices.jeep.JeepMessage;
import symphony.bm.bmlogicdevices.jeep.JeepResponse;
import symphony.bm.bmlogicdevices.rest.OutboundRestMicroserviceCommunicator;
import symphony.bm.bmlogicdevices.services.exceptions.MessageParameterCheckingException;

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

    public JeepResponse processMessage(JeepMessage message) throws MessageParameterCheckingException {
        LOG.debug("Checking secondary message parameters for MRN: " + message.getMRN());
        checkSecondaryMessageParameters(message);
        LOG.debug("Secondary message parameters checked! Processing...");
        return process(message);
    }

    protected MessageParameterCheckingException secondaryMessageCheckingException(String errorMsg) {
        LOG.error(errorMsg);
        return new MessageParameterCheckingException(errorMsg);
    }

    protected MessageParameterCheckingException secondaryMessageCheckingException(String errorMsg,
                                                                                           Exception e) {
        LOG.error(errorMsg);
        return new MessageParameterCheckingException(errorMsg, e);
    }

    protected abstract JeepResponse process(JeepMessage message);

    protected abstract void checkSecondaryMessageParameters(JeepMessage message)
            throws MessageParameterCheckingException;

}
