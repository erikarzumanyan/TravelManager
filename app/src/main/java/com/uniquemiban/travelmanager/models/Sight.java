package com.uniquemiban.travelmanager.models;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Sight extends RealmObject {

    @PrimaryKey
    private String mId;

    private String mName;
    private String mPhotoUrl;
    private String mAbout;

    private String mCategory;
    private String mLocation;

    private double mLongitude;
    private double mLatitude;

    public Sight(){

    }

    public Sight(String pAbout, String pCategory, String pId, double pLatitude, String pLocation, double pLongitude, String pName, String pPhotoUrl) {
        mAbout = pAbout;
        mCategory = pCategory;
        mId = pId;
        mLatitude = pLatitude;
        mLocation = pLocation;
        mLongitude = pLongitude;
        mName = pName;
        mPhotoUrl = pPhotoUrl;
    }

    public String getAbout() {
        return mAbout;
    }

    public void setAbout(String pAbout) {
        mAbout = pAbout;
    }

    public String getCategory() {
        return mCategory;
    }

    public void setCategory(String pCategory) {
        mCategory = pCategory;
    }

    public String getId() {
        return mId;
    }

    public void setId(String pId) {
        mId = pId;
    }

    public double getLatitude() {
        return mLatitude;
    }

    public void setLatitude(double pLatitude) {
        mLatitude = pLatitude;
    }

    public String getLocation() {
        return mLocation;
    }

    public void setLocation(String pLocation) {
        mLocation = pLocation;
    }

    public double getLongitude() {
        return mLongitude;
    }

    public void setLongitude(double pLongitude) {
        mLongitude = pLongitude;
    }

    public String getName() {
        return mName;
    }

    public void setName(String pName) {
        mName = pName;
    }

    public String getPhotoUrl() {
        return mPhotoUrl;
    }

    public void setPhotoUrl(String pPhotoUrl) {
        mPhotoUrl = pPhotoUrl;
    }
}
