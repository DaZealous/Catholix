package model;

import com.google.gson.annotations.SerializedName;

public class NotificationModel {
    @SerializedName("id")
    private String postId;
    @SerializedName("sndID")
    private String senderID;
    @SerializedName("date_added")
    private String date;

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getSenderID() {
        return senderID;
    }

    public void setSenderID(String senderID) {
        this.senderID = senderID;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
