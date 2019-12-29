package www.catholix.com.ng;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

public class FriendsProfile extends AppCompatActivity {

    Toolbar toolbar;
    String fullname, photo, username, email, phone, country, state, gender;
    ImageView profileImg;
    TextView textUsername, textEmail, textPhone, textCountry, textState, textGender;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkVersion();
        setContentView(R.layout.activity_friends_profile);
        toolbar = findViewById(R.id.friends_profile_toolbar);
        profileImg = findViewById(R.id.friends_profile_img_view);
        textUsername = findViewById(R.id.friends_profile_text_username);
        textEmail = findViewById(R.id.friends_profile_text_email);
        textPhone = findViewById(R.id.friends_profile_text_phone);
        textCountry = findViewById(R.id.friends_profile_text_nationality);
        textState = findViewById(R.id.friends_profile_text_state);
        textGender = findViewById(R.id.friends_profile_text_gender);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        supportPostponeEnterTransition();
        fullname = getIntent().getStringExtra("fullname");
        photo = getIntent().getStringExtra("photo");
        username = getIntent().getStringExtra("username");
        email = getIntent().getStringExtra("email");
        phone = getIntent().getStringExtra("phone");
        country = getIntent().getStringExtra("country");
        state = getIntent().getStringExtra("state");
        gender = getIntent().getStringExtra("gender");

        getSupportActionBar().setTitle(fullname);
        Glide.with(this)
                .load("https://www.catholix.com.ng/files/images/profilepics/"+photo)
                .dontAnimate()
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        supportStartPostponedEnterTransition();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        supportStartPostponedEnterTransition();
                        return false;
                    }
                })
                .into(profileImg);
        textUsername.setText(username);
        textEmail.setText("Email : "+email);
        textPhone.setText("Phone : "+phone);
        textCountry.setText("Country : "+country);
        textState.setText("State : "+state);
        textGender.setText("Gender : "+gender);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Chat coming soon!", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    private void checkVersion() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.setStatusBarColor(Color.TRANSPARENT);
        }
    }

}
