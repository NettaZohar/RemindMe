package com.example.remindMe.Notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.remindMe.MainActivity;
import com.example.remindMe.Reminders.Reminder;
import com.example.remindMe.ServerApi;

import java.util.ArrayList;

public class AlertReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        if (checkConditionsForNotification()){
            // if the condition is trye, builds notification
            NotificationHelper notificationHelper = new NotificationHelper(context);
            NotificationCompat.Builder nb = notificationHelper.getChannelNotification();
            notificationHelper.getManager().notify(1, nb.build());

            //creates a firebase notification for the log layout in the app
            ArrayList<Reminder> remindersList = MainActivity.curUser.getReminders();

            NotificationObject not = new NotificationObject(remindersList.get(0).getText());
            ArrayList<NotificationObject> Notificationslist = MainActivity.curUser.getNotifications();
            if (Notificationslist==null){Notificationslist = new ArrayList<>();}
            Notificationslist.add(not);

            MainActivity.curUser.setNotifications(Notificationslist);
            Log.d("ADD_NOTIFICATION", "printNotif" + MainActivity.curUser.getNotifications().get(0).getText());

            ServerApi.getInstance().addNotifications(MainActivity.userId, not,
                    MainActivity.curUser.getNotifications(), NotificationFragment.notificationAdapter);
        }
    }

    /**
     * checks the conditions inorder to produce a notification - the location is close enough to the
     * inserted location on the time specified.
     * @return boolean value accordingly
     */
    private boolean checkConditionsForNotification(){
        ArrayList<Reminder> rem = MainActivity.curUser.getReminders();

        Location wantedLocation = new Location("wanted location");
        wantedLocation.setLatitude(rem.get(0).getLatLng().getLatitude());
        wantedLocation.setLongitude(rem.get(0).getLatLng().getLongitude());

        Location currentLocation = new Location("current location");
        currentLocation.setLatitude(MainActivity.mUserLocation.getGeoPoint().getLatitude());
        currentLocation.setLongitude(MainActivity.mUserLocation.getGeoPoint().getLongitude());

        float distance = wantedLocation.distanceTo(currentLocation);
        Log.d("NotificationConditions", "distance is: "+ distance);

        //if the distance between user and the specified location is less then 40 meters
        if ((int)(distance) <= 40){
            Log.d("NotificationConditions", "true!!!!!!");
            return true;

        }
        return false;
    }
}
