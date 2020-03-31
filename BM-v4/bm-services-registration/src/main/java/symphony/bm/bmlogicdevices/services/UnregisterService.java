package symphony.bm.bmlogicdevices.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import symphony.bm.bmlogicdevices.SymphonyRegistry;
import symphony.bm.bmlogicdevices.jeep.JeepMessage;
import symphony.bm.bmlogicdevices.jeep.JeepResponse;
import symphony.bm.bmlogicdevices.services.exceptions.MessageParameterCheckingException;

@Service
@Scope(value = "prototype", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class UnregisterService extends AbstService {
    private SymphonyRegistry registry;

    public UnregisterService(@Value("${log.logic}") String logDomain,
                             @Value("${services.unreg..name}") String serviceName,
                             @Value("${services.unreg.msn}") String messageServiceName,
                             SymphonyRegistry symphonyRegistry) {
        super(logDomain, serviceName, messageServiceName);
        this.registry = symphonyRegistry;
    }

    @Override
    protected JeepResponse process(JeepMessage message) {
        LOG.info("Unregistering device " + message.getCID() + " from Symphony network");
        registry.deleteDeviceObject(message.getCID());
        LOG.info("Device " + message.getCID() + " unregistered successfully!");
        return new JeepResponse(message);
    }

    @Override
    protected void checkSecondaryMessageParameters(JeepMessage message) throws MessageParameterCheckingException {
        if (!registry.containsDeviceObject(message.getCID()))
            throw secondaryMessageCheckingException("CID does not exist!");
    }
}
