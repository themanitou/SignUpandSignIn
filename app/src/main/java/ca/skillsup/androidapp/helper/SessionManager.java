package ca.skillsup.androidapp.helper;

/**
 * Created by manitou on 11/3/16.
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.renderscript.Double2;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import ca.skillsup.androidapp.R;

public class SessionManager {
    // LogCat tag
    private static String TAG = SessionManager.class.getSimpleName();

    // implement this class as a singleton
    private static SessionManager mInstance;

    // Shared Preferences
    private SharedPreferences pref, userPref;

    private Editor editor, userEditor;
    private Context _context;

    private String userEmail;
    private boolean isLoggedIn;

    private SessionManager() { }

    public static SessionManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new SessionManager();
            mInstance._context = context;
            mInstance.pref = context.getSharedPreferences(context.getString(R.string.preference_filename),
                    context.MODE_PRIVATE);
            mInstance.editor = mInstance.pref.edit();

            mInstance.isLoggedIn = mInstance.pref.getBoolean(mInstance._context.getString(R.string.preference_key_is_logged_in), false);
            mInstance.userEmail = mInstance.pref.getString(mInstance._context.getString(R.string.preference_key_user_email), null);

            if (mInstance.isLoggedIn) {
                mInstance.initializeUserPref();
            }
            Log.i(TAG, "Create new global SessionManager");
        }
        return mInstance;
    }

    public void setLogout() {
        isLoggedIn = false;
        editor.putBoolean(_context.getString(R.string.preference_key_is_logged_in), false);
        editor.commit();

        userEditor = null;
        userPref = null;
    }

    public void setLogin(String email) {
        editor.putString(_context.getString(R.string.preference_key_user_email), email);
        editor.putBoolean(_context.getString(R.string.preference_key_is_logged_in), true);
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
        editor.putString(_context.getString(R.string.preference_key_user_email), email);
        editor.commit();

        Log.d(TAG, "Set user email " + email);
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setClassName(String className) {
        userEditor.putString(_context.getString(R.string.preference_key_class_name), className);
        userEditor.commit();

        Log.d(TAG, "Class name saved to session preference.");
    }

    public String getClassName() {
        return userPref.getString(_context.getString(R.string.preference_key_class_name), null);
    }

    public void setClassDateTime(String classDateTime) {
        userEditor.putString(_context.getString(R.string.preference_key_class_date_time), classDateTime);
        userEditor.commit();

        Log.d(TAG, "Class date saved to session preference.");
    }

    public String getClassDateTime() {
        return userPref.getString(_context.getString(R.string.preference_key_class_date_time), null);
    }

    public void setClassDuration(String classDuration) {
        userEditor.putString(_context.getString(R.string.preference_key_class_duration), classDuration);
        userEditor.commit();

        Log.d(TAG, "Class duration saved to session preference.");
    }

    public String getClassDuration() {
        return userPref.getString(_context.getString(R.string.preference_key_class_duration), null);
    }

    public void setClassAddress(String classAddress) {
        userEditor.putString(_context.getString(R.string.preference_key_class_address), classAddress);
        userEditor.commit();

        Log.d(TAG, "Class address saved to session preference.");
    }

    public String getClassAddress() {
        return userPref.getString(_context.getString(R.string.preference_key_class_address), null);
    }

    public void setClassAddressLatLng(LatLng classAddressLatLng) {
        Double lat = classAddressLatLng.latitude;
        Double lng = classAddressLatLng.longitude;

        userEditor.putString(_context.getString(R.string.preference_key_class_address_latitude), lat.toString());
        userEditor.putString(_context.getString(R.string.preference_key_class_address_longitude), lng.toString());
        userEditor.commit();

        Log.d(TAG, "Class latitude and longitude saved to session preference.");
    }

    public LatLng getClassAddressLatLng() {
        String strLat = userPref.getString(_context.getString(R.string.preference_key_class_address_latitude), null);
        String strLng = userPref.getString(_context.getString(R.string.preference_key_class_address_longitude), null);

        if (strLat == null || strLng == null) {
            return null;
        }

        return new LatLng(Double.parseDouble(strLat), Double.parseDouble(strLng));
    }

    public void setClassDescription(String classDescription) {
        userEditor.putString(_context.getString(R.string.preference_key_class_description), classDescription);
        userEditor.commit();

        Log.d(TAG, "Class description saved to session preference.");
    }

    public String getClassDescription() {
        return userPref.getString(_context.getString(R.string.preference_key_class_description), null);
    }

    public void setClassFee(float classFee) {
        userEditor.putFloat(_context.getString(R.string.preference_key_class_fee), classFee);
        userEditor.commit();

        Log.d(TAG, "Class fee saved to session preference.");
    }

    public float getClassFee() {
        return userPref.getFloat(_context.getString(R.string.preference_key_class_fee), 0);
    }

    private void initializeUserPref() {
        userPref = _context.getSharedPreferences(
                _context.getString(R.string.preference_filename) + userEmail,
                _context.MODE_PRIVATE);
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
