package www.catholix.com.ng;

import androidx.appcompat.app.AppCompatActivity;

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

import Service.RegisterService;
import presenter.RegisterPresenter;
import view.RegisterView;

public class Register extends AppCompatActivity implements RegisterView {

    TextView login;
    EditText editFirstName, editLastName, editUserName, editPhone, editEmail, editPassword, editRetypePass;
    Button btnRegister;
    RegisterPresenter presenter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        checkVersion();
        initUI();

        presenter = new RegisterPresenter(this, new RegisterService(this));
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Register.this, LoginActivity.class));
                finish();
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                presenter.onButtonCLicked();
            }
        });
    }

    private void checkVersion() {
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
    }

    private void initUI() {
        login = findViewById(R.id.login_text_login);
        editFirstName = findViewById(R.id.activity_register_first_name);
        editLastName = findViewById(R.id.activity_register_last_name);
        editUserName = findViewById(R.id.activity_register_user_name);
        editPhone = findViewById(R.id.activity_register_phone);
        editEmail = findViewById(R.id.activity_register_email_address);
        editPassword = findViewById(R.id.activity_register_password);
        editRetypePass = findViewById(R.id.activity_register_retype_password);
        btnRegister = findViewById(R.id.activity_register_btn_submit);
    }

    @Override
    public String getFirstName() {
        return editFirstName.getText().toString().trim();
    }

    @Override
    public String getLastName() {
        return editLastName.getText().toString().trim();
    }

    @Override
    public String getUserName() {
        return editUserName.getText().toString().trim();
    }

    @Override
    public String getPhone() {
        return editPhone.getText().toString().trim();
    }

    @Override
    public String getEmail() {
        return editEmail.getText().toString().trim();
    }

    @Override
    public String getPassword() {
        return editPassword.getText().toString().trim();
    }

    @Override
    public String getRetypePass() {
        return editRetypePass.getText().toString().trim();
    }

    @Override
    public void startActivity() {
        try {
            startActivity(new Intent(this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void ShowError(String error) {
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showFirstNameError(String error) {
        editFirstName.setError(error);
        editFirstName.requestFocus();
    }

    @Override
    public void showLastNameError(String error) {
        editLastName.setError(error);
        editLastName.requestFocus();
    }

    @Override
    public void showUserNameError(String error) {
        editUserName.setError(error);
        editUserName.requestFocus();
    }

    @Override
    public void showPhoneError(String error) {
        editPhone.setError(error);
        editPhone.requestFocus();
    }

    @Override
    public void showEmailError(String error) {
        editEmail.setError(error);
        editEmail.requestFocus();
    }

    @Override
    public void showPasswordError(String error) {
        editPassword.setError(error);
        editPassword.requestFocus();
    }

    @Override
    public void showRetypePassError(String error) {
        editRetypePass.setError(error);
        editRetypePass.requestFocus();
    }
}
