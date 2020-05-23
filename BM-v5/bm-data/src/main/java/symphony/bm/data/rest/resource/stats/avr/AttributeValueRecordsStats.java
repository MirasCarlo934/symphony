package symphony.bm.data.rest.resource.stats.avr;

import lombok.SneakyThrows;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.RepresentationModel;
import symphony.bm.data.rest.AttributeValueRecordRestController;

import java.util.Date;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public abstract class AttributeValueRecordsStats extends RepresentationModel<AttributeValueRecordsStats> {
    
    @SneakyThrows
    public AttributeValueRecordsStats(String thing, String aid, Date from, Date to, Pageable p) {
        this.add(linkTo(methodOn(AttributeValueRecordRestController.class).getStats(thing, aid, from, to, p)).withSelfRel());
//        if (to == null) {
//            this.add(linkTo(methodOn(AttributeValueRecordRepository.class).findByThingAndAidAndTimestampGreaterThanEqual(thing, aid, from, p)).withRel("data"));
//        } else {
//            this.add(linkTo(methodOn(AttributeValueRecordRepository.class).findByThingAndAidAndTimestampBetween(thing, aid, from, to, p)).withRel("data"));
//        }
    }
}
