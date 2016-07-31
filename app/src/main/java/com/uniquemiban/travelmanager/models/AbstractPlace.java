package com.uniquemiban.travelmanager.models;

import io.realm.annotations.PrimaryKey;

public abstract class AbstractPlace{
    @PrimaryKey
    private long mId;

    private String mName;
    private String mPhotoUrl;
    private String mAbout;

    private double mLongitude;
    private double mLatitude;

    public AbstractPlace(){}

    public String getAbout() {
        return mAbout;
    }

    public void setAbout(String pAbout) {
        this.mAbout = pAbout;
    }

    public long getId() {
        return mId;
    }

    public void setId(long pId) {
        this.mId = pId;
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
}
