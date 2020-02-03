package Service;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import api.VolleyInstance;
import config.SharedPref;
import view.RegisterView;

public class RegisterService {
    private Context context;
    private ProgressDialog dialog;
    public RegisterService(Context context) {
        this.context = context;
        dialog = new ProgressDialog(context);
        dialog.setTitle("Registering...");
        dialog.setMessage("please wait");
    }

    public void doRegister(final RegisterView view) {
        HashMap<String, String> map = new HashMap<>();
        map.put("req", "signUp");
        map.put("Fname", view.getFirstName());
        map.put("Uname", view.getUserName());
        map.put("Sname", view.getLastName());
        map.put("Email", view.getEmail());
        map.put("Phone", view.getPhone());
        map.put("Pword", view.getPassword());
        JSONObject object = new JSONObject(map);

        if(isInternetAvailable(context)){
            dialog.show();
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, "https://catholix.com.ng/api.developer/POST/req.php",
                    object,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                if(response.getBoolean("status")){
                                    getDetails(view.getEmail(), view);
                                    Toast.makeText(context, response.getString("message"), Toast.LENGTH_SHORT).show();
                                }else {
                                    Toast.makeText(context, response.getString("message"), Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                }
                            } catch (JSONException e) {
                                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            dialog.dismiss();
                            Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }){
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("Content-Type", "application/json");
                    return params;
                }
                @Override
                public String getBodyContentType() {
                    return "application/json";
                }

            };
            VolleyInstance.getInstance(context).addToQueue(request);
        }else{
            Toast.makeText(context, "no internet connection", Toast.LENGTH_SHORT).show();
        }
    }

    private static boolean isInternetAvailable(Context context){
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return manager.getActiveNetworkInfo() != null
                && manager.getActiveNetworkInfo().isAvailable()
                && manager.getActiveNetworkInfo().isConnectedOrConnecting();
    }

    private void getDetails(String email, final RegisterView view) {
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, "https://www.catholix.com.ng/api.developer/GET/req.php?qdata=more&table=users&dataz=email&valuez="+email,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject object) {
                        try {
                            SharedPref.getInstance(context).addUser(object.getString("Fname")+" "+object.getString("Sname"),
                                    object.getString("Email"),
                                    object.getString("Photo"),
                                    object.getString("ID"));
                            view.startActivity();
                            dialog.dismiss();
                        } catch (JSONException e) {
                            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
        VolleyInstance.getInstance(context).addToQueue(request);
    }
}
