package com.uniquemiban.travelmanager.start;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.uniquemiban.travelmanager.eat.EatListFragment;
import com.uniquemiban.travelmanager.utils.Constants;
import com.uniquemiban.travelmanager.R;
import com.uniquemiban.travelmanager.login.LoginActivity;
import com.uniquemiban.travelmanager.sight.SightsListFragment;

public class NavigationDrawerActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private DrawerLayout mDrawer;
    private Toolbar mToolbar;
    private int mWidth = 500;

    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int uiOps = getWindow().getDecorView().getSystemUiVisibility();
        uiOps |= View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        getWindow().getDecorView().setSystemUiVisibility(uiOps);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_drawer);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;

        mWidth = Math.min(width, height);

//        //Check if google play services is up to date
//        final int playServicesStatus = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
//        if(playServicesStatus != ConnectionResult.SUCCESS){
//            //If google play services in not available show an error dialog and return
//            final Dialog errorDialog = GoogleApiAvailability.getInstance().getErrorDialog(this, playServicesStatus, 0, null);
//            errorDialog.show();
//            return;
//        }

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null
                && !getSharedPreferences(Constants.SHARED_PREFS, MODE_PRIVATE).getBoolean(LoginActivity.SHARED_SKIP, false)) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }

        SharedPreferences userPrefs = getSharedPreferences(Constants.FIREBASE_USERS, MODE_PRIVATE);
        if (user != null && userPrefs != null) {
            View header = ((NavigationView) findViewById(R.id.nav_view)).getHeaderView(0);
            ((TextView) header.findViewById(R.id.text_view_user_name_nav_header)).setText(userPrefs.getString(LoginActivity.SHARED_NAME, ""));
            ((TextView) header.findViewById(R.id.text_view_email_nav_header)).setText(user.getEmail());
        }

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        FragmentManager manager = getSupportFragmentManager();
        SightsListFragment fragment = new SightsListFragment();
        manager.beginTransaction()
                .replace(R.id.fragment_container, fragment, SightsListFragment.FRAGMENT_TAG)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    public Location getLastLocation(){
        return mLastLocation;
    }

    public int getWidth() {
        return mWidth;
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

        FragmentManager manager = getSupportFragmentManager();

        if (id == R.id.nav_sightseeing) {

            Fragment fragment = manager.findFragmentByTag(SightsListFragment.FRAGMENT_TAG);

            if (fragment == null) {
                fragment = new SightsListFragment();
                manager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                manager.beginTransaction()
                        .replace(R.id.fragment_container, fragment, SightsListFragment.FRAGMENT_TAG)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        .commit();
            }

        } else if (id == R.id.nav_sleeping) {

        } else if (id == R.id.nav_eating) {

            Fragment fragment = manager.findFragmentByTag(EatListFragment.FRAGMENT_TAG);

            if (fragment == null) {
                fragment = new EatListFragment();
                manager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                manager.beginTransaction()
                        .replace(R.id.fragment_container, fragment, EatListFragment.FRAGMENT_TAG)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        .commit();
            }

        } else if (id == R.id.nav_tours) {

        } else if (id == R.id.nav_map) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_sign_in) {
            getSharedPreferences(Constants.SHARED_PREFS, MODE_PRIVATE).edit().putBoolean(LoginActivity.SHARED_SKIP, false).commit();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        } else if (id == R.id.nav_sign_out) {
            FirebaseAuth.getInstance().signOut();
            getSharedPreferences(Constants.SHARED_PREFS, MODE_PRIVATE).edit().putBoolean(LoginActivity.SHARED_SKIP, false).commit();
            getSharedPreferences(Constants.FIREBASE_USERS, MODE_PRIVATE).edit().putString(LoginActivity.SHARED_NAME, "").commit();
            View header = ((NavigationView) findViewById(R.id.nav_view)).getHeaderView(0);
            ((TextView) header.findViewById(R.id.text_view_user_name_nav_header)).setText("");
            ((TextView) header.findViewById(R.id.text_view_email_nav_header)).setText("");
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onConnected(@Nullable Bundle pBundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Needed Location Permission", Toast.LENGTH_LONG).show();
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);

        if(mLastLocation != null) {
            getSharedPreferences(Constants.SHARED_PREFS_SIGHT, Context.MODE_PRIVATE).edit()
                    .putString(Constants.SHARED_PREFS_KEY_LAST_LAT, mLastLocation.getLatitude() + "").commit();
            getSharedPreferences(Constants.SHARED_PREFS_SIGHT, Context.MODE_PRIVATE).edit()
                    .putString(Constants.SHARED_PREFS_KEY_LAST_LONG, mLastLocation.getLongitude() + "").commit();
        }

    }

    @Override
    public void onConnectionSuspended(int pI) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult pConnectionResult) {

    }
}
