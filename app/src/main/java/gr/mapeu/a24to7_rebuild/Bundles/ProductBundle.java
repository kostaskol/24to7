package gr.mapeu.a24to7_rebuild.Bundles;

public class ProductBundle {
    private String pharmacy;
    private String product;

    public ProductBundle(String ph, String p) {
        this.pharmacy = ph;
        this.product = p;
    }

    public String getPharmacy() { return this.pharmacy; }

    public String getProduct() { return this.product; }
}
