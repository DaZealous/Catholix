package www.catholix.com.ng;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import adapter.NotificationAdapter;
import api.VolleyInstance;
import config.NetworkConfig;
import config.SharedPref;
import model.NotificationModel;
import model.NotificationWithUserModel;

public class NotificationActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener{

    SwipeRefreshLayout swipeRefreshLayout;
    RecyclerView recyclerView;
    LinearLayout noNotificationLayout;
    Toolbar toolbar;
    List<NotificationModel> listId;
    List<NotificationWithUserModel> listUsers;
    NotificationAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        swipeRefreshLayout = findViewById(R.id.notification_activity_swipe_refresh);
        recyclerView = findViewById(R.id.notification_activity_recycler_view);
        noNotificationLayout = findViewById(R.id.notification_activity_no_notification_layout);
        listId = new ArrayList<>();
        listUsers = new ArrayList<>();
        adapter = new NotificationAdapter(this, listUsers);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Friend Requests");

        getFriendReq();

    }

    @Override
    public void onRefresh() {
       getFriendReq();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.home){
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void getFriendReq() {
        if (NetworkConfig.getInstance(this).networkAvailable()) {
            listId.clear();
            listUsers.clear();
            swipeRefreshLayout.setRefreshing(true);
            HashMap<String, Object> map = new HashMap<>();
            map.put("req", "friend_requests");
            map.put("userID", Integer.parseInt(SharedPref.getInstance(this).getId()));
            map.put("lim", 40);
            JSONObject params = new JSONObject(map);
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, "https://catholix.com.ng/api.developer/GET/reqq.php", params,
                    response -> {
                        try {
                            JSONArray array = response.getJSONArray("data");
                            if (array.length() == 0) {
                                swipeRefreshLayout.setRefreshing(false);
                                recyclerView.setVisibility(View.INVISIBLE);
                                noNotificationLayout.setVisibility(View.VISIBLE);
                                //Toast.makeText(this, ""+array.length(), Toast.LENGTH_SHORT).show();
                            } else {
                                for (int i = 0; i < array.length(); i++) {
                                   // Toast.makeText(this, array.getJSONObject(i).toString(), Toast.LENGTH_SHORT).show();
                                    listId.add(new Gson().fromJson(array.getJSONObject(i).toString(), NotificationModel.class));
                                    if(i == array.length() - 1)
                                        getUserDetails();
                                }
                            }
                        } catch (JSONException e) {
                            swipeRefreshLayout.setRefreshing(false);
                            e.printStackTrace();
                            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> {
                        swipeRefreshLayout.setRefreshing(false);
                        Toast.makeText(this, error.getMessage(), Toast.LENGTH_SHORT).show();
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
            VolleyInstance.getInstance(this).addToQueue(request);
        } else {
            swipeRefreshLayout.setRefreshing(false);
            Toast.makeText(this, "no internet connection", Toast.LENGTH_SHORT).show();
        }
    }

    private void getUserDetails() {
        if (NetworkConfig.getInstance(this).networkAvailable()) {
            swipeRefreshLayout.setRefreshing(true);
            StringRequest request = new StringRequest(Request.Method.GET, "https://catholix.com.ng/api.developer/GET/req.php?qdata=all&table=users",
                    response -> {
                        try {
                            JSONArray array = new JSONArray(response);
                                for (int i = 0; i < array.length(); i++) {
                                    for(int k = 0; k < listId.size(); k++){
                                        if(listId.get(k).getSenderID().equalsIgnoreCase(array.getJSONObject(i).getString("ID"))){
                                            listUsers.add(new NotificationWithUserModel(listId.get(k),
                                                    array.getJSONObject(i).getString("Uname"),
                                                    array.getJSONObject(i).getString("Photo")));
                                        }
                                    }
                                    if(i == array.length() - 1){
                                        swipeRefreshLayout.setRefreshing(false);
                                        adapter.notifyDataSetChanged();
                                    }
                            }
                        } catch (JSONException e) {
                            swipeRefreshLayout.setRefreshing(false);
                            e.printStackTrace();
                            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> {
                        swipeRefreshLayout.setRefreshing(false);
                        Toast.makeText(this, error.getMessage(), Toast.LENGTH_SHORT).show();
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
            VolleyInstance.getInstance(this).addToQueue(request);
        } else {
            swipeRefreshLayout.setRefreshing(false);
            Toast.makeText(this, "no internet connection", Toast.LENGTH_SHORT).show();
        }
    }

}
