package symphony.bm.bm_logic_devices.repositories;

import bm.context.adaptors.exceptions.AdaptorException;
import bm.context.rooms.Room;
import bm.main.engines.DBEngine;
import bm.main.engines.exceptions.EngineException;
import bm.main.engines.requests.DBEngine.RawDBEReq;
import bm.main.interfaces.Initializable;
import bm.tools.IDGenerator;
import org.apache.log4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;

/**
 * The RoomRepository is the container for all the the rooms in the Symphony Environment. Only one RoomRepository
 * object must exist in the Symphony Environment.
 */
public class RoomRepository implements Initializable {
	private Logger LOG;
	private String logDomain;
	protected DBEngine dbe;
	protected HashMap<String, Room> rooms = new HashMap<String, Room>(1);
	private String getRoomsQuery;
	protected IDGenerator idg;
	private Room recentlyAddedRoom;

	public RoomRepository(DBEngine dbe, String getRoomsQuery, String logDomain, IDGenerator idg) {
		this.LOG = Logger.getLogger(logDomain + "." + RoomRepository.class.getSimpleName());
		this.logDomain = logDomain;
		this.dbe = dbe;
		this.getRoomsQuery = getRoomsQuery;
		this.idg = idg;
	}

	/**
	 * @see Initializable#initialize()
	 */
	@Override
	public void initialize() throws Exception {
		retrieveRoomsFromDB();
	}

	/**
	 * Retrieves all rooms from the Symphony Database. This method is ONLY USUALLY called by the
	 * {@link bm.main.Maestro Maestro} in the startup phase.
	 */
	public void retrieveRoomsFromDB() throws SQLException{
		LOG.info("Retrieving rooms from DB...");
		RawDBEReq request = new RawDBEReq(idg.generateERQSRequestID(), dbe, getRoomsQuery);
		Object o;
		try {
			o = dbe.putRequest(request, Thread.currentThread(), true);
		} catch (EngineException e) {
			throw(new SQLException("Cannot retrieve rooms from DB!", e));
		}
		ResultSet rooms_rs = (ResultSet) o;
		try {
			while(rooms_rs.next()) {
				String id = rooms_rs.getString("ssid");
				String name = rooms_rs.getString("name");
				String parentID = rooms_rs.getString("parent_room");
				String color = rooms_rs.getString("color");
				int index = rooms_rs.getInt("index");
				Room room;
				LOG.debug("Adding room " + id + " (" + name + ") to repository!");
				if(parentID == null) {
					room = new Room(id, name, color, index);
				} else {
					Room parent = new Room(parentID, null, null, index); //placeholder, signifies that the retrieved room has a parent
					room = new Room(id, parent, name, color, index);
				}
				rooms.put(room.getSSID(), room);
			}
			LOG.debug(rooms.size() + " rooms added!");
			rooms_rs.close();
		} catch (SQLException e) {
			throw(new SQLException("Cannot retrieve rooms from DB!", e));
		}
		Iterator<Room> roomObjs = rooms.values().iterator();
		while(roomObjs.hasNext()) { 
			Room room = roomObjs.next();
			if(room.getParentRoom() != null) {
				Room parent = room.getParentRoom();
				room.setRoom(rooms.get(parent.getSSID()));
			}
		}
		LOG.debug("Room retrieval complete!");
	}

	/**
	 * Calls an update to all the adaptors connected to the rooms. This method is ONLY USUALLY called by the
	 * {@link bm.main.Maestro Maestro} in the startup phase.
	 */
	public void updateRoomsInEnvironment() {
	    LOG.debug("Updating rooms in Symphony Environment...");
        Iterator<Room> rooms = this.rooms.values().iterator();
        while(rooms.hasNext()) {
            Room room = rooms.next();
            try {
                room.update(logDomain, false);
            } catch (AdaptorException e) {
                LOG.error("Cannot updateRules room " + room.getSSID() + " in environment!", e);
            }
        }
        LOG.debug("Rooms updated in Symphony Environment!");
    }
	
	/**
	 * Checks if the room with the specified room ID exists.
	 * 
	 * @param roomID The room ID to check
	 * @return <b>true</b> if the room exists, <b>false</b> if not
	 */
	public boolean containsRoom(String roomID) {
		return rooms.containsKey(roomID);
	}
	
	/**
	 * Adds a room object to the repository. <b>NOTE:</b> Room is added ONLY to the repository. To integrate
	 * room to the environment completely, {@link Room#create(String, boolean)} must be called.
	 * 
	 * @param r The {@link Room} to be added
	 */
	public void addRoom(Room r) {
		rooms.put(r.getSSID(), r);
		recentlyAddedRoom = r;
	}
	
	/**
	 * Removes a room object from the repository. <b>NOTE:</b> Room is added ONLY to the repository. To integrate
	 * room to the environment completely, {@link Room#create(String, boolean)} must be called.
	 * 
	 * @param roomID The room ID of the Room object to delete
	 * @return The {@link Room} with the specified room ID, <i>null</i> if the room ID does not exist in the
	 * 		repository
	 */
	public Room removeRoom(String roomID) {
		return rooms.remove(roomID);
	}
	
	/**
	 * Returns the most recently added room in the repository
	 * 
	 * @return The {@link Room}
	 */
	public Room getRecentlyAddedRoom() {
		return recentlyAddedRoom;
	}
	
	/**
	 * Returns the Room object that represents a room in the Symphony system.
	 * 
	 * @param roomID The room ID of the Room object to get
	 * @return The Room object with the specified room ID, <i>null</i> if the room ID does not exist in the 
	 * 		repository
	 */
	public Room getRoom(String roomID) {
		return rooms.get(roomID);
	}
	
	/**
	 * Returns all the rooms in the repository
	 * 
	 * @return An array of {@link Room Rooms}
	 */
	public Room[] getAllRooms() {
		return rooms.values().toArray(new Room[rooms.size()]);
	}
	
	/**
	 * Returns all the room IDs in this RoomRepository
	 * 
	 * @return An array of room ID strings
	 * @return
	 */
	public String[] getAllRoomIDs() {
		String[] ids = new String[rooms.size()];
		Iterator<Room> rs = rooms.values().iterator();
		for(int i = 0; i < ids.length; i++) {
			ids[i] = rs.next().getSSID();
		}
		return ids;
	}
	
	/**
	 * Returns the highest index in the specified room
	 * 
	 * @param roomID The ID of the room. Can be null, which will return the highest index in the root room.
	 * @return the highest index in the room
	 */
	public int getLastIndexInRoom(String roomID) {
		int highest = 0;
		Room[] rs = rooms.values().toArray(new Room[0]);
		
		if(roomID == null ) {
			for(int i = 0; i < rs.length; i++) {
				if(rs[i].getParentRoom() == null && rs[i].getIndex() > highest) {
					highest = rs[i].getIndex();
				}
			}
		} else if(containsRoom(roomID)) {
			Room parent = rooms.get(roomID);
			for(int i = 0; i < rs.length; i++) {
				if(rs[i].getParentRoom().equals(parent) && rs[i].getIndex() > highest) {
					highest = rs[i].getIndex();
				}
			}
		}
		
		return highest;
	}
}
