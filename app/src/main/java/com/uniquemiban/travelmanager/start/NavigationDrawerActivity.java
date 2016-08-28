package com.uniquemiban.travelmanager.start;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Location;
import android.os.Build;
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
import android.support.v7.app.AlertDialog;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NavigationDrawerActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    final private int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 124;
    private DrawerLayout mDrawer;
    private Toolbar mToolbar;
    private int mWidth = 500;

    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;

    private Menu mNavMenu;

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

        requestPermissions();

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        mNavMenu  = ((NavigationView)findViewById(R.id.nav_view)).getMenu();

        if(user != null) {
            mNavMenu.findItem(R.id.nav_sign_in).setVisible(false);
            mNavMenu.findItem(R.id.nav_sign_out).setVisible(true);
        } else {
            mNavMenu.findItem(R.id.nav_sign_in).setVisible(true);
            mNavMenu.findItem(R.id.nav_sign_out).setVisible(false);
        }

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
                fragment = new GmapMainFragment();
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
            mNavMenu.findItem(R.id.nav_sign_in).setVisible(true);
            mNavMenu.findItem(R.id.nav_sign_out).setVisible(false);

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

            Toast.makeText(this, "Successfully signed out", Toast.LENGTH_LONG).show();
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

    private void requestPermissions() {
        List<String> permissionsNeeded = new ArrayList<String>();

        final List<String> permissionsList = new ArrayList<String>();
        if (!addPermission(permissionsList, Manifest.permission.ACCESS_FINE_LOCATION))
            permissionsNeeded.add("GPS");
        if (!addPermission(permissionsList, Manifest.permission.ACCESS_COARSE_LOCATION))
            permissionsNeeded.add("Network Location");
        if (!addPermission(permissionsList, Manifest.permission.READ_EXTERNAL_STORAGE))
            permissionsNeeded.add("Read External Storage");
        if (!addPermission(permissionsList, Manifest.permission.CALL_PHONE))
            permissionsNeeded.add("Call");

        if (permissionsList.size() > 0) {
            if (Build.VERSION.SDK_INT >= 23) {
                if (permissionsNeeded.size() > 0) {
                    // Need Rationale
                    String message = "You need to grant access to " + permissionsNeeded.get(0);
                    for (int i = 1; i < permissionsNeeded.size(); i++)
                        message = message + ", " + permissionsNeeded.get(i);
                    showMessageOKCancel(message,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (Build.VERSION.SDK_INT >= 23) {
                                        requestPermissions(permissionsList.toArray(new String[permissionsList.size()]),
                                                REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
                                    }
                                }
                            });
                    return;
                }
                requestPermissions(permissionsList.toArray(new String[permissionsList.size()]),
                        REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
                return;
            }
        }


    }

    private boolean addPermission(List<String> permissionsList, String permission) {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsList.add(permission);
                // Check for Rationale Option
                if (!shouldShowRequestPermissionRationale(permission))
                    return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS:
            {
                Map<String, Integer> perms = new HashMap<String, Integer>();
                // Initial
                perms.put(Manifest.permission.ACCESS_COARSE_LOCATION, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.ACCESS_FINE_LOCATION, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.CALL_PHONE, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.READ_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                // Fill with results
                for (int i = 0; i < permissions.length; i++)
                    perms.put(permissions[i], grantResults[i]);
                // Check for ACCESS_FINE_LOCATION
                if (perms.get(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        && perms.get(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        && perms.get(Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED
                        && perms.get(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    // All Permissions Granted

                } else {
                    // Permission Denied
                    Toast.makeText(this, "Some Permission is Denied", Toast.LENGTH_SHORT)
                            .show();
                }
            }
            break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }
}
