package com.uniquemiban.travelmanager.map;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class LocationMarker implements ClusterItem {
    private final LatLng mPosition;
    private int url;
    private String id;

    protected LocationMarker(LatLng pPosition, int pUrl, String pId) {
        mPosition = pPosition;
        url = pUrl;
        id=pId;
    }

    public LocationMarker(double lat, double lng) {
        mPosition = new LatLng(lat, lng);
    }

    protected String getId() {
        return id;
    }

    protected void setId(String pId) {
        id = pId;
    }

    protected int getUrl() {
        return url;
    }

    protected void setUrl(int pUrl) {
        url = pUrl;
    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }


}