package symphony.bm.data.rest.resource.stats;

import lombok.SneakyThrows;
import lombok.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.RepresentationModel;
import symphony.bm.data.repositories.AttributeValueRecordRepository;
import symphony.bm.data.rest.AttributeValueRecordRestController;

import java.util.Date;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Value
public class NumberAttributeValueRecordsStats extends AttributeValueRecordsStats {
    double min;
    double max;
    double ave;
    
    @SneakyThrows
    public NumberAttributeValueRecordsStats(double min, double max, double ave,
                                            String thing, String aid, Date from, Date to, Pageable p) {
        super(thing, aid, from, to, p);
        this.min = min;
        this.max = max;
        this.ave = ave;
    }
}
