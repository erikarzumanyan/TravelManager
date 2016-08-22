package com.uniquemiban.travelmanager.models;


import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Sleep extends RealmObject {
    @PrimaryKey
    private String mId;

    private String mName;
    private String mAbout;
    private String mLocation;

    private double mLongitude;
    private double mLatitude;

    private String mPhotoUrl;
    private String mPhoto1Url;
    private String mPhoto2Url;


    public Sleep(){

    }


    public Sleep(String pId, String pName, String pAbout, String pPhotoUrl, String pPhoto1Url, String pPhoto2Url, String pLocation, double pLongitude, double pLatitude) {
        mId = pId;
        mName = pName;
        mAbout = pAbout;
        mPhotoUrl = pPhotoUrl;
        mPhoto1Url = pPhoto1Url;
        mPhoto2Url = pPhoto2Url;
        mLocation = pLocation;
        mLongitude = pLongitude;
        mLatitude = pLatitude;
    }

    public String getId() {
        return mId;
    }

    public void setId(String pId) {
        mId = pId;
    }

    public String getName() {
        return mName;
    }

    public void setName(String pName) {
        mName = pName;
    }

    public String getAbout() {
        return mAbout;
    }

    public void setAbout(String pAbout) {
        mAbout = pAbout;
    }

    public String getPhotoUrl() {
        return mPhotoUrl;
    }

    public void setPhotoUrl(String pPhotoUrl) {
        mPhotoUrl = pPhotoUrl;
    }

    public String getPhoto1Url() {
        return mPhoto1Url;
    }

    public void setPhoto1Url(String pPhoto1Url) {
        mPhoto1Url = pPhoto1Url;
    }

    public String getPhoto2Url() {
        return mPhoto2Url;
    }

    public void setPhoto2Url(String pPhoto2Url) {
        mPhoto2Url = pPhoto2Url;
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

    public double getLatitude() {
        return mLatitude;
    }

    public void setLatitude(double pLatitude) {
        mLatitude = pLatitude;
    }


}
