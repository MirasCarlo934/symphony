package symphony.bm.data.rest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import symphony.bm.core.iot.Thing;
import symphony.bm.data.iot.ResourceRepository;
import symphony.bm.data.iot.thing.ThingActiveState;
import symphony.bm.data.repositories.ThingActiveStateRepository;
import symphony.bm.data.rest.resource.stats.tas.ThingActiveStateStats;
import symphony.bm.generics.exceptions.RestControllerProcessingException;

import java.util.Calendar;
import java.util.Date;

@RestController
@CrossOrigin
@RequestMapping("/data/thingActiveStates")
@RequiredArgsConstructor
@Slf4j
public class ThingActiveStateRestController {
    private final ThingActiveStateRepository tasRepository;
    private final ResourceRepository resourceRepository;
    
    @GetMapping("/stats/byDate")
    public ThingActiveStateStats getStats(@RequestParam("uid") String uid,
                                          @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date from,
                                          @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date to,
                                          Pageable p) throws RestControllerProcessingException {
        Thing thing = resourceRepository.getThing(uid);
        if (thing == null) {
            throw new RestControllerProcessingException("Thing " + uid + " does not exist", HttpStatus.NOT_FOUND);
        }
        if (to != null && from.compareTo(to) >= 0) {
            throw new RestControllerProcessingException("Date 'to' must always be greater than date 'from'",
                    HttpStatus.BAD_REQUEST);
        }
    
        Page<ThingActiveState> tasPage = tasRepository.findByUid(uid, p);
        Date currentTimestamp;
        long timeSpentAtActive = 0, timeSpentAtInactive = 0;
        if (to == null) {
            currentTimestamp = Calendar.getInstance().getTime();
        } else {
            currentTimestamp = to;
        }
        do {
            for (ThingActiveState tas : tasPage) {
                if (tas.isActive()) {
                    timeSpentAtActive += currentTimestamp.getTime() - tas.getTimestamp().getTime();
                } else {
                    timeSpentAtInactive += currentTimestamp.getTime() - tas.getTimestamp().getTime();
                }
                currentTimestamp = tas.getTimestamp();
            }
            tasPage = tasRepository.findByUid(uid, tasPage.nextPageable());
        } while(tasPage.hasNext());
        
        return new ThingActiveStateStats(timeSpentAtActive, timeSpentAtInactive,
                timeSpentAtActive + timeSpentAtInactive, uid, from, to, p);
    }
    
}
