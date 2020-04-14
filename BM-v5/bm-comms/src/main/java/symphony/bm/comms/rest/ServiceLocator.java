package symphony.bm.comms.rest;

import lombok.AllArgsConstructor;
import lombok.Value;
import org.springframework.http.HttpMethod;

@AllArgsConstructor
@Value
public class ServiceLocator {
    String MSN;
    String URL;
    String port;
    String path;
    HttpMethod httpMethod;
}
