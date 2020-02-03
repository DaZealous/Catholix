package config;

import android.content.Context;
import android.net.ConnectivityManager;

public class NetworkConfig {
    private static ConnectivityManager manager;
    private static NetworkConfig instance;

    private NetworkConfig(Context context){
        getManager(context);
    }

    public static synchronized NetworkConfig getInstance(Context context){
        if (instance == null)
            instance = new NetworkConfig(context);
        return instance;
    }

    private ConnectivityManager getManager(Context context){
        if(manager == null)
            manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return manager;
    }

    public boolean networkAvailable(){
        return manager.getActiveNetworkInfo() != null
                && manager.getActiveNetworkInfo().isAvailable()
                && manager.getActiveNetworkInfo().isConnectedOrConnecting();

    }
}
