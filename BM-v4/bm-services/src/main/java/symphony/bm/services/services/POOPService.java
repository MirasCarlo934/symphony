//package symphony.bm.bmservicespoop.services;
//
//import org.apache.http.client.HttpClient;
//import org.apache.http.client.methods.HttpPost;
//import org.apache.http.entity.StringEntity;
//import org.apache.http.impl.client.HttpClientBuilder;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Scope;
//import org.springframework.context.annotation.ScopedProxyMode;
//import symphony.bm.bmservicespoop.entities.DevicePropertyRegistry;
//import symphony.bm.bmservicespoop.cir.CIRManager;
//import symphony.bm.bmservicespoop.cir.rule.Rule;
//import symphony.bm.bmservicespoop.entities.DeviceProperty;
//import symphony.bm.bmservicespoop.jeep.JeepMessage;
//import symphony.bm.bmservicespoop.jeep.JeepResponse;
//import symphony.bm.bmservicespoop.services.exceptions.MessageParameterCheckingException;
//
//import org.springframework.stereotype.Service;
//
//import java.io.IOException;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Vector;
//import java.util.concurrent.Executors;
//import java.util.concurrent.ThreadPoolExecutor;
//
//@Service
//@Scope(value = "prototype", proxyMode = ScopedProxyMode.TARGET_CLASS)
//public class POOPService extends AbstService {
//    private CIRManager cirm;
//    private DevicePropertyRegistry dpr;
//    private Vector<JeepMessage> responses = new Vector<>();
//    private HttpClient httpClient = HttpClientBuilder.create().build();
//    private String bmServerURL;
//    private String bmRegistryPort;
//    private ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
//
//    private JeepMessage message;
//
//    public POOPService(@Value("${log.poop}") String logDomain, @Value("${services.poop.name}") String serviceName,
//                       @Value("${services.poop.msn}") String messageServiceName, @Value("${http.url.bm}") String bmServerURL,
//                       @Value("${services.register.url.port}") String bmRegistryPort,
//                       CIRManager cirManager, DevicePropertyRegistry devicePropertyRegistry) {
//        super(logDomain, serviceName, messageServiceName);
//        this.cirm = cirManager;
//        this.dpr = devicePropertyRegistry;
//        this.bmServerURL = bmServerURL;
//        this.bmRegistryPort = bmRegistryPort;
//    }
//
//    @Override
//    protected List<JeepMessage> process(JeepMessage message) {
//        this.message = message;
//        String cid = message.getCID();
//        int propIndex = message.getInt("prop-index");
//        int propValue = message.getInt("prop-value");
//        changeDevicePropertyValue(cid, propIndex, propValue);
//        List<Rule> rules = cirm.getRulesTriggered(cid, propIndex);
//        executeRules(rules);
//
//        return responses;
//    }
//
//    private void executeRules(List<Rule> rules) {
//        for (Rule rule : rules) {
//            LOG.debug("Checking if rule " + rule.getID() + "(" + rule.getName() + ") is triggered");
//
//            if (rule.isTriggered(dpr.getAllDevicePropertyValues())) {
//                LOG.info("Rule " + rule.getID() + "(" + rule.getName() + ") triggered!");
//                HashMap<String, HashMap<Integer, Integer>> actions = rule.getActionBlock();
//                for (String cid : actions.keySet()) {
//                    for (int index : actions.get(cid).keySet()) {
//                        int value = actions.get(cid).get(index);
//                        changeDevicePropertyValue(cid, index, value);
//                        if (rule.isCascading()) {
//                            List<Rule> cascadedRules = cirm.getRulesTriggered(cid, index);
//                            executeRules(cascadedRules);
//                        }
//                    }
//                }
//            }
//        }
//    }
//
//    private void changeDevicePropertyValue(String cid, int propIndex, int propValue) {
//        DeviceProperty prop = dpr.getDeviceProperty(cid, propIndex);
//        LOG.info("Changing value of " + prop.getID() + " to " + propValue + "...");
//        prop.setValue(propValue);
//        JeepMessage msg = new JeepMessage(message.toString());
//        if (cid.equals(message.getCID())) { // signifies that a response must be returned
//            msg = new JeepResponse(message);
//        } else { // request will be returned
//            msg.put("CID", cid);
//            msg.put("prop-index", propIndex);
//            msg.put("prop-value", propValue);
//        }
//        responses.add(msg);
//        executor.submit(new RegistryReloadNotification(cid, propIndex));
//        LOG.info(prop.getID() + " successfully changed value to " + propValue);
//    }
//
//    @Override
//    protected void checkSecondaryMessageParameters(JeepMessage message) throws MessageParameterCheckingException {
//        if (!message.has("prop-index")) {
//            throw secondaryMessageCheckingException("\"prop-index\" parameter not found");
//        }
//        if (!message.has("prop-value")) {
//            throw secondaryMessageCheckingException("\"prop-value\" parameter not found");
//        }
//
//        String cid = message.getCID();
//        int index = message.getInt("prop-index");
//        int value = message.getInt("prop-value");
//        if (!dpr.containsDeviceProperty(cid, index)) {
//            throw secondaryMessageCheckingException("prop-index " + index + " does not exist in device " + cid);
//        }
//
//        DeviceProperty prop = dpr.getDeviceProperty(cid, index);
//        if (value < prop.getMinValue() || value > prop.getMaxValue()) {
//            throw secondaryMessageCheckingException("prop-value " + value + " is invalid. [min:" + prop.getMinValue()
//                    + ", max:" + prop.getMaxValue() + "]");
//        }
//    }
//
//    /**
//     * Notifies bm-services-registry to reload its devices registry. This is implemented on separate threads in order
//     * to minimize the time for BM to respond to the devices during POOP.
//     */
//    private class RegistryReloadNotification implements Runnable {
//        private String cid;
//        private int prop_index;
//
//        public RegistryReloadNotification(String cid, int prop_index) {
//            this.cid = cid;
//            this.prop_index = prop_index;
//        }
//
//        @Override
//        public void run() {
//            LOG.debug("Notifying registry to reload " + cid + "." + prop_index);
//            try {
//                HttpPost request = new HttpPost(bmServerURL + ":" + bmRegistryPort + "/internal/reload");
//                StringEntity params = new StringEntity("cid=" + cid + "&propindex=" + prop_index);
//                request.addHeader("content-type", "application/x-www-form-urlencoded");
//                request.setEntity(params);
//                httpClient.execute(request);
////                HttpResponse response = httpClient.execute(request);
////                LOG.error(EntityUtils.toString(response.getEntity()));
//            } catch (IOException e) {
//                LOG.error("Error in notifying registry.", e);
//            }
//        }
//    }
//}
