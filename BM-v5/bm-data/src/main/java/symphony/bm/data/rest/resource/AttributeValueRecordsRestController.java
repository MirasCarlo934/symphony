package symphony.bm.data.rest.resource;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import symphony.bm.data.iot.attribute.AttributeValueRecord;
import symphony.bm.data.repositories.AttributeValueRecordRepository;

import java.util.Date;

@RestController
@CrossOrigin
@RequestMapping("/data/attributes/values/search")
@RequiredArgsConstructor
public class AttributeValueRecordsRestController {
    private final AttributeValueRecordRepository avrRepository;
    
    @GetMapping("/byDate")
    public AttributeValueRecordsResource get(String thing, String aid,
                                             @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date from,
                                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date to,
                                             Pageable p) {
        Page<AttributeValueRecord> records;
        if (to == null) {
           records = avrRepository.findByThingAndAidAndTimestampGreaterThanEqual(thing, aid, from, p);
        } else {
            records = avrRepository.findByThingAndAidAndTimestampBetween(thing, aid, from, to, p);
        }
        return new AttributeValueRecordsResource(records, 0, 0, 0);
    }
}
