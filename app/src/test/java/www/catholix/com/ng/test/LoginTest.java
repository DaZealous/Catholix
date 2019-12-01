package www.catholix.com.ng.test;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import Service.LoginService;
import presenter.LoginPresenter;
import view.LoginView;

@RunWith(MockitoJUnitRunner.class)
public class LoginTest {

    @Mock
    LoginView loginView;
    @Mock
    LoginService loginService;
    LoginPresenter presenter;

    @Before
    public void setUp() throws Exception {
        presenter = new LoginPresenter(loginView, loginService);
    }

    @Test
    public void showUsernameIsEmpty() throws Exception{
        Mockito.when(loginView.getUsername()).thenReturn("");
        presenter.onLoginClicked();
        Mockito.verify(loginView).setUsernameError("Username is empty");
    }

    @Test
    public void showPasswordIsEmpty() throws Exception{
        Mockito.when(loginView.getUsername()).thenReturn("james");
        Mockito.when(loginView.getPassword()).thenReturn("");
        presenter.onLoginClicked();
        Mockito.verify(loginView).setPasswordError("Password is empty");
    }


}