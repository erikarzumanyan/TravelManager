package com.uniquemiban.travelmanager.models;

import io.realm.RealmObject;

public class Hotel extends RealmObject {
    private String id;
    private String name;
    private String photoUrl;
    private String about;

    private double Longitude;
    private double Latitude;

    public Hotel() {
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String pAbout) {
        this.about = pAbout;
    }

    public String getId() {
        return id;
    }

    public void setId(String pId) {
        this.id = pId;
    }

    public String getName() {
        return name;
    }

    public void setName(String pName) {
        this.name = pName;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String pPhotoUrl) {
        this.photoUrl = pPhotoUrl;
    }

    public double getLatitude() {
        return Latitude;
    }

    public void setLatitude(double pLatitude) {
        this.Latitude = pLatitude;
    }

    public double getLongitude() {
        return Longitude;
    }

    public void setLongitude(double pLongitude) {
        this.Longitude = pLongitude;
    }
}
