package www.catholix.com.ng;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

import config.SharedPref;

public class SplashActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 1;
    Handler handler;
    LinearLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        handler = new Handler();
        layout = findViewById(R.id.splash_linear_bubble_layout);
        layout.startAnimation(AnimationUtils.loadAnimation(SplashActivity.this, R.anim.my_anim));
        askForMultiplePermissions();
    }

    public boolean hasPermission(String permission) {
        return ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED;
    }

    public void askForMultiplePermissions() {
        String writeExternalStoragePermission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        String readExternalStoragePermission = Manifest.permission.READ_EXTERNAL_STORAGE;
        String accessNetworkState = Manifest.permission.ACCESS_NETWORK_STATE;
        String readPhoneState = Manifest.permission.READ_PHONE_STATE;
        String recordAudio = Manifest.permission.RECORD_AUDIO;
        String camera = Manifest.permission.CAMERA;
        String contacts = Manifest.permission.READ_CONTACTS;

        List<String> permissionList = new ArrayList<>();

        if (hasPermission(writeExternalStoragePermission)) {
            permissionList.add(writeExternalStoragePermission);
        }
        if (hasPermission(readExternalStoragePermission)) {
            permissionList.add(readExternalStoragePermission);
        }
        if (hasPermission(accessNetworkState)) {
            permissionList.add(accessNetworkState);
        }
        if (hasPermission(readPhoneState)) {
            permissionList.add(readPhoneState);
        }
        if (hasPermission(recordAudio)) {
            permissionList.add(recordAudio);
        }
        if (hasPermission(camera)) {
            permissionList.add(camera);
        }
        if (hasPermission(contacts)) {
            permissionList.add(contacts);
        }
        if (!permissionList.isEmpty()) {
            String[] permissions = permissionList.toArray(new String[0]);
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE);
        } else {
            handler.postDelayed(() -> {
                layout.clearAnimation();
           /* ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(SplashActivity.this,
                    findViewById(R.id.splash_image_icon), "icon");*/
                if (SharedPref.getInstance(SplashActivity.this).getUser().isEmpty()) {
                    startActivity(new Intent(SplashActivity.this, WelcomeActivity.class));
                    finish();
                } else {
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                    finish();
                }

            }, 3500);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length != 0) {
                List<Integer> list = new ArrayList<>();

                for (int i : grantResults) {
                    list.add(i);
                }

                if (list.contains(PackageManager.PERMISSION_DENIED)) {
                    askForMultiplePermissions();
                } else {
                    handler.postDelayed(() -> {
                        layout.clearAnimation();
                        if (SharedPref.getInstance(SplashActivity.this).getUser().isEmpty()) {
                            startActivity(new Intent(SplashActivity.this, WelcomeActivity.class));
                            finish();
                        } else {
                            startActivity(new Intent(SplashActivity.this, MainActivity.class));
                            finish();
                        }
                    }, 3500);

                }
            }
        }
    }
}
