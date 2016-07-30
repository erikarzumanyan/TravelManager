package com.uniquemiban.travelmanager.models;

import io.realm.RealmObject;

public class Sight extends RealmObject {

    private String mId;
    private String mName;
    private String mPhotoUrl;

    protected String getAbout() {
        return mAbout;
    }

    protected void setAbout(String pAbout) {
        mAbout = pAbout;
    }

    protected String getPhotoUrl() {
        return mPhotoUrl;
    }

    protected void setPhotoUrl(String pPhotoUrl) {
        mPhotoUrl = pPhotoUrl;
    }

    private String mAbout;
    private double mLongitude;
    private double mLatitude;

    public Sight(){

    }

    public String getId() {
        return mId;
    }

    public void setId(String mId){
        this.mId = mId;
    }

    public String getName() {
        return mName;
    }

    public void setName(String mName) {
        this.mName = mName;
    }

    public String getUrl() {
        return mPhotoUrl;
    }

    public void setUrl(String mPhotoUrl) {
        this.mPhotoUrl = mPhotoUrl;
    }

    public double getLatitude() {
        return mLatitude;
    }

    public void setLatitude(double mLatitude) {
        this.mLatitude = mLatitude;
    }

    public double getLongitude() {
        return mLongitude;
    }

    public void setLongitude(double mLongitude) {
        this.mLongitude = mLongitude;
    }
}
