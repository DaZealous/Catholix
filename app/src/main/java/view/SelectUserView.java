package view;

import java.util.List;

import model.Users;

public interface SelectUserView {
    void selectUser(String id);
    void removeUser(String id);
    String getUser(int position);
    List<String> getUsers();
}
