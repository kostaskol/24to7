package gr.mapeu.a24to7_rebuild.Callbacks;

import java.util.List;

import gr.mapeu.a24to7_rebuild.Bundles.ProductBundle;

public interface ListManagerCallback {
    void handleList(int routeNum, List<ProductBundle> bundleList, int returnCode);
}
