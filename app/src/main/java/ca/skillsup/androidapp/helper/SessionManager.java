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

    // Shared Preferences
    SharedPreferences pref, userPref;

    Editor editor, userEditor;
    Context _context;

    public SessionManager(Context context) {
        this._context = context;
        pref = context.getSharedPreferences(context.getString(R.string.preference_filename),
                context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public void setLogin(boolean isLoggedIn) {

        editor.putBoolean(_context.getString(R.string.preference_key_is_logged_in), isLoggedIn);

        // commit changes
        editor.commit();

        Log.d(TAG, "User login session modified!");
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(_context.getString(R.string.preference_key_is_logged_in), false);
    }

    public void setUserEmail(String userEmail) {
        editor.putString(_context.getString(R.string.preference_key_user_email), userEmail);

        // commit changes
        editor.commit();

        Log.d(TAG, "User email saved to session preference.");
    }

    public String getUserEmail() {
        return pref.getString(_context.getString(R.string.preference_key_user_email), null);
    }

    public void setClassDateTime(String classDateTime) {
        if (userPref == null) {
            initializeUserPref();
        }

        if (userPref == null) {
            // initializing user preferences has failed
            return;
        }

        userEditor.putString(_context.getString(R.string.preference_key_class_date_time), classDateTime);

        // commit changes
        userEditor.commit();

        Log.d(TAG, "Class date saved to session preference.");
    }

    public String getClassDateTime() {
        if (userPref == null) {
            initializeUserPref();
        }

        if (userPref == null) {
            // initializing user preferences has failed
            return null;
        }

        return userPref.getString(_context.getString(R.string.preference_key_class_date_time), null);
    }

    public void setClassAddress(String classAddress) {
        if (userPref == null) {
            initializeUserPref();
        }

        if (userPref == null) {
            // initializing user preferences has failed
            return;
        }

        userEditor.putString(_context.getString(R.string.preference_key_class_address), classAddress);

        // commit changes
        userEditor.commit();

        Log.d(TAG, "Class address saved to session preference.");
    }

    public String getClassAddress() {
        if (userPref == null) {
            initializeUserPref();
        }

        if (userPref == null) {
            // initializing user preferences has failed
            return null;
        }

        return userPref.getString(_context.getString(R.string.preference_key_class_address), null);
    }

    public void setClassAddressLatLng(LatLng classAddressLatLng) {
        if (userPref == null) {
            initializeUserPref();
        }

        if (userPref == null) {
            // initializing user preferences has failed
            return;
        }

        Double lat = classAddressLatLng.latitude;
        Double lng = classAddressLatLng.longitude;

        userEditor.putString(_context.getString(R.string.preference_key_class_address_latitude), lat.toString());
        userEditor.putString(_context.getString(R.string.preference_key_class_address_longitude), lng.toString());

        // commit changes
        userEditor.commit();

        Log.d(TAG, "Class latitude and longitude saved to session preference.");
    }

    public LatLng getClassAddressLatLng() {
        if (userPref == null) {
            initializeUserPref();
        }

        if (userPref == null) {
            // initializing user preferences has failed
            return null;
        }

        String strLat = userPref.getString(_context.getString(R.string.preference_key_class_address_latitude), null);
        String strLng = userPref.getString(_context.getString(R.string.preference_key_class_address_longitude), null);

        if (strLat == null || strLng == null) {
            return null;
        }

        return new LatLng(Double.parseDouble(strLat), Double.parseDouble(strLng));
    }

    private void initializeUserPref() {
        String userEmail = pref.getString(_context.getString(R.string.preference_key_user_email), null);

        if (userEmail == null) {
            // this should not happen
            Log.e(TAG, "User email is null in SharedPreference.");
            return;
        }

        userPref = _context.getSharedPreferences(
                _context.getString(R.string.preference_filename) + userEmail,
                _context.MODE_PRIVATE);
        userEditor = userPref.edit();
    }
}
