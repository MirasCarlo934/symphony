//package ui.main.repositories;
//
//import bm.context.adaptors.AbstAdaptor;
//import bm.context.adaptors.exceptions.AdaptorException;
//import bm.context.devices.Device;
//import bm.main.engines.DBEngine;
//import bm.main.engines.exceptions.EngineException;
//import bm.main.engines.requests.DBEngine.SelectDBEReq;
//import bm.main.interfaces.Initializable;
//import bm.main.repositories.DeviceRepository;
//import bm.tools.IDGenerator;
//import org.apache.log4j.Logger;
//import ui.context.adaptors.UIAdaptor;
//import ui.context.devices.UIDevice;
//
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.util.HashMap;
//import java.util.Iterator;
//
//public class UIDeviceRepository implements Initializable {
//    private String logDomain;
//    private Logger LOG;
//    protected HashMap<String, UIDevice> devices = new HashMap<String, UIDevice>(1);
//    private UIProductRepository ui_pr;
//    private DeviceRepository dr;
//
//    public UIDeviceRepository(String logDomain, UIAdaptor uia, DeviceRepository deviceRepository,
//                              UIProductRepository uiProductRepository) {
//        LOG = Logger.getLogger(logDomain + "." + UIDeviceRepository.class.getSimpleName());
//        this.logDomain = logDomain;
//        this.dr = deviceRepository;
//        this.ui_pr = uiProductRepository;
//        uia.setUIDeviceRepository(this);
//    }
//
//    @Override
//    public void initialize() {
//        populate();
//        updateDevicesInEnvironment();
//    }
//
//    public void populate() {
//        LOG.info("Populating UIDeviceRepository...");
//        for(int i = 0; i < dr.getAllDevices().length; i++) {
//            Device dev = dr.getAllDevices()[i];
//            LOG.debug("Adding device " + dev.getSSID() + "to UIDeviceRepository...");
////            System.out.println(dev.getProduct().getSSID() + "-"
////                    + ui_pr.getUIProduct(dev.getProduct().getSSID()));
//            addUIDevice(new UIDevice(dev, ui_pr.getUIProduct(dev.getProduct().getSSID())));
//            LOG.debug("Device added!");
//        }
//        LOG.debug("UIDeviceRepository populated!");
//    }
//
//    public void updateDevicesInEnvironment() {
//        LOG.debug("Updating devices in Symphony Environment UI...");
//        Iterator<UIDevice> devices = this.devices.values().iterator();
//        while(devices.hasNext()) {
//            UIDevice device = devices.next();
//            try {
//                device.updateRules(logDomain, false);
//            } catch (AdaptorException e) {
//                LOG.error("Cannot updateRules device " + device.getSSID() + " in environment UI!", e);
//            }
//        }
//        LOG.debug("Devices updated in Symphony Environment UI!");
//    }
//
//    public boolean containsUIDevice(String ssid) {
//        return devices.containsKey(ssid);
//    }
//
//    public void addUIDevice(UIDevice d) {
//        devices.put(d.getSSID(), d);
//        if(!dr.containsDevice(d.getSSID())) {
//            dr.createDevice((Device)d.getSymphonyObject());
//        }
//    }
//
//    public UIDevice getUIDevice(String ssid) {
//        return devices.get(ssid);
//    }
//
//    public UIDevice removeUIDevice(String roomID) {
//        dr.detachDevice(roomID);
//        return devices.remove(roomID);
//    }
//
//    public UIDevice[] getAllUIDevices() {
//        return devices.values().toArray(new UIDevice[devices.size()]);
//    }
//}
