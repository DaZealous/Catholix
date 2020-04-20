package adapter;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

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

import java.io.File;
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
        Glide.with(context).clear(holder.imgProfile);
        Glide.with(context).clear(holder.feedImage);
        if(feed.getPhoto() == null){
            holder.feedImage.setVisibility(View.GONE);
            holder.videoView.setVisibility(View.GONE);
        }else {
            if(holder.isVideo(feed.getPhoto())){
                holder.feedImage.setVisibility(View.GONE);
                holder.videoView.setVisibility(View.VISIBLE);
                holder.playVideo("https://www.catholix.com.ng/files/images/feeds/" + feed.getPhoto());
                }else {
                Glide.with(context).load("https://www.catholix.com.ng/files/images/feeds/" + feed.getPhoto())
                        .apply(new RequestOptions().override(500, 500)).into(holder.feedImage);
                holder.feedImage.setVisibility(View.VISIBLE);
                holder.videoView.setVisibility(View.GONE);

            }
        }
       holder.getData(feed.getUserId());
        holder.textContinue.setOnClickListener(view -> context.startActivity(new Intent(context, PostView.class)
                .putExtra("userId", feed.getUserId())
        .putExtra("username", feed.getFname())
        .putExtra("caption", feed.getMessage())
        .putExtra("time", feed.getAddedOn())
        .putExtra("image", feed.getPhoto())));
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
        VideoView videoView;
        private viewHolder(@NonNull View itemView) {
            super(itemView);
            textUsername = itemView.findViewById(R.id.feeds_layout_username);
            textTime = itemView.findViewById(R.id.feeds_layout_time);
            textCaption = itemView.findViewById(R.id.feeds_layout_post_caption);
            imgProfile = itemView.findViewById(R.id.feeds_layout_image_profile);
            feedImage = itemView.findViewById(R.id.feeds_layout_post_image);
            textContinue = itemView.findViewById(R.id.feeds_layout_continue_text);
            videoView = itemView.findViewById(R.id.feeds_layout_videos_viewer);
        }

        private boolean isImage(String file) {
            return file.endsWith(".jpg") ||
                    file.endsWith(".png") ||
                    file.endsWith(".gif") ||
                    file.endsWith(".jpeg") ||
                    file.endsWith("jpg") ||
                    file.endsWith("png") ||
                    file.endsWith("gif") ||
                    file.endsWith("jpeg");
        }



        private void getData(String id) {
            try {
                JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, "https://www.catholix.com.ng/api.developer/GET/req.php?qdata=id&id="+id+"&table=users",
                        null,
                        object -> {
                            try {
                                Glide.with(context).clear(imgProfile);
                                Glide.with(context).load("https://www.catholix.com.ng/files/images/profilepics/"+object.getString("Photo")).placeholder(R.drawable.ic_person_profile_24dp).into(imgProfile);
                            } catch (JSONException e) {
                              //  Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }

                        },
                        error -> {
                           // Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                VolleyInstance.getInstance(context).addToQueue(request);
            } catch (Exception e) {
                //Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }

        private void playVideo(String photo) {
            //Toast.makeText(context, photo, Toast.LENGTH_SHORT).show();
            videoView.setVideoURI(Uri.parse(photo));
           // MediaController controller = new MediaController(context);
            //controller.setAnchorView(videoView);
                //controller.setVisibility(View.INVISIBLE);
            //videoView.setMediaController(controller);
            videoView.start();
            //videoView.setOnPreparedListener(mediaPlayer -> {
              //  videoView.seekTo(0);
                //    videoView.start();
            //});
        }

        private boolean isVideo(String photo) {
            return photo.endsWith(".mp4");
        }
    }
}
