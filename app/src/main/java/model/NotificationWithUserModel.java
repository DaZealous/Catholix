package model;

import java.util.List;

public class NotificationWithUserModel {
    private NotificationModel list;
    private String username;
    private String imgUrl;

    public NotificationWithUserModel(NotificationModel list, String username, String imgUrl) {
        this.list = list;
        this.username = username;
        this.imgUrl = imgUrl;
    }

    public NotificationModel getList() {
        return list;
    }

    public void setList(NotificationModel list) {
        this.list = list;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }
}
