package com.uniquemiban.travelmanager.models;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Hotel extends RealmObject{

    @PrimaryKey
    private String mId;

    private String mName;
    private String mPhotoUrl;
    private String mAbout;

    private double mLongitude;
    private double mLatitude;

    public Hotel(){}

    public String getAbout() {
        return mAbout;
    }

    public void setAbout(String pAbout) {
        this.mAbout = pAbout;
    }

    public String getId() {
        return mId;
    }

    public void setId(String pId) {
        this.mId = pId;
    }

    public String getName() {
        return mName;
    }

    public void setName(String pName) {
        this.mName = pName;
    }

    public String getPhotoUrl() {
        return mPhotoUrl;
    }

    public void setPhotoUrl(String pPhotoUrl) {
        this.mPhotoUrl = pPhotoUrl;
    }

    public double getLatitude() {
        return mLatitude;
    }

    public void setLatitude(double pLatitude) {
        this.mLatitude = pLatitude;
    }

    public double getLongitude() {
        return mLongitude;
    }

    public void setLongitude(double pLongitude) {
        this.mLongitude = pLongitude;
    }
}
