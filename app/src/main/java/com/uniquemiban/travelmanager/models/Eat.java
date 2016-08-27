package com.uniquemiban.travelmanager.models;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Eat extends RealmObject {
    @PrimaryKey
    private String mId;

    private String mName;
    private String mAbout;
    private String mCategory;

    private String mPhotoUrl;
    private String mPhoto1Url;
    private String mPhoto2Url;
    private double mLongitude;
    private double mLatitude;
    private String mLocation;

    public Eat(){
        mName = "";
        mAbout = "";
        mCategory = "";
        mLocation = "";


    }

    public String getAbout() {
        return mAbout;
    }

    public void setAbout(String pAbout) {
        mAbout = pAbout;
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

    public String getCategory() {
        return mCategory;
    }

    public void setCategory(String pCategory) {
        mCategory = pCategory;
    }
}
