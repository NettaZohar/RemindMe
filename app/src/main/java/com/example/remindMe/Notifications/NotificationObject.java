package com.example.remindMe.Notifications;

/**
 * represents a notification firebase object, has mainly constructor, getters and setters.
 */
public class NotificationObject {
    private String time;
    private String text;

    public NotificationObject() {}

    public NotificationObject(String text){
        this.text = text;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }


}
