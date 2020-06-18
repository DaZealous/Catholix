package www.catholix.com.ng;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Service.UserService;
import adapter.AddNewUserAdapter;
import api.RetrofitClient;
import config.SharedPref;
import model.Users;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import view.SelectUserView;

public class AddNewMember extends AppCompatActivity implements SelectUserView {

    String chat_id, username, userID, otherUsername;
    Button btnDone;
    RecyclerView recyclerView;
    Toolbar toolbar;
    ArrayList<String> groupUsers;
    List<Users> users;
    List<String> userIDs;
    AddNewUserAdapter adapter;
    ProgressDialog dialog;
    DatabaseReference rootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_member);

        chat_id = getIntent().getStringExtra("id");
        otherUsername = getIntent().getStringExtra("username");
        groupUsers = getIntent().getStringArrayListExtra("userIDs");
        assert groupUsers != null;
        userIDs.addAll(groupUsers);

        users = new ArrayList<>();
        userIDs = new ArrayList<>();
        adapter = new AddNewUserAdapter(this, users, this);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);

        btnDone = findViewById(R.id.activity_add_new_member_done_btn);
        recyclerView = findViewById(R.id.activity_add_new_member_recycler_view);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        supportPostponeEnterTransition();
        getSupportActionBar().setTitle(otherUsername);

        dialog = new ProgressDialog(this);
        dialog.setTitle("Adding....");
        dialog.setMessage("Please wait");
        dialog.setCancelable(false);

        userID = SharedPref.getInstance(this).getId();
        username = SharedPref.getInstance(this).getUser();

        rootRef = FirebaseDatabase.getInstance().getReference();

        loadUsers();

        btnDone.setOnClickListener(view -> {
            if(!userIDs.isEmpty()){
                addUsers();
            }
        });
    }

    private void addUsers() {
            dialog.setTitle("Adding Members");

            rootRef.child("Users").child(chat_id).child("members").setValue(Arrays.asList(userIDs.toArray()));

        String current_user_ref = "messages/" + userID+ "/" + chat_id;
        String chat_user_ref = "messages/" + chat_id;

        DatabaseReference user_message_push = rootRef.child("messages")
                .child(userID).child(chat_id).push();

        final String push_id = user_message_push.getKey();

        Map messageMap = new HashMap();
        messageMap.put("msg_body", " added new members");
        messageMap.put("key", push_id);
        messageMap.put("seen", false);
        messageMap.put("isRead", false);
        messageMap.put("msg_type", "new_member");
        messageMap.put("time_stamp", ServerValue.TIMESTAMP);
        messageMap.put("from", userID);
        messageMap.put("to", chat_id);
        messageMap.put("msg_name", "you added new members");
        messageMap.put("fromUsername", username);
        messageMap.put("toUsername", otherUsername);

        Map messageUserMap = new HashMap();
        messageUserMap.put(current_user_ref + "/" + push_id, messageMap);
        messageUserMap.put(chat_user_ref + "/" + push_id, messageMap);

        rootRef.child("Chat").child(userID).child(chat_id).child("seen").setValue(true);

        rootRef.child("Chat").child(userID).child(chat_id).child("time_stamp").setValue(ServerValue.TIMESTAMP);

        rootRef.child("Chat").child(chat_id).child(userID).child("seen").setValue(false);
        rootRef.child("Chat").child(chat_id).child(userID).child("time_stamp").setValue(ServerValue.TIMESTAMP);


        rootRef.updateChildren(messageUserMap, (databaseError, databaseReference) -> {

            if (databaseError == null) {

                rootRef.child("messages").child(userID).child(chat_id).child(push_id).child("seen").setValue(true);
                rootRef.child("messages").child(userID).child(chat_id).child(push_id).child("isRead").setValue(true);

                finish();
            }

        });

    }


    private void loadUsers() {
        users.clear();
        Toast.makeText(this, "fetching...", Toast.LENGTH_SHORT).show();
        if (isInternetAvailable(this)) {
            UserService service = RetrofitClient.getInstance().create(UserService.class);
            service.getUsers().enqueue(new Callback<List<Users>>() {
                @Override
                public void onResponse(Call<List<Users>> call, Response<List<Users>> response) {
                    for (Users user : response.body()) {
                        if (!groupUsers.contains(user.getUserId()))
                            users.add(user);
                    }
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onFailure(Call<List<Users>> call, Throwable t) {
                    Toast.makeText(AddNewMember.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "no internet connection", Toast.LENGTH_SHORT).show();
        }
    }

    private static boolean isInternetAvailable(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return manager.getActiveNetworkInfo() != null
                && manager.getActiveNetworkInfo().isAvailable()
                && manager.getActiveNetworkInfo().isConnectedOrConnecting();
    }

    @Override
    public void selectUser(String id) {
        userIDs.add(id);
    }

    @Override
    public void removeUser(String id) {
        userIDs.remove(id);
    }

    @Override
    public String getUser(int position) {
        return userIDs.get(position);
    }

    @Override
    public List<String> getUsers() {
        return userIDs;
    }
}
