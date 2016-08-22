package com.uniquemiban.travelmanager.map;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

public class LocationMarker  implements ClusterItem {
    private final LatLng mPosition;

    public LocationMarker(double lat, double lng) {
        mPosition = new LatLng(lat, lng);
    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }

}