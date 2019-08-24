//package ui.main.repositories;
//
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.util.HashMap;
//import java.util.Iterator;
//
//import bm.context.adaptors.exceptions.AdaptorException;
//import bm.context.rooms.Room;
//import org.apache.log4j.Logger;
//
//import bm.context.adaptors.AbstAdaptor;
//import bm.main.engines.DBEngine;
//import bm.main.engines.exceptions.EngineException;
//import bm.main.engines.requests.DBEngine.SelectDBEReq;
//import bm.main.interfaces.Initializable;
//import bm.main.repositories.RoomRepository;
//import bm.tools.IDGenerator;
//import ui.context.adaptors.UIAdaptor;
//import ui.context.rooms.UIRoom;
//
//public class UIRoomRepository implements Initializable {
//	private String logDomain;
//	private Logger LOG;
//	protected HashMap<String, UIRoom> rooms = new HashMap<String, UIRoom>(1);
//	private RoomRepository rr; // the RoomRepository of the Symphony instance
//	private DBEngine uiDBE;
//	private IDGenerator idg;
//
//	private String uiRoomsTable;
//	private String ssidColname;
//	private String colorColname;
//
//	public UIRoomRepository(String logDomain, AbstAdaptor[] adaptors, UIAdaptor uia, IDGenerator idg,
//							RoomRepository roomRepository, DBEngine uiDBE, String uiRoomsTable, String ssidColname,
//							String colorColname) {
//		LOG = Logger.getLogger(logDomain + "." + UIRoomRepository.class.getSimpleName());
//		this.logDomain = logDomain;
//		this.rr = roomRepository;
//		this.uiDBE = uiDBE;
//		this.uiRoomsTable = uiRoomsTable;
//		this.ssidColname = ssidColname;
//		this.colorColname = colorColname;
//		this.idg = idg;
//		uia.setUIRoomRepository(this);
//	}
//
//	@Override
//	public void initialize() throws Exception {
//		populate();
//		updateRoomsInEnvironment();
//	}
//
//	public void populate() {
//		for(int i = 0; i < rr.getAllRooms().length; i++) {
//		    bm.context.rooms.Room r = rr.getAllRooms()[i];
//        }
//		LOG.info("Populating UIRoomRepository...");
//		SelectDBEReq select1 = new SelectDBEReq(idg.generateERQSRequestID(), uiDBE, uiRoomsTable);
//		try {
//			ResultSet rs1 = (ResultSet) uiDBE.putRequest(select1, Thread.currentThread(), true);
//			while(rs1.next()) {
//				String ssid = rs1.getString(ssidColname);
//				String color = rs1.getString(colorColname);
//				LOG.trace("Adding room " + ssid + " to UIRoomRepository");
//				addUIRoom(new UIRoom(rr.getRoom(ssid), color));
//			}
//		} catch (EngineException | SQLException e) {
//			LOG.error("Cannot populate UIRoomRepository!", e);
//		}
//		LOG.debug("UIRoomRepository populated!");
//	}
//
//	public void updateRoomsInEnvironment() {
//		LOG.debug("Updating rooms in Symphony Environment UI...");
//		Iterator<UIRoom> rooms = this.rooms.values().iterator();
//		while(rooms.hasNext()) {
//			UIRoom room = rooms.next();
//			try {
//				room.updateRules(logDomain, false);
//			} catch (AdaptorException e) {
//				LOG.error("Cannot updateRules room " + room.getSSID() + " in environment UI!", e);
//			}
//		}
//		LOG.debug("Rooms updated in Symphony Environment UI!");
//	}
//
//	public boolean containsRoom(String roomID) {
//		return rr.containsRoom(roomID);
//	}
//
//	public void addUIRoom(UIRoom r) {
//		rooms.put(r.getSSID(), r);
//		if(!rr.containsRoom(r.getSSID())) {
//		    rr.addRoom((Room) r.getSymphonyObject());
//        }
//	}
//
//	public UIRoom getUIRoom(String roomID) {
//		return rooms.get(roomID);
//	}
//
//	public UIRoom removeUIRoom(String roomID) {
//        rr.removeRoom(roomID);
//	    return rooms.remove(roomID);
//	}
//
//	public UIRoom[] getAllUIRooms() {
//		return rooms.values().toArray(new UIRoom[rooms.size()]);
//	}
//
//	public String[] getAllRoomIDs() {
//		return rr.getAllRoomIDs();
//	}
//
//    /**
//     * Returns the RoomRepository associated with this UIRoomRepository.
//     * @return The RoomRepository
//     */
//	public RoomRepository getRoomRepository() {
//	    return rr;
//    }
//}
