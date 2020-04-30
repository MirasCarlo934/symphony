package symphony.bm.generics.messages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.experimental.NonFinal;

@Value
@NonFinal
public class MicroserviceMessage {
    boolean success;
    String message;

    @JsonCreator
    public MicroserviceMessage(@JsonProperty("success") boolean success, @JsonProperty("message") String message) {
        this.success = success;
        this.message = message;
    }
}
