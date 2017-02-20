package gr.mapeu.a24to7_rebuild.Callbacks;

import java.util.List;

import gr.mapeu.a24to7_rebuild.Bundles.ProductBundle;

public interface ListResponseHandler {
    void onListResponse(int routeNumber, List<ProductBundle> list, int code);
}
