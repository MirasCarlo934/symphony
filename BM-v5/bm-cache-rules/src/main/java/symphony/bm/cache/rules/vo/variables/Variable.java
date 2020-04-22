package symphony.bm.cache.rules.vo.variables;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;
import org.springframework.data.mongodb.core.mapping.Field;

@Value
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE)
public class Variable {
    @Field("CID") @JsonProperty("CID") String CID;
    String name;
    int index;
    boolean trigger;
    
    public Variable(@JsonProperty("CID") String CID, @JsonProperty("name") String name,
                    @JsonProperty("index") int index, @JsonProperty("trigger") boolean trigger) {
        this.CID = CID;
        this.name = name;
        this.index = index;
        this.trigger = trigger;
    }
}
