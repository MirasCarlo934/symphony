//package ui.context;
//
//import bm.context.SymphonyElement;
//import bm.context.SymphonyObject;
//import bm.context.adaptors.AbstAdaptor;
//import bm.context.adaptors.exceptions.AdaptorException;
//import bm.context.rooms.Room;
//
//public abstract class UISymphonyObject extends SymphonyElement {
//    private SymphonyObject obj;
//    private Room parentRoom;
//    private int roomIndex;
//
//	public UISymphonyObject(SymphonyObject obj, Room parentRoom, int index) {
//		super(obj.getSSID(), index);
//		this.parentRoom = parentRoom;
//		this.roomIndex = index;
//		this.obj = obj;
//	}
//
//	@Override
//    public void create(String parentLogDomain, boolean waitUntilCreated) throws AdaptorException {
//        obj.create(parentLogDomain, waitUntilCreated);
//        super.create(parentLogDomain, waitUntilCreated);
//    }
//
//    @Override
//    public void delete(String parentLogDomain, boolean waitUntilDeleted) throws AdaptorException {
//        obj.delete(parentLogDomain, waitUntilDeleted);
//        super.delete(parentLogDomain, waitUntilDeleted);
//    }
//
//    @Override
//    public void updateRules(String parentLogDomain, boolean waitUntilUpdated) throws AdaptorException {
//        obj.updateRules(parentLogDomain, waitUntilUpdated);
//        super.updateRules(parentLogDomain, waitUntilUpdated);
//    }
//
//    @Override
//    public void setSSID(String ssid) {
//        obj.setSSID(ssid);
//        super.setSSID(ssid);
//    }
//
//    @Override
//    public void setIndex(int index) {
//	    obj.setIndex(index);
//	    super.setIndex(index);
//    }
//
//    /**
//     * Returns the SymphonyObject associated with this UISymphonyObject.
//     * @return The SymphonyObject
//     */
//    public SymphonyObject getSymphonyObject() {
//	    return obj;
//    }
//
//    public Room getParentRoom() {
//        return parentRoom;
//    }
//
//    public int getRoomIndex() {
//        return roomIndex;
//    }
//}
