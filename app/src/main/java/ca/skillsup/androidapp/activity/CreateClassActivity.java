package ca.skillsup.androidapp.activity;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import ca.skillsup.androidapp.R;
import ca.skillsup.androidapp.dialog.DatePickerFragment;
import ca.skillsup.androidapp.dialog.TimePickerFragment;
import ca.skillsup.androidapp.helper.SessionManager;

public class CreateClassActivity extends AppCompatActivity
    implements DatePickerFragment.callBackListener, TimePickerFragment.callBackListener {

    // LogCat tag
    private static String TAG = CreateClassActivity.class.getSimpleName();

    private SessionManager session;

    private TextView tvClassDate, tvClassTime;
    private Calendar classDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_class);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Session manager
        session = new SessionManager(getApplicationContext());

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

        String strClassDateTime = session.getClassDateTime();

        // pre-fill class date and time if possible
        classDate = Calendar.getInstance();
        if (strClassDateTime != null) {
            try {
                Date date = new SimpleDateFormat(getString(
                        R.string.preference_key_class_date_time_pattern)).parse(strClassDateTime);
                classDate.setTime(date);
                showDate();
                showTime();
            }
            catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG, "Date time saved in SharedPreference has wrong format: " +
                        strClassDateTime);
                Toast.makeText(getApplicationContext(),
                        "Date time saved in SharedPreference has wrong format: " +
                                strClassDateTime + "\n" + e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onDatePickerListener(int year, int month, int day) {
        classDate.set(year, month, day,
                classDate.get(Calendar.HOUR_OF_DAY), classDate.get(Calendar.MINUTE));

        // save date time to SharedPreference
        SimpleDateFormat format = new SimpleDateFormat(getString(
                R.string.preference_key_class_date_time_pattern));
        session.setClassDateTime(format.format(classDate.getTime()));

        showDate();
    }

    @Override
    public void onTimePickerListener(int hourOfDay, int minute) {
        classDate.set(classDate.get(Calendar.YEAR), classDate.get(Calendar.MONTH),
                classDate.get(Calendar.DAY_OF_MONTH), hourOfDay, minute);

        // save date time to SharedPreference
        SimpleDateFormat format = new SimpleDateFormat(getString(
                R.string.preference_key_class_date_time_pattern));
        session.setClassDateTime(format.format(classDate.getTime()));

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
