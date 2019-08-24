//package ui.context.rooms;
//
//import bm.context.HTMLTransformable;
//import bm.context.OHItemmable;
//import bm.context.adaptors.AbstAdaptor;
//import bm.context.adaptors.exceptions.AdaptorException;
//import bm.context.rooms.Room;
//import org.apache.log4j.Logger;
//import org.json.JSONObject;
//import ui.context.UISymphonyObject;
//
//public class UIRoom extends UISymphonyObject implements HTMLTransformable, OHItemmable {
//	private Room room;
//	private String color;
//
//	public UIRoom(Room room, String color) {
//	    super(room, room.getParentRoom(), room.getIndex());
//		this.room = room;
//		this.color = color;
//	}
//
//	@Override
//	protected void create(AbstAdaptor adaptor, String parentLogDomain, boolean waitUntilCreated)
//			throws AdaptorException {
//		Logger LOG = getLogger(parentLogDomain);
//		LOG.debug("Creating room " + SSID + " (" + room.getName() + ") in " + adaptor.getName() + "...");
//		adaptor.roomCreated(room, false);
//		LOG.debug("Room created!");
//	}
//
//	@Override
//	protected void delete(AbstAdaptor adaptor, String parentLogDomain, boolean waitUntilDeleted)
//			throws AdaptorException {
//		// TODO Auto-generated method stub
//
//	}
//
//	@Override
//	protected void updateRules(AbstAdaptor adaptor, String parentLogDomain, boolean waitUntilUpdated)
//			throws AdaptorException {
//        Logger LOG = getLogger(parentLogDomain);
//        LOG.debug("Updating room " + SSID + " (" + room.getName() + ") in " + adaptor.getName() + "...");
//        adaptor.roomCredentialsUpdated(room, false);
//        LOG.debug("Room updated!");
//	}
//
//    //Ex. var r_CRL0 = new Room("CRL0", "Kuya's Bedroom");
//    @Override
//    public String convertToJavascript() {
//        String s = "var r_" + SSID + " = new Room(\"" + SSID + "\", \"" + room.getName() + "\");";
//        return s;
//    }
//
//	@Override
//	public JSONObject[] convertToItemsJSON() {
//		JSONObject json = new JSONObject();
//		json.put("name", SSID);
//		json.put("type", "Group");
//		json.put("label", room.getName());
//		if(room.getParentRoom() != null)
//			json.put("groupNames", new String[]{room.getParentRoom().getSSID()});
//		return new JSONObject[]{json};
//	}
//
//	/**
//	 * Converts this Room to a simple sitemap string where it is included ONLY in the main frame of the sitemap
//	 */
//	@Override
//	public String convertToSitemapString() {
//		//Group item=J444 label="Kitchen"
//		String s = "Group item=" + SSID + " label=\"" + room.getName() + "\"";
//		return s;
//	}
//
//	private Logger getLogger(String parentLogDomain) {
//		return Logger.getLogger(parentLogDomain + ".UIRoom:" + SSID);
//	}
//
//	public String getColor() {
//		return color;
//	}
//
//	public void setColor(String color) {
//		this.color = color;
//	}
//
//    @Override
//    public Room getSymphonyObject() {
//        return room;
//    }
//}
