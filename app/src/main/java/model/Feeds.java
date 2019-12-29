package model;

import com.google.gson.annotations.SerializedName;

public class Feeds {

    @SerializedName("UID")
    String userId;
    @SerializedName("Fname")
    String Fname;
    @SerializedName("Message")
    String message;
    @SerializedName("AddedOn")
    String addedOn;
    @SerializedName("Photo")
    String photo;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFname() {
        return Fname;
    }

    public void setFname(String fname) {
        Fname = fname;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getAddedOn() {
        return addedOn;
    }

    public void setAddedOn(String addedOn) {
        this.addedOn = addedOn;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

}
