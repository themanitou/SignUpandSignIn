package ca.skillsup.androidapp.activity;

import android.app.DialogFragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
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
import ca.skillsup.androidapp.helper.PlaceManager;
import ca.skillsup.androidapp.helper.SessionManager;

public class CreateClassActivity extends AppCompatActivity
        implements DatePickerFragment.callBackListener, TimePickerFragment.callBackListener {

    // LogCat tag
    private static String TAG = CreateClassActivity.class.getSimpleName();

    private static final int REQUEST_ADDRESS_PICKER = 1000;

    private SessionManager session;
    private PlaceManager placeManager;

    FloatingActionButton fabCreateClass;

    private EditText edtClassName;
    private String className;

    private TextView tvClassDate, tvClassTime;
    private Calendar classDate;

    private EditText edtClassAddress;
    private LatLng classLatLng;
    private String classAddress;

    private EditText edtClassDescription;
    private String classDescription;

    private EditText edtClassFee;
    private float classFee;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_create_class);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Session manager
        session = new SessionManager(this);

        // Place manager
        placeManager = new PlaceManager(this);

        fabCreateClass = (FloatingActionButton) findViewById(R.id.fabCreateClass);
        fabCreateClass.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_done_36dp));
        fabCreateClass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                publishClass();
            }
        });

        // pre-fill class name if possible
        edtClassName = (EditText) findViewById(R.id.edtClassName);
        className = session.getClassName();
        if (className != null) {
            edtClassName.setText(className);
        }

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
                Toast.makeText(this, errorString, Toast.LENGTH_LONG).show();
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

        // pre-fill class description if possible
        classDescription = session.getClassDescription();
        edtClassDescription = (EditText) findViewById(R.id.edtClassDescription);
        if (classDescription != null) {
            edtClassDescription.setText(classDescription);
        }

        // pre-fill class fee if possible
        classFee = session.getClassFee();
        edtClassFee = (EditText) findViewById(R.id.edtClassFee);
        if (classFee != 0) {
            edtClassFee.setText(String.valueOf(classFee));
        }
    }

    @Override
    public void onBackPressed() {
        // save user input data
        saveUserInput();

        // return to main activity, without advertising class
        setResult(RESULT_CANCELED);
        finish();
    }

    private void saveUserInput() {
        className = edtClassName.getText().toString();
        if (className != null && !className.isEmpty()) {
            session.setClassName(className);
        }

        classAddress = edtClassAddress.getText().toString();
        if (classAddress != null && !classAddress.isEmpty()) {
            session.setClassAddress(classAddress);
            LatLng latlng = placeManager.getLocationFromAddress(classAddress);
            if (latlng != null) {
                classLatLng = latlng;
            }
            if (classLatLng != null) {
                session.setClassAddressLatLng(classLatLng);
            }
        }

        classDescription = edtClassDescription.getText().toString();
        if (classDescription != null && !classDescription.isEmpty()) {
            session.setClassDescription(classDescription);
        }

        String strClassFee = edtClassFee.getText().toString();
        try {
            if (strClassFee != null && !strClassFee.isEmpty()) {
                classFee = Float.parseFloat(strClassFee);
            }
            else {
                classFee = 0;
            }
            session.setClassFee(classFee);
        }
        catch (Exception e) {
            classFee = 0;
            String errorStr = "saveUserInput: error parsing class fee " + strClassFee + "\n" +
                    e.getMessage();
            Log.e(TAG, errorStr);
            Toast.makeText(this, errorStr, Toast.LENGTH_LONG).show();
        }
    }

    private void publishClass() {
        // save user input data
        saveUserInput();

        // return to main activity and indicate that class is published
        JSONObject classDetails = new JSONObject();
        try {
            classDetails.put(getString(R.string.EXTRA_MESSAGE_NAME), className);
            SimpleDateFormat format = new SimpleDateFormat(getString(
                    R.string.preference_key_class_date_time_pattern));
            classDetails.put(getString(R.string.EXTRA_MESSAGE_DATETIME), format.format(classDate.getTime()));
            classDetails.put(getString(R.string.EXTRA_MESSAGE_ADDRESS), classAddress);
            classDetails.put(getString(R.string.EXTRA_MESSAGE_LATITUDE), classLatLng.latitude);
            classDetails.put(getString(R.string.EXTRA_MESSAGE_LONGITUDE), classLatLng.longitude);
        }
        catch (JSONException e) {
            String errorString = "publishClass: error returning class details\n" + e.getMessage();
            Log.e(TAG, errorString);
            Toast.makeText(this, errorString, Toast.LENGTH_LONG).show();
        }

        Intent data = new Intent();
        data.setData(Uri.parse(classDetails.toString()));
        setResult(RESULT_OK, data);
        finish();
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
