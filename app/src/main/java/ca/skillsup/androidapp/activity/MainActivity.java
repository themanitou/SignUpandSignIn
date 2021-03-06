package ca.skillsup.androidapp.activity;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONObject;

import ca.skillsup.androidapp.R;
import ca.skillsup.androidapp.helper.PlaceManager;
import ca.skillsup.androidapp.helper.SQLiteHandler;
import ca.skillsup.androidapp.helper.SessionManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {

    private static final String TAG = RegisterActivity.class.getSimpleName();

    private SessionManager sessionManager;
    private SQLiteHandler db;
    private PlaceManager placeManager;

    private NavigationView navigationView;
    private TextView nvHeaderText, nvHeaderEmail;

    private FloatingActionButton fabMain;

    private GoogleMap mMap;
    private MapFragment mapFragment;

    private static final int NAV_MENU_ACTION_GROUP = 90;
    private static final int NAV_MENU_ACTION_GROUP_SIGNIN = 9000;
    private static final int NAV_MENU_ACTION_GROUP_SIGNOUT = 9001;
    private static final int NAV_MENU_ACTION_GROUP_CREATECLASS = 9011;

    private static final int NAV_MENU_HELP_GROUP = 99;
    private static final int NAV_MENU_HELP_GROUP_MAKEWISH = 9900;
    private static final int NAV_MENU_HELP_GROUP_DEMO = 9901;
    private static final int NAV_MENU_HELP_GROUP_ABOUT = 9902;

    private static final int ACT_RES_SIGNIN = 10;
    private static final int ACT_RES_CREATECLASS = 20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fabMain = (FloatingActionButton) findViewById(R.id.fab);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, ca.skillsup.androidapp.R.string.navigation_drawer_open, ca.skillsup.androidapp.R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View hView = navigationView.getHeaderView(0);
        nvHeaderText = (TextView) hView.findViewById(R.id.nvHeaderText);
        nvHeaderEmail = (TextView) hView.findViewById(R.id.nvHeaderEmail);

        // SqLite database handler
        db = new SQLiteHandler(this);
        // Session manager
        sessionManager = SessionManager.getInstance();

        // Place Manager
        placeManager = PlaceManager.getInstance();

        // set up onMapReadyCallback
        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.main_map);
        mapFragment.getMapAsync(this);

        // build the navigation menu
        buildNavigationMenu();

        // assign an action to floating action button
        assignActionToFab();
    }

    private void buildNavigationMenu() {
        Menu menu = navigationView.getMenu();
        menu.clear();

        if (!sessionManager.isLoggedIn()) {
            nvHeaderText.setText("");
            nvHeaderEmail.setText("");
            menu.add(NAV_MENU_ACTION_GROUP,
                    NAV_MENU_ACTION_GROUP_SIGNIN,
                    Menu.NONE,
                    getString(R.string.title_activity_sign_in))
                    .setIcon(ContextCompat.getDrawable(this, R.drawable.ic_person_24dp));
        } else {
            // Fetching user details from sqlite
            HashMap<String, String> user = db.getUserDetails();
            String name = user.get("name");
            String email = user.get("email");
            nvHeaderText.setText(name);
            nvHeaderEmail.setText(email);
            menu.add(NAV_MENU_ACTION_GROUP,
                    NAV_MENU_ACTION_GROUP_CREATECLASS,
                    Menu.NONE,
                    getString(R.string.title_activity_create_class))
                    .setIcon(ContextCompat.getDrawable(this, R.drawable.ic_share_24dp));
            menu.add(NAV_MENU_ACTION_GROUP,
                    NAV_MENU_ACTION_GROUP_SIGNOUT,
                    99,
                    getString(R.string.title_activity_sign_out));
        }

        menu.add(NAV_MENU_HELP_GROUP, NAV_MENU_HELP_GROUP_MAKEWISH, Menu.NONE, "Make a wish");
        menu.add(NAV_MENU_HELP_GROUP, NAV_MENU_HELP_GROUP_DEMO, Menu.NONE, "Show demo");
        menu.add(NAV_MENU_HELP_GROUP, NAV_MENU_HELP_GROUP_ABOUT, Menu.NONE, "About");
    }

    private void assignActionToFab() {
        if (!sessionManager.isLoggedIn()) {
            fabMain.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    userLogin();
                }
            });
            fabMain.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_person_36dp));
        }
        else {
            fabMain.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    createClass();
                }
            });
            fabMain.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_share_36dp));
        }
    }

    private void userLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivityForResult(intent, ACT_RES_SIGNIN);
    }

    private void createClass() {
        CameraPosition camPos = mMap.getCameraPosition();

        Intent intent = new Intent(this, CreateClassActivity.class);
        intent.putExtra(getString(R.string.EXTRA_MESSAGE_LATLNG),
                new double[] { camPos.target.latitude, camPos.target.longitude });
        startActivityForResult(intent, ACT_RES_CREATECLASS);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(ca.skillsup.androidapp.R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == NAV_MENU_ACTION_GROUP_SIGNIN) {
            userLogin();
        }
        else if (id == NAV_MENU_ACTION_GROUP_SIGNOUT) {
            sessionManager.setLogout();
            db.deleteUsers();

            Log.i(TAG, "onNavigationItemSelected: Logout successful.");
            Toast.makeText(this, "Logout successful",
                    Toast.LENGTH_SHORT).show();

            // rebuild the navigation menu
            buildNavigationMenu();

            // assign an action to floating action button
            assignActionToFab();
        }
        else if (id == NAV_MENU_ACTION_GROUP_CREATECLASS) {
            createClass();
        }
        else if (id == NAV_MENU_HELP_GROUP_MAKEWISH) {
            sessionManager.clearUserPref();
        }
        else if (id == NAV_MENU_HELP_GROUP_DEMO) {
            sessionManager.clearPref();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(ca.skillsup.androidapp.R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ACT_RES_SIGNIN) {
            if (resultCode == RESULT_OK) {
                JSONObject userInfo;
                String userName;
                try {
                    userInfo = new JSONObject(data.getDataString());
                    userName = userInfo.getString("name");
                    String infoString = "onActivityResult: Login successful.\n Welcome " + userName;
                    Log.i(TAG, infoString);
                    Toast.makeText(this, infoString, Toast.LENGTH_LONG).show();
                }
                catch (Exception e) {
                    String errorString = "onActivityResult: Login successful, but unable to retrieve user info.\n" +
                            e.getMessage();
                    Log.d(TAG, errorString);
                    Toast.makeText(this, errorString, Toast.LENGTH_LONG).show();
                }

                // rebuild the navigation menu
                buildNavigationMenu();

                // assign an action to floating action button
                assignActionToFab();
            }
        }
        else if (requestCode == ACT_RES_CREATECLASS) {
            if (resultCode == RESULT_OK) {
                try {
                    JSONObject classDetails = new JSONObject(data.getDataString());

                    String className = classDetails.getString(getString(R.string.EXTRA_MESSAGE_NAME));

                    String strClassDate = classDetails.getString(getString(R.string.EXTRA_MESSAGE_DATETIME));
                    Date classDate = new SimpleDateFormat(SessionManager.PREFERENCE_KEY_CLASS_DATE_TIME_PATTERN).parse(strClassDate);

                    String classAddress = classDetails.getString(getString(R.string.EXTRA_MESSAGE_ADDRESS));
                    Double lat = classDetails.getDouble(getString(R.string.EXTRA_MESSAGE_LATITUDE));
                    Double lng = classDetails.getDouble(getString(R.string.EXTRA_MESSAGE_LONGITUDE));
                    LatLng classLatLng = new LatLng(lat, lng);

                    // add marker on map
                    addMarkerOnMap(className, classDate, classAddress, classLatLng);

                    // move camera to this location
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(
                            new LatLng(lat, lng)));

                    // change zoom level
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(getResources().getInteger(R.integer.map_zoom)));
                }
                catch (Exception e) {
                    String errorString = "Error getting class details\n" +
                            e.getMessage();
                    Log.e(TAG, errorString);
                    Toast.makeText(this, errorString, Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private void addMarkerOnMap(String className, Date classDate,
                                String classAddress, LatLng classLatLng) {
        SimpleDateFormat format = new SimpleDateFormat("EEEE, MMMM d, yyyy 'at' h:mm a");
        String strDate = format.format(classDate);

        String markerSnippet = "Date: " + strDate + "\nAddress: " + classAddress;
        mMap.addMarker(new MarkerOptions().position(classLatLng)
                .title(className)
                .snippet(markerSnippet));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // in case app does not have permission to access location services
        if (!PlaceManager.checkLocationPermission()) {
            String warningStr = "App does not have permission to access location service";
            Log.w(TAG, warningStr);
            Toast.makeText(this, warningStr, Toast.LENGTH_SHORT).show();

            return;
        }

        mMap.setMyLocationEnabled(true);
        Location mLocation = placeManager.getLastKnownLocation();
        if (mLocation == null) {
            String warningStr = "Last known location is not available";
            Log.w(TAG, warningStr);
            Toast.makeText(this, warningStr, Toast.LENGTH_SHORT).show();

            return;
        }

        // Add a marker and move the camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(
                new LatLng(mLocation.getLatitude(), mLocation.getLongitude())));

        // change zoom level
        mMap.animateCamera(CameraUpdateFactory.zoomTo(getResources().getInteger(R.integer.map_zoom)));
    }
}
