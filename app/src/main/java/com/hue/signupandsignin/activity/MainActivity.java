package com.hue.signupandsignin.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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

import com.hue.signupandsignin.R;
import com.hue.signupandsignin.helper.SQLiteHandler;
import com.hue.signupandsignin.helper.SessionManager;

import org.w3c.dom.Text;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private SessionManager session;
    private SQLiteHandler db;
    private NavigationView navigationView;
    private TextView nvHeaderText, nvHeaderEmail;

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
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        if (navigationView == null) {
            navigationView = (NavigationView) findViewById(R.id.nav_view);
        }
        navigationView.setNavigationItemSelectedListener(this);
        View hView = navigationView.getHeaderView(0);
        nvHeaderText = (TextView) hView.findViewById(R.id.nvHeaderText);
        nvHeaderEmail = (TextView) hView.findViewById(R.id.nvHeaderEmail);

        // SqLite database handler
        db = new SQLiteHandler(getApplicationContext());
        // Session manager
        session = new SessionManager(getApplicationContext());

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
        }
        else {
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
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
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
            // rebuild the navigation menu
            buildNavigationMenu();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ACT_RES_SIGNIN) {
            if (resultCode == RESULT_OK) {
                // rebuild the navigation menu
                buildNavigationMenu();
            }
        }
    }
}
