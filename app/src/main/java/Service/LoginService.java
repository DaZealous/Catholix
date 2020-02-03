package Service;

import android.app.ProgressDialog;
import android.content.Context;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import api.VolleyInstance;
import config.NetworkConfig;
import config.SharedPref;
import view.LoginView;

public class LoginService {

    private Context context;
    private ProgressDialog dialog;


    public LoginService(Context context){
        this.context = context;
        dialog = new ProgressDialog(context);
        dialog.setTitle("Loading....");
        dialog.setMessage("Please wait");
        dialog.setCanceledOnTouchOutside(true);
    }

    public void doLogin(final String username, String password, final LoginView loginView) {
        if(NetworkConfig.getInstance(context).networkAvailable()) {
            dialog.show();
            HashMap<String, String> map = new HashMap<>();
            map.put("req", "login");
            map.put("Uemail", username);
            map.put("Upass", password);
            JSONObject params = new JSONObject(map);
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, "https://www.catholix.com.ng/api.developer/POST/req.php", params,
                    response -> {
                        try {
                            if (response.getBoolean("status")) {
                                getDetails(username, loginView);
                            } else {
                                //Toast.makeText(context, response.getString("Login Failed"), Toast.LENGTH_SHORT).show();
                                loginView.showLoginFailed(response.getString("message"));
                                dialog.dismiss();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> {
                        Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
            ) {
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
            loginView.showLoginFailed("no internet connection");
        }
    }

    private void getDetails(String username, final LoginView loginView) {
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, "https://www.catholix.com.ng/api.developer/GET/req.php?qdata=more&table=users&dataz=email&valuez="+username,
                null,
                object -> {
                    try {
                        SharedPref.getInstance(context).addUser(object.getString("Fname")+" "+object.getString("Sname"),
                                object.getString("Email"),
                                object.getString("Photo"),
                                object.getString("ID"));
                        loginView.startMainActivity();
                        dialog.dismiss();
                    } catch (JSONException e) {
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                },
                error -> Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show());
        VolleyInstance.getInstance(context).addToQueue(request);
    }

    public void doForgetPassword(LoginView loginView) {
        if(NetworkConfig.getInstance(context).networkAvailable()){
            loginView.startEmailRecovery();
            HashMap<String, String> map = new HashMap<>();
            map.put("req", "forget password");
            map.put("email", loginView.getForgetEmail());
            JSONObject params = new JSONObject(map);
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, "https://www.catholix.com.ng/api.developer/POST/forgetPassword.php", params,
                    response -> {
                        try {
                            if (response.getBoolean("status")) {
                                loginView.showForgetEmailSuccess();
                            } else {
                                loginView.showForgetEmailError(response.getString("message"));
                                dialog.dismiss();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> {
                        loginView.showForgetEmailError(error.getMessage());
                        dialog.dismiss();
                    }
            ) {
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
            loginView.showForgetEmailError("no internet connection");
        }
    }
}
