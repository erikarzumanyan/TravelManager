package com.uniquemiban.travelmanager;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.uniquemiban.travelmanager.login.LoginActivity;

public class NavigationDrawerActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    DrawerLayout mDrawer;

    ViewGroup mActionBar;
    TextView mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_drawer);

        if(FirebaseAuth.getInstance().getCurrentUser() == null
                && !getSharedPreferences(Constants.SHARED_PREFS, MODE_PRIVATE).getBoolean(LoginActivity.SHARED_SKIP, false)){
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }

        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        mActionBar = (ViewGroup)findViewById(R.id.layout_action_bar);
        mActionBar.findViewById(R.id.image_view_nav_view_open).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View pView) {
                mDrawer.openDrawer(Gravity.LEFT);
            }
        });

        mTitle = (TextView)mActionBar.findViewById(R.id.text_view_title);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        FragmentManager manager = getSupportFragmentManager();
        Fragment fragmentSightsList = manager.findFragmentByTag(SightsListFragment.FRAGMENT_TAG);
        Fragment fragmentSight = manager.findFragmentByTag(SightFragment.FRAGMENT_TAG);

        if(fragmentSightsList == null && fragmentSight == null){
            fragmentSightsList = new SightsListFragment();
            manager.beginTransaction()
                    .add(R.id.fragment_container, fragmentSightsList, SightsListFragment.FRAGMENT_TAG)
                    .commit();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        FragmentManager manager = getSupportFragmentManager();
        Fragment fragment = manager.findFragmentByTag(SightFragment.FRAGMENT_TAG);

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if(fragment != null){
            manager.beginTransaction()
                    .replace(R.id.fragment_container, new SightsListFragment(), SightsListFragment.FRAGMENT_TAG)
                    .commit();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.navigation_drawer, menu);
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
