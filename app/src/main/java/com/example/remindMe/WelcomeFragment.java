package com.example.remindMe;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.remindMe.Notifications.AlertReceiver;
import com.example.remindMe.Reminders.RemindFragment;
import com.example.remindMe.Reminders.Reminder;

import java.util.ArrayList;


/**
 * Welcome fragment - the first fragment the user is directed to after logging in
 */
public class WelcomeFragment extends Fragment {

    private static TextView msgText;
    private final String CANCEL_MSG = "Alarm canceled";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.welcome_fragment, container, false);
        msgText = (TextView) view.findViewById(R.id.question);

        handlingWelcomeButtons(view);

        return view;
    }

    /**
     * function that handels the UI buttons in the fragment
     * @param view - the view in where all objects are located
     */
    private void handlingWelcomeButtons(View view){
        Button addButton = (Button) view.findViewById(R.id.add_rem_button);
        Button cancelButton = (Button) view.findViewById(R.id.cancel_rem_button);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadRemindFragment(); //moves user to the add reminder fragment

            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelAlarm(); //cancels current alarm
            }
        });
    }

    /**
     * this function replaces the layout to a book page layout in case some book was clicked in the list
     */
    private void loadRemindFragment() {
        RemindFragment fragment = new RemindFragment();

        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.addToBackStack("welcomeFragment");  // enables to press "return" and go back to the welcome frag
        transaction.replace(R.id.main_fragment_container, fragment);
        transaction.commit();
    }

    /**
     * this function cancels the reminder that set currently by disabling the alarm set for the notification
     */
    private void cancelAlarm(){
        AlarmManager alarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(getContext(), AlertReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), 1, intent, 0);

        alarmManager.cancel(pendingIntent);
        msgText.setText(CANCEL_MSG);

        ArrayList<Reminder> remList = MainActivity.curUser.getReminders();
        if (remList==null){
            remList = new ArrayList<>();
            Toast.makeText(getContext(), "No reminder to delete", Toast.LENGTH_SHORT).show();
        }
        remList.remove(0);
        MainActivity.curUser.setReminders(remList);
        Toast.makeText(getContext(), "Reminder was deleted", Toast.LENGTH_SHORT).show();
        ServerApi.getInstance().removeReminder(MainActivity.userId, remList);
    }

    /**
     * changes the msg in the welcome fragment.
     */
    public static void changeMsg(String msg) {
        msgText.setText(msg);
        }

}