package presenter;

import android.text.TextUtils;

import Service.RegisterService;
import view.RegisterView;
import www.catholix.com.ng.Register;

public class RegisterPresenter {

    private RegisterView view;
    private RegisterService service;

    public RegisterPresenter(RegisterView view, RegisterService service) {
        this.view = view;
        this.service = service;
    }

    public void onButtonCLicked() {
        if(TextUtils.isEmpty(view.getFirstName())){
            view.showFirstNameError("first name is empty");
            return;
        }
        if(TextUtils.isEmpty(view.getLastName())){
            view.showLastNameError("last name is empty");
            return;
        }
        if(TextUtils.isEmpty(view.getUserName())){
            view.showUserNameError("username is empty");
            return;
        }
        if(TextUtils.isEmpty(view.getPhone())){
            view.showPhoneError("phone number is empty");
            return;
        }
        if(TextUtils.isEmpty(view.getEmail())){
            view.showEmailError("email is empty");
            return;
        }
        if(TextUtils.isEmpty(view.getPassword())){
            view.showPasswordError("password is empty");
            return;
        }
        if(TextUtils.isEmpty(view.getRetypePass())){
            view.showRetypePassError("this field is empty");
            return;
        }
        if (!TextUtils.equals(view.getPassword(), view.getRetypePass())){
            view.ShowError("password do not match");
            return;
        }
        if(view.getPassword().length() < 6){
            view.showPhoneError("password must be six characters above");
            return;
        }
        service.doRegister(view);
    }
}
