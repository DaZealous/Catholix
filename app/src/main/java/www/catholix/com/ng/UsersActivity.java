package www.catholix.com.ng;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import Service.UserService;
import adapter.UsersAdapter;
import api.RetrofitClient;
import config.SharedPref;
import model.Users;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UsersActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    SwipeRefreshLayout swipeRefreshLayout;
    RecyclerView recyclerView;
    UsersAdapter adapter;
    List<Users> list;
    UserService service;
    EditText searchEdit;
    ImageButton btnBack, btnClear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);
        swipeRefreshLayout = findViewById(R.id.users_activity_swipe_refresh);
        recyclerView = findViewById(R.id.users_activity_recycler_view);
        searchEdit = findViewById(R.id.activity_user_search_edit);
        btnBack = findViewById(R.id.activity_user_back_btn);
        btnClear = findViewById(R.id.activity_user_clear_search);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        list = new ArrayList<>();
        adapter = new UsersAdapter(this, list);
        recyclerView.setAdapter(adapter);

        btnClear.setOnClickListener(view -> {
            searchEdit.setText("");
            btnClear.setVisibility(View.INVISIBLE);
        });

        btnBack.setOnClickListener(view -> onBackPressed());

        searchEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!searchEdit.getText().toString().isEmpty())
                    btnClear.setVisibility(View.VISIBLE);
                else
                    btnClear.setVisibility(View.INVISIBLE);



            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        loadUsers();
    }

    private void loadUsers() {
        list.clear();
        swipeRefreshLayout.setRefreshing(true);
        if(isInternetAvailable(this)){
            service = RetrofitClient.getInstance().create(UserService.class);
            service.getUsers().enqueue(new Callback<List<Users>>(){
                @Override
                public void onResponse(Call<List<Users>> call, Response<List<Users>> response) {
                    for(Users users : response.body()){
                        if(!users.getUserId().equals(SharedPref.getInstance(UsersActivity.this).getId()))
                            list.add(users);
                    }
                    adapter.notifyDataSetChanged();
                    swipeRefreshLayout.setRefreshing(false);
                }

                @Override
                public void onFailure(Call<List<Users>> call, Throwable t) {
                    swipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(UsersActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }else{
            swipeRefreshLayout.setRefreshing(false);
            Toast.makeText(this, "no internet connection", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRefresh() {
        if(service.getUsers() != null)
        service.getUsers().cancel();
        loadUsers();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.home){
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private static boolean isInternetAvailable(Context context){
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return manager.getActiveNetworkInfo() != null
                && manager.getActiveNetworkInfo().isAvailable()
                && manager.getActiveNetworkInfo().isConnectedOrConnecting();
    }
}
