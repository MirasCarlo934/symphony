package symphony.bm.generics.jeep.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public abstract class JeepResponse {
    @Getter final boolean success;
    @Getter String message;
}
