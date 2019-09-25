package com.example.remindMe.Reminders;

import com.google.firebase.firestore.GeoPoint;

/**
 * represents a reminder object, has mainly constructor, getters and setters.
 */
public class Reminder {

    private String text, timeSlot, location;
    private GeoPoint latLng;

    public Reminder() {}

    public Reminder(String text, String time, String location) {
        this.text = text;
        this.location = location;
        this.timeSlot = time;
        latLng = null;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(String time) {
        this.timeSlot = time;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public GeoPoint getLatLng() {
        return latLng;
    }

    public void setLatLng(GeoPoint latLng) {
        this.latLng = latLng;
    }


}
