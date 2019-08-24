//package bm.main.modules.admin;
//
//import bm.context.adaptors.AdaptorManager;
//import bm.jeep.vo.device.ResBasic;
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import bm.context.adaptors.exceptions.AdaptorException;
//import bm.context.rooms.Room;
//import bm.jeep.vo.JEEPRequest;
//import bm.main.repositories.DeviceRepository;
//import bm.main.repositories.RoomRepository;
//import bm.tools.Cipher;
//import bm.tools.IDGenerator;
//
//public class CreateRoomModule extends AbstAdminModule {
//	private RoomRepository rr;
//	private IDGenerator idg;
//	private AdaptorManager am;
//	private String nameParam;
//	private String parentParam;
//	private String indexParam;
//
//	public CreateRoomModule(String logDomain, String errorLogDomain, String RTY, AdaptorManager adaptorManager,
//							DeviceRepository dr, Cipher cipher, String pwdParam, String encryptedPwd,
//                            RoomRepository rr, IDGenerator idg, String nameParam,
//                            String parentParam, String indexParam) {
//		super(logDomain, errorLogDomain, CreateRoomModule.class.getSimpleName(), RTY,
//				new String[] {pwdParam, nameParam, parentParam, indexParam}, /*mp, */dr, cipher,
//				pwdParam, encryptedPwd);
//		this.rr = rr;
//		this.idg = idg;
//		this.am = adaptorManager;
//		this.nameParam = nameParam;
//		this.parentParam = parentParam;
//		this.indexParam = indexParam;
//	}
//
//	@Override
//	protected boolean processRequest(JEEPRequest request) {
//		JSONObject json = request.getJSON();
//		String name = json.getString(nameParam);
//		String parentID = json.getString(parentParam);
//		int index = json.getInt(indexParam);
//
//		LOG.debug("Room creation requested!");
//		Room r;
//		try {
//			r = createBasicRoom(name, parentID, "black", index);
//			LOG.info("Room " + r.getSSID() + " (" + r.getName() + ") created!");
//			request.getProtocol().getSender().send(new ResBasic(request, true));
//		} catch (AdaptorException e) {
//			LOG.error("Cannot create room!", e);
//            request.getProtocol().getSender().send(new ResBasic(request, false));
//			return false;
//		}
//		return true;
//	}
//
//	/**
//	 * Creates a new <i>Room</i> in the Symphony environment. Initializes the new <i>Room</i> object then
//	 * adds it to the <i>RoomRepository.</i>
//	 *
//	 * @param name The name of the new room
//	 * @param parentID The ID of the parent room of this room. Can be null to put in root room
//	 * @param index The index of this room inside its parent room
//	 * @return The new <i>Room</i> object
//	 * @throws AdaptorException if new Room cannot be created in one of the plugged adaptors
//	 */
//	public Room createBasicRoom(String name, String parentID, String color, int index)
//			throws AdaptorException {
//		Room r;
//		if(parentID != null) {
//			r = new Room(idg.generateCID(rr.getAllRoomIDs()), rr.getRoom(parentID), name, color, index);
//			r.setAdaptors(am.getUniversalAdaptors());
//		} else {
//			r = new Room(idg.generateCID(rr.getAllRoomIDs()), name, color, index);
//			r.setAdaptors(am.getAllAdaptors());
//		}
//		rr.addRoom(r);
//		r.create(logDomain, true);
//
//		return r;
//	}
//
//	/**
//	 * Creates a Room Smarthome Object in the root room of the Symphony Environment.
//	 *
//	 * @param name The name of the room
//	 * @return The new Room Smarthome Object
//	 * @throws AdaptorException if room cannot be created in one of the plugged adaptors
//	 */
//	public Room createBasicRoom(String name, String color)
//            throws AdaptorException {
//		return createBasicRoom(name, null, color, rr.getLastIndexInRoom(null));
//	}
//
////    public UIRoom createCompleteRoom(String name, String parentID, int index, AbstAdaptor[] adaptors) throws AdaptorException {
////        UIRoom uir;
////        if(parentID != null) {
////            r = new Room(idg.generateCID(rr.getAllRoomIDs()), rr.getRoom(parentID), name, adaptors,
////                    index);
////        } else {
////            r = new Room(idg.generateCID(rr.getAllRoomIDs()), name, adaptors, index);
////        }
////    }
////
////    /**
////     * Creates a complete Room Smarthome Object in the root room of the Symphony Environment. This Room
////     * includes UI-related elements and will be shown in the UI.
////     * @param name The name of the room
////     * @param adaptors The adaptors plugged to this room
////     * @return The new UIRoom object
////     * @throws AdaptorException if room cannot be created in one of the plugged adaptors
////     */
////	public UIRoom createCompleteRoom(String name, AbstAdaptor[] adaptors) throws AdaptorException {
////
////    }
//
//	@Override
//	protected boolean additionalRequestChecking(JEEPRequest request) {
//		JSONObject json = request.getJSON();
//		String parentID;
//
////		LOG.debug("Checking...");
//		try {
//			json.getString(nameParam);
//		} catch(JSONException e) {
//			LOG.error("No name specified!", e);
//			return false;
//		}
//		try {
//			parentID = json.getString(parentParam);
//		} catch(JSONException e) {
//			LOG.error("No parent room ID specified!", e);
//			return false;
//		}
//		try {
//			json.getInt(indexParam);
//		} catch(JSONException e) {
//			LOG.error("No room index specified!", e);
//			return false;
//		}
//
//		if(!rr.containsRoom(parentID)) {
//			LOG.error("Parent room ID '" + parentID + "' does not exist!");
//			return false;
//		}
//
//		return true;
//	}
//
//}
