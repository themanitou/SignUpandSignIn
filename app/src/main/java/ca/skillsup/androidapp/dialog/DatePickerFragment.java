package ca.skillsup.androidapp.dialog;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.DatePicker;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by manitou on 12/8/16.
 */

public class DatePickerFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {

    public callBackListener mCallback;

    public interface callBackListener {
        void onDatePickerListener(int year, int month, int day);
    }

    public static DatePickerFragment newInstance(Calendar calendar) {
        DatePickerFragment datePickerFragment = new DatePickerFragment();
        Bundle args = new Bundle();
        args.putInt("Year", calendar.get(Calendar.YEAR));
        args.putInt("Month", calendar.get(Calendar.MONTH));
        args.putInt("Day of month", calendar.get(Calendar.DAY_OF_MONTH));
        datePickerFragment.setArguments(args);
        return datePickerFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCallback = (callBackListener) getActivity();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int year = getArguments().getInt("Year");
        int month = getArguments().getInt("Month");
        int day = getArguments().getInt("Day of month");

        // Create a new instance of DatePickerDialog and return it
        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        mCallback.onDatePickerListener(year, month, day);
    }

}
