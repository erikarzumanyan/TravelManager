package com.uniquemiban.travelmanager.map;


import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;
import com.uniquemiban.travelmanager.R;
import com.uniquemiban.travelmanager.eat.EatFragment;
import com.uniquemiban.travelmanager.models.Eat;
import com.uniquemiban.travelmanager.models.Sight;
import com.uniquemiban.travelmanager.models.Sleep;
import com.uniquemiban.travelmanager.sight.SightFragment;
import com.uniquemiban.travelmanager.sleep.SleepFragment;
import com.uniquemiban.travelmanager.start.NavigationDrawerActivity;

import io.realm.Realm;
import io.realm.RealmResults;


public class GmapMainFragment extends Fragment implements OnMapReadyCallback {


    public static final String FRAGMENT_TAG = "main_map_fragment_tag";
    private static View view;
    LocationMarker mSightsDestinations;
    LocationMarker mEatsDirections;
    IconRenderer iconRenderer;
    private GoogleMap mMap;
    private String serverKey = "AIzaSyDajDviN69XW2QVkOLmL1ZtVPAB9TZ6Dms";
    private LatLng origin;
    private LatLng destination = new LatLng(40.181247, 44.514308);
    private ClusterManager<LocationMarker> mClusterManager;
    private double mLng;
    private double mLat;
    private String mTitle;
    private double mMyLng;
    private double mMyLat;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private RealmResults<Eat> mEats;
    private RealmResults<Sleep> mSleep;
    private RealmResults mSights;
    private Realm realm;
    private RealmResults<Sight> sightsResults;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {


        if (view != null) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null)
                parent.removeView(view);
        }
        try {
            view = inflater.inflate(R.layout.fragment_gmap_main, container, false);
        } catch (InflateException e) {
        }


        View locationButton = ((View) view.findViewById(1).getParent()).findViewById(2);

        RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
        rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        rlp.setMargins(0, 0, 30, 30);


        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        SupportMapFragment fragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.main_map);
        fragment.getMapAsync(this);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        setHasOptionsMenu(true);

        NavigationDrawerActivity activity = (NavigationDrawerActivity) getActivity();

        ActionBar bar = activity.getSupportActionBar();
        bar.setDisplayHomeAsUpEnabled(false);
        bar.setDisplayShowCustomEnabled(true);

        Toolbar toolbar = activity.getToolbar();

        toolbar.setTitle("Map");
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                activity, activity.getDrawer(), toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        activity.getDrawer().setDrawerListener(toggle);
        toggle.syncState();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public void onStop() {
        super.onStop();

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {


        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(destination, 10));
        iconRenderer = new IconRenderer();
        iconRenderer.startDemo();


    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
    }

    private class IconRenderer extends DefaultClusterRenderer<LocationMarker> {
        private final IconGenerator mIconGenerator = new IconGenerator(getContext());
        private final IconGenerator mClusterIconGenerator = new IconGenerator(getContext());
        private final ImageView mImageView;
        private final ImageView mClusterImageView;
        private final int mDimension;


        public IconRenderer() {
            super(getContext(), mMap, mClusterManager);


            View multiProfile = getLayoutInflater(new Bundle()).inflate(R.layout.multi_profile, null);
            mClusterIconGenerator.setContentView(multiProfile);
            mClusterImageView = (ImageView) multiProfile.findViewById(R.id.image);

            mImageView = new ImageView(getContext());
            mDimension = (int) getResources().getDimension(R.dimen.custom_profile_image);
            mImageView.setLayoutParams(new ViewGroup.LayoutParams(mDimension, mDimension));
            int padding = (int) getResources().getDimension(R.dimen.custom_profile_padding);
            mImageView.setPadding(padding, padding, padding, padding);
            mIconGenerator.setContentView(mImageView);
        }


        @Override
        protected boolean shouldRenderAsCluster(Cluster<LocationMarker> cluster) {
            return cluster.getSize() > 1;
        }

        @Override
        protected void onBeforeClusterItemRendered(LocationMarker item, MarkerOptions markerOptions) {
            markerOptions.icon(markerOptions.icon(BitmapDescriptorFactory.fromResource(item.getUrl())).getIcon());


        }


        @Override
        protected void onBeforeClusterRendered(Cluster<LocationMarker> cluster, MarkerOptions markerOptions) {
            mClusterImageView.setImageDrawable(getResources().getDrawable(R.mipmap.all_together));

            Bitmap icon = mClusterIconGenerator.makeIcon(String.valueOf(cluster.getSize()));
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
        }

        protected void startDemo() {

            mClusterManager = new ClusterManager<>(getContext(), mMap);
            mClusterManager.setRenderer(new IconRenderer());
            mMap.setOnCameraChangeListener(mClusterManager);
            mMap.setOnMarkerClickListener(mClusterManager);
            mMap.setOnInfoWindowClickListener(mClusterManager);

            addItems();
//            mQuery = mRef.orderByKey().limitToFirst(LOADING_ITEMS_NUMBER);
//            mQuery.addChildEventListener(mChildEventListener);


            mClusterManager.cluster();
            mClusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<LocationMarker>() {
                @Override
                public boolean onClusterItemClick(LocationMarker pLocationMarker) {


                    FragmentManager manager = getActivity().getSupportFragmentManager();

                    Fragment fragment = manager.findFragmentByTag(SightFragment.FRAGMENT_TAG);

                    if (fragment == null) {
                        if (pLocationMarker.getUrl() == R.mipmap.icon_sight) {
                            fragment = SightFragment.newInstance(pLocationMarker.getId());
                            manager.beginTransaction()
                                    .replace(R.id.fragment_container, fragment, SightFragment.FRAGMENT_TAG)
                                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                                    .addToBackStack(SightFragment.FRAGMENT_TAG)
                                    .commit();
                        }
                    }

                    fragment = manager.findFragmentByTag(EatFragment.FRAGMENT_TAG);

                    if (fragment == null) {
                        if (pLocationMarker.getUrl() == R.mipmap.icon_eat) {
                            fragment = EatFragment.newInstance(pLocationMarker.getId());
                            manager.beginTransaction()
                                    .replace(R.id.fragment_container, fragment, EatFragment.FRAGMENT_TAG)
                                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                                    .addToBackStack(EatFragment.FRAGMENT_TAG)
                                    .commit();
                        }
                    }
                    fragment = manager.findFragmentByTag(SleepFragment.FRAGMENT_TAG);

                    if (fragment == null) {
                        if (pLocationMarker.getUrl() == R.mipmap.icon_sleep) {
                            fragment = SleepFragment.newInstance(pLocationMarker.getId());
                            manager.beginTransaction()
                                    .replace(R.id.fragment_container, fragment, SleepFragment.FRAGMENT_TAG)
                                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                                    .addToBackStack(SleepFragment.FRAGMENT_TAG)
                                    .commit();
                        }
                    }
                    return false;

                }
            });
        }


        private void addItems() {
            realm = Realm.getDefaultInstance();
            sightsResults = realm.where(Sight.class).findAll();
            for (Sight sight : sightsResults) {
                mSightsDestinations = new LocationMarker(new LatLng(sight.getLatitude(), sight.getLongitude()), R.mipmap.icon_sight, sight.getId());
                mClusterManager.addItem(mSightsDestinations);

            }
            mEats = realm.where(Eat.class).findAll();
            for (Eat eat : mEats) {
                mEatsDirections = new LocationMarker(new LatLng(eat.getLatitude(), eat.getLongitude()), R.mipmap.icon_eat, eat.getId());
                mClusterManager.addItem(mEatsDirections);

            }

            mSleep = realm.where(Sleep.class).findAll();
            for (Sleep sleep : mSleep) {
                mEatsDirections = new LocationMarker(new LatLng(sleep.getLatitude(), sleep.getLongitude()), R.mipmap.icon_sleep, sleep.getId());
                mClusterManager.addItem(mEatsDirections);

            }


        }


    }

}
