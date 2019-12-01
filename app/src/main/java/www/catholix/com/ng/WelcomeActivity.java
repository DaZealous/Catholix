package www.catholix.com.ng;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.animation.LayoutTransition;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import adapter.WelcomePager;

public class WelcomeActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener{

    ViewPager viewPager;
    Button btnLogin;
    TextView register;
    WelcomePager pager;
    TextView[] dots;
    LinearLayout dotsLayout;
    int delay = 5000;
    Handler handler;
    int page = 0;
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (pager.getCount() == page)
                page = 0;
            else
                page++;
            viewPager.setCurrentItem(page, true);
            handler.postDelayed(this, delay);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE|View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            else
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE|View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            window.setStatusBarColor(Color.TRANSPARENT);
        }

        pager = new WelcomePager(this);
        handler = new Handler();
        btnLogin = findViewById(R.id.welcome_btn_login);
        viewPager = findViewById(R.id.welcome_viewpager);
        register = findViewById(R.id.welcome_register);
        dotsLayout = findViewById(R.id.welcome_dots_layout);
        viewPager.setOnPageChangeListener(this);
        viewPager.setAdapter(pager);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(WelcomeActivity.this, LoginActivity.class));
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(WelcomeActivity.this, Register.class));
            }
        });
        initDots(0);
    }

    private void initDots(int position) {
        dotsLayout.removeAllViews();
        dots = new TextView[3];
        for(int i = 0; i < dots.length; i++){
            dots[i] = new TextView(this);
            dots[i].setText(Html.fromHtml("&#8226;"));
            dots[i].setTextColor(Color.GRAY);
            dots[i].setTextSize(50);
            dotsLayout.addView(dots[i]);
        }
        dots[position].setTextColor(Color.WHITE);
    }


    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        page = position;
        initDots(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.postDelayed(runnable, delay);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(runnable);
    }
}
