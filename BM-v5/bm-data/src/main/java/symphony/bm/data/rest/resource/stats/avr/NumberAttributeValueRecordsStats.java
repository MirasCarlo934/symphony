package symphony.bm.data.rest.resource.stats.avr;

import lombok.SneakyThrows;
import lombok.Value;
import org.springframework.data.domain.Pageable;

import java.util.Date;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

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
