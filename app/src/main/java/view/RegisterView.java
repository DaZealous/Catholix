package view;

public interface RegisterView {
    String getFirstName();
    String getLastName();
    String getUserName();
    String getPhone();
    String getEmail();
    String getPassword();
    String getRetypePass();
    void startActivity();
    void ShowError(String error);
    void showFirstNameError(String error);
    void showLastNameError(String error);
    void showUserNameError(String error);
    void showPhoneError(String error);
    void showEmailError(String error);
    void showPasswordError(String error);
    void showRetypePassError(String error);
}
