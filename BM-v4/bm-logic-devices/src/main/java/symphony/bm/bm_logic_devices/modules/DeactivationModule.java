package symphony.bm.bm_logic_devices.modules;

import bm.context.adaptors.exceptions.AdaptorException;
import bm.context.devices.Device;
import bm.jeep.vo.JEEPRequest;
import bm.jeep.vo.JEEPResponse;
import bm.main.modules.exceptions.RequestProcessingException;
import bm.main.modules.exceptions.ResponseProcessingException;
import bm.main.repositories.DeviceRepository;

/**
 * The DeactivationModule handles the JEEP requests sent by a component that is unexpectedly disconnected from the MQTT 
 * server. This module deactivates the component in the database, OpenHAB, and other peripheral systems where the component 
 * is linked to, preventing any interactions with it.
 * 
 * @author carlomiras
 *
 */
public class DeactivationModule extends Module {
	
	/**
	 * Creates a DeactivationModule
	 * 
	 * @param logDomain the log4j domain that this module will use
	 * @param errorLogDomain the log4j domain where errors will be logged to
	 * @param RTY the JEEP request type that this module handles
	 */
	public DeactivationModule(String logDomain, String errorLogDomain, String RTY, /*MQTTPublisher mp,*/ 
			DeviceRepository dr) {
		super(logDomain, errorLogDomain, "ByeModule", RTY, new String[0], null, dr);
	}
	
//	/**
//	 * Creates a DeactivationModule
//	 *
//	 * @param logDomain the log4j domain that this module will use
//	 * @param errorLogDomain the log4j domain where errors will be logged to
//	 * @param RTY the JEEP request type that this module handles
//	 * @param extensions the module extensions attached to this module
//	 */
//	public DeactivationModule(String logDomain, String errorLogDomain, String RTY, /*MQTTPublisher mp,*/
//			DeviceRepository dr, AbstModuleExtension[] extensions) {
//		super(logDomain, errorLogDomain, "ByeModule", RTY, new String[0], null, dr, extensions);
//	}

	@Override
	protected void processRequest(JEEPRequest request) throws RequestProcessingException {
		Device d = dr.getDevice(request.getCID());
		LOG.info("Deactivating device " + d.getSSID() + " (MAC:" + d.getMAC() + ")");
		try {
			d.setActive(false);
			d.update(logDomain, true);
            LOG.info("Device deactivated!");
		} catch (AdaptorException e) {
//			LOG.error("Cannot deactivate device in one of the adaptors!", e);
//			return false;
			throw new RequestProcessingException("Cannot deactivate device in one of the adaptors!", e);
		}
	}

    @Override
    protected void processResponse(JEEPResponse response) throws ResponseProcessingException {
	    LOG.info("Device " + response.getCID() + " deactivated!");
    }

    @Override
    public void processNonResponse(JEEPRequest request) {

    }

    @Override
	protected boolean additionalRequestChecking(JEEPRequest request) {
		return true;
	}

    @Override
    protected boolean additionalResponseChecking(JEEPResponse response) {
        return true;
    }
}
