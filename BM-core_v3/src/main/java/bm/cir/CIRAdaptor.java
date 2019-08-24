package bm.cir;

import bm.context.adaptors.AbstAdaptor;
import bm.context.adaptors.exceptions.AdaptorException;
import bm.context.devices.Device;
import bm.context.properties.Property;
import bm.context.rooms.Room;

public class CIRAdaptor extends AbstAdaptor {
    private CIRManager cir;

    public CIRAdaptor(String logDomain, String adaptorID, String adaptorName, CIRManager cir) {
        super(logDomain, adaptorID, adaptorName);
        this.cir = cir;
    }

    @Override
    public void deviceCreated(Device d, boolean waitUntilCreated) throws AdaptorException {

    }

    @Override
    public void deviceDeleted(Device d, boolean waitUntilDeleted) throws AdaptorException {

    }

    @Override
    public void deviceCredentialsUpdated(Device d, boolean waitUntilUpdated) throws AdaptorException {

    }

    @Override
    public void propertyCreated(Property p, boolean waitUntilPersisted) throws AdaptorException {

    }

    @Override
    public void propertyDeleted(Property p, boolean waitUntilDeleted) throws AdaptorException {

    }

    @Override
    public void propertyValueUpdated(Property p, boolean waitUntilUpdated) throws AdaptorException {
        cir.propertyChangedValue(p);
    }

    @Override
    public void roomCreated(Room r, boolean waitUntilPersisted) throws AdaptorException {

    }

    @Override
    public void roomDeleted(Room r, boolean waitUntilDeleted) throws AdaptorException {

    }

    @Override
    public void roomCredentialsUpdated(Room r, boolean waitUntilUpdated) throws AdaptorException {

    }

    @Override
    public void roomParentUpdated(Room r, boolean waitUntilUpdated) throws AdaptorException {

    }
}
