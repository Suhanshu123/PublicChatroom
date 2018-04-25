package com.example.android.authfirebase.models;

/**
 * Created by Suhanshu on 10-04-2018.
 */

public class User {
    private String name;
    private String phone;
    private String profile_image;
    private String user_id;

    public User(String name, String phone, String profile_image, String user_id) {
        this.name = name;
        this.phone = phone;
        this.profile_image = profile_image;
        this.user_id = user_id;
    }

    public User() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getProfile_image() {
        return profile_image;
    }

    public void setProfile_image(String profile_image) {
        this.profile_image = profile_image;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", phone='" + phone + '\'' +
                ", profile_image='" + profile_image + '\'' +
                ", user_id='" + user_id + '\'' +
                '}';
    }
}
