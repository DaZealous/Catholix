package www.catholix.com.ng;


import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Service.FeedsService;
import adapter.FeedsAdapter;
import api.RetrofitClient;
import api.VolleyInstance;
import config.NetworkConfig;
import config.SharedPref;
import de.hdodenhof.circleimageview.CircleImageView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import model.Feeds;
import view.PostView;


public class NewsFeed extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private FeedsService feedsService;
    private List<Feeds> list;
    private CompositeDisposable disposable;
    private RecyclerView recyclerView;
    private FeedsAdapter adapter;
    private SwipeRefreshLayout layout;
    private CircleImageView profileImage;
    private String image;
    private TextView youtMindBtn, notifyText;
    private PostFragment fragment;
    private RelativeLayout notificationBtn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_news_feed, container, false);
        recyclerView = view.findViewById(R.id.news_feed_recycler_view);
        profileImage = view.findViewById(R.id.news_feed_profile_image_view);
        youtMindBtn = view.findViewById(R.id.btn_what_is_on_your_mind_news_feed);
        layout = view.findViewById(R.id.news_feed_swipe_refresh);
        notifyText = view.findViewById(R.id.fragment_news_feed_notification_text);
        notificationBtn = view.findViewById(R.id.fragment_news_feed_notification_layout);
        image = SharedPref.getInstance(getContext()).getImage();
        Glide.with(getContext()).load("https://www.catholix.com.ng/files/images/profilepics/" + image).placeholder(R.drawable.ic_person_profile_24dp).into(profileImage);
        layout.setOnRefreshListener(this);
        layout.setRefreshing(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
        list = new ArrayList<>();
        adapter = new FeedsAdapter(getContext(), list);
        recyclerView.setAdapter(adapter);
        feedsService = RetrofitClient.getInstance().create(FeedsService.class);
        disposable = new CompositeDisposable();

        notificationBtn.setOnClickListener(view12 -> {
            startActivity(new Intent(getContext(), NotificationActivity.class));
        });
        youtMindBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSheet();
            }
        });

        getFriendReq();
        loadData();
        return view;
    }

    private void getFriendReq() {
        if (NetworkConfig.getInstance(getContext()).networkAvailable()) {
            try{
            HashMap<String, Object> map = new HashMap<>();
            map.put("req", "friend_requests");
            map.put("userID", Integer.parseInt(SharedPref.getInstance(getContext()).getId()));
            map.put("lim", 40);
            JSONObject params = new JSONObject(map);
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, "https://catholix.com.ng/api.developer/GET/reqq.php", params,
                    response -> {
                        try {
                            JSONArray array = response.getJSONArray("data");
                            if (array.length() == 0) {
                                notifyText.setVisibility(View.INVISIBLE);
                            } else {
                                notifyText.setVisibility(View.VISIBLE);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> {
                        Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
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
            VolleyInstance.getInstance(getContext()).addToQueue(request);
            } catch (Exception e) {
                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getContext(), "no internet connection", Toast.LENGTH_SHORT).show();
        }
    }

    private void showSheet() {
        fragment = new PostFragment();
        assert getFragmentManager() != null;
        fragment.show(getFragmentManager(), fragment.getTag());
    }

    @Override
    public void onRefresh() {
        layout.setRefreshing(true);
        list.clear();
        loadData();
    }

    private void loadData() {
        list.clear();
        try {
            if (isInternetAvailable(getContext())) {
                disposable.add(
                        feedsService.getNewsFeeds()
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(feed -> {
                                    list.addAll(feed);
                                    adapter.notifyDataSetChanged();
                                    layout.setRefreshing(false);
                                }));
            } else {
                Toast.makeText(getContext(), "No internet connection", Toast.LENGTH_SHORT).show();
                layout.setRefreshing(false);
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private static boolean isInternetAvailable(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return manager.getActiveNetworkInfo() != null
                && manager.getActiveNetworkInfo().isAvailable()
                && manager.getActiveNetworkInfo().isConnectedOrConnecting();
    }

}
