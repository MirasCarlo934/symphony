package symphony.bm.services.poop.jeep;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class POOPResponse {
    @Getter final boolean success;
    @Getter final String message;
}
