package view;

public interface LoginView {
    String getUsername();

    void setUsernameError(String error);

    String getPassword();

    void setPasswordError(String error);

    void startMainActivity();

    void showLoginFailed(String error);

    String getForgetEmail();

    void showForgetEmailError(String error);

    void startEmailRecovery();

    void showForgetEmailSuccess();

}
