package com.uniquemiban.travelmanager.map;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.TransportMode;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Route;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterManager;
import com.uniquemiban.travelmanager.R;

import java.util.ArrayList;


public class GmapFragment extends Fragment implements OnMapReadyCallback, DirectionCallback, LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    public static final String FRAGMENT_TAG = "map_fragment_tag";
    private static final String ARG_TITLE = "arg_title";
    private static final String ARG_LONGITUDE = "arg_longitude";
    private static final String ARG_LATITUDE = "arg_latitude";


    private GoogleMap mMap;
    private String serverKey = "AIzaSyDajDviN69XW2QVkOLmL1ZtVPAB9TZ6Dms";
    private LatLng origin;
    private LatLng destination = new LatLng(41.931644, 43.698519);
    private ClusterManager<LocationMarker> mClusterManager;
    private double mLng;
    private double mLat;
    private String mTitle;
    private double mMyLng;
    private double mMyLat;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;

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
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        destination = new LatLng(getArguments().getDouble(ARG_LATITUDE), getArguments().getDouble(ARG_LONGITUDE));
    }

    @Override
    public void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
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

    public void requestDirection() {
        Snackbar.make(getView(), "Direction Requesting...", Snackbar.LENGTH_SHORT).show();
        GoogleDirection.withServerKey(serverKey)
                .from(origin)
                .to(destination)
                .transportMode(TransportMode.DRIVING)
                .execute(this);


    }


    @Override
    public void onDirectionSuccess(Direction direction, String rawBody) {
        Snackbar.make(getView(), "Success with status : " + direction.getStatus(), Snackbar.LENGTH_SHORT).show();
        if (direction.isOK()) {
            mMap.addMarker(new MarkerOptions().position(destination));
            for (int i = 0; i < direction.getRouteList().size(); i++) {
                Route route = direction.getRouteList().get(i);
                ArrayList<LatLng> directionPositionList = route.getLegList().get(0).getDirectionPoint();
                mMap.addPolyline(DirectionConverter.createPolyline(getContext(), directionPositionList, 5, Color.parseColor("#7fff8a00")));
            }


        }
    }

    @Override
    public void onDirectionFailure(Throwable t) {
        Snackbar.make(getView(), t.getMessage(), Snackbar.LENGTH_SHORT).show();
    }


    private void setUpClusterer() {
        mClusterManager = new ClusterManager<>(getActivity(), mMap);

        // Point the map's listeners at the listeners implemented by the cluster
        // manager.
        mMap.setOnCameraChangeListener(mClusterManager);
        mMap.setOnMarkerClickListener(mClusterManager);
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getActivity(), "Needed location permission", Toast.LENGTH_LONG).show();
            return;
        }
        mMap.setMyLocationEnabled(true);
        LocationMarker offsetItem = new LocationMarker(mLat, mLng);
        mClusterManager.addItem(offsetItem);
    //       mClusterManager.getMarkerCollection().addMarker(new MarkerOptions().position(offsetItem.getPosition()).icon(BitmapDescriptorFactory.fromResource(R.mipmap.cloudy)));
    }


    public void moveCamera() {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(destination, 10));
    }

    @Override
    public void onLocationChanged(Location pLocation) {

    }

    @Override
    public void onConnected(@Nullable Bundle pBundle) {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getActivity(), "NEEDED LOCATION PERMISSION", Toast.LENGTH_LONG).show();
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
            mMyLat = mLastLocation.getLatitude();
            mMyLng = mLastLocation.getLongitude();
            origin = new LatLng(mMyLat, mMyLng);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(origin, 10));

//            LocationMarker originMarker=new LocationMarker(origin.latitude,origin.longitude);
//            LocationMarker destinationMarker=new LocationMarker(destination.latitude,destination.longitude);
//            mClusterManager.addItem(originMarker);
//            mClusterManager.addItem(destinationMarker);

            requestDirection();
        }
    }


    @Override
    public void onConnectionSuspended(int pI) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult pConnectionResult) {

    }
}
