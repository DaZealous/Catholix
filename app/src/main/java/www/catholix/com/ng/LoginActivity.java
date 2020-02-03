package www.catholix.com.ng;

import androidx.appcompat.app.AppCompatActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import Service.LoginService;
import presenter.LoginPresenter;
import view.LoginView;

public class LoginActivity extends AppCompatActivity implements LoginView, View.OnClickListener {

    TextView register, forgotPasswordText;
    LoginPresenter presenter;
    EditText editUser, editPass, editEmail;
    Button btnLogin, btnSubmit, btnGotIt;
    ProgressDialog dialog;
    RelativeLayout emailSuccessLayout, forgotPasswordLayout, bubblesLayout;
    ImageButton btnCloseLayout;
    Animation slideRight;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        presenter = new LoginPresenter(this, new LoginService(this));
        dialog = new ProgressDialog(this);
        dialog.setTitle("Loading....");
        dialog.setMessage("Please wait");
        dialog.setCanceledOnTouchOutside(true);
        checkVersion();
        initUI();
        btnLogin.setOnClickListener(this);
        register.setOnClickListener(this);
        btnSubmit.setOnClickListener(this);
        forgotPasswordText.setOnClickListener(this);
        btnCloseLayout.setOnClickListener(this);
        btnGotIt.setOnClickListener(this);

        slideRight.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                forgotPasswordLayout.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        editEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!TextUtils.isEmpty(charSequence.toString()))
                    if(Patterns.EMAIL_ADDRESS.matcher(charSequence.toString()).matches()){
                        btnSubmit.setAlpha(1f);
                        btnSubmit.setEnabled(true);
                    }else{
                        btnSubmit.setAlpha(0.3f);
                        btnSubmit.setEnabled(false);
                    }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void initUI() {
        register = findViewById(R.id.login_text_register);
        editPass = findViewById(R.id.login_edit_password);
        editUser = findViewById(R.id.login_edit_username);
        btnLogin = findViewById(R.id.login_btn_submit);
        btnSubmit = findViewById(R.id.forgot_password_layout_btn_submit);
        btnGotIt = findViewById(R.id.email_sent_success_dialogue_btn_got_it);
        btnCloseLayout = findViewById(R.id.forgot_password_layout_btn_cancel);
        emailSuccessLayout = findViewById(R.id.email_sent_success_dialogue_black_layout);
        forgotPasswordLayout = findViewById(R.id.forgot_password_layout_relative_layout_black);
        editEmail = findViewById(R.id.forgot_pass_layout_edit_text);
        bubblesLayout = findViewById(R.id.forgot_password_layout_loader_bubble);
        forgotPasswordText = findViewById(R.id.forgot_password_text);
        slideRight = AnimationUtils.loadAnimation(this, R.anim.slide_right2);
    }

    private void checkVersion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            else
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            window.setStatusBarColor(Color.TRANSPARENT);
        }
    }

    @Override
    public String getUsername() {
        return editUser.getText().toString().trim();
    }

    @Override
    public void setUsernameError(String error) {
        editUser.setError(error);
        editUser.requestFocus();
    }

    @Override
    public String getPassword() {
        return editPass.getText().toString().trim();
    }

    @Override
    public void setPasswordError(String error) {
        editPass.setError(error);
        editPass.requestFocus();
    }

    @Override
    public void startMainActivity() {
        try {
            startActivity(new Intent(this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void showLoginFailed(String error) {
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
    }

    @Override
    public String getForgetEmail() {
        return editEmail.getText().toString();
    }

    @Override
    public void showForgetEmailError(String error) {
        btnSubmit.setVisibility(View.VISIBLE);
        bubblesLayout.setVisibility(View.INVISIBLE);
        bubblesLayout.clearAnimation();
        Snackbar.make(forgotPasswordLayout, error, Snackbar.LENGTH_SHORT)
                .setAction("OK", null).show();
    }

    @Override
    public void startEmailRecovery() {
        btnSubmit.setVisibility(View.INVISIBLE);
        bubblesLayout.setVisibility(View.VISIBLE);
        bubblesLayout.startAnimation(AnimationUtils.loadAnimation(this, R.anim.my_anim));
    }

    @Override
    public void showForgetEmailSuccess() {
        emailSuccessLayout.setVisibility(View.VISIBLE);
        btnSubmit.setVisibility(View.VISIBLE);
        bubblesLayout.setVisibility(View.INVISIBLE);
        bubblesLayout.clearAnimation();
    }


    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.login_text_register) {
            startActivity(new Intent(LoginActivity.this, Register.class));
            finish();
        }
        if (id == R.id.login_btn_submit)
            presenter.onLoginClicked();
        if(id == R.id.forgot_password_layout_btn_submit)
            presenter.onForgetPasswordClicked();
        if(id == R.id.forgot_password_text){
            forgotPasswordLayout.setVisibility(View.VISIBLE);
            forgotPasswordLayout.startAnimation(AnimationUtils.loadAnimation(this, R.anim.card_slide_left2));
        }
        if(id == R.id.forgot_password_layout_btn_cancel)
            forgotPasswordLayout.startAnimation(slideRight);
        if(id == R.id.email_sent_success_dialogue_btn_got_it){
            emailSuccessLayout.setVisibility(View.INVISIBLE);
            forgotPasswordLayout.startAnimation(slideRight);
        }
    }
}
