package gr.mapeu.a24to7_rebuild.Managers;

import android.content.Context;
import android.util.Log;

import java.util.List;

import gr.mapeu.a24to7_rebuild.Bundles.ProductBundle;
import gr.mapeu.a24to7_rebuild.Callbacks.ListManagerCallback;
import gr.mapeu.a24to7_rebuild.Callbacks.ListResponseHandler;
import gr.mapeu.a24to7_rebuild.Etc.Constants;

public class ListManager implements ListManagerCallback {

    private Context context;
    private String key;
    private String user;
    private ListResponseHandler callback;

    public ListManager(Context context, String key, String user) {
        this.context = context;
        this.key = key;
        this.user = user;
        this.callback = (ListResponseHandler) context;
    }

    public void getList() {
        SoapManager sManager = new SoapManager(this.key, this.user, this);
        sManager.getProductList();
    }

    public boolean deleteList() {
        DatabaseManager dbManager = new DatabaseManager(this.context);
        return dbManager.deleteAll();
    }

    @Override
    public void handleList(int routeNum, List<ProductBundle> productList, int returnCode) {
        switch (returnCode) {
            case Constants.ERROR_INV_FORM:
                Log.e("ListManager", "Invalid format");
                this.callback.onListResponse(Constants.ERROR_INV_FORM);
            case Constants.ERROR_MALFORMED_ROUTE_NUMBER:
                Log.e("ListManager", "Malformed number");
                this.callback.onListResponse(Constants.ERROR_MALFORMED_ROUTE_NUMBER);
            case Constants.NO_ERROR:
                DatabaseManager dbManager = new DatabaseManager(this.context);
                if (routeNum == -1) {
                    this.callback.onListResponse(Constants.ERROR_NO_MORE_ROUTES);
                    return;
                }
                for (ProductBundle tmp : productList) {
                    if (!dbManager.saveProd(tmp)) {
                        Log.e("ListManager", "Error in saving products " +
                                "(debug Managers$DatabaseManager)");
                        this.callback.onListResponse(Constants.ERROR_UNKNOWN);
                        return;
                    }
                }
                Log.d("ListManager", "Saved all products");
                this.callback.onListResponse(Constants.NO_ERROR);
                break;
            default:
                Log.d("ListManager", "Why am I here?");
                this.callback.onListResponse(Constants.ERROR_UNKNOWN);
        }
    }
}
