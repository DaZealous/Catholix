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
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import adapter.ChatHistoryAdapter;
import config.SharedPref;
import model.ChatHistoryModel;
import model.Conv;

public class ChatHistory extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

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
    DataSnapshot snapshot;

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
        adapter = new ChatHistoryAdapter(list, convs, this);
        mConvList.setAdapter(adapter);

        floatingActionButton.setOnClickListener(view ->
                startActivity(new Intent(this, UsersActivity.class))
        );

    }

    @Override
    protected void onResume() {
        super.onResume();
        list.clear();
        convs.clear();
        loadHistory();
    }

    protected void loadHistory() {
        swipeRefreshLayout.setRefreshing(true);
        list.clear();
        convs.clear();

        Query conversationQuery = mConvDatabase.orderByChild("time_stamp");

        conversationQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot1) {
                if (dataSnapshot1.hasChildren()) {
                    list.clear();
                    convs.clear();
                    mMessageDatabase.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot2) {
                            for (DataSnapshot snap : dataSnapshot1.getChildren()) {
                                if (snap.getKey().matches("\\d+")) {
                                    if(dataSnapshot2.child(Objects.requireNonNull(snap.getKey())).exists())
                                    if (snap.child("time_stamp").getValue(Long.class) != null) {
                                        list.add(snap.getKey());
                                        convs.add(snap.getValue(Conv.class));
                                    }
                                }else{
                                    if (snap.child("time_stamp").getValue(Long.class) != null) {
                                        list.add(snap.getKey());
                                        convs.add(snap.getValue(Conv.class));
                                    }
                                }
                            }
                            swipeRefreshLayout.setRefreshing(false);
                            adapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
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
        int id = item.getItemId();
        switch (id) {
            case R.id.chat_history_create_group_id:
                createGroup();
                return true;
            default:
                return false;
        }
    }

    private void createGroup() {
        startActivity(new Intent(this, CreateGroup.class));
    }

    @Override
    public void onRefresh() {
        loadHistory();
    }


}
