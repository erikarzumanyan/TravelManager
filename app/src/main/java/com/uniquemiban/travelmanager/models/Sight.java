package com.uniquemiban.travelmanager.models;

import io.realm.RealmObject;

public class Sight extends RealmObject {

    private String id;

    private String name;

    private String photoUrl;

    private String about;

    public int getPhotoId() {
        return photoId;
    }

    public void setPhotoId(int pPhotoId) {
        photoId = pPhotoId;
    }

    private  int photoId;
    private double destination;
    private double longitude;
    private double latitude;

    public Sight(int pPhotoId, String pName, double pDestination) {
        pPhotoId = pPhotoId;
        name = pName;
        destination = pDestination;
    }

    public Sight() {

    }

    public Sight(String pId, String pName, String pPhotoUrl, String pAbout, double pDestination, double pLongitude, double pLatitude) {
        id = pId;
        name = pName;
        photoUrl = pPhotoUrl;
        about = pAbout;
        destination = pDestination;
        longitude = pLongitude;
        latitude = pLatitude;
    }

    public double getDestination() {
        return destination;
    }

    public void setDestination(double pDestination) {
        destination = pDestination;
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

    public String getAbout() {
        return about;
    }

    public void setAbout(String pAbout) {
        this.about = pAbout;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double pLatitude) {
        this.latitude = pLatitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double pLongitude) {
        this.longitude = pLongitude;
    }
}
