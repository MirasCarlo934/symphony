package bm.context;

import bm.context.adaptors.AbstAdaptor;
import bm.context.adaptors.exceptions.AdaptorException;
import bm.context.rooms.Room;

/**
 * A child class of <i>SmarthomeElement</i> class. A SmarthomeObject is a representation of an actual, 
 * physical object that generally belongs to a room.
 * @author carlomiras
 *
 */
public abstract class SymphonyObject extends SymphonyElement implements OHItemmable, HTMLTransformable {
	protected Room parentRoom;

	public SymphonyObject(String SSID, Room room, int index) {
		super(SSID, index);
		setRoom(room, index);
	}

	public Room getParentRoom() {
		return parentRoom;
	}

	/**
	 * Sets the parent room of this SmarthomeObject. <i><b>NOTE:</b> This method will not updateRules the SmarthomeObject in
	 * peripheral systems. </i>
	 * @param room The new parent room of this device
	 * @throws AdaptorException 
	 */
	public synchronized void setRoom(Room room) {
		if(this.parentRoom != null) {
			this.parentRoom.removeSmarthomeObject(this);
		}
		if(room != null) {
			this.parentRoom = room;
			room.addSymphonyObject(this);
		} else {
			this.parentRoom = room;
		}
	}
	
	/**
	 * Sets the parent room of this SmarthomeObject. <i><b>NOTE:</b> This method will not updateRules the SmarthomeObject in
	 * peripheral systems. </i>
	 * @param room The new parent room of this device
	 * @param index	The index of this SmarthomeObject in the room. <i>Index dictates the order in which SmarthomeObjects
	 * 		are ordered in a room.</i>
	 * @throws AdaptorException 
	 */
	public synchronized void setRoom(Room room, int index) {
		if(this.parentRoom != null) {
			this.parentRoom.removeSmarthomeObject(this);
		}
		if(room != null) {
			this.parentRoom = room;
			room.addSymphonyObject(this, index);
		} else {
			this.parentRoom = room;
		}
	}
}
