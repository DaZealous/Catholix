package www.catholix.com.ng;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import org.json.JSONException;
import org.json.JSONObject;

import api.VolleyInstance;
import de.hdodenhof.circleimageview.CircleImageView;

public class PostView extends AppCompatActivity {

    String username, userId, caption, time, image;
    TextView textUsername, textTime, textCaption, textSeeLess, textUserTitle;
    ImageButton btnClose;
    ImageView postImage;
    CircleImageView imageProfile;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_view);
        btnClose = findViewById(R.id.post_view_btn_close);
        textUsername = findViewById(R.id.post_view_username);
        textTime = findViewById(R.id.post_view_time);
        textCaption = findViewById(R.id.post_view_post_caption);
        textSeeLess = findViewById(R.id.post_view_see_less_text);
        textUserTitle = findViewById(R.id.post_view_app_title);
        postImage = findViewById(R.id.post_view_post_image);
        imageProfile = findViewById(R.id.post_view_image_profile);
        userId = getIntent().getStringExtra("userId");
        username = getIntent().getStringExtra("username");
        time = getIntent().getStringExtra("time");
        caption = getIntent().getStringExtra("caption");
        image = getIntent().getStringExtra("image");

        getData(userId);
         Glide.with(this).load("https://www.catholix.com.ng/files/images/feeds/"+image)
                 .apply(new RequestOptions().override(400, 600)).into(postImage);

        textUsername.setText(username);
        textCaption.setText(android.text.Html.fromHtml(caption));
        textTime.setText(time);
        textUserTitle.setText(username+"'s post");

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        textSeeLess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (textSeeLess.getText().toString().equalsIgnoreCase("see less")){
                    textCaption.setMaxLines(5);
                    textSeeLess.setText("see more");
                }else{
                    textCaption.setMaxLines(1000);
                    textSeeLess.setText("see less");
                }
            }
        });
    }

    private void getData(String id) {
        try {
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, "https://www.catholix.com.ng/api.developer/GET/req.php?qdata=id&id="+id+"&table=users",
                    null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject object) {
                            try {
                                Glide.with(PostView.this).load("https://www.catholix.com.ng/files/images/profilepics/"+object.getString("Photo")).placeholder(R.drawable.ic_person_profile_24dp).into(imageProfile);
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
            VolleyInstance.getInstance(this).addToQueue(request);
        } catch (Exception e) {
            //Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
