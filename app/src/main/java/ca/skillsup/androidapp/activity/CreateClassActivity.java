package ca.skillsup.androidapp.activity;

import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import ca.skillsup.androidapp.R;
import ca.skillsup.androidapp.dialog.AddressPickerFragment;
import ca.skillsup.androidapp.dialog.DatePickerFragment;
import ca.skillsup.androidapp.dialog.TimePickerFragment;
import ca.skillsup.androidapp.helper.SessionManager;

public class CreateClassActivity extends AppCompatActivity
        implements DatePickerFragment.callBackListener, TimePickerFragment.callBackListener {

    // LogCat tag
    private static String TAG = CreateClassActivity.class.getSimpleName();

    private static final int REQUEST_ADDRESS_PICKER = 1000;

    private SessionManager session;

    private TextView tvClassDate, tvClassTime;
    private Calendar classDate;
    private EditText edtClassAddress;

    private LatLng classLatLng;
    private String classAddress;

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

        // pre-fill class date and time if possible
        String strClassDateTime = session.getClassDateTime();
        classDate = Calendar.getInstance();
        if (strClassDateTime != null) {
            try {
                Date date = new SimpleDateFormat(getString(
                        R.string.preference_key_class_date_time_pattern)).parse(strClassDateTime);
                classDate.setTime(date);
                showDate();
                showTime();
            } catch (Exception e) {
                String errorString = "Date time saved in SharedPreference has wrong format: " +
                        strClassDateTime + "\n" + e.getMessage();
                Log.w(TAG, errorString);
                Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_LONG).show();
            }
        }

        // pre-fill class address if possible
        classAddress = session.getClassAddress();
        edtClassAddress = (EditText) findViewById(R.id.edtClassAddress);
        if (classAddress != null) {
            edtClassAddress.setText(classAddress);
        }

        // fetch previously saved latitude and longitude if possible
        if (classAddress != null && classAddress != "") {
            classLatLng = session.getClassAddressLatLng();
        }
        else {
            double latlng[] = getIntent().getDoubleArrayExtra(getString(R.string.EXTRA_MESSAGE_LATLNG));
            if (latlng.length == 2) {
                classLatLng = new LatLng(latlng[0],latlng[1]);
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

    private void setClassAddress() {
        session.setClassAddress(classAddress);
        session.setClassAddressLatLng(classLatLng);
        edtClassAddress.setText(classAddress);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ADDRESS_PICKER) {
            if (resultCode == RESULT_OK) {
                try {
                    JSONObject classVenue = new JSONObject(data.getDataString());
                    classAddress = classVenue.getString(getString(R.string.EXTRA_MESSAGE_ADDRESS));
                    Double lat = classVenue.getDouble(getString(R.string.EXTRA_MESSAGE_LATITUDE));
                    Double lng = classVenue.getDouble(getString(R.string.EXTRA_MESSAGE_LONGITUDE));
                    classLatLng = new LatLng(lat, lng);

                    setClassAddress();
                }
                catch (JSONException e) {
                    String errorString = "Error getting selected address and latitude, longitude\n" +
                            e.getMessage();
                    Log.e(TAG, errorString);
                    Toast.makeText(this, errorString, Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    public void onPickAddress(View view) {
        Intent intent = new Intent(this, AddressPickerFragment.class);
        if (classLatLng != null) {
            intent.putExtra(getString(R.string.EXTRA_MESSAGE_LATLNG),
                    new double[]{classLatLng.latitude, classLatLng.longitude});
        }
        classAddress = edtClassAddress.getText().toString();
        if (classAddress != null) {
            intent.putExtra(getString(R.string.EXTRA_MESSAGE_ADDRESS),
                    classAddress);
        }
        startActivityForResult(intent, REQUEST_ADDRESS_PICKER);
    }
}
