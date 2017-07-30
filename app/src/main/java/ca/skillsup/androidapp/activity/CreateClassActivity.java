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
import ca.skillsup.androidapp.dialog.NumberPickerFragment;
import ca.skillsup.androidapp.dialog.TimePickerFragment;
import ca.skillsup.androidapp.helper.PlaceManager;
import ca.skillsup.androidapp.helper.SessionManager;
import ca.skillsup.androidapp.helper.TransactionManager;

public class CreateClassActivity extends AppCompatActivity
        implements DatePickerFragment.callBackListener,
                    TimePickerFragment.callBackListener,
                    NumberPickerFragment.callBackListener,
                    TransactionManager.callBackListener {

    // LogCat tag
    private static String TAG = CreateClassActivity.class.getSimpleName();

    private static final int REQUEST_ADDRESS_PICKER = 1000;

    private SessionManager sessionManager;
    private PlaceManager placeManager;
    private TransactionManager transactionManager;

    FloatingActionButton fabCreateClass;

    private EditText edtClassName;
    private String className;

    private TextView tvClassDate, tvClassTime;
    private Calendar classDate;

    private String classDuration;
    private TextView tvSetDuration;
    private int[] allDurations;

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
        sessionManager = SessionManager.getInstance();

        // Place manager
        placeManager = PlaceManager.getInstance();

        // Transaction manager
        transactionManager = TransactionManager.getInstance();

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
        className = sessionManager.getClassName();
        if (className != null) {
            edtClassName.setText(className);
        }

        // pre-fill class date and time if possible
        tvClassDate = (TextView) findViewById(R.id.tvSetDate);
        tvClassTime = (TextView) findViewById(R.id.tvSetTime);
        String strClassDateTime = sessionManager.getClassDateTime();
        classDate = Calendar.getInstance();
        if (strClassDateTime != null) {
            try {
                Date date = new SimpleDateFormat(SessionManager.PREFERENCE_KEY_CLASS_DATE_TIME_PATTERN).parse(strClassDateTime);
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

        // pre-fill class duration if possible
        classDuration = sessionManager.getClassDuration();
        tvSetDuration = (TextView) findViewById(R.id.tvSetDuration);
        allDurations = getResources().getIntArray(R.array.allDurations);
        if (classDuration == null) {
            classDuration = getString(R.string.defaultClassDuration);
        }
        tvSetDuration.setText(classDuration);

        // pre-fill class address if possible
        classAddress = sessionManager.getClassAddress();
        edtClassAddress = (EditText) findViewById(R.id.edtClassAddress);
        if (classAddress != null) {
            edtClassAddress.setText(classAddress);
        }

        // fetch previously saved latitude and longitude if possible
        if (classAddress != null && !classAddress.isEmpty()) {
            classLatLng = sessionManager.getClassAddressLatLng();
        }
        else {
            double latlng[] = getIntent().getDoubleArrayExtra(getString(R.string.EXTRA_MESSAGE_LATLNG));
            if (latlng.length == 2) {
                classLatLng = new LatLng(latlng[0],latlng[1]);
            }
        }

        // pre-fill class description if possible
        classDescription = sessionManager.getClassDescription();
        edtClassDescription = (EditText) findViewById(R.id.edtClassDescription);
        if (classDescription != null) {
            edtClassDescription.setText(classDescription);
        }

        // pre-fill class fee if possible
        classFee = sessionManager.getClassFee();
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
        if (!className.isEmpty()) {
            sessionManager.setClassName(className);
        }

        // save date, time and duration to SharedPreference
        SimpleDateFormat format = new SimpleDateFormat(SessionManager.PREFERENCE_KEY_CLASS_DATE_TIME_PATTERN);
        sessionManager.setClassDateTime(format.format(classDate.getTime()));
        sessionManager.setClassDateTime(format.format(classDate.getTime()));

        sessionManager.setClassDuration(classDuration);

        classAddress = edtClassAddress.getText().toString();
        if (!classAddress.isEmpty()) {
            sessionManager.setClassAddress(classAddress);
            LatLng latlng = placeManager.getLocationFromAddress(classAddress);
            if (latlng != null) {
                classLatLng = latlng;
            }
            if (classLatLng != null) {
                sessionManager.setClassAddressLatLng(classLatLng);
            }
        }

        classDescription = edtClassDescription.getText().toString();
        if (!classDescription.isEmpty()) {
            sessionManager.setClassDescription(classDescription);
        }

        String strClassFee = edtClassFee.getText().toString();
        try {
            if (!strClassFee.isEmpty()) {
                classFee = Float.parseFloat(strClassFee);
            }
            else {
                classFee = 0;
            }
            sessionManager.setClassFee(classFee);
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

        // transact to MySQL
        // create venue
        transactionManager.createVenue(this, classAddress, classLatLng.longitude, classLatLng.latitude);

        // return to main activity and indicate that class is published
        JSONObject classDetails = new JSONObject();
        try {
            classDetails.put(getString(R.string.EXTRA_MESSAGE_NAME), className);
            SimpleDateFormat format = new SimpleDateFormat(SessionManager.PREFERENCE_KEY_CLASS_DATE_TIME_PATTERN);
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
    public void onDatePicked(int year, int month, int day) {
        classDate.set(year, month, day,
                classDate.get(Calendar.HOUR_OF_DAY), classDate.get(Calendar.MINUTE));
        showDate();
    }

    @Override
    public void onTimePicked(int hourOfDay, int minute) {
        classDate.set(classDate.get(Calendar.YEAR), classDate.get(Calendar.MONTH),
                classDate.get(Calendar.DAY_OF_MONTH), hourOfDay, minute);
        showTime();
    }

    @Override
    public void onNumberPicked(int selectPosition) {
        classDuration = String.valueOf(allDurations[selectPosition - 1]);
        tvSetDuration.setText(classDuration);
    }

    @Override
    public void onTransactionResult(int code, String message) {
        Toast.makeText(this, "Code: " + String.valueOf(code) + "\n" + message,
                Toast.LENGTH_LONG).show();
    }

    public void onSelectDateClicked(View view) {
        DialogFragment fragmentDateSelection = DatePickerFragment.newInstance(classDate);
        fragmentDateSelection.show(getFragmentManager(), "datePicker");
    }

    public void onSelectTimeClicked(View view) {
        DialogFragment fragmentTimeSelection = TimePickerFragment.newInstance(classDate);
        fragmentTimeSelection.show(getFragmentManager(), "timePicker");
    }

    public void onSelectDurationClicked(View view) {
        int selectPosition = Integer.parseInt(classDuration)/15 + 1;
        DialogFragment fragmentDurationSelection = NumberPickerFragment.newInstance
                (getString(R.string.setDuration), allDurations, selectPosition);
        fragmentDurationSelection.show(getFragmentManager(), "durationPicker");
    }

    private void showDate() {
        SimpleDateFormat format = new SimpleDateFormat("EEEE, MMMM d, yyyy");
        tvClassDate.setText(format.format(classDate.getTime()));
    }

    private void showTime() {
        SimpleDateFormat format = new SimpleDateFormat("h:mm a");
        tvClassTime.setText(format.format(classDate.getTime()));
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

                    edtClassAddress.setText(classAddress);
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
        intent.putExtra(getString(R.string.EXTRA_MESSAGE_ADDRESS), classAddress);
        startActivityForResult(intent, REQUEST_ADDRESS_PICKER);
    }
}
