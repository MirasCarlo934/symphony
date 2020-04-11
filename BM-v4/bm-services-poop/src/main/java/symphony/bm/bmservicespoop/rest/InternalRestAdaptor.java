package symphony.bm.bmservicespoop.rest;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import symphony.bm.bmservicespoop.adaptors.POOPAdaptor;
import symphony.bm.bmservicespoop.cir.rule.Rule;
import symphony.bm.bmservicespoop.entities.DeviceProperty;

import java.io.IOException;
import java.util.concurrent.ThreadPoolExecutor;

public class InternalRestAdaptor implements POOPAdaptor {
    private Logger LOG;
    private ThreadPoolExecutor executor;
    private String bmServerURL;
    private String bmRegistryPort;
    
    private HttpClient httpClient = HttpClientBuilder.create().build();
    
    public InternalRestAdaptor(String logDomain, String logName, ThreadPoolExecutor threadPoolExecutor) {
        LOG = LoggerFactory.getLogger(logDomain + "." + logName);
        this.executor = threadPoolExecutor;
    }
    
    @Override
    public void propertyValueUpdated(DeviceProperty property) {
        executor.submit(() -> {
            LOG.debug("Notifying registry to reload " + property.getID());
            try {
                HttpPatch request = new HttpPatch(bmServerURL + ":" + bmRegistryPort + "/devices/"
                        + property.getDeviceCID() + "/" + property.getIndex());
//                StringEntity params = new StringEntity("cid=" + property.getDeviceCID() + "&propindex="
//                        + property.getIndex());
//                request.addHeader("content-type", "application/x-www-form-urlencoded");
//                request.setEntity(params);
                httpClient.execute(request);
                HttpResponse response = httpClient.execute(request);
                LOG.error(EntityUtils.toString(response.getEntity()));
            } catch (IOException e) {
                LOG.error("Error in notifying registry.", e);
            }
        });
    }
    
    @Override
    public void ruleCreated(Rule rule) {
    
    }
}
