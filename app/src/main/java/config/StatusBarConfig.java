package config;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class StatusBarConfig {
    private Context context;
    private static StatusBarConfig statusBarConfig;
    public StatusBarConfig(Context context) {
        this.context = context;
    }
    public static synchronized StatusBarConfig getInstance(Context context){
        if(statusBarConfig == null)
            statusBarConfig = new StatusBarConfig(context);
        return statusBarConfig;
    }

    public void setStatusBar(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){

        }
    }
}
