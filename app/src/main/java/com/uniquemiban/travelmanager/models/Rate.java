package com.uniquemiban.travelmanager.models;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Rate extends RealmObject {

    private String mPlaceId;
    private String mUserId;
    private float mRate;
    private String mMessage;

    public String getMessage() {
        return mMessage;
    }

    public void setMessage(String pMessage) {
        mMessage = pMessage;
    }

    public String getPlaceId() {
        return mPlaceId;
    }

    public void setPlaceId(String pPlaceId) {
        mPlaceId = pPlaceId;
    }

    public float getRate() {
        return mRate;
    }

    public void setRate(float pRate) {
        mRate = pRate;
    }

    public String getUserId() {
        return mUserId;
    }

    public void setUserId(String pUserId) {
        mUserId = pUserId;
    }
}
