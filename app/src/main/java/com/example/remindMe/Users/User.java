package com.example.remindMe.Users;

import com.example.remindMe.Notifications.NotificationObject;
import com.example.remindMe.Reminders.Reminder;

import java.util.ArrayList;


/**
 * represents a user object, has mainly constructor, getters and setters.
 */
public class User {
    private String name, id, email, userLocationId;
    private ArrayList<Reminder> Reminders;
    private ArrayList<NotificationObject> Notifications;

    public User() {}

    public User(String name, String email) {
        this.name = name;
        this.email = email;
        this.userLocationId = "";
        this.Reminders = new ArrayList<>();
        this.Notifications = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public ArrayList<Reminder> getReminders() {
        return Reminders;
    }

    public void setReminders(ArrayList<Reminder> reminders) {
        Reminders = reminders;
    }

    public ArrayList<NotificationObject> getNotifications() {
        return Notifications;
    }

    public void setNotifications(ArrayList<NotificationObject> notifications) {
        Notifications = notifications;
    }

    public String getUserLocationId() {
        return userLocationId;
    }

    public void setUserLocationId(String userLocationId) {
        this.userLocationId = userLocationId;
    }
}
