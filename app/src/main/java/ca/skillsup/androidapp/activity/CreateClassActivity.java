package ca.skillsup.androidapp.activity;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import ca.skillsup.androidapp.R;
import ca.skillsup.androidapp.dialog.DatePickerFragment;
import ca.skillsup.androidapp.dialog.TimePickerFragment;

public class CreateClassActivity extends AppCompatActivity
    implements DatePickerFragment.callBackListener, TimePickerFragment.callBackListener {

    private TextView tvClassDate, tvClassTime;
    private Calendar classDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_class);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        tvClassDate = (TextView) findViewById(R.id.tvSetDate);
        tvClassTime = (TextView) findViewById(R.id.tvSetTime);

        classDate = Calendar.getInstance();
    }

    @Override
    public void onDatePickerListener(int year, int month, int day) {
        classDate.set(year, month, day,
                classDate.get(Calendar.HOUR_OF_DAY), classDate.get(Calendar.MINUTE));
        showDate();
    }

    @Override
    public void onTimePickerListener(int hourOfDay, int minute) {
        classDate.set(classDate.get(Calendar.YEAR), classDate.get(Calendar.MONTH),
                classDate.get(Calendar.DAY_OF_MONTH), hourOfDay, minute);
        showTime();
    }

    public void onSelectDateClicked(View view) {
        DialogFragment fragmentDateSelection = DatePickerFragment.newInstance(classDate);
        fragmentDateSelection.show(getFragmentManager(), "datePicker");
    }

    public void onSelectTimeClicked(View view) {
        DialogFragment fragmentTimeSelection = TimePickerFragment.newInstance(classDate);
        fragmentTimeSelection.show(getFragmentManager(), "timePicker");
    }

    private void showDate() {
        SimpleDateFormat format = new SimpleDateFormat("EEEE, MMMM d, yyyy");
        tvClassDate.setText(format.format(classDate.getTime()));
    }

    private void showTime() {
        SimpleDateFormat format = new SimpleDateFormat("h:mm a");
        tvClassTime.setText(format.format(classDate.getTime()));
    }
}
