//package ui.main.repositories;
//
//import bm.context.adaptors.AbstAdaptor;
//import bm.context.devices.Device;
//import bm.context.products.Product;
//import bm.main.engines.DBEngine;
//import bm.main.engines.exceptions.EngineException;
//import bm.main.engines.requests.DBEngine.SelectDBEReq;
//import bm.main.interfaces.Initializable;
//import bm.main.repositories.DeviceRepository;
//import bm.main.repositories.ProductRepository;
//import bm.tools.IDGenerator;
//import org.apache.log4j.Logger;
//import ui.context.adaptors.UIAdaptor;
//import ui.context.devices.UIDevice;
//import ui.context.devices.products.UIProduct;
//
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.util.HashMap;
//
//public class UIProductRepository implements Initializable {
//    private Logger LOG;
//    protected HashMap<String, UIProduct> products = new HashMap<String, UIProduct>(1);
//    private ProductRepository pr; // the RoomRepository of the Symphony instance
//    private DBEngine uiDBE;
//    private AbstAdaptor[] adaptors;
//    private IDGenerator idg;
//
//    private String uiProdsTable;
//    private String ssidColname;
//    private String iconColname;
//
//    public UIProductRepository(String logDomain, AbstAdaptor[] adaptors, IDGenerator idg,
//                              ProductRepository productRepository, DBEngine uiDBE, String uiProdsTable,
//                              String ssidColname, String iconColname) {
//        LOG = Logger.getLogger(logDomain + "." + UIProductRepository.class.getSimpleName());
//        this.pr = productRepository;
//        this.uiDBE = uiDBE;
//        this.uiProdsTable = uiProdsTable;
//        this.ssidColname = ssidColname;
//        this.iconColname = iconColname;
//        this.adaptors = adaptors;
//        this.idg = idg;
//    }
//
//    @Override
//    public void initialize() {
//        populate();
//    }
//
//    public void populate() {
//        LOG.info("Populating UIProductRepository...");
//        SelectDBEReq select1 = new SelectDBEReq(idg.generateERQSRequestID(), uiDBE, uiProdsTable);
//        try {
//            LOG.debug("Retrieving Product UI data...");
//            ResultSet rs1 = (ResultSet) uiDBE.putRequest(select1, Thread.currentThread(), true);
//            while(rs1.next()) {
//                String prodId = rs1.getString(ssidColname);
//                String icon = rs1.getString(iconColname);
//                addUIProduct(new UIProduct(pr.getProduct(prodId), adaptors, icon));
//            }
//            LOG.debug("Product UI data retrieved!");
//        } catch (EngineException | SQLException e) {
//            LOG.error("Cannot retrieve Product UI data!", e);
//        }
//        LOG.debug("UIProductRepository populated!");
//    }
//
//    public void addUIProduct(UIProduct prod) {
//        products.put(prod.getOriginalProduct().getSSID(), prod);
//        if(!pr.containsProduct(prod.getOriginalProduct().getSSID())) {
//            pr.addProduct(prod.getOriginalProduct());
//        }
//    }
//
//    public UIProduct getUIProduct(String ssid) {
//        return products.get(ssid);
//    }
//
//    public UIProduct[] getAllUIProduct() {
//        return products.values().toArray(new UIProduct[products.size()]);
//    }
//}
