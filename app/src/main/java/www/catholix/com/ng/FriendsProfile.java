package www.catholix.com.ng;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import api.VolleyInstance;
import config.NetworkConfig;
import config.SharedPref;

public class FriendsProfile extends AppCompatActivity {

    Toolbar toolbar;
    String fullname, photo, username, email, phone, country, state, gender, id;
    ImageView profileImg, addFriendImg;
    TextView textUsername, textEmail, textPhone, textCountry, textState, textGender, addFriendText;
    LinearLayout callAFriend, addFriend;
    ProgressBar progressBar;
    List<String> list;
    int reqId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkVersion();
        setContentView(R.layout.activity_friends_profile);
        toolbar = findViewById(R.id.friends_profile_toolbar);
        profileImg = findViewById(R.id.friends_profile_img_view);
        textUsername = findViewById(R.id.friends_profile_text_username);
        textEmail = findViewById(R.id.friends_profile_text_email);
        textPhone = findViewById(R.id.friends_profile_text_phone);
        textCountry = findViewById(R.id.friends_profile_text_nationality);
        textState = findViewById(R.id.friends_profile_text_state);
        textGender = findViewById(R.id.friends_profile_text_gender);
        callAFriend = findViewById(R.id.friends_profile_call_a_friend);
        addFriend = findViewById(R.id.friends_profile_add_friends_layout);
        addFriendImg = findViewById(R.id.friends_profile_add_friends_img_view);
        addFriendText = findViewById(R.id.friends_profile_add_friends_text_view);
        progressBar = findViewById(R.id.friends_profile_add_friends_progress_bar);

        list = new ArrayList<>();
        reqId = 0;

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        supportPostponeEnterTransition();
        fullname = getIntent().getStringExtra("fullname");
        photo = getIntent().getStringExtra("photo");
        username = getIntent().getStringExtra("username");
        email = getIntent().getStringExtra("email");
        phone = getIntent().getStringExtra("phone");
        country = getIntent().getStringExtra("country");
        state = getIntent().getStringExtra("state");
        gender = getIntent().getStringExtra("gender");
        id = getIntent().getStringExtra("id");

        getSupportActionBar().setTitle(fullname);

        getFriendReq();
        addFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (addFriendText.getText().toString().equalsIgnoreCase("Add Friend"))
                    addFriends();
                else if (addFriendText.getText().toString().equalsIgnoreCase("Friends"))
                    Toast.makeText(FriendsProfile.this, "already friends", Toast.LENGTH_SHORT).show();
                else if (addFriendText.getText().toString().equalsIgnoreCase("Accept Request"))
                    acceptFriend();
                else
                    declineFriends();
            }
        });

        callAFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!TextUtils.isEmpty(phone)) {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:" + phone));
                    startActivity(intent);
                }
            }
        });

        Glide.with(this)
                .load("https://www.catholix.com.ng/files/images/profilepics/" + photo)
                .dontAnimate()
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        supportStartPostponedEnterTransition();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        supportStartPostponedEnterTransition();
                        return false;
                    }
                })
                .into(profileImg);

        textUsername.setText(username);
        textEmail.setText("Email : " + email);
        textPhone.setText("Phone : " + phone);
        textCountry.setText("Country : " + country);
        textState.setText("State : " + state);
        textGender.setText("Gender : " + gender);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Chat coming soon!", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    private void acceptFriend() {
        if (NetworkConfig.getInstance(this).networkAvailable()) {
            addFriend.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);
            list.clear();
            HashMap<String, Object> map = new HashMap<>();
            map.put("req", "accept_friend_request");
            map.put("frndReqId", reqId);
            JSONObject params = new JSONObject(map);
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, "https://catholix.com.ng/api.developer/POST/req.php", params,
                    response -> {
                        try {
                            if(response.getString("message").equalsIgnoreCase("Friend request accepted")){
                                Toast.makeText(this, response.getString("message"), Toast.LENGTH_SHORT).show();
                                addFriendText.setText("Friends");
                                addFriendImg.setImageResource(R.drawable.ic_supervisor_account_black_24dp);
                                addFriend.setVisibility(View.VISIBLE);
                                progressBar.setVisibility(View.INVISIBLE);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> {
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
            Toast.makeText(this, "no internet connection", Toast.LENGTH_SHORT).show();
        }
    }

    private void declineFriends() {
        if (NetworkConfig.getInstance(this).networkAvailable()) {
            list.clear();
            addFriend.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);
            HashMap<String, Object> map = new HashMap<>();
            map.put("req", "decline_friend_request");
            map.put("frndReqId", reqId);
            JSONObject params = new JSONObject(map);
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, "https://catholix.com.ng/api.developer/POST/req.php", params,
                    response -> {
                        try {
                            if(response.getString("message").equalsIgnoreCase("Friend request declined")){
                                Toast.makeText(this, "request canceled", Toast.LENGTH_SHORT).show();
                                addFriendText.setText("Add Friend");
                                addFriend.setVisibility(View.VISIBLE);
                                progressBar.setVisibility(View.INVISIBLE);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> {
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
            Toast.makeText(this, "no internet connection", Toast.LENGTH_SHORT).show();
        }
    }

    private void addFriends() {

        if (NetworkConfig.getInstance(this).networkAvailable()) {
            list.clear();
            addFriend.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);
            HashMap<String, Object> map = new HashMap<>();
            map.put("req", "send_friend_request");
            map.put("senderID", Integer.parseInt(SharedPref.getInstance(this).getId()));
            map.put("receiverID", Integer.parseInt(id));
            JSONObject params = new JSONObject(map);
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, "https://catholix.com.ng/api.developer/POST/req.php", params,
                    response -> {
                        try {
                            if(response.getString("message").equalsIgnoreCase("Request sent")){
                                Toast.makeText(this, response.getString("message"), Toast.LENGTH_SHORT).show();
                                addFriendText.setText("Cancel Request");
                                addFriend.setVisibility(View.VISIBLE);
                                progressBar.setVisibility(View.INVISIBLE);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> {
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
            Toast.makeText(this, "no internet connection", Toast.LENGTH_SHORT).show();
        }
    }

    private void getFriendReq() {
        if (NetworkConfig.getInstance(this).networkAvailable()) {
            list.clear();
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
                                getAcceptReq();
                            } else {
                                for (int i = 0; i < array.length(); i++) {
                                    list.add(array.getJSONObject(i).getString("sndID"));
                                    if (i == array.length() - 1) {
                                        if (list.contains(id)) {
                                            reqId = Integer.parseInt(array.getJSONObject(i).getString("id"));
                                            addFriendText.setText("Accept Request");
                                            addFriend.setVisibility(View.VISIBLE);
                                            progressBar.setVisibility(View.INVISIBLE);
                                        } else {
                                            getAcceptReq();
                                        }
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> {
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
            Toast.makeText(this, "no internet connection", Toast.LENGTH_SHORT).show();
        }
    }

    private void getFriends() {
        if (NetworkConfig.getInstance(this).networkAvailable()) {
            list.clear();
            HashMap<String, Object> map = new HashMap<>();
            map.put("req", "friend_list");
            map.put("userID", Integer.parseInt(SharedPref.getInstance(this).getId()));
            map.put("lim", 40);
            JSONObject params = new JSONObject(map);
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, "https://catholix.com.ng/api.developer/GET/reqq.php", params,
                    response -> {
                        try {
                            JSONArray array = response.getJSONArray("data");
                            if (array.length() == 0) {
                                addFriendText.setText("Add Friend");
                                addFriend.setVisibility(View.VISIBLE);
                                progressBar.setVisibility(View.INVISIBLE);
                            } else {
                                for (int i = 0; i < array.length(); i++) {
                                    list.add(Integer.toString(array.getInt(i)));
                                    if (i == array.length() - 1) {
                                        if (list.contains(id)) {
                                            addFriendText.setText("Friends");
                                            addFriendImg.setImageResource(R.drawable.ic_supervisor_account_black_24dp);
                                            addFriend.setVisibility(View.VISIBLE);
                                            progressBar.setVisibility(View.INVISIBLE);
                                        } else {
                                            addFriendText.setText("Add Friend");
                                            addFriend.setVisibility(View.VISIBLE);
                                            progressBar.setVisibility(View.INVISIBLE);
                                        }
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> {
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
            Toast.makeText(this, "no internet connection", Toast.LENGTH_SHORT).show();
        }
    }

    private void getAcceptReq() {
        if (NetworkConfig.getInstance(this).networkAvailable()) {
            list.clear();
            HashMap<String, Object> map = new HashMap<>();
            map.put("req", "friend_requests");
            map.put("userID", Integer.parseInt(id));
            map.put("lim", 40);
            JSONObject params = new JSONObject(map);
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, "https://catholix.com.ng/api.developer/GET/reqq.php", params,
                    response -> {
                        try {
                            JSONArray array = response.getJSONArray("data");
                            if (array.length() == 0) {
                                getFriends();
                            } else {
                                for (int i = 0; i < array.length(); i++) {
                                    list.add(array.getJSONObject(i).getString("sndID"));
                                    if (i == array.length() - 1) {
                                        if (list.contains(SharedPref.getInstance(this).getId())) {
                                            reqId = Integer.parseInt(array.getJSONObject(i).getString("id"));
                                            addFriendText.setText("Cancel Request");
                                            addFriend.setVisibility(View.VISIBLE);
                                            progressBar.setVisibility(View.INVISIBLE);
                                        } else {
                                            getFriends();
                                        }
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> {
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
            Toast.makeText(this, "no internet connection", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkVersion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.setStatusBarColor(Color.TRANSPARENT);
        }
    }

}
