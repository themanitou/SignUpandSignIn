package ca.skillsup.androidapp.dialog;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.TimePicker;

import java.util.Calendar;

/**
 * Created by manitou on 12/8/16.
 */

public class TimePickerFragment extends DialogFragment
        implements TimePickerDialog.OnTimeSetListener {

    public callBackListener mCallback;

    public interface callBackListener {
        void onTimePicked(int hourOfDay, int minute);
    }

    public static TimePickerFragment newInstance(Calendar calendar) {
        TimePickerFragment timePickerFragment = new TimePickerFragment();
        Bundle args = new Bundle();
        args.putInt("Hour of day", calendar.get(Calendar.HOUR_OF_DAY));
        args.putInt("Minute", calendar.get(Calendar.MINUTE));
        timePickerFragment.setArguments(args);
        return timePickerFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCallback = (callBackListener) getActivity();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int hour = getArguments().getInt("Hour of day");
        int minute = getArguments().getInt("Minute");

        // Create a new instance of TimePickerDialog and return it
        return new TimePickerDialog(getActivity(), this, hour, minute,
                DateFormat.is24HourFormat(getActivity()));
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        mCallback.onTimePicked(hourOfDay, minute);
    }

}
