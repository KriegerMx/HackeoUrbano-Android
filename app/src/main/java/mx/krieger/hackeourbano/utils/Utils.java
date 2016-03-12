package mx.krieger.hackeourbano.utils;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;

import mx.krieger.labplc.clients.dashboardAPI.DashboardAPI;
import mx.krieger.labplc.clients.internalAPI.InternalAPI;

public class Utils {

    public static InternalAPI.InternalAPIOperations getInternalAPI(){
        InternalAPI.Builder builder = new InternalAPI.Builder(AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null);
        return builder.build().internalAPI();
    }

    public static DashboardAPI getMapatonPublicAPI(){
        DashboardAPI.Builder builder = new DashboardAPI.Builder(AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null);
        return builder.build();
    }
}
