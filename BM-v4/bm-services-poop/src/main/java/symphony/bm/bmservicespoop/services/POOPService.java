package symphony.bm.bmservicespoop.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import symphony.bm.bmservicespoop.DevicePropertyRegistry;
import symphony.bm.bmservicespoop.cir.CIRManager;
import symphony.bm.bmservicespoop.cir.rule.Rule;
import symphony.bm.bmservicespoop.entities.DeviceProperty;
import symphony.bm.bmservicespoop.jeep.JeepMessage;
import symphony.bm.bmservicespoop.jeep.JeepResponse;
import symphony.bm.bmservicespoop.services.exceptions.MessageParameterCheckingException;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Vector;

@Service
@Scope(value = "prototype", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class POOPService extends AbstService {
    private CIRManager cirm;
    private DevicePropertyRegistry dpr;
    private Vector<JeepResponse> responses = new Vector<>();
    private JeepMessage message;

    public POOPService(@Value("${log.poop}") String logDomain, @Value("${services.poop.name}") String serviceName,
                       @Value("${services.poop.msn}") String messageServiceName,
                       CIRManager cirManager, DevicePropertyRegistry devicePropertyRegistry) {
        super(logDomain, serviceName, messageServiceName);
        this.cirm = cirManager;
        this.dpr = devicePropertyRegistry;
    }

    @Override
    protected List<JeepResponse> process(JeepMessage message) {
        this.message = message;
        String cid = message.getCID();
        int propIndex = message.getInt("prop-index");
        int propValue = message.getInt("prop-value");
        changeDevicePropertyValue(cid, propIndex, propValue);
        List<Rule> rules = cirm.getRulesTriggered(cid, propIndex);
        executeRules(rules);

        return responses;
    }

    private void executeRules(List<Rule> rules) {
        for (Rule rule : rules) {
            LOG.debug("Checking if rule " + rule.getID() + "(" + rule.getName() + ") is triggered");

            if (rule.isTriggered(dpr.getAllDevicePropertyValues())) {
                LOG.info("Rule " + rule.getID() + "(" + rule.getName() + ") triggered!");
                HashMap<String, HashMap<Integer, Integer>> actions = rule.getActionBlock();
                for (String cid : actions.keySet()) {
                    for (int index : actions.get(cid).keySet()) {
                        int value = actions.get(cid).get(index);
                        changeDevicePropertyValue(cid, index, value);
                        if (rule.isCascading()) {
                            List<Rule> cascadedRules = cirm.getRulesTriggered(cid, index);
                            executeRules(cascadedRules);
                        }
                    }
                }
            }
        }
    }

    private void changeDevicePropertyValue(String cid, int propIndex, int propValue) {
        DeviceProperty prop = dpr.getDeviceProperty(cid, propIndex);
        LOG.info("Changing value of " + prop.getID() + " to " + propValue + "...");
        prop.setValue(propValue);
        JeepResponse res = new JeepResponse(message);
        res.put("CID", cid);
        res.put("prop-index", propIndex);
        res.put("prop-value", propValue);
        responses.add(res);
        LOG.info(prop.getID() + " successfully changed value to " + propValue);
    }

    @Override
    protected void checkSecondaryMessageParameters(JeepMessage message) throws MessageParameterCheckingException {
        if (!message.has("prop-index")) {
            throw secondaryMessageCheckingException("\"prop-index\" parameter not found");
        }
        if (!message.has("prop-value")) {
            throw secondaryMessageCheckingException("\"prop-value\" parameter not found");
        }

        String cid = message.getCID();
        int index = message.getInt("prop-index");
        int value = message.getInt("prop-value");
        if (!dpr.containsDeviceProperty(cid, index)) {
            throw secondaryMessageCheckingException("prop-index " + index + " does not exist in device " + cid);
        }

        DeviceProperty prop = dpr.getDeviceProperty(cid, index);
        if (value < prop.getMinValue() || value > prop.getMaxValue()) {
            throw secondaryMessageCheckingException("prop-value " + value + " is invalid. [min:" + prop.getMinValue()
                    + ", max:" + prop.getMaxValue() + "]");
        }
    }
}
