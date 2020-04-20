package www.catholix.com.ng;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.io.File;

import config.SharedPref;
import de.hdodenhof.circleimageview.CircleImageView;

public class FileViewer extends AppCompatActivity {

    TextView textUsername, textTime;
    ImageView imageView;
    ImageButton backBtn;
    String username, imgUrl;
    Long time;
    byte[] thumb_byte;
    CircleImageView userImg;
    String type, filePath;
    File file;
    VideoView videoView;
    int position = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_viewer);

        textUsername = findViewById(R.id.activity_file_viewer_text_username);
        userImg = findViewById(R.id.activity_file_viewer_user_img);
        textTime = findViewById(R.id.activity_file_viewer_text_time);
        backBtn = findViewById(R.id.activity_file_viewer_back_btn);
        imageView = findViewById(R.id.activity_file_viewer_img_view);
        videoView = findViewById(R.id.activity_file_viewer_video_view);

        username = getIntent().getStringExtra("username");
        imgUrl = getIntent().getStringExtra("img_url");
        time = getIntent().getLongExtra("time", 0);
        type = getIntent().getStringExtra("type");

        filePath = "";

        if(type.equals("video")){
            videoView.setVisibility(View.VISIBLE);
            imageView.setVisibility(View.GONE);
            filePath = getIntent().getStringExtra("file");
            file = new File(filePath);
            playVideo();
        }
        else{
            imageView.setVisibility(View.VISIBLE);
            videoView.setVisibility(View.GONE);
            thumb_byte = getIntent().getByteArrayExtra("photo");
            imageView.setImageBitmap(BitmapFactory.decodeByteArray(thumb_byte, 0, thumb_byte.length));
        }

        textUsername.setText(username);
        textTime.setText(DateUtils.getRelativeTimeSpanString(time));

        Glide.with(this).load("https://www.catholix.com.ng/files/images/profilepics/" + imgUrl).placeholder(R.drawable.ic_person_profile_24dp).into(userImg);

        //Glide.with(this).load(thumb_byte).placeholder(R.drawable.ic_person_profile_24dp).into(imageView);



        backBtn.setOnClickListener(view -> onBackPressed());

    }

    private void playVideo() {
        videoView.setVideoPath(filePath);
        videoView.start();

        MediaController controller = new MediaController(this);
        videoView.setMediaController(controller);
        videoView.setOnPreparedListener(mediaPlayer -> {
            videoView.seekTo(position);
            if (position == 0) {
                videoView.start();
            } else {
                videoView.pause();
            }
        });
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState, @NonNull PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        if(!TextUtils.isEmpty(filePath)) {
            outState.putInt("position", videoView.getCurrentPosition());
            videoView.pause();
        }
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if(!TextUtils.isEmpty(filePath)) {
            position = savedInstanceState.getInt("position");
            videoView.seekTo(position);
            videoView.resume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(!TextUtils.isEmpty(filePath))
        videoView.pause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(!TextUtils.isEmpty(filePath))
        videoView.stopPlayback();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!TextUtils.isEmpty(filePath)) {
            videoView.seekTo(position);
            if (position == 0) {
                videoView.start();
            } else {
                videoView.resume();
            }
        }
    }
}
