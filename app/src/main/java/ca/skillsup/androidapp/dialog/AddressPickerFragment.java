package ca.skillsup.androidapp.dialog;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.SphericalUtil;

import org.json.JSONException;
import org.json.JSONObject;

import ca.skillsup.androidapp.R;
import ca.skillsup.androidapp.helper.PlaceManager;

public class AddressPickerFragment extends FragmentActivity implements
        OnMapReadyCallback,
        GoogleMap.OnCameraIdleListener {

    // LogCat tag
    private static String TAG = AddressPickerFragment.class.getSimpleName();

    private GoogleMap mMap;
    private MapFragment mapFragment;
    private TextView tvSearchAddress;
    private ImageView imageView;

    private FloatingActionButton fabSelectAddress, fabSearchAddress;
    private String selectedAddress;
    private LatLng selectedLatlng;

    private PlaceManager placeManager;

    private final static int REQUEST_ADDRESS_AUTOCOMPLETE = 1000;

    private boolean ignoreCameraIdleEvent = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address_picker);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.address_picker_map);
        mapFragment.getMapAsync(this);
        tvSearchAddress = (TextView) findViewById(R.id.tvSearchAddress);
        imageView = (ImageView) findViewById(R.id.imgView);
        imageView.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        imageView.setScaleX(0.2f);
                        imageView.setScaleY(0.2f);
                    }
                }
        );

        fabSelectAddress = (FloatingActionButton) findViewById(R.id.fabSelectAddress);
        fabSelectAddress.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_done_36dp));

        fabSearchAddress = (FloatingActionButton) findViewById(R.id.fabSearchAddress);
        fabSearchAddress.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_search_36dp));

        Intent intent = getIntent();
        selectedAddress = intent.getStringExtra(getString(R.string.EXTRA_MESSAGE_ADDRESS));
        double latlng[] = getIntent().getDoubleArrayExtra(getString(R.string.EXTRA_MESSAGE_LATLNG));
        if (latlng.length == 2) {
            selectedLatlng = new LatLng(latlng[0],latlng[1]);
        }

        placeManager = PlaceManager.getInstance();
    }

    public void onOKButtonClicked(View view) {
        selectedAddress = tvSearchAddress.getText().toString();
        selectedLatlng = mMap.getCameraPosition().target;

        // return selected address and latitude longitude
        JSONObject classAddress = new JSONObject();
        try {
            classAddress.put(getString(R.string.EXTRA_MESSAGE_ADDRESS), selectedAddress);
            classAddress.put(getString(R.string.EXTRA_MESSAGE_LATITUDE), selectedLatlng.latitude);
            classAddress.put(getString(R.string.EXTRA_MESSAGE_LONGITUDE), selectedLatlng.longitude);
        }
        catch (JSONException e) {
            String errorString = "onOKButtonClicked: error returning class address " + selectedAddress +
                    " - " + selectedLatlng.toString() + "; exception: " + e.getMessage();
            Log.e(TAG, errorString);
            Toast.makeText(this, errorString, Toast.LENGTH_LONG).show();
        }

        Intent data = new Intent();
        data.setData(Uri.parse(classAddress.toString()));
        setResult(RESULT_OK, data);
        finish();
    }

    private void launchAddressAutocomplete() {
        int latlng_bound_radius = getResources().getInteger(R.integer.latlng_bound_radius);

        LatLng currentLatLng = mMap.getCameraPosition().target;
        LatLngBounds latLngBounds = new LatLngBounds.Builder().
                include(SphericalUtil.computeOffset(currentLatLng, latlng_bound_radius, 0)).
                include(SphericalUtil.computeOffset(currentLatLng, latlng_bound_radius, 90)).
                include(SphericalUtil.computeOffset(currentLatLng, latlng_bound_radius, 180)).
                include(SphericalUtil.computeOffset(currentLatLng, latlng_bound_radius, 270)).build();

        try {
            Intent intent = new PlaceAutocomplete.IntentBuilder
                    (PlaceAutocomplete.MODE_OVERLAY)
                    .setBoundsBias(latLngBounds)
                    .build(AddressPickerFragment.this);
            startActivityForResult(intent, REQUEST_ADDRESS_AUTOCOMPLETE);
        } catch (GooglePlayServicesRepairableException |
                GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }

    public void onFabSearchAddressClicked(View view) {
        launchAddressAutocomplete();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ADDRESS_AUTOCOMPLETE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                LatLng latLng = place.getLatLng();
                String addr = place.getAddress().toString();
                // add these entries to cache
                PlaceManager.addr2LatLngCache.put(addr, latLng);
                PlaceManager.latLng2AddrCache.put(latLng, addr);
                // move map view to the new address
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                tvSearchAddress.setText(addr);
                ignoreCameraIdleEvent = true;
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // in case app does not have permission to access location services
        if (!PlaceManager.checkLocationPermission()) {
            Toast.makeText(this, "App does not have permission to access location service",
                    Toast.LENGTH_SHORT).show();

            return;
        }

        mMap.setMyLocationEnabled(true);
        mMap.setOnCameraIdleListener(this);

        if (selectedAddress != null) {
            tvSearchAddress.setText(selectedAddress);
            ignoreCameraIdleEvent = true;
        }

        LatLng latlng = placeManager.getLocationFromAddress(selectedAddress);
        if (latlng != null) {
            selectedLatlng = latlng;
        }

        if (selectedLatlng != null) {
            // Move the camera
            mMap.moveCamera(CameraUpdateFactory.newLatLng(selectedLatlng));

            // change zoom level
            mMap.animateCamera(CameraUpdateFactory.zoomTo(getResources().getInteger(R.integer.map_zoom)));
        }
    }

    @Override
    public void onCameraIdle() {
        if (ignoreCameraIdleEvent) {
            ignoreCameraIdleEvent = false;
            return;
        }

        CameraPosition camPos = mMap.getCameraPosition();
        String addr = placeManager.getAddressFromLocation(camPos.target);
        if (addr != null) {
            tvSearchAddress.setText(addr);
        }
    }
}
