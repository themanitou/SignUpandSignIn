package ca.skillsup.androidapp.helper;

/**
 * Created by manitou on 11/3/16.
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import ca.skillsup.androidapp.app.AppController;

public class SessionManager {
    // LogCat tag
    private static String TAG = SessionManager.class.getSimpleName();

    // implement this class as a singleton
    private static SessionManager mInstance;

    // Shared Preferences
    private SharedPreferences pref, userPref;
    private Editor editor, userEditor;

    private String userEmail;
    private boolean isLoggedIn;

    private static final String PREFERENCE_FILENAME = "ca.skillsup.androidapp.USER_SESSION_DATA";
    private static final String PREFERENCE_KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String PREFERENCE_KEY_USER_EMAIL = "userEmail";
    private static final String PREFERENCE_KEY_CLASS_NAME = "className";
    private static final String PREFERENCE_KEY_CLASS_DATE_TIME = "classDateTime";
    private static final String PREFERENCE_KEY_CLASS_DURATION = "classDuration";
    private static final String PREFERENCE_KEY_CLASS_ADDRESS = "classAddress";
    private static final String PREFERENCE_KEY_CLASS_ADDRESS_LATITUDE = "classAddressLatitude";
    private static final String PREFERENCE_KEY_CLASS_ADDRESS_LONGITUDE = "classAddressLongitude";
    private static final String PREFERENCE_KEY_CLASS_DESCRIPTION = "classDescription";
    private static final String PREFERENCE_KEY_CLASS_FEE = "classFee";

    public static final String PREFERENCE_KEY_CLASS_DATE_TIME_PATTERN = "yyyy-MM-dd\'T\'HH:mm";

    private SessionManager() { }

    public static SessionManager getInstance() {
        if (mInstance == null) {
            Context mContext = AppController.getContext();

            mInstance = new SessionManager();
            mInstance.pref = mContext.getSharedPreferences(PREFERENCE_FILENAME, Context.MODE_PRIVATE);
            mInstance.editor = mInstance.pref.edit();

            mInstance.isLoggedIn = mInstance.pref.getBoolean(PREFERENCE_KEY_IS_LOGGED_IN, false);
            mInstance.userEmail = mInstance.pref.getString(PREFERENCE_KEY_USER_EMAIL, null);

            if (mInstance.isLoggedIn) {
                mInstance.initializeUserPref();
            }
            Log.i(TAG, "Create new global SessionManager");
        }
        return mInstance;
    }

    public void setLogout() {
        isLoggedIn = false;
        editor.putBoolean(PREFERENCE_KEY_IS_LOGGED_IN, false);
        editor.commit();

        userEditor = null;
        userPref = null;
    }

    public void setLogin(String email) {
        editor.putString(PREFERENCE_KEY_USER_EMAIL, email);
        editor.putBoolean(PREFERENCE_KEY_IS_LOGGED_IN, true);
        editor.commit();

        isLoggedIn = true;
        userEmail = email;
        initializeUserPref();

        Log.d(TAG, "Login session modified for " + email);
    }

    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    public void setUserEmail(String email) {
        userEmail = email;
        editor.putString(PREFERENCE_KEY_USER_EMAIL, email);
        editor.commit();

        Log.d(TAG, "Set user email " + email);
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setClassName(String className) {
        userEditor.putString(PREFERENCE_KEY_CLASS_NAME, className);
        userEditor.commit();

        Log.d(TAG, "Class name saved to session preference.");
    }

    public String getClassName() {
        return userPref.getString(PREFERENCE_KEY_CLASS_NAME, null);
    }

    public void setClassDateTime(String classDateTime) {
        userEditor.putString(PREFERENCE_KEY_CLASS_DATE_TIME, classDateTime);
        userEditor.commit();

        Log.d(TAG, "Class date saved to session preference.");
    }

    public String getClassDateTime() {
        return userPref.getString(PREFERENCE_KEY_CLASS_DATE_TIME, null);
    }

    public void setClassDuration(String classDuration) {
        userEditor.putString(PREFERENCE_KEY_CLASS_DURATION, classDuration);
        userEditor.commit();

        Log.d(TAG, "Class duration saved to session preference.");
    }

    public String getClassDuration() {
        return userPref.getString(PREFERENCE_KEY_CLASS_DURATION, null);
    }

    public void setClassAddress(String classAddress) {
        userEditor.putString(PREFERENCE_KEY_CLASS_ADDRESS, classAddress);
        userEditor.commit();

        Log.d(TAG, "Class address saved to session preference.");
    }

    public String getClassAddress() {
        return userPref.getString(PREFERENCE_KEY_CLASS_ADDRESS, null);
    }

    public void setClassAddressLatLng(LatLng classAddressLatLng) {
        Double lat = classAddressLatLng.latitude;
        Double lng = classAddressLatLng.longitude;

        userEditor.putString(PREFERENCE_KEY_CLASS_ADDRESS_LATITUDE, lat.toString());
        userEditor.putString(PREFERENCE_KEY_CLASS_ADDRESS_LONGITUDE, lng.toString());
        userEditor.commit();

        Log.d(TAG, "Class latitude and longitude saved to session preference.");
    }

    public LatLng getClassAddressLatLng() {
        String strLat = userPref.getString(PREFERENCE_KEY_CLASS_ADDRESS_LATITUDE, null);
        String strLng = userPref.getString(PREFERENCE_KEY_CLASS_ADDRESS_LONGITUDE, null);

        if (strLat == null || strLng == null) {
            return null;
        }

        return new LatLng(Double.parseDouble(strLat), Double.parseDouble(strLng));
    }

    public void setClassDescription(String classDescription) {
        userEditor.putString(PREFERENCE_KEY_CLASS_DESCRIPTION, classDescription);
        userEditor.commit();

        Log.d(TAG, "Class description saved to session preference.");
    }

    public String getClassDescription() {
        return userPref.getString(PREFERENCE_KEY_CLASS_DESCRIPTION, null);
    }

    public void setClassFee(float classFee) {
        userEditor.putFloat(PREFERENCE_KEY_CLASS_FEE, classFee);
        userEditor.commit();

        Log.d(TAG, "Class fee saved to session preference.");
    }

    public float getClassFee() {
        return userPref.getFloat(PREFERENCE_KEY_CLASS_FEE, 0);
    }

    private void initializeUserPref() {
        userPref = AppController.getContext().getSharedPreferences
                (PREFERENCE_FILENAME + userEmail, Context.MODE_PRIVATE);
        userEditor = userPref.edit();
    }

    public void clearUserPref() {
        userEditor.clear();
        userEditor.commit();

        Log.d(TAG, "Clear all preferences for " + userEmail);
    }

    public void clearPref() {
        editor.clear();
        editor.commit();

        Log.d(TAG, "Clear global preferences.");
    }
}
