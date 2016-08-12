package com.uniquemiban.travelmanager.map;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterManager;
import com.uniquemiban.travelmanager.R;


public class GmapFragment extends Fragment implements OnMapReadyCallback {

    private static final String ARG_TITLE = "arg_title";
    private static final String ARG_LONGITUDE = "arg_longitude";
    private static final String ARG_LATITUDE = "arg_latitude";

    private GoogleMap mMap;
    private ClusterManager<LocationMarker> mClusterManager;

    private double mLng;
    private double mLat;
    private String mTitle;

    public static GmapFragment newInstance(String pTitle, double pLongitude, double pLatitude){
        GmapFragment fragment = new GmapFragment();
        Bundle bundle = new Bundle();
        bundle.putString(ARG_TITLE, pTitle);
        bundle.putDouble(ARG_LONGITUDE, pLongitude);
        bundle.putDouble(ARG_LATITUDE, pLatitude);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return  inflater.inflate(R.layout.fragment_gmaps,container,false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MapFragment fragment=(MapFragment)getChildFragmentManager().findFragmentById(R.id.map);
        fragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        Bundle bundle = getArguments();

        //mLatLng = new LatLng(bundle.getDouble(ARG_LONGITUDE), bundle.getDouble(ARG_LATITUDE));
        mLng = bundle.getDouble(ARG_LONGITUDE);
        mLat = bundle.getDouble(ARG_LATITUDE);
        mTitle = bundle.getString(ARG_TITLE);

        mMap = googleMap;

        //Custom Icon
//        int hight = 40, wight = 40;
//        BitmapDrawable bitmapDrawable = (BitmapDrawable) getResources().getDrawable(R.drawable.map_1);
//        Bitmap b = bitmapDrawable.getBitmap();
//        Bitmap smallMarker = Bitmap.createScaledBitmap(b, hight, wight, false);

        setUpClusterer();

    }



        private void setUpClusterer() {
            // Declare a variable for the cluster manager.

            // Position the map.
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLat, mLng), 10));

            // Initialize the manager with the context and the map.
            // (Activity extends context, so we can pass 'this' in the constructor.)
            mClusterManager = new ClusterManager<LocationMarker>(getActivity(), mMap);

            // Point the map's listeners at the listeners implemented by the cluster
            // manager.
            mMap.setOnCameraChangeListener(mClusterManager);
            mMap.setOnMarkerClickListener(mClusterManager);

            // Add cluster items (markers) to the cluster manager.
            //addItems();
            LocationMarker offsetItem = new LocationMarker(mLat, mLng);
            mClusterManager.addItem(offsetItem);
        }

        private void addItems() {

            // Set some lat/lng coordinates to start with.
            double lat = 51.5145160;
            double lng = -0.1270060;

            // Add ten cluster items in close proximity, for purposes of this example.
            for (int i = 0; i < 10; i++) {
                double offset = i / 60d;
                lat = lat + offset;
                lng = lng + offset;
                LocationMarker offsetItem = new LocationMarker(lat, lng);
                mClusterManager.addItem(offsetItem);
            }
        }

}
