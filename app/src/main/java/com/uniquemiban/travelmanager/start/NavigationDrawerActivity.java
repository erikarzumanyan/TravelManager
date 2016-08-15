package com.uniquemiban.travelmanager.start;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.uniquemiban.travelmanager.utils.Constants;
import com.uniquemiban.travelmanager.R;
import com.uniquemiban.travelmanager.login.LoginActivity;
import com.uniquemiban.travelmanager.sight.SightFragment;
import com.uniquemiban.travelmanager.sight.SightsListFragment;

public class NavigationDrawerActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout mDrawer;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_drawer);

//        //Check if google play services is up to date
//        final int playServicesStatus = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
//        if(playServicesStatus != ConnectionResult.SUCCESS){
//            //If google play services in not available show an error dialog and return
//            final Dialog errorDialog = GoogleApiAvailability.getInstance().getErrorDialog(this, playServicesStatus, 0, null);
//            errorDialog.show();
//            return;
//        }

        if (FirebaseAuth.getInstance().getCurrentUser() == null
                && !getSharedPreferences(Constants.SHARED_PREFS, MODE_PRIVATE).getBoolean(LoginActivity.SHARED_SKIP, false)) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        FragmentManager manager = getSupportFragmentManager();
        Fragment fragmentSightsList = manager.findFragmentByTag(SightsListFragment.FRAGMENT_TAG);
        Fragment fragmentSight = manager.findFragmentByTag(SightFragment.FRAGMENT_TAG);

        if (fragmentSightsList == null && fragmentSight == null) {
            fragmentSightsList = new SightsListFragment();
            manager.beginTransaction()
                    .add(R.id.fragment_container, fragmentSightsList, SightsListFragment.FRAGMENT_TAG)
                    .commit();
        }
    }

    public DrawerLayout getDrawer() {
        return mDrawer;
    }

    public Toolbar getToolbar() {
        return mToolbar;
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
        getMenuInflater().inflate(R.menu.navigation_drawer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_sightseeing) {

        } else if (id == R.id.nav_sleeping) {

        } else if (id == R.id.nav_eating) {

        } else if (id == R.id.nav_tours) {

        } else if (id == R.id.nav_map) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
