package symphony.bm.data.iot.thing;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.data.annotation.Id;

import java.util.Date;

@Data
public class ThingActiveState {
    @Id @JsonIgnore String _id;
    @JsonIgnore final String uid;
    final Date timestamp;
    final boolean active;
}
