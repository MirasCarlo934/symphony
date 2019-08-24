//package ui.context.properties;
//
//import bm.context.HTMLTransformable;
//import bm.context.OHItemmable;
//import bm.context.SymphonyElement;
//import bm.context.adaptors.AbstAdaptor;
//import bm.context.adaptors.exceptions.AdaptorException;
//import bm.context.properties.Property;
//import org.apache.log4j.Logger;
//import org.json.JSONObject;
//import ui.context.devices.UIDevice;
//
//public class UIProperty extends SymphonyElement implements OHItemmable, HTMLTransformable {
//    private UIDevice uiDevice;
//    private Property property;
//
//    public UIProperty(Property property, AbstAdaptor[] adaptors) {
//        super(property.getSSID(), property.getIndex());
//        this.property = property;
//    }
//
//    @Override
//    protected void create(AbstAdaptor adaptor, String parentLogDomain, boolean waitUntilCreated) throws AdaptorException {
//        Logger LOG = getLogger(parentLogDomain);
//        LOG.debug("Creating property " + SSID + " (" + property.getDisplayName() + ") in device "
//                + uiDevice.getSymphonyObject().getSSID() + " in " + adaptor.getName() + "...");
//        adaptor.propertyCreated(property, waitUntilCreated);
//        LOG.debug("B_Property created!");
//    }
//
//    @Override
//    protected void delete(AbstAdaptor adaptor, String parentLogDomain, boolean waitUntilDeleted) throws AdaptorException {
//        Logger LOG = getLogger(parentLogDomain);
//        LOG.debug("Deleting property " + SSID + " (" + property.getDisplayName() + ") in device "
//                + uiDevice.getSymphonyObject().getSSID() + " in " + adaptor.getName() + "...");
//        adaptor.propertyDeleted(property, waitUntilDeleted);
//        LOG.debug("B_Property deleted!");
//    }
//
//    @Override
//    protected void updateRules(AbstAdaptor adaptor, String parentLogDomain, boolean waitUntilUpdated) throws AdaptorException {
//        Logger LOG = getLogger(parentLogDomain);
//        LOG.debug("Updating property " + SSID + " (" + property.getDisplayName() + ") in device "
//                + uiDevice.getSymphonyObject().getSSID() + " in " + adaptor.getName() + "...");
//        adaptor.propertyValueUpdated(property, waitUntilUpdated);
//        LOG.debug("B_Property updated!");
//    }
//
//    @Override
//    public JSONObject[] convertToItemsJSON() {
//        JSONObject json = new JSONObject();
//        json.put("name", property.getOH_ID());
//        json.put("type", property.getPropType().getOHIcon());
//        if(property.getDevice().getPropvals().length > 1) {
//            json.put("groupNames", new String[]{property.getDevice().getSSID()});
//            json.put("label", property.getDisplayName());
//        } else {
//            json.put("groupNames", new String[]{property.getDevice().getParentRoom().getSSID()});
//            json.put("label", property.getDevice().getName());
//            json.put("category", uiDevice.getUIProduct().getIconImg());
//        }
//        return new JSONObject[]{json};
//    }
//
//    /**
//     * <b><i>NOTE: </b> This method returns null because properties can never be placed in the sitemap.
//     * @return <b><i>null</i></b>
//     */
//    @Override
//    public String convertToSitemapString() {
//        return null;
//    }
//
//    /**
//     * Returns a javascript object named "B_Property" with the format:
//     *
//     * "new B_Property('[propType ID]', '[display name]', '[mode]', '[property value]')";
//     */
//    @Override
//    public String convertToJavascript() {
//        String str = "new B_Property('" + property.getPropType().getSSID() + "', '" + property.getDisplayName()
//                + "', '" + property.getMode().toXML() + "', '" + property.getValue().toXML() + "')";
//        return str;
//    }
//
//    public UIDevice getUIDevice() {
//        return uiDevice;
//    }
//
//    public void setUIDevice(UIDevice uiDevice) {
//        this.uiDevice = uiDevice;
//    }
//
//    private Logger getLogger(String parentLogDomain) {
//        return Logger.getLogger(parentLogDomain + ".UIProperty:" + SSID);
//    }
//
//    /**
//     * Returns the original B_Property associated with this UIProperty.
//     * @return The original B_Property
//     */
//    public Property getOriginalProperty() {
//        return property;
//    }
//}
