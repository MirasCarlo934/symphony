package symphony.bm.bmservicespoop.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import symphony.bm.bmservicespoop.jeep.JeepMessage;
import symphony.bm.bmservicespoop.jeep.JeepResponse;
import symphony.bm.bmservicespoop.services.exceptions.MessageParameterCheckingException;

public abstract class AbstService {
    protected Logger LOG;
    protected String logDomain;
    protected String serviceName;
    protected String messageServiceName;

    public AbstService(String logDomain, String serviceName, String messageServiceName) {
        LOG = LoggerFactory.getLogger(logDomain + "." + serviceName);
        this.logDomain = logDomain;
        this.serviceName = serviceName;
        this.messageServiceName = messageServiceName;
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
