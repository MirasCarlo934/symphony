package bm.comms;

import bm.context.adaptors.exceptions.AdaptorException;
import bm.context.devices.Device;
import bm.jeep.JEEPManager;
import bm.jeep.vo.JEEPRequest;
import bm.main.controller.Controller;
import bm.main.repositories.DeviceRepository;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class ResponseManager extends TimerTask {
    private final Logger LOG;
    private String logDomain;
    private DeviceRepository dr;
    private Controller controller;
    private JEEPManager jm;
    private HashMap<JEEPRequest, Integer> activeRequests = new HashMap<JEEPRequest, Integer>(1);
    private int timeout;
    private int resendPeriod;

    /**
     *  @param logDomain The log domain of this ResponseManager
     * @param resendPeriod Time (in milliseconds) in which the ResponseManager will resend the unresponded
     *                     JEEPRequests
     * @param timeout Time (in milliseconds) to wait for a device to respond before considering it
     * @param deviceRepository The DeviceRepository of this deployment of Maestro
     * @param controller The Controller of this deployment of Maestro
     * @param isResending <b><i>true</i></b> if this ResponseManager will resend unresponded requests,
     *                    <b><i>false</i></b> if not
     */
    public ResponseManager(String logDomain, int resendPeriod, int timeout, DeviceRepository deviceRepository,
                           Controller controller, JEEPManager jeepManager, boolean isResending) {
        LOG = Logger.getLogger(logDomain + "." + ResponseManager.class.getSimpleName());
        this.dr = deviceRepository;
        this.logDomain = logDomain;
        this.timeout = timeout;
        this.resendPeriod = resendPeriod;
        this.controller = controller;
        this.jm = jeepManager;

        Timer timer = new Timer("ResponseManagerResender");
        if(isResending) {
            timer.schedule(this, 0, resendPeriod);
        }
        LOG.info("ResponseManager started!");
    }

    @Override
    public void run() {
        for(JEEPRequest request : activeRequests.keySet()) {
            if(activeRequests.get(request) > 0) {
                LOG.warn("Device " + request.getCID() + " has not yet responded to request " + request.getRID()
                        + ". Resending request...");
                request.send();
                activeRequests.put(request, activeRequests.get(request) - resendPeriod);
            } else {
                LOG.warn("Device " + request.getCID() + " did not respond to request " + request.getCID()
                        + " in time. Setting device to inactive.");
                Device d = dr.getDevice(request.getCID());
                d.setActive(false);
//                d.sendDeactivationMessage("Deactivated due to nonresponsiveness to request " + request.getRID());
                jm.sendDeactivationRequest(d, "Deactivated due to nonresponsiveness to request "
                        + request.getRID());
                try {
                    d.update(logDomain, true);
                } catch (AdaptorException e) {
                    LOG.error("Couldn't update device!", e);
                }
                controller.processNonResponsiveJEEPRequest(request);
                activeRequests.remove(request);
            }
        }
    }

    public void removeActiveRequest(String rid) {
        for(JEEPRequest request : activeRequests.keySet()) {
            if(request.getRID().equals(rid)) {
                LOG.debug("Request " + rid + " already responded to. Removing from active list...");
                activeRequests.remove(request);
            }
        }
    }

    /**
     * Adds a JEEP request sent by Maestro that has yet to be responded to by the device.
     * @param request The JEEPRequest object
     */
    public void addActiveRequest(JEEPRequest request) {
        activeRequests.put(request, timeout);
    }
}
