//package ui.context.devices.products;
//
//import bm.context.HTMLTransformable;
//import bm.context.adaptors.AbstAdaptor;
//import bm.context.products.Product;
//import bm.context.products.Product;
//import bm.context.properties.Property;
//import ui.context.properties.UIProperty;
//
//import java.util.HashMap;
//import java.util.Iterator;
//
//public class UIProduct implements HTMLTransformable {
//    private Product product;
//    private HashMap<String, UIProperty> properties = new HashMap<String, UIProperty>(10);
//    private AbstAdaptor[] adaptors;
//    private String iconImg;
//
//    public UIProduct(Product product, AbstAdaptor[] adaptors, String iconImg) {
//        this.product = product;
//        this.adaptors = adaptors;
//        this.iconImg = iconImg;
//
//        Iterator<Property> props = product.getPropvals().values().iterator();
//        while(props.hasNext()) {
//            Property prop = props.next();
//            properties.put(prop.getSSID(), new UIProperty(prop, adaptors));
//        }
//    }
//
//    @Override
//	public String convertToJavascript() {
//		Iterator<UIProperty> props = properties.values().iterator();
//		String str = "new Product('" + product.getSSID() + "', '" + product.getName() + "', '"
//                + product.getDescription() + "', '" + iconImg + "', [";
//		while(props.hasNext()) {
//			UIProperty prop = props.next();
//			str += prop.convertToJavascript() + ", ";
//		}
//		str = str.substring(0, str.length() - 2) + "])";
//		return str;
//	}
//
//    public void setIconImg(String iconImg) {
//        this.iconImg = iconImg;
//    }
//
//    public String getIconImg() {
//        return iconImg;
//    }
//
//    /**
//     * Returns the original Product associated with this UIProduct.
//     * @return The original Product
//     */
//    public Product getOriginalProduct() {
//        return product;
//    }
//
//    public void setUIProperties(HashMap<String, Property> properties) {
//
//    }
//
//    public HashMap<String, UIProperty> getUIProperties() {
//        return properties;
//    }
//
//    public AbstAdaptor[] getAdaptors() {
//        return adaptors;
//    }
//}
