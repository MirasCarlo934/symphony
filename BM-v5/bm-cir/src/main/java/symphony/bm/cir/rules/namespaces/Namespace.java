package symphony.bm.cir.rules.namespaces;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.Value;
import lombok.experimental.NonFinal;
import org.springframework.data.annotation.Transient;
import symphony.bm.core.iot.IotResource;

import javax.validation.constraints.NotNull;

public class Namespace {
    @NonNull @NotNull @Getter String name;
    @NonNull @NotNull @Getter String uid;
    @NonNull @NotNull @Getter boolean condition;
    @Getter String aid;
    
    @JsonIgnore @Transient @Setter @Getter IotResource resource;

    public String getURL() {
        String url = uid;
        if (aid != null) {
            url += "/attributes/" + aid;
        }
        return url;
    }

    public String getThingURL() {
        return uid;
    }

    /**
     *
     * @return <b>true</b> if Thing is being tracked, <b>false</b> if Attribute
     */
    public boolean isThing() {
        return aid == null;
    }
}
