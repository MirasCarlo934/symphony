package symphony.bm.data.rest.resource;

import lombok.Data;
import lombok.Value;
import org.springframework.data.domain.Page;
import org.springframework.hateoas.RepresentationModel;
import symphony.bm.data.iot.attribute.AttributeValueRecord;

import java.util.List;
import java.util.Vector;

@Value
public class AttributeValueRecordsResource extends RepresentationModel<AttributeValueRecordsResource> {
    List<AttributeValueRecord> contents;
    double min;
    double max;
    double ave;
    
    public AttributeValueRecordsResource(Page<AttributeValueRecord> records, double min, double max, double ave) {
        this.min = min;
        this.max = max;
        this.ave = ave;
        contents = records.toList();
    }
}
