package www.catholix.com.ng;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_viewer);

        textUsername = findViewById(R.id.activity_file_viewer_text_username);
        userImg = findViewById(R.id.activity_file_viewer_user_img);
        textTime = findViewById(R.id.activity_file_viewer_text_time);
        backBtn = findViewById(R.id.activity_file_viewer_back_btn);
        imageView = findViewById(R.id.activity_file_viewer_img_view);
        username = getIntent().getStringExtra("username");
        imgUrl = getIntent().getStringExtra("img_url");
        time = getIntent().getLongExtra("time", 0);
        thumb_byte = getIntent().getByteArrayExtra("photo");

        textUsername.setText(username);
        textTime.setText(DateUtils.getRelativeTimeSpanString(time));

        Glide.with(this).load("https://www.catholix.com.ng/files/images/profilepics/" + imgUrl).placeholder(R.drawable.ic_person_profile_24dp).into(userImg);

        //Glide.with(this).load(thumb_byte).placeholder(R.drawable.ic_person_profile_24dp).into(imageView);

        imageView.setImageBitmap(BitmapFactory.decodeByteArray(thumb_byte, 0, thumb_byte.length));

        backBtn.setOnClickListener(view -> onBackPressed());

    }

}
