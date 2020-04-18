package symphony.bm.services.registry.jeep.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Value
@EqualsAndHashCode(callSuper = false)
public class JeepErrorResponse extends JeepResponse {
    HttpStatus status;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss")
    String message;
    LocalDateTime timestamp;
    
    public JeepErrorResponse(HttpStatus status, String msgStr) {
        super(false, msgStr);
        this.status = status;
        this.message = msgStr;
        timestamp = LocalDateTime.now();
    }
}
