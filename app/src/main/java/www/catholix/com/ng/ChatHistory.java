package www.catholix.com.ng;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import adapter.ChatHistoryAdapter;
import config.SharedPref;
import model.ChatHistoryModel;
import model.Conv;

public class ChatHistory extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener{

    Toolbar toolbar;
    List<String> list;
    List<Conv> convs;
    ChatHistoryAdapter adapter;
    SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView mConvList;

    private DatabaseReference mConvDatabase;
    private DatabaseReference mMessageDatabase;
    private DatabaseReference mUsersDatabase;
    private String mCurrent_user_id;
    FloatingActionButton floatingActionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_history);
        toolbar = findViewById(R.id.toolbar);
        floatingActionButton = findViewById(R.id.activity_chat_history_fab);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle(R.string.app_name);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        mCurrent_user_id = SharedPref.getInstance(this).getId();

        swipeRefreshLayout = findViewById(R.id.activity_chat_history_swipe_refresh);
        mConvList = findViewById(R.id.activity_chat_history_recycler_view);
        swipeRefreshLayout.setOnRefreshListener(this);

        mConvDatabase = FirebaseDatabase.getInstance().getReference().child("Chat").child(mCurrent_user_id);

        mConvDatabase.keepSynced(true);
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mMessageDatabase = FirebaseDatabase.getInstance().getReference().child("messages").child(mCurrent_user_id);
        mUsersDatabase.keepSynced(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        list = new ArrayList<>();
        convs = new ArrayList<>();
        mConvList.setHasFixedSize(true);
        mConvList.setLayoutManager(linearLayoutManager);
        adapter = new ChatHistoryAdapter(list, convs,this);
        mConvList.setAdapter(adapter);

        floatingActionButton.setOnClickListener(view ->
            startActivity(new Intent(this, UsersActivity.class))
        );
        loadHistory();
    }

    protected void loadHistory() {
        swipeRefreshLayout.setRefreshing(true);
        list.clear();
        convs.clear();
        Query conversationQuery = mConvDatabase.orderByChild("time_stamp");

        /*conversationQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.hasChildren()){
                    if(dataSnapshot.child("time_stamp").getValue(Long.class) != null) {
                        list.add(dataSnapshot.getKey());
                        convs.add(dataSnapshot.getValue(Conv.class));
                        swipeRefreshLayout.setRefreshing(false);
                        adapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });*/
        conversationQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    list.clear();
                    convs.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            if (snapshot.child("time_stamp").getValue(Long.class) != null) {
                                list.add(snapshot.getKey());
                                convs.add(snapshot.getValue(Conv.class));
                    }
                    }
                    swipeRefreshLayout.setRefreshing(false);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                swipeRefreshLayout.setRefreshing(false);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat_history_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRefresh() {
       loadHistory();
    }

}
