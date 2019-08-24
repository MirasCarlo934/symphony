package bm.context.products;

import bm.tools.IDGenerator;

public class NoProduct extends Product {
    public NoProduct(String mainLogDomain, String name, String description, String iconImg, IDGenerator idGenerator) {
        super(mainLogDomain, "0000", name, description, iconImg, idGenerator);
    }
}
