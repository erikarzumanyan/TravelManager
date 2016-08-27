package com.uniquemiban.travelmanager.start;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.uniquemiban.travelmanager.eat.EatListFragment;
import com.uniquemiban.travelmanager.map.GmapFragment;
import com.uniquemiban.travelmanager.map.GmapMainFragment;
import com.uniquemiban.travelmanager.sleep.SleepListFragment;
import com.uniquemiban.travelmanager.tour.TourListFragment;
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

        final SharedPreferences userPrefs = getSharedPreferences(Constants.FIREBASE_USERS, MODE_PRIVATE);
        if (user != null && userPrefs != null) {
            final View header = ((NavigationView) findViewById(R.id.nav_view)).getHeaderView(0);
            ((TextView) header.findViewById(R.id.text_view_email_nav_header)).setText(user.getEmail());

            String name = userPrefs.getString(LoginActivity.SHARED_NAME, "");

            if(!TextUtils.isEmpty(name)){
                ((TextView) header.findViewById(R.id.text_view_user_name_nav_header)).setText(name);
            } else{
                final DatabaseReference nameReference = FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUid()).child("Name");

                nameReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot pDataSnapshot) {
                        if(pDataSnapshot != null){
                            String name = pDataSnapshot.getValue(String.class);
                            userPrefs.edit().putString(LoginActivity.SHARED_NAME, name).commit();
                            ((TextView) header.findViewById(R.id.text_view_user_name_nav_header)).setText(name);
                        }

                        nameReference.removeEventListener(this);
                    }

                    @Override
                    public void onCancelled(DatabaseError pDatabaseError) {
                        nameReference.removeEventListener(this);
                    }
                });
            }

            String photoUrl = userPrefs.getString(LoginActivity.SHARED_PHOTO_URL, "");
            final RoundedImageView headImage = (RoundedImageView) header.findViewById(R.id.image_view_profile_pic_nav_header);
            if(!TextUtils.isEmpty(photoUrl)){

                Picasso.with(this)
                        .load(photoUrl)
                        .resize(200, 200)
                        .centerCrop()
                        .into(headImage);
            } else {
                final DatabaseReference photoReference = FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUid()).child("PhotoUrl");

                photoReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot pDataSnapshot) {
                        if(pDataSnapshot != null){
                            String url = pDataSnapshot.getValue(String.class);

                            userPrefs.edit().putString(LoginActivity.SHARED_PHOTO_URL, pDataSnapshot.getValue(String.class)).commit();

                            Picasso.with(NavigationDrawerActivity.this)
                                    .load(url)
                                    .resize(200, 200)
                                    .centerCrop()
                                    .into(headImage);
                        }

                        photoReference.removeEventListener(this);
                    }

                    @Override
                    public void onCancelled(DatabaseError pDatabaseError) {
                        photoReference.removeEventListener(this);
                    }
                });
            }
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
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    public void connect(){mGoogleApiClient.connect();}
    public void disconnect(){mGoogleApiClient.disconnect();}

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
            Fragment fragment = manager.findFragmentByTag(SleepListFragment.FRAGMENT_TAG);

            if (fragment == null) {
                fragment = new SleepListFragment();
                manager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                manager.beginTransaction()
                        .replace(R.id.fragment_container, fragment, SleepListFragment.FRAGMENT_TAG)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        .commit();
            }

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
            Fragment fragment = manager.findFragmentByTag(TourListFragment.FRAGMENT_TAG);

            if (fragment == null) {
                fragment = new TourListFragment();
                manager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                manager.beginTransaction()
                        .replace(R.id.fragment_container, fragment, TourListFragment.FRAGMENT_TAG)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        .commit();
            }
        } else if (id == R.id.nav_map) {
            Fragment fragment = manager.findFragmentByTag(GmapMainFragment.FRAGMENT_TAG);

            if (fragment == null) {
                fragment =new GmapMainFragment();
                manager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                manager.beginTransaction()
                        .replace(R.id.fragment_container, fragment, GmapMainFragment.FRAGMENT_TAG)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        .commit();
            }


        }  else if (id == R.id.nav_sign_in) {
            View header = ((NavigationView) findViewById(R.id.nav_view)).getHeaderView(0);
            header.findViewById(R.id.image_view_profile_pic_nav_header).setVisibility(View.VISIBLE);

            getSharedPreferences(Constants.SHARED_PREFS, MODE_PRIVATE).edit().putBoolean(LoginActivity.SHARED_SKIP, false).commit();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        } else if (id == R.id.nav_sign_out) {
            String photoUrl = getSharedPreferences(Constants.FIREBASE_USERS, MODE_PRIVATE).getString(LoginActivity.SHARED_PHOTO_URL, "");
            if(photoUrl != null)
                Picasso.with(this).invalidate(photoUrl);

            getSharedPreferences(Constants.FIREBASE_USERS, MODE_PRIVATE).edit().putString(LoginActivity.SHARED_PHOTO_URL, "").commit();

            View header = ((NavigationView) findViewById(R.id.nav_view)).getHeaderView(0);
            header.findViewById(R.id.image_view_profile_pic_nav_header).setVisibility(View.INVISIBLE);

            FirebaseAuth.getInstance().signOut();
            getSharedPreferences(Constants.SHARED_PREFS, MODE_PRIVATE).edit().putBoolean(LoginActivity.SHARED_SKIP, false).commit();
            getSharedPreferences(Constants.FIREBASE_USERS, MODE_PRIVATE).edit().putString(LoginActivity.SHARED_NAME, "").commit();

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
                    .putString(Constants.SHARED_PREFS_KEY_LAST_LONG, mLastLocation.getLongitude() + "").apply();
        }

    }

    @Override
    public void onConnectionSuspended(int pI) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult pConnectionResult) {

    }
}
