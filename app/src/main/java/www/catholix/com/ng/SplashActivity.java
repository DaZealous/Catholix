package www.catholix.com.ng;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

public class SplashActivity extends AppCompatActivity {

    Handler handler;
    LinearLayout layout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        handler = new Handler();
        layout = findViewById(R.id.splash_linear_bubble_layout);
        layout.startAnimation(AnimationUtils.loadAnimation(SplashActivity.this, R.anim.my_anim));
       handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                layout.clearAnimation();
                ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(SplashActivity.this,
                        findViewById(R.id.splash_image_icon), "icon");
                startActivity(new Intent(SplashActivity.this, WelcomeActivity.class), optionsCompat.toBundle());
                supportFinishAfterTransition();
            }
        }, 3500);
    }
}
