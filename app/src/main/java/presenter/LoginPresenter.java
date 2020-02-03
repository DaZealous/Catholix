package presenter;


import Service.LoginService;

import view.LoginView;

public class LoginPresenter {
    private LoginView loginView;
    private LoginService loginService;

    public LoginPresenter(LoginView loginView, LoginService loginService) {
        this.loginView = loginView;
        this.loginService = loginService;
    }

    public void onLoginClicked(){
        if(loginView.getUsername().isEmpty()){
            loginView.setUsernameError("Username is empty");
            return;
        }
        if(loginView.getPassword().isEmpty()){
            loginView.setPasswordError("Password is empty");
            return;
        }
        loginService.doLogin(loginView.getUsername(), loginView.getPassword(), loginView);
    }

    public void onForgetPasswordClicked() {
        loginService.doForgetPassword(loginView);
    }
}
