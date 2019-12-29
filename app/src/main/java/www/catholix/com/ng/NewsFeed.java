package www.catholix.com.ng;


import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import Service.FeedsService;
import adapter.FeedsAdapter;
import api.RetrofitClient;
import config.SharedPref;
import de.hdodenhof.circleimageview.CircleImageView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import model.Feeds;


public class NewsFeed extends Fragment implements SwipeRefreshLayout.OnRefreshListener{

    private FeedsService feedsService;
    private List<Feeds> list;
    private CompositeDisposable disposable;
    private RecyclerView recyclerView;
    private FeedsAdapter adapter;
    private SwipeRefreshLayout layout;
    private CircleImageView profileImage;
    private String image;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_news_feed, container, false);
        recyclerView = view.findViewById(R.id.news_feed_recycler_view);
        profileImage = view.findViewById(R.id.news_feed_profile_image_view);
        layout = view.findViewById(R.id.news_feed_swipe_refresh);
        image = SharedPref.getInstance(getContext()).getImage();
        Glide.with(getContext()).load("https://www.catholix.com.ng/files/images/profilepics/"+image).placeholder(R.drawable.ic_person_profile_24dp).into(profileImage);
        layout.setOnRefreshListener(this);
        layout.setRefreshing(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
        list = new ArrayList<>();
        adapter = new FeedsAdapter(getContext(), list);
        recyclerView.setAdapter(adapter);
        feedsService = RetrofitClient.getInstance().create(FeedsService.class);
        disposable = new CompositeDisposable();
        loadData();
        return view;

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
            if(isInternetAvailable(getContext())) {
                disposable.add(
                        feedsService.getNewsFeeds()
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Consumer<List<Feeds>>() {
                                    @Override
                                    public void accept(List<Feeds> feeds) throws Exception {
                                        list.addAll(feeds);
                                        adapter.notifyDataSetChanged();
                                        layout.setRefreshing(false);
                                    }
                                }));
            }else{
                Toast.makeText(getContext(), "No internet connection", Toast.LENGTH_SHORT).show();
                layout.setRefreshing(false);
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private static boolean isInternetAvailable(Context context){
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return manager.getActiveNetworkInfo() != null
                && manager.getActiveNetworkInfo().isAvailable()
                && manager.getActiveNetworkInfo().isConnectedOrConnecting();
    }
}
