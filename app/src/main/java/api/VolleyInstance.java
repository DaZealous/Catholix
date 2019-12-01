package api;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class VolleyInstance {
    private Context context;
    private RequestQueue requestQueue;
    private static VolleyInstance instance;
    private VolleyInstance(Context context){
        this.context = context;
        requestQueue = getRequestQueue();
    }

    public RequestQueue getRequestQueue() {
        if(requestQueue == null){
            requestQueue = Volley.newRequestQueue(context);
        }
        return requestQueue;
    }

    public static synchronized VolleyInstance getInstance(Context context){
        if(instance == null){
            instance = new VolleyInstance(context.getApplicationContext());
        }
        return instance;
    }

    public  <T> void addToQueue(Request<T> request){
        requestQueue.add(request);
    }
}
