package com.uniquemiban.travelmanager.models;

public class RateMsg {

    private float mRate;
    private String mMessage;
    private String mName;

    public String getMessage() {
        return mMessage;
    }

    public void setMessage(String pMessage) {
        mMessage = pMessage;
    }

    public String getName() {
        return mName;
    }

    public void setName(String pName) {
        mName = pName;
    }

    public float getRate() {
        return mRate;
    }

    public void setRate(float pRate) {
        mRate = pRate;
    }
}
