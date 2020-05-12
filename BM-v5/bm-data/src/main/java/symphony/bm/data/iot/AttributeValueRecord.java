package symphony.bm.data.iot;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Value;
import org.springframework.data.annotation.Id;

import java.util.Date;

@Value
public class AttributeValueRecord {
    String aid;
    String thing;
    Date timestamp;
    Object value;
}
