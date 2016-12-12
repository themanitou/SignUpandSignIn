package ca.skillsup.androidapp.dialog;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
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

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class AddressPickerFragment extends FragmentActivity implements
        OnMapReadyCallback,
        GoogleMap.OnCameraIdleListener {

    // LogCat tag
    private static String TAG = AddressPickerFragment.class.getSimpleName();

    private GoogleMap mMap;
    private MapFragment mapFragment;
    private TextView tvSearchAddress;
    private ImageView imageView;

    private String selectedAddress;
    private LatLng selectedLatlng;

    private PlaceManager placeManager;

    private final static int REQUEST_ADDRESS_AUTOCOMPLETE = 1000;
    private final static int LATLNG_BOUND_RADIUS = 75;

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

        Intent intent = getIntent();
        selectedAddress = intent.getStringExtra(getString(R.string.EXTRA_MESSAGE_ADDRESS));
        double latlng[] = getIntent().getDoubleArrayExtra(getString(R.string.EXTRA_MESSAGE_LATLNG));
        if (latlng.length == 2) {
            selectedLatlng = new LatLng(latlng[0],latlng[1]);
        }

        placeManager = new PlaceManager(this);
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

    public void onTvClassAddressClicked(View view) {
        LatLng currentLatLng = mMap.getCameraPosition().target;
        LatLngBounds latLngBounds = new LatLngBounds.Builder().
                include(SphericalUtil.computeOffset(currentLatLng, LATLNG_BOUND_RADIUS, 0)).
                include(SphericalUtil.computeOffset(currentLatLng, LATLNG_BOUND_RADIUS, 90)).
                include(SphericalUtil.computeOffset(currentLatLng, LATLNG_BOUND_RADIUS, 180)).
                include(SphericalUtil.computeOffset(currentLatLng, LATLNG_BOUND_RADIUS, 270)).build();

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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ADDRESS_AUTOCOMPLETE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                // move map view to the new address
                mMap.moveCamera(CameraUpdateFactory.newLatLng(place.getLatLng()));
                tvSearchAddress.setText(place.getAddress());
                ignoreCameraIdleEvent = true;
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) != PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            String errorString = "onMapReady: App does not have permission to access location service";
            Log.w(TAG, errorString);
            Toast.makeText(this, errorString, Toast.LENGTH_SHORT).show();

            return;
        }

        mMap.setMyLocationEnabled(true);
        mMap.setOnCameraIdleListener(this);

        if (selectedLatlng != null) {
            // Move the camera
            mMap.moveCamera(CameraUpdateFactory.newLatLng(selectedLatlng));

            // change zoom level
            mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        }

        if (selectedAddress != null) {
            tvSearchAddress.setText(selectedAddress);
            ignoreCameraIdleEvent = true;
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
