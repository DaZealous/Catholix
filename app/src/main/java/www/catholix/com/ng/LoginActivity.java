package www.catholix.com.ng;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import Service.LoginService;
import api.VolleyInstance;
import presenter.LoginPresenter;
import view.LoginView;

public class LoginActivity extends AppCompatActivity implements LoginView, View.OnClickListener {

    TextView register;
    LoginPresenter presenter;
    EditText editUser, editPass;
    Button btnLogin;
    ProgressDialog dialog;

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
    }

    private void initUI() {
        register = findViewById(R.id.login_text_register);
        editPass = findViewById(R.id.login_edit_password);
        editUser = findViewById(R.id.login_edit_username);
        btnLogin = findViewById(R.id.login_btn_submit);
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
        startActivity(new Intent(this, MainActivity.class));
    }

    @Override
    public void showLoginFailed(String error) {
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.login_text_register) {
            startActivity(new Intent(LoginActivity.this, Register.class));
            finish();
        }
        if (id == R.id.login_btn_submit) {
            presenter.onLoginClicked();
        }
    }
}
