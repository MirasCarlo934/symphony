package symphony.bm.data.rest.resource.stats.tas;

import lombok.SneakyThrows;
import lombok.Value;
import lombok.experimental.NonFinal;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.RepresentationModel;
import symphony.bm.data.rest.AttributeValueRecordRestController;
import symphony.bm.data.rest.ThingActiveStateRestController;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Value
@NonFinal
public class ThingActiveStateStats extends RepresentationModel<ThingActiveStateStats> {
    Map<String, Long> timeSpentAt;
    long totalTime;
    
    @SneakyThrows
    public ThingActiveStateStats(long timeSpentAtActive, long timeSpentAtInactive, long totalTime,
                                 String uid, Date from, Date to, Pageable p) {
        timeSpentAt = new HashMap<>();
        timeSpentAt.put("active", timeSpentAtActive);
        timeSpentAt.put("inactive", timeSpentAtInactive);
        this.totalTime = totalTime;
        this.add(linkTo(methodOn(ThingActiveStateRestController.class).getStats(uid, from, to, p)).withSelfRel());
    }
}
