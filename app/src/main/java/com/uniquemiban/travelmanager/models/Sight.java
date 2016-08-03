package com.uniquemiban.travelmanager.models;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Sight extends RealmObject {

    @PrimaryKey
    private String mId;

    private String mName;
    private String mPhotoUrl;
    private String mAbout;

    private double mLongitude;
    private double mLatitude;


    public Sight(){

    }

    public Sight(String mAbout, String mId, double mLatitude, double mLongitude, String mName, String mPhotoUrl) {
        this.mAbout = mAbout;
        this.mId = mId;
        this.mLatitude = mLatitude;
        this.mLongitude = mLongitude;
        this.mName = mName;
        this.mPhotoUrl = mPhotoUrl;
    }

    public String getId() {
        return mId;
    }

    public void setId(String pId){
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

    public String getAbout() {
        return mAbout;
    }

    public void setAbout(String pAbout) {
        this.mAbout = pAbout;
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
