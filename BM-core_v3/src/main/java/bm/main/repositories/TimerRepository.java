package bm.main.repositories;

import java.util.HashMap;

import org.apache.log4j.Logger;

import bm.context.devices.Device;
import bm.main.interfaces.Initializable;

public class TimerRepository implements Initializable {
	private static Logger LOG;
	private DeviceRepository dr;
	private String timerProdSSID;
	private HashMap<String, Device> timers = new HashMap<String, Device>(1);

	public TimerRepository(String logDomain, String name, DeviceRepository dr, String timerProdSSID) {
		this.LOG = Logger.getLogger(logDomain + "." + TimerRepository.class.getSimpleName());
		this.dr = dr;
		this.timerProdSSID = timerProdSSID;
	}

	@Override
	public void initialize() throws Exception {
		LOG.info("Initializing system timers...");
		LOG.debug("Retrieving system timers from DeviceRepository...");
		Device[] timerDevs = dr.getAllDevicesUnderProductSSID(timerProdSSID);
		for(int i = 0; i < timerDevs.length; i++) {
			timers.put(timerDevs[i].getSSID(), timerDevs[i]);
		}
	}
	
	
}
