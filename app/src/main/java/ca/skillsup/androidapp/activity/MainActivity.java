package ca.skillsup.androidapp.activity;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
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
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONObject;

import ca.skillsup.androidapp.R;
import ca.skillsup.androidapp.helper.PlaceManager;
import ca.skillsup.androidapp.helper.SQLiteHandler;
import ca.skillsup.androidapp.helper.SessionManager;

import java.util.HashMap;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {

    private static final String TAG = RegisterActivity.class.getSimpleName();

    private SessionManager session;
    private SQLiteHandler db;
    private PlaceManager placeManager;

    private NavigationView navigationView;
    private TextView nvHeaderText, nvHeaderEmail;

    private GoogleMap mMap;
    private MapFragment mapFragment;
    private View mapView;

    private static final int NAV_MENU_ACTION_GROUP = 90;
    private static final int NAV_MENU_ACTION_GROUP_SIGNIN = 9000;
    private static final int NAV_MENU_ACTION_GROUP_SIGNOUT = 9001;
    private static final int NAV_MENU_ACTION_GROUP_CREATECLASS = 9011;

    private static final int NAV_MENU_HELP_GROUP = 99;
    private static final int NAV_MENU_HELP_GROUP_MAKEWISH = 9900;
    private static final int NAV_MENU_HELP_GROUP_DEMO = 9901;
    private static final int NAV_MENU_HELP_GROUP_ABOUT = 9902;

    private static final int ACT_RES_SIGNIN = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

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
        db = new SQLiteHandler(getApplicationContext());
        // Session manager
        session = new SessionManager(getApplicationContext());

        // Place Manager
        placeManager = new PlaceManager(getApplicationContext());

        // set up onMapReadyCallback
        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.main_map);
        mapFragment.getMapAsync(this);
        mapView = mapFragment.getView();

        // build the navigation menu
        buildNavigationMenu();
    }

    private void buildNavigationMenu() {
        Menu menu = navigationView.getMenu();
        menu.clear();

        if (!session.isLoggedIn()) {
            nvHeaderText.setText("");
            nvHeaderEmail.setText("");
            menu.add(NAV_MENU_ACTION_GROUP,
                    NAV_MENU_ACTION_GROUP_SIGNIN,
                    Menu.NONE,
                    "Sign in");
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
                    "Create class");
            menu.add(NAV_MENU_ACTION_GROUP,
                    NAV_MENU_ACTION_GROUP_SIGNOUT,
                    Menu.NONE,
                    "Log out");
        }

        menu.add(NAV_MENU_HELP_GROUP, NAV_MENU_HELP_GROUP_MAKEWISH, Menu.NONE, "Make a wish");
        menu.add(NAV_MENU_HELP_GROUP, NAV_MENU_HELP_GROUP_DEMO, Menu.NONE, "Show demo");
        menu.add(NAV_MENU_HELP_GROUP, NAV_MENU_HELP_GROUP_ABOUT, Menu.NONE, "About");
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == NAV_MENU_ACTION_GROUP_SIGNIN) {
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivityForResult(intent, ACT_RES_SIGNIN);
        } else if (id == NAV_MENU_ACTION_GROUP_SIGNOUT) {
            session.setLogin(false);
            db.deleteUsers();

            Log.i(TAG, "onNavigationItemSelected: Logout successful.");
            Toast.makeText(this, "Logout successful",
                    Toast.LENGTH_SHORT).show();

            // rebuild the navigation menu
            buildNavigationMenu();
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
                try {
                    userInfo = new JSONObject(data.getDataString());
                    Log.i(TAG, "onActivityResult: Login successful.\n Welcome " + userInfo.getString("user"));
                    Toast.makeText(this, "Login successful:\n" +
                                    " Welcome " + userInfo.getString("user"),
                            Toast.LENGTH_LONG).show();
                }
                catch (Exception e) {
                    Log.d(TAG, "onActivityResult: Login successful, but unable to retrieve user info");
                    Toast.makeText(this, "Login successful, but unable to retrieve user info",
                            Toast.LENGTH_LONG).show();
                    Log.d(TAG, e.toString());
                }

                // rebuild the navigation menu
                buildNavigationMenu();
            }
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the MapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
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
            Log.w(TAG, "onMapReady: App does not have permission to access location service");
            Toast.makeText(this, "App does not have permission to access location service",
                    Toast.LENGTH_SHORT).show();

            return;
        }
        mMap.setMyLocationEnabled(true);

        Intent intent = getIntent();
        Location mLocation = placeManager.getLastKnownLocation();

        if (mLocation == null) {
            return;
        }

        // Add a marker and move the camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(
                new LatLng(mLocation.getLatitude(), mLocation.getLongitude())));

        // change zoom level
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
    }
}
