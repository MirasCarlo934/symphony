package symphony.bm.comms.rest;

import lombok.AllArgsConstructor;
import lombok.Value;
import org.springframework.http.HttpMethod;

import java.util.List;

@AllArgsConstructor
@Value
public class ServiceLocator {
    String MSN;
    String bmURL;
    String port;
    String path;
    HttpMethod httpMethod;
    List<String> variablePaths;
    
    public String getServiceURL() {
        return bmURL + ":" + port + "/" + path;
    }
}
