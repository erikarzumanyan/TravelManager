package com.uniquemiban.travelmanager.models;

import io.realm.RealmObject;

/**
 * Created by narek on 7/30/16.
 */
public class Tour extends RealmObject {
    private int id;

    private int price;

    private String name;
    private String about;
    private String duration;
    private String toPlaces;

    public String getToPlaces() {
        return toPlaces;
    }

    public void setToPlaces(String pToPlaces) {
        toPlaces = pToPlaces;
    }

    public int getId() {
        return id;
    }

    public void setId(int pId) {
        id = pId;
    }

    public String getName() {
        return name;
    }

    public void setName(String pName) {
        name = pName;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String pAbout) {
        about = pAbout;
    }


    public String getDuration() {
        return duration;
    }

    public void setDuration(String pDuration) {
        duration = pDuration;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int pPrice) {
        price = pPrice;
    }
}
