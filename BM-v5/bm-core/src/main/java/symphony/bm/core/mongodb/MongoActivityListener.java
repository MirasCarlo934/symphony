package symphony.bm.core.mongodb;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import symphony.bm.core.activitylisteners.ActivityListener;
import symphony.bm.core.iot.Group;
import symphony.bm.core.iot.Thing;
import symphony.bm.core.iot.attribute.Attribute;

import java.util.Map;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingQueue;

@Slf4j
public class MongoActivityListener extends TimerTask implements ActivityListener {
    private final MongoOperations mongo;
    private final Queue<Thing> thingsToSave = new LinkedBlockingQueue<>();
    private final Queue<Group> groupsToSave = new LinkedBlockingQueue<>();
    private final Queue<Attribute> attributesToSave = new LinkedBlockingQueue<>();

    public MongoActivityListener(MongoTemplate mongoTemplate, long timeBetweenPersists) {
        this.mongo = mongoTemplate;
        Timer runner = new Timer(MongoActivityListener.class.getSimpleName() + "Updater");
        runner.schedule(this, 0, timeBetweenPersists);
    }

    @Override
    public void run() {
        while (!thingsToSave.isEmpty()) {
            Thing thing = thingsToSave.poll();
            log.info("Saving thing " + thing.getUid() + " to DB");
            mongo.save(thing);
            log.info("Thing saved to DB");
        }
        while (!groupsToSave.isEmpty()) {
            Group group = groupsToSave.poll();
            log.info("Saving group " + group.getGid() + " to DB");
            mongo.save(group);
            log.info("Group saved to DB");
        }
        while (!attributesToSave.isEmpty()) {
            Attribute attribute = attributesToSave.poll();
            log.info("Saving attribute " + attribute.getThing() + "/" + attribute.getAid() + " to DB");
            mongo.save(attribute);
            log.info("Attribute saved to DB");
        }
    }

    private void save(Thing thing) {
        if (!thingsToSave.contains(thing)) {
            thingsToSave.offer(thing);
        }
    }

    private void save(Group group) {
        if (!groupsToSave.contains(group)) {
            groupsToSave.offer(group);
        }
    }

    private void save(Attribute attribute) {
        if (!attributesToSave.contains(attribute)) {
            attributesToSave.offer(attribute);
        }
    }

    @Override
    public void thingCreated(Thing thing) {
        save(thing);
    }

    @Override
    public void thingUpdated(Thing thing, Map<String, Object> updatedFields) {
        save(thing);
    }

    @Override
    public void thingAddedToGroup(Thing thing, Group group) {
        save(thing);
    }

    @Override
    public void thingRemovedFromGroup(Thing thing, Group group) {
        save(thing);
    }

    @Override
    public void thingDeleted(Thing thing) {
        log.debug("Deleting thing " + thing.getUid() + " from DB");
        thingsToSave.remove(thing);
        mongo.remove(thing);
        log.info("Thing deleted from DB");
    }

    @Override
    public void groupCreated(Group group) {
        save(group);
    }

    @Override
    public void groupUpdated(Group group, Map<String, Object> updatedFields) {
        save(group);
    }

    @Override
    public void groupAddedToGroup(Group group, Group parent) {
        save(group);
    }

    @Override
    public void groupRemovedFromGroup(Group group, Group parent) {
        save(group);
    }

    @Override
    public void groupDeleted(Group group) {
        log.debug("Deleting group " + group.getGid() + " from DB");
        groupsToSave.remove(group);
        mongo.remove(group);
        log.info("Group deleted from DB");
    }

    @Override
    public void attributeUpdated(Attribute attribute, Map<String, Object> updatedFields) {
        save(attribute);
    }

    @Override
    public void attributeUpdatedValue(Attribute attribute, Object value) {
        save(attribute);
    }

    @Override
    public void attributeAddedToThing(Attribute attribute, Thing thing) {
        save(attribute);
    }

    @Override
    public void attributeRemovedFromThing(Attribute attribute, Thing thing) {
        log.debug("Deleting attribute " + thing.getUid() + "/" + attribute.getAid() + " from DB");
        attributesToSave.remove(attribute);
        mongo.remove(attribute);
        log.info("Attribute deleted from DB");
    }
}
