package bm.context.adaptors;

import bm.context.devices.Device;
import bm.context.rooms.Room;
import bm.main.engines.DBEngine;
import bm.main.engines.requests.DBEngine.SelectDBEReq;
import bm.main.interfaces.Initializable;
import bm.main.repositories.DeviceRepository;
import bm.main.repositories.ProductRepository;
import bm.main.repositories.RoomRepository;
import bm.tools.IDGenerator;
import org.apache.log4j.Logger;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

public class AdaptorManager implements Initializable {
    private Logger LOG;
    private IDGenerator idg;
    private ProductRepository pr;
    private DeviceRepository dr;
    private RoomRepository rr;
    private DBEngine adaptorDBE;
    private HashMap<String, AbstAdaptor> adaptors;
    private HashMap<String, Vector<AbstAdaptor>> links = new HashMap<String, Vector<AbstAdaptor>>(1, 1);
    private Vector<AbstAdaptor> universalAdaptors = new Vector<AbstAdaptor>(1, 1);

    private String adaptorLinksTable;
    private String prodIDcol;
    private String adaptorLinkCol;

    public AdaptorManager(String logDomain, AbstAdaptor[] adaptors, DBEngine adaptorDBE, String adaptorLinksTable,
                          String prodIDcol, String adaptorLinkCol, IDGenerator idGenerator) {
        LOG = Logger.getLogger(logDomain + "." + AdaptorManager.class.getSimpleName());
        this.adaptorDBE = adaptorDBE;
        this.adaptorLinksTable = adaptorLinksTable;
        this.prodIDcol = prodIDcol;
        this.adaptorLinkCol = adaptorLinkCol;
        this.idg = idGenerator;
        this.adaptors = new HashMap<String, AbstAdaptor>(adaptors.length, 1);
        for (int i = 0; i < adaptors.length; i++) {
            this.adaptors.put(adaptors[i].getID(), adaptors[i]);
        }
    }

    @Override
    public void initialize() throws Exception {
        LOG.info("Linking adaptors to products...");
        int plugged = 0;
        int unplugged = 0;
        int total = 0;
        Vector<AbstAdaptor> linked = new Vector<AbstAdaptor>(1,1);
        SelectDBEReq select1 = new SelectDBEReq(idg.generateERQSRequestID(), adaptorDBE, adaptorLinksTable);
        ResultSet rs1 = (ResultSet) adaptorDBE.putRequest(select1, Thread.currentThread(), true);
        while(rs1.next()) {
            String prodID = rs1.getString(prodIDcol);
            String adaptorLink = rs1.getString(adaptorLinkCol);
            if (!pr.containsProduct(prodID)) {
                LOG.warn("Product " + prodID + " specified does not exist for product-adaptor link: "
                        + prodID + "<-->" + adaptorLink + "!");
                unplugged++;
            } else if(!adaptors.containsKey(adaptorLink)) {
                LOG.warn("Adaptor " + adaptorLink + " specified does not exist for product-adaptor link: "
                        + prodID + "<-->" + adaptorLink + "!");
                unplugged++;
            } else {
                LOG.info("Linking adaptor " + adaptorLink + " to all devices of product " + prodID + "...");
                AbstAdaptor adaptor = adaptors.get(adaptorLink);
                Device[] devices = dr.getAllDevicesUnderProductSSID(prodID);
                for (Device device: devices) {
                    device.addAdaptor(adaptor);
                }
                if(!links.containsKey(prodID)) {
                    Vector<AbstAdaptor> adptrs = new Vector<AbstAdaptor>(1, 1);
                    adptrs.add(adaptor);
                    links.put(prodID, adptrs);
                } else if(!links.get(prodID).contains(adaptor)) {
                    links.get(prodID).add(adaptor);
                }
                linked.add(adaptor);
                plugged++;
            }
            total++;
        }
        LOG.info(plugged + " adaptor-product links made. " + unplugged + " adaptor-product links failed. "
                + total + " total.");

        if(linked.size() != adaptors.size()) {
            LOG.info("No links found for " + (adaptors.size() - linked.size()) + " adaptors. Assuming to be " +
                    "universal adaptors. Linking to all products...");
            Iterator<AbstAdaptor> adptrs = adaptors.values().iterator();
            while(adptrs.hasNext()) {
                AbstAdaptor adaptor = adptrs.next();
                universalAdaptors.add(adaptor);
                if(!linked.contains(adaptor)) {
                    LOG.info("Linking adaptor " + adaptor.getID() + " to all devices...");
                    Device[] devices = dr.getAllDevices();
                    for (Device device: devices) {
                        device.addAdaptor(adaptor);
                    }
                }
            }
            LOG.info("Universal adaptors linked!");
        }

        LOG.info("Linking all adaptors to all rooms...");
        Room[] rooms = rr.getAllRooms();
        Iterator<AbstAdaptor> a = adaptors.values().iterator();
        while(a.hasNext()) {
            AbstAdaptor adaptor = a.next();
            LOG.info("Linking adaptor " + adaptor.getID() + " to all rooms!");
            for(int i = 0; i < rooms.length; i++) {
                rooms[i].addAdaptor(adaptor);
            }
        }
        LOG.info("All adaptors linked to rooms!");
    }

    /**
     * Returns the adaptors linked to all products. <b>Not to be confused with <i>getAllAdaptors()</i>.</b>
     * @return The universal adaptors
     */
    public AbstAdaptor[] getUniversalAdaptors() {
        return universalAdaptors.toArray(new AbstAdaptor[universalAdaptors.size()]);
    }

    /**
     * Retrieves all adaptors linked to a specified product, including universal adaptors.
     * @param prodID The product ID
     * @return The retrieved adaptors
     */
    public AbstAdaptor[] getAdaptorsLinkedToProduct(String prodID) {
        Vector<AbstAdaptor> adaptors = universalAdaptors;
        if(links.get(prodID) != null) {
            adaptors.addAll(links.get(prodID));
        }
        return adaptors.toArray(new AbstAdaptor[adaptors.size()]);
    }

    /**
     * Returns all adaptors in the Environment.
     * @return The adaptors
     */
    public AbstAdaptor[] getAllAdaptors() {
        return adaptors.values().toArray(new AbstAdaptor[adaptors.size()]);
    }

    public void setRepositories(ProductRepository productRepository, DeviceRepository deviceRepository,
                                RoomRepository roomRepository) {
        this.pr = pr;
        this.dr = deviceRepository;
        this.rr = roomRepository;
    }
}
