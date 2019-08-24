//package ui.context.devices;
//
//import bm.context.HTMLTransformable;
//import bm.context.OHItemmable;
//import bm.context.adaptors.AbstAdaptor;
//import bm.context.adaptors.exceptions.AdaptorException;
//import bm.context.devices.Device;
//import bm.context.properties.Property;
//import bm.context.rooms.Room;
//import org.apache.log4j.Logger;
//import org.json.JSONObject;
//import ui.context.UISymphonyObject;
//import ui.context.devices.products.UIProduct;
//import ui.context.properties.UIProperty;
//
//import java.util.Arrays;
//import java.util.Iterator;
//
//public class UIDevice extends UISymphonyObject implements HTMLTransformable, OHItemmable {
//    private UIProduct uiProduct;
//    private Device device;
//
//    public UIDevice(Device device, UIProduct uiProduct) {
//        super(device, device.getParentRoom(), device.getIndex());
//        this.device = device;
//        this.uiProduct = uiProduct;
//
//        Iterator<UIProperty> props = uiProduct.getUIProperties().values().iterator();
//        while(props.hasNext()) {
//            UIProperty prop = props.next();
//            prop.setUIDevice(this);
//            prop.getOriginalProperty().setDevice(device);
//        }
//    }
//
//    @Override
//    protected void create(AbstAdaptor adaptor, String parentLogDomain, boolean waitUntilCreated) throws AdaptorException {
//        Logger LOG = getLogger(parentLogDomain);
//        LOG.debug("Creating device " + SSID + " (" + device.getName() + ") in " + adaptor.getName() + "...");
//        adaptor.deviceCreated(device, waitUntilCreated);
//        LOG.debug("Device created!");
//    }
//
//    @Override
//    protected void delete(AbstAdaptor adaptor, String parentLogDomain, boolean waitUntilDeleted) throws AdaptorException {
//        Logger LOG = getLogger(parentLogDomain);
//        LOG.debug("Deleting device " + SSID + " (" + device.getName() + ") in " + adaptor.getName() + "...");
//        adaptor.deviceDeleted(device, waitUntilDeleted);
//        LOG.debug("Device deleted!");
//    }
//
//    @Override
//    protected void updateRules(AbstAdaptor adaptor, String parentLogDomain, boolean waitUntilUpdated) throws AdaptorException {
//        Logger LOG = getLogger(parentLogDomain);
//        LOG.debug("Updating device " + SSID + " (" + device.getName() + ") in " + adaptor.getName() + "...");
//        adaptor.deviceCredentialsUpdated(device, waitUntilUpdated);
//        LOG.debug("Device updated!");
//    }
//
//    @Override
//    public String convertToJavascript() {
//        String s = "var d_" + SSID + " = new Device(\"" + SSID + "\", \"" + device.getProduct().getName() + "\", \""
//                + device.getName() + "\", ";
//        if(device.getParentRoom() != null) {
//            s += "\"" + device.getParentRoom().getSSID() + "\", ";
//        }
//        s += "[";
//        Iterator<Property> props = Arrays.asList(device.getPropvals()).iterator();
//
//        while(props.hasNext()) { //convert each property to format ex. {id:"0006",label:"Detected",io:"I"}
//            Property prop = props.next();
//            if(!(prop.getPropType().getSSID().equals("INN"))) {
//                s += "{id:\"" + prop.getSSID() + "\","
//                        + "label:\"" + prop.getDisplayName() + "\","
//                        + "io:\"" + prop.getMode().toXML() + "\",";
//                if(!prop.getPropType().getSSID().equals("STR")) {
//                    s += "min:" + prop.getPropType().getMin() + ","
//                            + "max:" + prop.getPropType().getMax() + ",";
//                }
//                s = s.substring(0, s.length() - 1) + "},"; //to chomp the last comma and add closing curly bracket
//            }
//        }
//        s = s.substring(0, s.length() - 1) + "]);"; //to chomp the last comma and add closing var characters
//        return s;
//    }
//
//
//    @Override
//    public JSONObject[] convertToItemsJSON() {
//        if(device.getPropvals().length > 1) { //creates a group item if component has >1 properties
//            JSONObject json = new JSONObject();
//            json.put("type", "Group");
//            json.put("name", getSSID());
//            json.put("label", device.getName());
//            json.put("category", uiProduct.getIconImg());
//            if(device.getParentRoom() != null)
//                json.put("groupNames", new String[]{getParentRoom().getSSID()});
//            return new JSONObject[]{json};
//        } else { //lets the single property define the component in registry
//            return new JSONObject[0];
//        }
//    }
//
//    @Override
//    public String convertToSitemapString() {
//        String itemType;
//        if(device.getPropvals().length > 1) {
//            itemType = "Group";
//        } else {
//            Property p = device.getPropvals()[0];
//            itemType = p.getOHItemType();
//        }
//        return itemType + " item=" + SSID + " [label=\"" + device.getName() + "\"] [icon=\"" +
//                uiProduct.getIconImg() + "\"]";
//    }
//
//    private Logger getLogger(String parentLogDomain) {
//        return Logger.getLogger(parentLogDomain + ".UIDevice:" + SSID);
//    }
//
//    public UIProperty[] getAllUIProperties() {
//        return uiProduct.getUIProperties().values().toArray(new UIProperty[uiProduct.getUIProperties().size()]);
//    }
//
//    public UIProperty getUIProperty(String ssid) {
//        return uiProduct.getUIProperties().get(ssid);
//    }
//
//    @Override
//    public Device getSymphonyObject() {
//        return device;
//    }
//
//    public UIProduct getUIProduct() {
//        return uiProduct;
//    }
//}
