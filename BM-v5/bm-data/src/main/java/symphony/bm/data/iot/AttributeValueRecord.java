package symphony.bm.data.iot;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.data.annotation.Id;

import java.util.Date;

@Data
public class AttributeValueRecord {
    @Id @JsonIgnore String _id;
    @JsonIgnore final String aid;
    @JsonIgnore final String thing;
    final Date timestamp;
    final Object value;
}