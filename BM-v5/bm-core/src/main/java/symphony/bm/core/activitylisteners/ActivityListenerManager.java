package symphony.bm.core.activitylisteners;

import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import symphony.bm.core.iot.Attribute;
import symphony.bm.core.iot.Group;
import symphony.bm.core.iot.Thing;

import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ActivityListenerManager implements ActivityListener {
    @Setter private List<ActivityListener> singleThreadedListeners = new Vector<>();
    @Setter private List<ActivityListener> multiThreadedListeners = new Vector<>();

    private final ExecutorService executorService = Executors.newCachedThreadPool();

//    public ActivityListenerManager(@Qualifier("bm-core.singleThreadedListeners") List<ActivityListener> singleThreadedListeners,
//                                   @Qualifier("bm-core.multiThreadedListeners") List<ActivityListener> multiThreadedListeners) {
//        this.singleThreadedListeners = singleThreadedListeners;
//        this.multiThreadedListeners = multiThreadedListeners;
//    }

    @Override
    public void thingCreated(Thing thing) {
        for (ActivityListener listener : multiThreadedListeners) {
            executorService.submit(() -> listener.thingCreated(thing));
        }
        for (ActivityListener listener : singleThreadedListeners) {
            listener.thingCreated(thing);
        }
    }

    @Override
    public void thingUpdated(Thing thing, String fieldName, Object fieldValue) {
        for (ActivityListener listener : singleThreadedListeners) {
            listener.thingUpdated(thing, fieldName, fieldValue);
        }
        for (ActivityListener listener : multiThreadedListeners) {
            executorService.submit(() -> listener.thingUpdated(thing, fieldName, fieldValue));
        }
    }

    @Override
    public void thingAddedToGroup(Thing thing, Group group) {
        for (ActivityListener listener : singleThreadedListeners) {
            listener.thingAddedToGroup(thing, group);
        }
        for (ActivityListener listener : multiThreadedListeners) {
            executorService.submit(() -> listener.thingAddedToGroup(thing, group));
        }
    }

    @Override
    public void thingRemovedFromGroup(Thing thing, Group group) {
        for (ActivityListener listener : singleThreadedListeners) {
            listener.thingRemovedFromGroup(thing, group);
        }
        for (ActivityListener listener : multiThreadedListeners) {
            executorService.submit(() -> listener.thingRemovedFromGroup(thing, group));
        }
    }

    @Override
    public void thingDeleted(Thing thing) {
        for (ActivityListener listener : singleThreadedListeners) {
            listener.thingDeleted(thing);
        }
        for (ActivityListener listener : multiThreadedListeners) {
            executorService.submit(() -> listener.thingDeleted(thing));
        }
    }

    @Override
    public void groupCreated(Group group) {
        for (ActivityListener listener : singleThreadedListeners) {
            listener.groupCreated(group);
        }
        for (ActivityListener listener : multiThreadedListeners) {
            executorService.submit(() -> listener.groupCreated(group));
        }
    }

    @Override
    public void groupUpdated(Group group, String fieldName, Object fieldValue) {
        for (ActivityListener listener : singleThreadedListeners) {
            listener.groupUpdated(group, fieldName, fieldValue);
        }
        for (ActivityListener listener : multiThreadedListeners) {
            executorService.submit(() -> listener.groupUpdated(group, fieldName, fieldValue));
        }
    }

    @Override
    public void groupAddedToGroup(Group group, Group parent) {
        for (ActivityListener listener : singleThreadedListeners) {
            listener.groupAddedToGroup(group, parent);
        }
        for (ActivityListener listener : multiThreadedListeners) {
            executorService.submit(() -> listener.groupAddedToGroup(group, parent));
        }
    }

    @Override
    public void groupRemovedFromGroup(Group group, Group parent) {
        for (ActivityListener listener : singleThreadedListeners) {
            listener.groupRemovedFromGroup(group, parent);
        }
        for (ActivityListener listener : multiThreadedListeners) {
            executorService.submit(() -> listener.groupRemovedFromGroup(group, parent));
        }
    }

    @Override
    public void groupDeleted(Group group) {
        for (ActivityListener listener : singleThreadedListeners) {
            listener.groupDeleted(group);
        }
        for (ActivityListener listener : multiThreadedListeners) {
            executorService.submit(() -> listener.groupDeleted(group));
        }
    }

    @Override
    public void attributeUpdated(Attribute attribute, String fieldName, Object fieldValue) {
        for (ActivityListener listener : singleThreadedListeners) {
            listener.attributeUpdated(attribute, fieldName, fieldValue);
        }
        for (ActivityListener listener : multiThreadedListeners) {
            executorService.submit(() -> listener.attributeUpdated(attribute, fieldName, fieldValue));
        }
    }

    @Override
    public void attributeAddedToThing(Attribute attribute, Thing thing) {
        for (ActivityListener listener : singleThreadedListeners) {
            listener.attributeAddedToThing(attribute, thing);
        }
        for (ActivityListener listener : multiThreadedListeners) {
            executorService.submit(() -> listener.attributeAddedToThing(attribute, thing));
        }
    }

    @Override
    public void attributeRemovedFromThing(Attribute attribute, Thing thing) {
        for (ActivityListener listener : singleThreadedListeners) {
            listener.attributeRemovedFromThing(attribute, thing);
        }
        for (ActivityListener listener : multiThreadedListeners) {
            executorService.submit(() -> listener.attributeRemovedFromThing(attribute, thing));
        }
    }
}
