package com.uniquemiban.travelmanager.models;


import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Tour extends RealmObject {
    @PrimaryKey
    private String mId;

    private String mCategory;

    private String mName;
    private String mTourOperatorName;
    private int mPrice;
    private String mPhotoUrl;
    private String mAbout;
    private double mLatitude;
    private double mLongitude;

    public Tour(){
        mName = "";
        mAbout = "";
        mCategory = "";
        mTourOperatorName = "";
    }


    public String getCategory() {
        return mCategory;
    }

    public void setCategory(String pCategory) {
        mCategory = pCategory;
    }

    public double getLatitude() {
        return mLatitude;
    }

    public void setLatitude(double pLatitude) {
        mLatitude = pLatitude;
    }

    public double getLongitude() {
        return mLongitude;
    }

    public void setLongitude(double pLongitude) {
        mLongitude = pLongitude;
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

    public String getTourOperatorName() {
        return mTourOperatorName;
    }

    public void setTourOperatorName(String pTourOperatorName) {
        mTourOperatorName = pTourOperatorName;
    }

    public int getPrice() {
        return mPrice;
    }

    public void setPrice(int pPrice) {
        mPrice = pPrice;
    }

    public String getPhotoUrl() {
        return mPhotoUrl;
    }

    public void setPhotoUrl(String pPhotoUrl) {
        mPhotoUrl = pPhotoUrl;
    }

    public String getAbout() {
        return mAbout;
    }

    public void setAbout(String pAbout) {
        mAbout = pAbout;
    }
}
