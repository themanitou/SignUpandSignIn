package ca.skillsup.androidapp.helper;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;

import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.content.Context.LOCATION_SERVICE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.location.LocationManager.GPS_PROVIDER;
import static android.location.LocationManager.NETWORK_PROVIDER;

/**
 * Created by manitou on 12/1/16.
 */

public class PlaceManager {
    // LogCat tag
    private static String TAG = PlaceManager.class.getSimpleName();

    Context mContext;

    public PlaceManager(Context context) {
        mContext = context;
    }

    public Location getLastKnownLocation() {
        if (ActivityCompat.checkSelfPermission(mContext, ACCESS_FINE_LOCATION) != PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(mContext, ACCESS_COARSE_LOCATION) != PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Log.w(TAG, "getLastKnownLocation: App does not have permission to access location service");
            Toast.makeText(mContext, "App does not have permission to access location service",
                    Toast.LENGTH_SHORT).show();

            return null;
        }
        LocationManager locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);
        Location mLastLocation = locationManager.getLastKnownLocation(NETWORK_PROVIDER);
        if (mLastLocation == null) {
            mLastLocation = locationManager.getLastKnownLocation(GPS_PROVIDER);
        }

        if (mLastLocation == null) {
            Log.w(TAG, "getLastKnownLocation: last location not known");
            Toast.makeText(mContext, "Last location not known",
                    Toast.LENGTH_SHORT).show();
        }

        return mLastLocation;
    }
}
