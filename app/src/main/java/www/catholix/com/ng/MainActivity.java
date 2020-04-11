package www.catholix.com.ng;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import api.VolleyInstance;
import config.SharedPref;
import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    Toolbar toolbar;
    DrawerLayout layout;
    NavigationView navigationView;
    ActionBarDrawerToggle toggle;
    String email, name, image;
    TextView navTitle;
    CircleImageView profileImage;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        email = SharedPref.getInstance(this).getEmail();
        name = SharedPref.getInstance(this).getUser();
        image = SharedPref.getInstance(this).getImage();
        layout = findViewById(R.id.main_drawer_layout);
        navigationView = findViewById(R.id.main_navigation_view);
        navigationView.setNavigationItemSelectedListener(this);
        toggle = new ActionBarDrawerToggle(this, layout, R.string.open, R.string.close);
        layout.addDrawerListener(toggle);
        toggle.syncState();
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getData();
        getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment_navigation, new NewsFeed()).commit();

        //FirebaseDatabase.getInstance().getReference().child("Users").child(SharedPref.getInstance(this).getId()).child("online").setValue("true");
        FirebaseDatabase.getInstance().getReference().child("Users").child(SharedPref.getInstance(this).getId()).child("online").onDisconnect().setValue(System.currentTimeMillis());

        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                String token = task.getResult().getToken();

                Map chatAddMap = new HashMap();
                chatAddMap.put("device_token", token);
                chatAddMap.put("img_url", "https://www.catholix.com.ng/files/images/profilepics/"+SharedPref.getInstance(MainActivity.this).getImage());
                chatAddMap.put("username", SharedPref.getInstance(MainActivity.this).getUser());
                chatAddMap.put("online", "true");

                Map chatUserMap = new HashMap();
                chatUserMap.put("Users/" + SharedPref.getInstance(MainActivity.this).getId(), chatAddMap);

                FirebaseDatabase.getInstance().getReference().updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                    }
                });
            }
        });

        layout.setDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
                navTitle = findViewById(R.id.nav_header_username);
                profileImage = findViewById(R.id.nav_header_image_profile);
                navTitle.setText(name);
                Glide.with(MainActivity.this).load("https://www.catholix.com.ng/files/images/profilepics/"+image).placeholder(R.drawable.ic_person_profile_24dp).into(profileImage);
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {

            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });

    }

    private void getData() {
        try {
            if(isInternetAvailable(this)) {
                JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, "https://www.catholix.com.ng/api.developer/GET/req.php?qdata=more&table=users&dataz=email&valuez=" + email,
                        null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject object) {
                                try {
                                    name = object.getString("Fname") + " " + object.getString("Sname");
                                    image = object.getString("Photo");
                                    SharedPref.getInstance(MainActivity.this).addPhoto(image);
                                } catch (JSONException e) {
                                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                VolleyInstance.getInstance(this).addToQueue(request);
            }
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private static boolean isInternetAvailable(Context context){
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return manager.getActiveNetworkInfo() != null
                && manager.getActiveNetworkInfo().isAvailable()
                && manager.getActiveNetworkInfo().isConnectedOrConnecting();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.nav_menu_news_feed:
                getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment_navigation, new NewsFeed()).commit();
                layout.closeDrawer(GravityCompat.START);
                return true;
            case R.id.nav_menu_chats:
                startActivity(new Intent(this, ChatHistory.class));
                return false;
            case R.id.nav_menu_profile:
                return false;
            case R.id.nav_menu_settings:
                return false;
            case R.id.nav_menu_logout:
                layout.closeDrawer(GravityCompat.START);
                FirebaseDatabase.getInstance().getReference().child("Users").child(SharedPref.getInstance(this).getId()).child("online").setValue(System.currentTimeMillis());
                FirebaseDatabase.getInstance().getReference().child("Users").child(SharedPref.getInstance(this).getId()).child("device_token").setValue("");
                SharedPref.getInstance(this).removeUser();
                startActivity(new Intent(this, WelcomeActivity.class));
                finish();
                return true;
            case R.id.nav_menu_users:
                startActivity(new Intent(this, UsersActivity.class));
                layout.closeDrawer(GravityCompat.START);
                return true;
                default:
                    return false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(toggle.onOptionsItemSelected(item)){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if(layout.isDrawerOpen(GravityCompat.START)){
            layout.closeDrawer(GravityCompat.START);
        }
        else
            super.onBackPressed();
    }

}
