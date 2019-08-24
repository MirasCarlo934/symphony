package ui.webapp;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import bm.context.SymphonyObject;
import bm.context.adaptors.AdaptorManager;
import bm.context.properties.Property;
import bm.jeep.vo.device.ResBasic;
import bm.main.repositories.DeviceRepository;
import bm.main.repositories.RoomRepository;
import bm.tools.IDGenerator;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import bm.cir.objects.Rule;
import bm.context.adaptors.exceptions.AdaptorException;
import bm.context.devices.Device;
import bm.context.properties.PropertyMode;
import bm.context.rooms.Room;
import bm.main.engines.exceptions.EngineException;
import bm.cir.CIRManager;

@Controller
@RequestMapping("/devices")
public class DevicesController extends AbstController {
	@Autowired
	protected DeviceRepository dr;
	@Autowired
	protected RoomRepository rr;
	@Autowired
	protected CIRManager cirm;
	@Autowired
	private AdaptorManager am;
	@Autowired
	private IDGenerator idg;

	public DevicesController(@Value("${log.domain.ui}") String logDomain) {
		super(logDomain, DevicesController.class.getSimpleName());
//		dr = (DeviceRepository) config.getApplicationContext().getBean(drStr);
//		rr = (UIRoomRepository) config.getApplicationContext().getBean(rrStr);
//		cirm = (CIRManager) config.getApplicationContext().getBean(cirStr);
//		createRoomModule = (CreateRoomModule) config.getApplicationContext().getBean(crmStr);
	}

//	@RequestMapping()
//	public String devices(Model model) {
//		if(authenticate(model)) {
//			return dispatchView("devices");
//		} else {
//			return requireLogin(model);
//		}
//	}
	
	@RequestMapping({"", "/overview"})
	public String overview(Model model) {
		if(authenticate(model, "/devices/overview").equals("/devices/overview")) {
			String devices = "";
			String rooms = "";
			String devArray = "var deviceArray = [";
			String roomArray = "var roomArray = [";
			String roomIDArray = "[";
			Iterator<Device> d = Arrays.asList(dr.getAllDevices()).iterator();
			Iterator<Room> r = Arrays.asList(rr.getAllRooms()).iterator();
			Vector<SymphonyObject> root = new Vector<SymphonyObject>(1,1);
			while(d.hasNext()) {
				Device dev = d.next();
				devices += dev.convertToJavascript() + " \n";
				devArray += "d_" + dev.getSSID() + ",";
				if(dev.getParentRoom() == null) {
					root.add(dev);
				}
			}
			while(r.hasNext()) {
				Room room = r.next();
				rooms += room.convertToJavascript() + " \n";
				roomArray += "r_" + room.getSSID() + ",";
				roomIDArray += "'r_" + room.getSSID() + "',";
				if(room.getParentRoom() == null) {
					root.add(room);
				}
			}
			
			if(dr.getAllDevices().length != 0) {
				devArray = devArray.substring(0, devArray.length() - 1) + "];";
			} else {
				devArray = "var deviceArray = [];";
			}
			if(rr.getAllRooms().length != 0) {
				roomArray = roomArray.substring(0, roomArray.length() - 1) + "];";
			} else {
				roomArray = "var roomArray = [];";
			}
			if(rr.getAllRoomIDs().length != 0) {
				roomIDArray = roomIDArray.substring(0, roomIDArray.length() - 1) + "];";
			} else {
				roomIDArray = "[]";
			}
			
			model.addAttribute("devices", devices);
			model.addAttribute("rooms", rooms);
			model.addAttribute("devArray", devArray);
			model.addAttribute("roomArray", roomArray);
			model.addAttribute("roomIDArray", roomIDArray);
			model.addAttribute("devObjs", dr.getAllDevices());
			model.addAttribute("roomObjs", rr.getAllRooms());
			model.addAttribute("rootObjs", sort(root));
			model.addAttribute("devRepo", dr);
			model.addAttribute("roomRepo", rr);
			
			return dispatchView("devices-overview");
		} else {
			return authenticate(model, "/devices/overview");
		}
	}
	
	/**
	 * Rearranges all the children of a room by increasing index.
	 * 
	 * @param children The SymphonyObject children of the room to be rearranged
	 * @param roomID The SSID of the room to be rearranged
	 */
	private void rearrangeRoom(String children, String roomID) {
		Room room = rr.getRoom(roomID);
		String[] children_id = children.split(";;;");
		if(room != null)
			LOG.debug("Rearranging room " + roomID + "(" + room.getName() + ") contents...");
		else
			LOG.debug("Rearranging root SymphonyObjects...");	
		for(int i = 0; i < children_id.length; i++) {
			SymphonyObject child = dr.getDevice(children_id[i]);
			if(child == null) { //child is not a device
				child = rr.getRoom(children_id[i]);
			}
			if(child != null) {
				child.setIndex(i);
				try {
					child.update(logDomain, false);
				} catch (AdaptorException e) {
					LOG.error("Could not updateRules index of " + child.getClass().getSimpleName() + " " + child.getSSID());
				}
			}
		}
	}
	
	@RequestMapping("/rearrangeHome")
	public String rearrangeSmarthome(@RequestParam(value="string", required=true) String string,
//			@RequestParam(value="rooms", required=false) String[] rooms, 
			Model model) {
		LOG.debug("Smarthome elements arrangement updateRules requested!");
		String[] rooms = string.split(";;;");
		
		for(int i = 0; i < rooms.length; i++) {
			String roomSSID = rooms[i].split(":")[0];
			String[] roomContents;
			try {
				roomContents = rooms[i].split(":")[1].split(",");
			} catch(ArrayIndexOutOfBoundsException e) {
				continue;
			}
			
			for(int j = 0; j < roomContents.length; j++) {
				if(dr.containsDevice(roomContents[j])) {
					Device d = (Device) dr.getDevice(roomContents[j]);
					d.setIndex(j);
					d.setRoom((Room) rr.getRoom(roomSSID));
					try {
						d.update(logDomain, false);
					} catch (AdaptorException e) {
						LOG.error("Cannot updateRules device!");
						notifyError("Cannot updateRules device! Please refresh.", model);
					}
				} else { //the content is a room!
					Room r = (Room) rr.getRoom(roomContents[j]);
					r.setIndex(j);
					if(!roomSSID.equals("root_sort")) {
						r.setRoom((Room) rr.getRoom(roomSSID));
						try {
							r.update(logDomain, false);
						} catch (AdaptorException e) {
							LOG.error("Cannot updateRules room!");
							notifyError("Cannot updateRules room! Please refresh.", model);
						}
					}
				}
			}
		}
		
		return notify(null, "Symphony Home updated!", model);
	}
	
	@RequestMapping("/relocate")
	public String relocate(@RequestParam(value="element", required=true) String elementID,
			@RequestParam(value="room", required=true) String roomID,
			@RequestParam(value="siblings", required=true) String siblings, Model model) {
		Room room;
		if(rr.containsRoom(roomID)) {
			room = rr.getRoom(roomID);
		} else {
			room = null;
		}
		
		//updates SymphonyObject
		if(dr.containsDevice(elementID)) {
			LOG.debug("Relocating device " + elementID + " to room " + roomID);
			Device d = dr.getDevice(elementID);
			d.setRoom(room);
			try {
				d.update(logDomain, false);
			} catch (AdaptorException e) {
				LOG.error("Cannot updateRules device!");
				notifyError("Cannot updateRules device! Please refresh.", model);
			}
		} else {
			LOG.debug("Relocating room " + elementID + " to room " + roomID);
			Room r = rr.getRoom(elementID);
			r.setRoom(room);
			try {
				r.update(logDomain, false);
			} catch (AdaptorException e) {
				LOG.error("Cannot updateRules room!");
				notifyError("Cannot updateRules room! Please refresh.", model);
			}
		}
		
		//updates indices of all room children
		rearrangeRoom(siblings, roomID);
		
		return notify(null, "Symphony Home updated!", model);
	}
	
	/**
	 * <b><i>AJAX-accessed request-mapping</i></b>
	 * 
	 * @param devID The ID of the device to be updated
	 * @param newName The new name of the device to be updated, can be null
	 * @param newRoom The new room ID of the device to be updated, can be null
	 * @return A notification on whether the updateRules is successful or has failed
	 */
	@RequestMapping("/editDevice")
	public String editDevice(@RequestParam(value="devID", required=true) String devID, 
			@RequestParam(value="name", required=false) String newName, 
			@RequestParam(value="room", required=false) String newRoom, Model model) {
		LOG.debug("Device " + devID + " credential updateRules requested!");
		Device d = (Device) dr.getDevice(devID);
		Room r = (Room) rr.getRoom(newRoom);
		boolean updated = false;
		if(d != null) {
			LOG.debug("Changing device " + devID + " credentials...");
			if(newName != null && !newName.isEmpty() && !newName.equals(d.getName())) {
				LOG.info("Changing name of device " + devID + " " + d.getName() + " (" +
						d.getParentRoom().getName() + ") to " + newName);
				d.setName(newName);
				updated = true;
			}
			
			if(r != null) {
				LOG.info("Changing room of device " + devID + " " + d.getName() + " (" +
						d.getParentRoom().getName() + ") to " + r.getSSID() + " (" + r.getName() + ")");
				d.setRoom(r);
				updated = true;
			} else if(newRoom != null) { //meaning a room ID was specified but is invalid
				String warn = "Room ID " + newRoom + " does not exist!";
				LOG.warn(warn);
				return notifyError("Specified room doesn't exist!", model);
			}
			
			if(updated) {
				try {
					d.update(logDomain, true);
					model.addAttribute("status", true);
					return notify(null, "Device updated", model);
				} catch (AdaptorException e) {
					String error = "Cannot updateRules device " + devID + "! Contact helpdesk to fix!";
					LOG.error(error, e);
					model.addAttribute("status", false);
					return notifyError(error, model);
				}
			} else {
				LOG.debug("There is nothing to updateRules!");
				model.addAttribute("status", false);
				return notify(null, "There is nothing to updateRules.", model);
			}
		} else {
			LOG.error("Device doesn't exist!");
			model.addAttribute("status", false);
			return notifyError("Device doesn't exist!", model);
		}
	}
	
	@RequestMapping("/editRoom")
	public String editRoom(@RequestParam(value="roomID", required=true) String roomID,
			@RequestParam(value="name", required=false) String name,
			@RequestParam(value="color", required=false) String color, Model model) {
		LOG.info("Room " + roomID + " credential change requested!");
		Room r = rr.getRoom(roomID);
		boolean updated = false;
		
		if(name != null && !name.isEmpty())  {
            r.setName(name);
			updated = true;
		}
		
		if(color != null && !color.isEmpty()) {
			r.setColor(color);
			updated = true;
		} 
		
		if(updated) {
			try {
				r.update(logDomain, true);
				LOG.info("Room " + roomID + " name changed to " + name + "!");
				return notify(null, "Room updated", model);
			} catch (AdaptorException e) {
				LOG.error("Cannot updateRules room " + roomID + "!", e);
				return notify(null, "Cannot updateRules room! Contact helpdesk to fix.", model);
			}
		}
		else {
			return notify(null, "There is nothing to updateRules.", model);
		}
	}
	
	@RequestMapping("/deleteRoom")
	public String deleteRoom(@RequestParam(value="roomID", required=true) String roomID, Model model) {
		Room r = rr.getRoom(roomID);
		LOG.debug("Room " + roomID + "(" + r.getName() + ") deletion requested!");
		if(r.getChildren().length > 0) {
			LOG.error("Cannot delete room! Still has children!");
			return notifyError("There are still devices/rooms inside the room!", model);
		}
		rr.removeRoom(roomID);
		try {
			r.delete(logDomain, true);
			LOG.info("Room " + roomID + " deleted!");
			return notify(null, "Room deleted", model);
		} catch (AdaptorException e) {
			LOG.error("Cannot delete room!", e);
			return notifyError("Cannot delete room! Contact helpdesk to fix.", model);
		}
	}
	
	@RequestMapping("/createRoom")
	public String createRoom(@RequestParam(value="name", required=true) String name, 
			@RequestParam(value="color", required=true) String color, Model model) {
		if(name == null || name.isEmpty()) {
			LOG.warn("Empty name in room creation!");
			return notifyError("Room name cannot be empty!", model);
		}
		if(color == null || color.isEmpty()) {
			color = "black";
		}
		
		LOG.debug("Creating room '" + name + "'");
		//TODO uncomment
		Room r;
		try {
            r = createRoom(name, null, color, rr.getLastIndexInRoom(null));
			HashMap<String, String> responses = new HashMap<String, String>();
			responses.put("SSID", r.getSSID());
			model.addAttribute("responses", responses);
		} catch (AdaptorException e) {
			LOG.error("Cannot create room", e);
			return notifyError("Cannot create room! Contact helpdesk to fix.", model);
		}
		HashMap<String, String> responses = new HashMap<String, String>();
		responses.put("SSID", r.getSSID());
		model.addAttribute("responses", responses);
		return notify(null, "Room created", model);
	}
	
	@RequestMapping("/getDevicesCredentials")
	public String getDevicesCredentials(Model model) {
		LOG.trace("Device credentials requested!");
		HashMap<String, String> responses = new HashMap<String, String>(dr.getAllDevices().length);
		Device[] devices = dr.getAllDevices();
		for(int i = 0; i < devices.length; i++) {
			Device dev = devices[i];
			responses.put(dev.getSSID() + "_state", String.valueOf(dev.isActive()));
			responses.put(dev.getSSID() + "_name", dev.getName());
			responses.put(dev.getSSID() + "_room", dev.getParentRoom().getSSID());
		}
		return sendResponse(responses, model);
	}
	
	@RequestMapping("/composer")
	//TASK Put javascript-transformed rules to model
	public String composer(Model model) {
		LOG.debug("Requested 'devices-composer' view access...");
		if(!authenticate(model, "/devices/composer").equals("/devices/composer")) {
			return authenticate(model, "/devices/composer");
		}
		
		String devices = "";
		String devList = "var deviceList = [ ";
//		String sensorsArray = "var sensors = [";
//		String controllersArray = "var controllers = [";
		String rulesStr = "";
		String rulesArray = "var rules = [ ";
		Iterator<Device> devs = Arrays.asList(dr.getAllDevices()).iterator();
		Iterator<Rule> rules = Arrays.asList(cirm.getAllRules()).iterator();
		
		while(devs.hasNext()) {
			//get component in javascript object string
			Device d = devs.next();
			devices += d.convertToJavascript() + " \n";
			Iterator<Property> props = Arrays.asList(d.getProperties()).iterator();
			
			//check if component is a sensor, controller, or both
			boolean sensor = false;
			boolean controller = false;
			while(props.hasNext()) {
				Property p = props.next();
				if(p.getMode().equals(PropertyMode.I)) {
					sensor = true;
				} else if(p.getMode().equals(PropertyMode.O)) {
					controller = true;
				} else if(p.getMode().equals(PropertyMode.IO)) {
					sensor = true;
					controller = true;
				}
				if(sensor && controller) break;
			}
			devList += "d_" + d.getSSID() + ",";
		}
		devList = devList.substring(0, devList.length() - 1) + "];"; //chomp last comma and add ending square bracket
		
		while(rules.hasNext()) {
			Rule r = rules.next();
			rulesStr += r.convertToJavascript();
			rulesArray += "rule" + r.getIndex() + ",";
		}
		rulesArray = rulesArray.substring(0, rulesArray.length() - 1) + "];";
		
		model.addAttribute("devices", devices);
		model.addAttribute("deviceList", devList);
		model.addAttribute("rules", rulesStr);
		model.addAttribute("rulesList", rulesArray);
		return dispatchView("devices-composer");
	}
	
	/**
	 * <b><i>AJAX-accessed request-mapping</i></b>
	 * 
	 * @param cir the new CIR
	 * @param model
	 * @return
	 */
	@RequestMapping("/composeCIR")
	public String composeCIR(@RequestParam(value="cir", required=false, defaultValue="<rules></rules>") String cir,
							 Model model) {
		LOG.debug("CIR updateRules requested!");
		Document cirXML = null;
		try {
			cirXML = DocumentHelper.parseText(cir);
		} catch (DocumentException e) {
			LOG.error("CIR XML not propertly constructed!", e);
			return notifyError("Cannot update rules! (CIR XML improper construction)", model);
		}
		cirm.overwriteRules(cirXML);
		try {
			cirm.updateRules();
		} catch (EngineException e) {
			LOG.error("Cannot update CIRManager!", e);
			return notifyError("Cannot update rules! Restart BM!", model);
		}
		LOG.info("CIR updated!");
		return notify(null, "Home rules composed!", model);
	}
	
	private Vector<SymphonyObject> sort(Vector<SymphonyObject> objs) {
		Vector<SymphonyObject> sorted = new Vector<SymphonyObject>(objs.size());
		for(int i = 0; i < objs.size(); i++) {
			SymphonyObject child = objs.get(i);
			int index = 0;
			for(; index < sorted.size(); index++) {
				if(sorted.get(index).getIndex() > child.getIndex()) {
					break;
				}
			}
			sorted.add(index, child);
		}
		return sorted;
	}

	/**
	 * Creates a new Room object and adds it to the RoomRepository.
	 *
	 * @param name The name of the new room
	 * @param parentID The ID of the parent room of the new room
	 * @param color The color of the new room
	 * @param index The index of the new room
	 * @return The created room
	 * @throws AdaptorException when an error was encountered in creating the room
	 */
	private Room createRoom(String name, String parentID, String color, int index) throws AdaptorException {
		Room r;
		if(parentID != null) {
			r = new Room(idg.generateCID(), rr.getRoom(parentID), name, color, index);
			r.setAdaptors(am.getUniversalAdaptors());
		} else {
			r = new Room(idg.generateCID(), name, color, index);
			r.setAdaptors(am.getAllAdaptors());
		}
		r.create(logDomain, true);
		rr.addRoom(r);
		LOG.info("Room " + r.getSSID() + " (" + r.getName() + ") created!");
		return r;
	}
}
