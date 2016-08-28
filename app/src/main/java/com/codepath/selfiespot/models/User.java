package com.codepath.selfiespot.models;


import com.parse.ParseClassName;
import com.parse.ParseObject;

@ParseClassName("User")
public class User extends ParseObject {

    private final String USER_NAME = "username";
    private final String USER_EMAIL = "email";

    // empty constructor required
    public User() {

    }

    public String getUserName() {
        return USER_NAME;
    }

    public String getUserEmail() {
        return USER_EMAIL;
    }



}
