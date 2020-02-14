package model;

public class ChatHistoryModel {
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    private String username, userid, message, time;

    public ChatHistoryModel(String username, String userid, String message, String time) {
        this.username = username;
        this.userid = userid;
        this.message = message;
        this.time = time;
    }

    public ChatHistoryModel(){

    }
}
