package ca.skillsup.androidapp.helper;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;

import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.List;
import java.util.concurrent.Callable;

import ca.skillsup.androidapp.app.AppController;

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

    // implement as a singleton
    private static PlaceManager mInstance;

    private static LocationManager locationManager;
    private static Geocoder geoCoder;

    private static Location lastKnownLocation;

    private static Cache<LatLng, String> latLng2AddrCache;
    private static Cache<String, LatLng> addr2LatLngCache;

    private static final int MAX_CACHE_ENTRIES = 1000;

    private PlaceManager() { }

    public static PlaceManager getInstance() {
        if (mInstance == null) {
            Context appContext = AppController.getContext();
            mInstance = new PlaceManager();

            locationManager = (LocationManager) appContext.getSystemService(LOCATION_SERVICE);
            geoCoder = new Geocoder(appContext);

            // initialize cache memory
            latLng2AddrCache = CacheBuilder.newBuilder().maximumSize(MAX_CACHE_ENTRIES).build();
            addr2LatLngCache = CacheBuilder.newBuilder().maximumSize(MAX_CACHE_ENTRIES).build();
        }
        return mInstance;
    }

    public static boolean checkLocationPermission() {
        Context appContext = AppController.getContext();
        if (ActivityCompat.checkSelfPermission(appContext, ACCESS_FINE_LOCATION) != PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(appContext, ACCESS_COARSE_LOCATION) != PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Log.w(TAG, "App does not have permission to access location services");

            return false;
        }

        return true;
    }

    public Location getLastKnownLocation() {
        if (lastKnownLocation != null) {
            return lastKnownLocation;
        }

        // if app does not have permission to access location service
        if (!checkLocationPermission()) {
            return null;
        }

        lastKnownLocation = locationManager.getLastKnownLocation(NETWORK_PROVIDER);
        if (lastKnownLocation == null) {
            lastKnownLocation = locationManager.getLastKnownLocation(GPS_PROVIDER);
        }

        return lastKnownLocation;
    }

    private String getAddressFromLocation_GeoCoder(LatLng latLng) {
        if (!Geocoder.isPresent()) {
            Log.e(TAG, "Geocoder not present!");
            return null;
        }

        List<Address> address = null;

        try {
            address = geoCoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
        } catch (Exception ex) {
            Log.e(TAG, "Error when trying to resolv address from " + latLng.toString() +
                    "\n" + ex.getMessage());
        }

        if (address == null || address.size() == 0) {
            Log.i(TAG, "Address not found for " + latLng.toString());
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

    public String getAddressFromLocation(final LatLng latLng) {
        String result = null;
        try {
            // if the address is not in cache then we'll fetch it from Google's API
            result = latLng2AddrCache.get(latLng, new Callable<String>() {
                @Override
                public String call() throws Exception {
                    String addr = getAddressFromLocation_GeoCoder(latLng);
                    if (addr != null) {
                        // also cache it in addr2LatLngCache
                        addr2LatLngCache.put(addr, latLng);
                    }
                    return addr;
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error with latLng2AddrCache: " + e.getMessage());
        }

        return result;
    }

    private LatLng getLocationFromAddress_GeoCoder(String strAddress) {
        if (!Geocoder.isPresent()) {
            Log.e(TAG, "Geocoder not present!");
            return null;
        }

        List<Address> addresses = null;
        try {
            addresses = geoCoder.getFromLocationName(strAddress, 1);
        } catch (Exception ex) {
            Log.e(TAG, "Error when trying to resolve location from " + strAddress +
                    "\n" + ex.getMessage());
        }

        if (addresses == null || addresses.size() == 0) {
            Log.i(TAG, "Location not found for " + strAddress);
            return null;
        }

        Address location = addresses.get(0);
        return (new LatLng(location.getLatitude(), location.getLongitude()));
    }

    public LatLng getLocationFromAddress(final String strAddress) {
        LatLng result = null;

        try {
            // if the address is not in cache then we'll fetch it from Google's API
            result = addr2LatLngCache.get(strAddress, new Callable<LatLng>() {
                @Override
                public LatLng call() throws Exception {
                    LatLng latLng = getLocationFromAddress_GeoCoder(strAddress);
                    if (latLng != null) {
                        // also cache it in latLng2AddrCache
                        latLng2AddrCache.put(latLng, strAddress);
                    }
                    return latLng;
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error with addr2LatLngCache: " + e.getMessage());
        }

        return result;
    }
}
