package com.uniquemiban.travelmanager;

import android.app.Application;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class AppController extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        RealmConfiguration realmConfig = new RealmConfiguration.Builder(this).build();
        Realm.setDefaultConfiguration(realmConfig);
    }
}
