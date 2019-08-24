package bm.tools;

import java.sql.SQLException;
import java.util.Random;
import java.util.Vector;

import bm.context.devices.Device;
import bm.context.rooms.Room;
import bm.main.interfaces.Initializable;
import bm.main.repositories.DeviceRepository;
import bm.main.repositories.RoomRepository;
import org.apache.log4j.Logger;

public class IDGenerator implements Initializable {
	private int ridLength;
	private int cidLength;
	private int engineRequestIDLength;
	private static final Logger LOG = Logger.getLogger("IDGenerator");
	private DeviceRepository dr;
	private RoomRepository rr;
	private Vector<String> cids = new Vector<String>(1,1);
	private Vector<String> rids = new Vector<String>(1,1);
	private Vector<String> erqs_ids = new Vector<String>(1,1);
	
	public IDGenerator(int ridLength, int cidLength, int engineRequestIDLength) {
		this.ridLength = ridLength;
		this.cidLength = cidLength;
		this.engineRequestIDLength = engineRequestIDLength;
	}

	@Override
	public void initialize() throws Exception {
		getCIDs();
	}

	private void getCIDs() {
		for(Device d : dr.getAllDevices()) {
			cids.add(d.getSSID());
		}
		for(Room r : rr.getAllRooms()) {
			cids.add(r.getSSID());
		}
	}

	public IDGenerator() {
		cidLength = 4;
		engineRequestIDLength = 10;
	}
	
	public String generateERQSRequestID() {
		if(erqs_ids.size() > Math.pow(10, engineRequestIDLength) / 2) {
			erqs_ids.clear();
		}
		String id = generateMixedCharID(engineRequestIDLength, erqs_ids);
		erqs_ids.add(id);
		return id;
	}
	
	public String generateRID() {
		if(rids.size() > Math.pow(10, ridLength) / 2) {
			rids.clear();
		}
		String rid = generateIntID(ridLength, rids);
		rids.add(rid);
		return rid;
	}
	
	public String generateCID() {
		String cid = generateMixedCharID(cidLength, cids);
		cids.add(cid);
		return cid;
	}
	
	/**
	 * Generates a String with mixed characters consisting of numbers and letters that is used as an ID in various system processes.
	 * Mixed character ID's are used almost exclusively as ID's of requests and components. <br><br>
	 * 
	 * <i><b>By convention,</b> all mixedchar IDs start with a letter.</i>
	 *
	 * @param length of the String ID to be generated.
	 * @return The generated String mixed character ID with the specified length.
	 */
	public String generateMixedCharID(int length, Vector<String> existingIDs) {
		String id = "";
		Random r = new Random();
		do {
			char[] alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
			id = id + alphabet[r.nextInt(25)]; //starting char will ALWAYS be a letter.
			for(int i = 1; i < length; i++) {
				if(r.nextBoolean()) { //if true then next id char will be a number, letter otherwise
					id = id + String.valueOf(r.nextInt(9));
				} else {
					id = id + alphabet[r.nextInt(25)]; //random from 0 to 25
				}
			}
		} while(existingIDs.contains(id));
		existingIDs.add(id);
		return id;
	}
	
	/**
	 * Generates a String with mixed characters consisting of numbers and letters that is used as an ID in various system processes.
	 * Mixed character ID's are used almost exclusively as ID's of requests and components. Mixed character ID's are also used as ID
	 * of database entries in a table which accommodate lots of persistence of objects. An issue with these tables is the case of ID
	 * uniqueness. Mixed-character ID's have more possible combinations than Integer ID's, which is ideal for tables with massive numbers
	 * of entries.
	 *
	 * Because this type of ID is used in database entries, this method will generate a unique ID based on the existing ID's that are 
	 * already in the database.
	 * 
	 * @param length
	 * @param existingIDs The existing CIDs in BM. Can be null.
	 * @return
	 * @throws SQLException
	 */
	public String generateMixedCharID(int length, String[] existingIDs) {
		String id = "";
		Random r = new Random();
		boolean b = false; //true if ID generated is already unique
		do {
			char[] alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
			id = id + alphabet[r.nextInt(25)];
			//generates the ID
			for(int i = 1; i < length; i++) {
				if(r.nextBoolean()) { //if true then next id char will be a number, letter otherwise
					id = id + String.valueOf(r.nextInt(9));
				} else {
					id = id + alphabet[r.nextInt(25)]; //random from 0 to 25
				}
			}
			
			//compares the generated ID to the existing ID's
			if(existingIDs != null) { //checks if there are no existing IDs
				boolean c = true; //true if ID no longer has a duplicate in the cids array
				for(int i = 0; i < existingIDs.length; i++) {
					if(id.equals(existingIDs[i])) {
						c = false;
						break;
					}
				}
				
				if(c) {
					b = true;
					break;
				}
			}
		} while(!b);
		return id;
		
		/*Vector<String> ids = new Vector<String>(1,1); //container of all existing IDs
		
		//gets existing ids from ResultSet
		if(cids == null) { //generic ID generation, no existing IDs given
			b = true;
		} else {
			while(cids.next()) {
				ids.add(cids.getString(Application.configuration.getDatabaseConfig().getSsidColName()));
			}
		}
		
		do {
			//generates the ID
			for(int i = 0; i < length; i++) {
				if(r.nextBoolean()) { //if true then next id char will be a number, letter otherwise
					id = id + String.valueOf(r.nextInt(9));
				} else {
					char[] alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
					id = id + alphabet[r.nextInt(25)]; //random from 0 to 25
				}
			}
			
			//compares the generated ID to the existing ID's
			boolean c = true; //true if ID no longer has a duplicate in the cids array
			for(int i = 0; i < ids.size(); i++) {
				if(id.equals(ids.get(i))) {
					c = false;
					break;
				}
			}
			
			if(c) {
				b = true;
				break;
			}
		} while(!b);
		return id;*/
	}
	
	/**
	 * Generates a String with exclusively numerical characters that is used as an ID in various system processes.
	 * This type of ID is used almost exclusively as ID's of different database entries. Because this type of ID is used in 
	 * database entries, this method will generate a unique ID based on the existing ID's that are already in the database.
	 * 
	 * @param length
	 * @param existingIDs SQL ResultSet containing all the existing SSIDs of the table
	 * @return
	 * @throws SQLException 
	 */
	public static String generateIntID(int length, String[] existingIDs) {
		String id = "";
		boolean b = false; //true if ID generated is already unique
		//Vector<String> ids = new Vector<String>(1,1); //container of all existing IDs
		
		do {
			//generates the ID
			Random r = new Random();
			for(int i = 0; i < length; i++) {
				id = id + String.valueOf(r.nextInt(9));
			}
			
			//compares the generated ID to the existing ID's
			boolean c = true; //true if ID no longer has a duplicate in the cids array
			for(int i = 0; i < existingIDs.length; i++) {
				if(id.equals(existingIDs[i])) {
					c = false;
					break;
				}
			}
			
			if(c) {
				b = true;
				break;
			}
		} while(!b);
		return id;
	}
	
	public String generateIntID(int length, Vector<String> existingIDs) {
		String id = "";
		boolean b = false; //true if ID generated is already unique
		//Vector<String> ids = new Vector<String>(1,1); //container of all existing IDs
		
		//generates the ID
		do {
			Random r = new Random();
			for (int i = 0; i < length; i++) {
				id += String.valueOf(r.nextInt(9));
			}
		} while (existingIDs.contains(id));
		existingIDs.add(id);
		return id;
	}

	/**
	 * Removes the specified CID from this IDGenerator's list of existing CIDs.
	 * @param cid The cid to be removed
	 */
	public void removeCID(String cid) {
		cids.remove(cid);
	}

	public void setDeviceRepository(DeviceRepository deviceRepository) {
		this.dr = deviceRepository;
	}

	public void setRoomRepository(RoomRepository roomRepository) {
		this.rr = roomRepository;
	}

	//gets existing ids from ResultSet
		/*if(cids == null) { //generic ID generation, no existing IDs given
			b = true;
		} else {
			while(cids.next()) {
				ids.add(cids.getString(Application.configuration.getDatabaseConfig().getSsidColName()));
			}
		}*/
		
		/*do {
			//generates the ID
			Random r = new Random();
			for(int i = 0; i < length; i++) {
				id = id + String.valueOf(r.nextInt(9));
			}
			
			//compares the generated ID to the existing ID's
			boolean c = true; //true if ID no longer has a duplicate in the cids array
			for(int i = 0; i < ids.size(); i++) {
				if(id.equals(ids.get(i))) {
					c = false;
					break;
				}
			}
			
			if(c) {
				b = true;
				break;
			}
		} while(!b);
		return id;
	}*/
}
