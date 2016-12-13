package ca.skillsup.androidapp.helper;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;

import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

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

    public String getAddressFromLocation(LatLng latLng) {
        Geocoder coder = new Geocoder(mContext);
        List<Address> address;

        try {
            if (coder.isPresent()) {
                address = coder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                if (address == null || address.size() == 0) {
                    String infoStr = "Address not found for " + latLng.toString();
                    Log.i(TAG, infoStr);
                    Toast.makeText(mContext, infoStr, Toast.LENGTH_SHORT).show();
                    return null;
                }
                String result = "";
                int i = 0;
                for (; i < address.get(0).getMaxAddressLineIndex() - 1; i++) {
                    result += address.get(0).getAddressLine(i) + ", ";
                }
                result += address.get(0).getAddressLine(i);
                return result;
            }
            else {
                String debugStr = "Geocoder not present!";
                Log.d(TAG, debugStr);
                Toast.makeText(mContext, debugStr, Toast.LENGTH_LONG).show();
            }
        } catch (Exception ex) {
            String errorStr = "Error when trying to resolv address from " + latLng.toString() +
                    "\n" + ex.getMessage();
            Log.e(TAG, errorStr);
            Toast.makeText(mContext, errorStr, Toast.LENGTH_LONG).show();
        }

        return null;
    }

    public LatLng getLocationFromAddress(String strAddress) {
        Geocoder coder = new Geocoder(mContext);

        try {
            if (coder.isPresent()) {
                List<Address> addresses = coder.getFromLocationName(strAddress, 1);
                if (addresses != null) {
                    Address location = addresses.get(0);
                    return (new LatLng(location.getLatitude(), location.getLongitude()));
                }
            }
        } catch (Exception ex) {
            String errorStr = "Error when trying to resolve location from " + strAddress +
                    "\n" + ex.getMessage();
            Log.e(TAG, errorStr);
            Toast.makeText(mContext, errorStr, Toast.LENGTH_LONG).show();
        }

        return null;
    }
}
