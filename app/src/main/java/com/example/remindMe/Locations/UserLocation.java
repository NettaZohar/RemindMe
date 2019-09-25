package com.example.remindMe.Locations;

import com.example.remindMe.Users.User;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

/**
 * represents a userLocation object, has mainly constructor, getters and setters.
 */
public class UserLocation {

    private GeoPoint geoPoint;
    private @ServerTimestamp Date timestamp;
    private User user;
    private String userLocationId;

    public UserLocation(GeoPoint geoPoint, Date timestamp, User user) {
        this.userLocationId = "";
        this.geoPoint = geoPoint;
        this.timestamp = timestamp;
        this.user = user;
    }

    public UserLocation(){}

    public GeoPoint getGeoPoint() {
        return geoPoint;
    }

    public void setGeoPoint(GeoPoint geoPoint) {
        this.geoPoint = geoPoint;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getUserLocationId() {
        return userLocationId;
    }

    public void setUserLocationId(String userLocationId) {
        this.userLocationId = userLocationId;
    }

    @Override
    public String toString() {
        return "UserLocation{" +
                "geoPoint=" + geoPoint +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}
