package symphony.bm.cir.rules.namespaces;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;
import lombok.experimental.NonFinal;
import org.springframework.data.annotation.Transient;
import symphony.bm.core.rest.resources.Resource;

public class Namespace {
    @Getter String name;
    @Getter String uid;
    @Getter String aid;
    
    @JsonIgnore @Transient @Setter @Getter Resource resourceTracked;
    
    public String getURL() {
        String url = uid;
        if (aid != null) {
            url += "/attributes/" + aid;
        }
        return url;
    }
}
