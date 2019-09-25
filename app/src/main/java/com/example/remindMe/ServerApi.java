package com.example.remindMe;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.remindMe.Locations.UserLocation;
import com.example.remindMe.Notifications.NotificationAdapter;
import com.example.remindMe.Notifications.NotificationObject;
import com.example.remindMe.Reminders.Reminder;
import com.example.remindMe.Users.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * easy API for server
 */
public class ServerApi {
    public final static String USER_LOC_DB = "User Locations";
    private final static String REMINDER_DB = "Reminders";
    private final static String USERS_DB = "Users";
    private final static String NOTIF_DB = "Notifications";
    private FirebaseFirestore db;
    private final static ServerApi instance = new ServerApi();


    /**
     * constructor
     */
    private ServerApi(){
        db = FirebaseFirestore.getInstance();
    }

    /**
     * get singleton instance of this class
     * @return instance of this server
     */
    public static ServerApi getInstance(){
        return instance;
    }

    /**
     * this method saves the current user location according to the updated userLocation object.
     * @param userLocation the userLocation object to save to database.
     */
    public void saveUserLocation(final UserLocation userLocation){
        try{
            DocumentReference locationRef = db.collection(USER_LOC_DB).document(MainActivity.userId);
            String id = locationRef.getId();
            userLocation.getUser().setUserLocationId(id);
            locationRef.set(MainActivity.mUserLocation).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                            Log.d("saveUserLocation: ", "successful");
                    }
                }
            });
        }catch (NullPointerException e){
            Log.e("saveUserLocation", "saveUserLocation: user instance is null. NullPointerException");
        }
    }

    /**
     * this method is used when there's a need to get the notifications list in order to display
     * in notification Fragment
     * @param user User array to insert User object at index 0
     * @param notifications the list to fill with notifications from database
     * @param adapt - the notifications adapter
     */
    public void getNotificationsList(final User[] user, final List<NotificationObject> notifications, final NotificationAdapter adapt) {

        DocumentReference reference = db.collection(USERS_DB).document(MainActivity.userId);
                reference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {

                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            notifications.clear();

                            if (document != null && document.exists()) {
                                User got = document.toObject(User.class);
                                user[0] = got;
                                ArrayList<NotificationObject> nl = got.getNotifications();
                                for (NotificationObject notificationObject : nl) {
                                    notifications.add(notificationObject);
                                    Log.d("getNotificationsList", document.getId() + " => " + document.getData());
                                }
                                Collections.reverse(notifications);
                                adapt.notifyDataSetChanged();

                            } else {
                                Log.d("getNotificationsList", "Error getting documents: ", task.getException());
                            }
                        }
                    }
                });
    }

    /**
     * the method adds new reminder to firebase and assign it's id to the reminder object.
     * @param reminder - the BookItem to add.
     */
    public void addNewReminder(Reminder reminder ,final Callable<Void> func) {
        DocumentReference addDocRef = db.collection(USERS_DB).document(MainActivity.userId);
        addDocRef.update(REMINDER_DB,FieldValue.arrayUnion(reminder))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                System.out.println("REMINDER_ADDED_SUCCESSFULLY");
                try {
                    func.call();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                System.out.println("REMINDER_ADDING_FAILED");
            }
        });
    }


    /**
     * the method adds new notification to database and updates the notifications adapter.
     * @param userId - string id of the user to display
     * @param notification - the new notification object to add
     * @param notificationsList - the list with the currently displayed notifications
     * @param NotificationsAdapter - the notifications adapter
     */
    public void addNotifications(final String userId, final NotificationObject notification,
                                 final ArrayList<NotificationObject> notificationsList,
                                 final NotificationAdapter NotificationsAdapter){
        DocumentReference docRef = db.collection(USERS_DB).document(userId);
        Date date = new Date();
        String [] temp = date.toString().split(" ");
        String timeToSave = temp[1]+" "+temp[2]+", "+temp[5] + ", " + temp[3];
        notification.setTime(timeToSave);

        docRef.update(NOTIF_DB, FieldValue.arrayUnion(notification)).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void v) {
                notificationsList.add(notification);
                NotificationsAdapter.notifyDataSetChanged();
                System.out.println("COMMENT_ADDED_SUCCESSFULLY");
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        System.out.println("COMMENT_ADDING_FAILED");
                    }
                });
    }


    public void removeReminder(final String userId, final ArrayList<Reminder> RemindersList){
        DocumentReference docRef = db.collection(USERS_DB).document(userId);

        docRef.update(REMINDER_DB, RemindersList).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void v) {
                System.out.println("REMINDER_REMOVED_SUCCESSFULLY");
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        System.out.println("REMINDER_REMOVE_FAILED");
                    }
                });
    }




    /**
     * updates the main fields with the information off the Auth0 integration.
     * @param user the user to update
     */
    public void updateUser(User user) {
        DocumentReference userRef = db.collection(USERS_DB).document(user.getId());
        userRef.update("email", user.getEmail());
        userRef.update("name", user.getName());
    }


}
