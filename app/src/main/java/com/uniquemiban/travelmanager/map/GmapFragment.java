package com.uniquemiban.travelmanager.map;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
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

    public static GmapFragment newInstance(String pTitle, double pLongitude, double pLatitude) {
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
        return inflater.inflate(R.layout.fragment_gmaps, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment fragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        fragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        Bundle bundle = getArguments();

        mLng = bundle.getDouble(ARG_LONGITUDE);
        mLat = bundle.getDouble(ARG_LATITUDE);
        mTitle = bundle.getString(ARG_TITLE);

        mMap = googleMap;

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
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Toast.makeText(getActivity(), "Needed location permission", Toast.LENGTH_LONG).show();
            return;
        }
        mMap.setMyLocationEnabled(true);

            // Add cluster items (markers) to the cluster manager.
            //addItems();
            LocationMarker offsetItem = new LocationMarker(mLat, mLng);
            mClusterManager.addItem(offsetItem);
        }

}
