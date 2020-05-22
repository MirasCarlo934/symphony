package symphony.bm.data.rest.resource;

import lombok.Value;
import org.springframework.data.domain.Page;
import org.springframework.hateoas.RepresentationModel;
import symphony.bm.data.iot.attribute.AttributeValueRecord;

import java.util.List;

@Value
public class AttributeValueRecordsPageableResource extends RepresentationModel<AttributeValueRecordsPageableResource> {
    List<AttributeValueRecord> contents;
    double min;
    double max;
    double ave;
//    PageModel page;
    
    public AttributeValueRecordsPageableResource(Page<AttributeValueRecord> records, double min, double max, double ave) {
        this.min = min;
        this.max = max;
        this.ave = ave;
        contents = records.toList();
    }
}
