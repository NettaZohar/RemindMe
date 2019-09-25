package com.example.remindMe.Reminders;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.remindMe.MainActivity;
import com.example.remindMe.Notifications.AlertReceiver;
import com.example.remindMe.R;
import com.example.remindMe.ServerApi;
import com.example.remindMe.WelcomeFragment;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.firestore.GeoPoint;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Callable;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

/**
 * Reminder fragment - the user is moved here from welcome fragment, creates the alarm for the
 * notification and the reminder object
 */
public class RemindFragment extends Fragment{

    public static int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
    private Button remindBtn, timePicker;
    private EditText editRemind, editLocation;
    private final String TAG = "add location";
    private LatLng newLatLng;
    private String name;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.reminder_layout, container, false);

        setAttributes(view);
        initElements(inflater);

        //initializing the google places API
        if (!Places.isInitialized()) {
        Places.initialize(getContext(), "AIzaSyCk5BDlgKbZb4hM7m43lk2fk0Te7XGUY_M");
        }

        //sets onClick listener with a google autocomplete search intent
        editLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME,
                        Place.Field.LAT_LNG);
                Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY,
                        fields).build(getContext());
                startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
            }
        });

    return view;
    }

    /**
     * connects to the google places API and produces the location according to the autocomplete
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                newLatLng = place.getLatLng();
                editLocation.setText(place.getName());
                Log.i(TAG, "latlng: " + newLatLng);
                Log.i(TAG, "Place: " + place.getName() + ", " + place.getId());
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                // Handle the error.
                Status status = Autocomplete.getStatusFromIntent(data);
                Log.i(TAG, status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }

    /**
     * this func gets the objects by their ID in XML
     * @param view - the view where all objects are located
     */
    private void setAttributes(View view){
        timePicker = (Button) view.findViewById(R.id.time_spinner);
        remindBtn = (Button) view.findViewById(R.id.Remind_me_button);
        editRemind = (EditText) view.findViewById(R.id.enter_remind);
        editLocation = (EditText) view.findViewById(R.id.choose_location_edit_text);

    }

    /**
     * this func initialize the elements in this fragment and add listeners to it's buttons.
     * @param inflater - the LayoutInflater
     */
    private void initElements(LayoutInflater inflater){

        timePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment timePicker = new TimePickerFragment();
                timePicker.show(getChildFragmentManager(), "time picker");
            }
        });

        /* handling add Reminder button */
        remindBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //creates the reminder object + sets location
                Reminder rem = createReminder();
                rem.setLatLng(new GeoPoint(newLatLng.latitude, newLatLng.longitude));
                //adds current reminder to the list
                ArrayList<Reminder> remList = MainActivity.curUser.getReminders();
                if (remList==null){remList = new ArrayList<>();}
                remList.add(rem);
                MainActivity.curUser.setReminders(remList);
                Toast.makeText(getContext(), "Reminder was added successfully", Toast.LENGTH_SHORT).show();
                // sets the alarm inorder to create a notification
                setFinalAlarm();

                Callable<Void> func = new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        return loadFragment();
                    }
                };
                ServerApi.getInstance().addNewReminder(rem, func); // updates database
            }
        });
    }

    /**
     * this function replaces the layout to a book page layout in case some book was clicked in the list
     */
    private Void loadFragment() {
        WelcomeFragment fragment = new WelcomeFragment();

        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.addToBackStack("welcomeFragment");  // enables to press "return" and go back to the welcome frag
        transaction.replace(R.id.main_fragment_container, fragment);
        transaction.commit();
        return null;
    }

    /**
     * this func get the details of new reminder that where inserted by the user, and creates a
     * new reminder object.
     * @return the reminder created.
     */
    private Reminder createReminder(){
        String minsString = "";
        name = editRemind.getText().toString();
        if (TimePickerFragment.minutePicked<10){ //adds 0 to the number between 1-9
            minsString = "0"+(TimePickerFragment.minutePicked);
        }else{
            minsString = String.valueOf(TimePickerFragment.minutePicked);
        }
        String time = TimePickerFragment.hourPicked + ":"+ minsString; //inserts the hour and minute
        String location = editLocation.getText().toString();
        Log.d("createReminder", time);
        return new Reminder(name, time, location);
    }


    /**
     * creates a pending intent to when the receiver gets the alert - inorder to send notification
     * @param c Calender object that has the time the user set in the time picker.
     */
    private void startAlarm(Calendar c){
        AlarmManager alarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(getContext(), AlertReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity(), 1, intent, 0);

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pendingIntent);
    }

    /**
     * creates and updates a calender object with the time inserted by user, and starts alarm.
     */
    private void setFinalAlarm(){
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, TimePickerFragment.hourPicked);
        c.set(Calendar.MINUTE, TimePickerFragment.minutePicked);
        c.set(Calendar.SECOND, 0);

        String s = DateFormat.getTimeInstance(DateFormat.SHORT).format(c.getTime());
        startAlarm(c);
    }
}
