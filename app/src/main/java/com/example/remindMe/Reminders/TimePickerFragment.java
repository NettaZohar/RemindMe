package com.example.remindMe.Reminders;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import java.util.Calendar;

/**
 * the timePicker dialog that is a part of the Reminder setting proccess
 */
public class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener{

    public static int hourPicked;
    public static int minutePicked;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        return new TimePickerDialog(getActivity(), this, hour, minute, DateFormat.is24HourFormat(getActivity()));
    }

    /**
     * sets the hour and minute picked by the user and assigns them to global variables.
     * @param view the timePicker view
     * @param hourOfDay the hour selected
     * @param minute the minute selected
     */
    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        this.hourPicked = hourOfDay;
        this.minutePicked = minute;
    }
}
