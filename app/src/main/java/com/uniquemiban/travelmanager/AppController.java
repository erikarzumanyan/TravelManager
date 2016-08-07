package com.uniquemiban.travelmanager;

import android.app.Application;

import com.squareup.picasso.Cache;
import com.squareup.picasso.LruCache;
import com.squareup.picasso.Picasso;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class AppController extends Application {

    private static final long PICASSO_DISK_CACHE_SIZE = 1024 * 1024 * 100;

    @Override
    public void onCreate() {
        super.onCreate();
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder(this).build();
        Realm.setDefaultConfiguration(realmConfiguration);

        Cache memoryCache = new LruCache((int)PICASSO_DISK_CACHE_SIZE);

        Picasso picasso = new Picasso.Builder(this)
                .memoryCache(memoryCache).build();

        Picasso.setSingletonInstance(picasso);
    }
}
