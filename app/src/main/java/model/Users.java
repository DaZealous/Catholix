package model;

import com.google.gson.annotations.SerializedName;

public class Users {

    @SerializedName("ID")
    String userId;
    @SerializedName("Fname")
    String firstname;
    @SerializedName("Sname")
    String surname;
    @SerializedName("Photo")
    String photo;
    @SerializedName("Uname")
    String username;
    @SerializedName("Email")
    String email;
    @SerializedName("Gender")
    String gender;
    @SerializedName("Country")
    String country;
    @SerializedName("State")
    String state;
    @SerializedName("LocalGov")
    String lga;
    @SerializedName("Phone")
    String phone;
    @SerializedName("Addr")
    String address;

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getGender() {
        return gender;
    }

    public String getCountry() {
        return country;
    }

    public String getState() {
        return state;
    }

    public String getLga() {
        return lga;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getFirstname() {
        return firstname;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
