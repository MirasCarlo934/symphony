package symphony.bm.registry.entities;

import org.springframework.data.annotation.Transient;
import symphony.bm.registry.adaptors.Adaptor;

import java.util.List;

public abstract class Entity {
    @Transient protected List<Adaptor> adaptors;
    
    /**
     * Sets this entity's AND its children's adaptors.
     * @param adaptors The adaptors list
     */
    void setAdaptors(List<Adaptor> adaptors) {
        this.adaptors = adaptors;
        setAdaptorsToChildren(adaptors);
    }
    
    abstract void setAdaptorsToChildren(List<Adaptor> adaptors);
    abstract void setSelfToChildren();
}
