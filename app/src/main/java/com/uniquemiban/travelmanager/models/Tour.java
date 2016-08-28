package com.uniquemiban.travelmanager.models;


import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Tour extends RealmObject {
    @PrimaryKey
    private String mId;

    private String mName;
    private String mTourOperatorName;
    private int mPrice;
    private String mPhotoUrl;
    private String mAbout;
    private double mLatitude;
    private double mLongitude;
    private String mNumber;
    private String mFacebookUrl;

    public Tour(){
        mName = "";
        mTourOperatorName = "";
        mAbout = "";
        mNumber = "";
        mFacebookUrl = "";
    }


    public Tour(String pId, String pName, String pTourOperatorName, int pPrice, String pPhotoUrl, String pAbout, int pRate, double pLatitude, double pLongitude, String pNumber, String pFacebookUrl) {
        mId = pId;
        mName = pName;
        mTourOperatorName = pTourOperatorName;
        mPrice = pPrice;
        mPhotoUrl = pPhotoUrl;
        mAbout = pAbout;
        mLatitude = pLatitude;
        mLongitude = pLongitude;
        mNumber = pNumber;
        mFacebookUrl = pFacebookUrl;
    }


    public String getFacebookUrl() {
        return mFacebookUrl;
    }

    public void setFacebookUrl(String pFacebookUrl) {
        mFacebookUrl = pFacebookUrl;
    }

    public String getNumber() {
        return mNumber;
    }

    public void setNumber(String pNumber) {
        mNumber = pNumber;
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
