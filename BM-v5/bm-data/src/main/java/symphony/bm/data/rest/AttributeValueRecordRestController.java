package symphony.bm.data.rest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import symphony.bm.core.iot.Attribute;
import symphony.bm.core.iot.Thing;
import symphony.bm.data.iot.ResourceRepository;
import symphony.bm.data.iot.attribute.AttributeValueRecord;
import symphony.bm.data.repositories.AttributeValueRecordRepository;
import symphony.bm.data.rest.resource.stats.avr.*;
import symphony.bm.generics.exceptions.RestControllerProcessingException;

import java.util.*;

@RestController
@CrossOrigin
@RequestMapping("/data/attributeValueRecords")
@RequiredArgsConstructor
@Slf4j
public class AttributeValueRecordRestController {
    private final ResourceRepository resourceRepository;
    private final AttributeValueRecordRepository avrRepository;
    
    @GetMapping("/stats/byDate")
    public AttributeValueRecordsStats getStats(@RequestParam("thing") String thing,
                                               @RequestParam("aid") String aid,
                                               @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date from,
                                               @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date to,
                                               Pageable p) throws RestControllerProcessingException {
        Attribute attr = getAttribute(thing, aid);
        if (to != null && from.compareTo(to) >= 0) {
            throw new RestControllerProcessingException("Date 'to' must always be greater than date 'from'",
                    HttpStatus.BAD_REQUEST);
        }
    
        AttributeValueRecordsStats stats = null;
        Page<AttributeValueRecord> records;
        if (to == null) {
            records = avrRepository.findByThingAndAidAndTimestampGreaterThanEqual(thing, aid, from, p);
        } else {
            records = avrRepository.findByThingAndAidAndTimestampBetween(thing, aid, from, to, p);
        }
        
        switch (attr.getDataType().getType()) {
            
            case number: {
                double min = 0, max = 0, ave = 0;
                do {
                    for (AttributeValueRecord avr : records) {
                        double value = Double.parseDouble(avr.getValue().toString());
                        if (value < min) min = value;
                        else if (value > max) max = value;
                        ave += value;
                    }
                    if (to == null) {
                        records = avrRepository.findByThingAndAidAndTimestampGreaterThanEqual(thing, aid, from, records.nextPageable());
                    } else {
                        records = avrRepository.findByThingAndAidAndTimestampBetween(thing, aid, from, to, records.nextPageable());
                    }
                } while (records.hasNext());
                ave /= records.getTotalElements();
                stats = new NumberAttributeValueRecordsStats(min, max, ave, thing, aid, from, to, p);
                break;
            }
            case binary: {
                long timeAtOne = 0, timeAtZero = 0;
                Date currentTimestamp;
                if (to == null) {
                    currentTimestamp = Calendar.getInstance().getTime();
                } else {
                    currentTimestamp = to;
                }
                do {
                    for (AttributeValueRecord avr : records) {
                        long difference = currentTimestamp.getTime() - avr.getTimestamp().getTime(); // assumed that avr is listed in descending order
                        if (Integer.parseInt(avr.getValue().toString()) == 1) {
                            timeAtOne += difference;
                        } else {
                            timeAtZero += difference;
                        }
                        currentTimestamp = avr.getTimestamp();
                    }
                    if (to == null) {
                        records = avrRepository.findByThingAndAidAndTimestampGreaterThanEqual(thing, aid, from, records.nextPageable());
                    } else {
                        records = avrRepository.findByThingAndAidAndTimestampBetween(thing, aid, from, to, records.nextPageable());
                    }
                } while (records.hasNext());
                // get latest record since 'from' for the complete time range
                AttributeValueRecord latestSinceFrom = avrRepository.findFirstByThingAndAidAndTimestampLessThanEqual(thing, aid, from);
                if (Integer.parseInt(latestSinceFrom.getValue().toString()) == 1) {
                    timeAtOne += currentTimestamp.getTime() - from.getTime();
                } else {
                    timeAtZero += currentTimestamp.getTime() - from.getTime();
                }
                stats = new BinaryAttributeValueRecordsStats(timeAtOne, timeAtZero, timeAtOne + timeAtZero, thing, aid, from, to, p);
                break;
            }
            case enumeration: {
                Map<String, Long> timeSpentAt = new HashMap<>();
                long totalTime = 0;
                Date currentTimestamp;
                if (to == null) {
                    currentTimestamp = Calendar.getInstance().getTime();
                } else {
                    currentTimestamp = to;
                }
                for (String enumValue : ((List<String>) attr.getDataType().getConstraints().get("values"))) {
                    timeSpentAt.put(enumValue, 0L);
                }
                do {
                    for (AttributeValueRecord avr : records) {
                        long difference = currentTimestamp.getTime() - avr.getTimestamp().getTime(); // assumed that avr is listed in descending order
                        long time = timeSpentAt.get(avr.getValue().toString());
                        time += difference;
                        totalTime += difference;
                        timeSpentAt.put(avr.getValue().toString(), time);
                        currentTimestamp = avr.getTimestamp();
                    }
                    if (to == null) {
                        records = avrRepository.findByThingAndAidAndTimestampGreaterThanEqual(thing, aid, from, records.nextPageable());
                    } else {
                        records = avrRepository.findByThingAndAidAndTimestampBetween(thing, aid, from, to, records.nextPageable());
                    }
                } while (records.hasNext());
                // if no records were retrieved from the time range, get latest record since 'from'
                AttributeValueRecord latestSinceFrom = avrRepository.findFirstByThingAndAidAndTimestampLessThanEqual(thing, aid, from);
                long difference = currentTimestamp.getTime() - from.getTime();
                timeSpentAt.put(latestSinceFrom.getValue().toString(), difference);
                for (long time : timeSpentAt.values()) {
                    totalTime += time;
                }
                stats = new EnumerationAttributeValueRecordsStats(timeSpentAt, totalTime, thing, aid, from, to, p);
                break;
            }
            default:
                stats = new EmptyAttributeValueRecordsStats(thing, aid, from, to, p);
        }
        
        return stats;
    }
    
    private Attribute getAttribute(String thing, String aid) throws RestControllerProcessingException {
        Thing t = resourceRepository.getThing(thing);
        if (t == null) {
            throw new RestControllerProcessingException("Thing " + thing + " does not exist", HttpStatus.NOT_FOUND);
        }
        Attribute attr = t.getAttribute(aid);
        if (attr == null) {
            throw new RestControllerProcessingException("Attribute " + aid + " in Thing " + thing + " does not exist",
                    HttpStatus.NOT_FOUND);
        }
        return attr;
    }
}
