package config;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPref {
    private Context context;
    SharedPreferences pref;
    private static SharedPref instance;

    public SharedPref(Context context){
        this.context = context;
        pref = getPref();
    }

    private SharedPreferences getPref() {
        if(pref == null){
            pref = context.getSharedPreferences("User", Context.MODE_PRIVATE);
        }
        return pref;
    }

    public static synchronized SharedPref getInstance(Context context){
        if(instance == null){
            instance = new SharedPref(context.getApplicationContext());
        }
        return instance;
    }

    public void addUser(String username, String email, String image, String _id){
        SharedPreferences.Editor editor = getPref().edit();
        editor.putString("username", username);
        editor.putString("email", email);
        editor.putString("image", image);
        editor.putString("_id", _id);

        editor.apply();
    }

    public void addPhoto(String photo){
        SharedPreferences.Editor editor = getPref().edit();
        editor.putString("image", photo);
        editor.apply();
    }

    public String getUser(){
        return getPref().getString("username", "");
    }


    public String getEmail(){
        return getPref().getString("email", "");
    }


    public String getImage(){
        return getPref().getString("image", "");
    }

    public String getId(){
        return getPref().getString("_id", "");
    }

    public void removeUser(){
        SharedPreferences.Editor editor = getPref().edit();
        editor.clear();
        editor.apply();
    }


}
