package com.example.remindMe.Users;

import android.app.Application;

import com.example.remindMe.Users.User;

/**
 * A singelton for the user thats authenticated
 */
public class UserClient extends Application {

    private User user = null;

    public User getUser(){ return user;}

    public void setUser(User user) {this.user = user;} // sets the user to the userClient object
}
