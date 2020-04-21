package symphony.bm.generics.messages;

import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.experimental.NonFinal;

@Value
@NonFinal
@AllArgsConstructor
public abstract class MicroserviceMessage {
    boolean success;
    String message;
}
