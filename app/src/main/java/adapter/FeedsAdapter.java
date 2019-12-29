package adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import api.VolleyInstance;
import de.hdodenhof.circleimageview.CircleImageView;
import model.Feeds;
import www.catholix.com.ng.MainActivity;
import www.catholix.com.ng.PostView;
import www.catholix.com.ng.R;

public class FeedsAdapter extends RecyclerView.Adapter<FeedsAdapter.viewHolder>{

    private Context context;
    private List<Feeds> feeds;

    public FeedsAdapter(Context context, List<Feeds> feeds) {
        this.context = context;
        this.feeds = feeds;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.feeds_layout, parent, false);
        return new FeedsAdapter.viewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        final Feeds feed = feeds.get(position);
        holder.textUsername.setText(feed.getFname());
        holder.textTime.setText(feed.getAddedOn());
        holder.textCaption.setText(android.text.Html.fromHtml(feed.getMessage()));
        if(feed.getPhoto() == null){
            holder.feedImage.setVisibility(View.GONE);
        }else {
            Glide.with(context).load("https://www.catholix.com.ng/files/images/feeds/" + feed.getPhoto())
                    .apply(new RequestOptions().override(500, 500)).into(holder.feedImage);
            holder.feedImage.setVisibility(View.VISIBLE);
        }
       holder.getData(feed.getUserId());
        holder.textContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context.startActivity(new Intent(context, PostView.class)
                        .putExtra("userId", feed.getUserId())
                .putExtra("username", feed.getFname())
                .putExtra("caption", feed.getMessage())
                .putExtra("time", feed.getAddedOn())
                .putExtra("image", feed.getPhoto()));
            }
        });
    }

    @Override
    public int getItemCount() {
        return feeds.size();
    }

    class viewHolder extends RecyclerView.ViewHolder{

        TextView textUsername;
        TextView textTime;
        CircleImageView imgProfile;
        TextView textCaption;
        ImageView feedImage;
        TextView textContinue;
        private viewHolder(@NonNull View itemView) {
            super(itemView);
            textUsername = itemView.findViewById(R.id.feeds_layout_username);
            textTime = itemView.findViewById(R.id.feeds_layout_time);
            textCaption = itemView.findViewById(R.id.feeds_layout_post_caption);
            imgProfile = itemView.findViewById(R.id.feeds_layout_image_profile);
            feedImage = itemView.findViewById(R.id.feeds_layout_post_image);
            textContinue = itemView.findViewById(R.id.feeds_layout_continue_text);
        }

        private void getData(String id) {
            try {
                JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, "https://www.catholix.com.ng/api.developer/GET/req.php?qdata=id&id="+id+"&table=users",
                        null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject object) {
                                try {
                                    Glide.with(context).load("https://www.catholix.com.ng/files/images/profilepics/"+object.getString("Photo")).placeholder(R.drawable.ic_person_profile_24dp).into(imgProfile);
                                } catch (JSONException e) {
                                  //  Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }

                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                               // Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                VolleyInstance.getInstance(context).addToQueue(request);
            } catch (Exception e) {
                //Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }

    }
}
