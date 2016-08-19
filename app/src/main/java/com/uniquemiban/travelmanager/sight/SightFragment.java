package com.uniquemiban.travelmanager.sight;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.DefaultSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.daimajia.slider.library.Tricks.ViewPagerEx;
import com.squareup.picasso.Picasso;
import com.survivingwithandroid.weather.lib.WeatherClient;
import com.survivingwithandroid.weather.lib.WeatherConfig;
import com.survivingwithandroid.weather.lib.exception.WeatherLibException;
import com.survivingwithandroid.weather.lib.exception.WeatherProviderInstantiationException;
import com.survivingwithandroid.weather.lib.model.CurrentWeather;
import com.survivingwithandroid.weather.lib.provider.openweathermap.OpenweathermapProviderType;
import com.survivingwithandroid.weather.lib.request.WeatherRequest;
import com.uniquemiban.travelmanager.start.NavigationDrawerActivity;
import com.uniquemiban.travelmanager.R;
import com.uniquemiban.travelmanager.weather.WeatherFragment;
import com.uniquemiban.travelmanager.map.GmapFragment;
import com.uniquemiban.travelmanager.models.Sight;

import java.util.ArrayList;
import java.util.Random;

import io.realm.Realm;

public class SightFragment extends Fragment{

    public static final String FRAGMENT_TAG = "sight_fragment";
    private static final String KEY_ID = "sight_fragment_key_id";

    private static final String WEATHER_TAG = "weather_tag_sight";

    private Sight mSight;

    private SliderLayout mSliderShow;

    private WeatherClient mClient;

    public static SightFragment newInstance(String pId){
        SightFragment fragment = new SightFragment();
        Bundle bundle = new Bundle();
        bundle.putString(KEY_ID, pId);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WeatherClient.ClientBuilder builder = new WeatherClient.ClientBuilder();
        WeatherConfig config = new WeatherConfig();
        config.ApiKey = getResources().getString(R.string.forecastio_key);

        try {
            mClient = builder.attach(getActivity())
                    .provider(new OpenweathermapProviderType())
                    .httpClient(com.survivingwithandroid.weather.lib.client.volley.WeatherClientDefault.class)
                    .config(config)
                    .build();
        } catch (WeatherProviderInstantiationException pE) {
            pE.printStackTrace();
        }

        String id = getArguments().getString(KEY_ID);
        mSight = Realm.getDefaultInstance().where(Sight.class).equalTo("mId", id).findFirst();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_sight, container, false);

        setHasOptionsMenu(true);

        NavigationDrawerActivity activity = ((NavigationDrawerActivity)getActivity());

        ActionBar bar = activity.getSupportActionBar();
        bar.setDisplayShowCustomEnabled(false);
        bar.setDisplayHomeAsUpEnabled(true);
        bar.show();

        Toolbar toolbar = activity.getToolbar();

        toolbar.setTitle(mSight.getName());
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View pView) {
                getActivity().onBackPressed();
            }
        });

        if(mSight != null){

            mSliderShow = (SliderLayout) v.findViewById(R.id.slider);
            mSliderShow.stopAutoCycle();

            ArrayList<String> list = new ArrayList<>();
            list.add(mSight.getPhotoUrl());
            list.add(mSight.getPhoto1Url());
            list.add(mSight.getPhoto2Url());

            for (String url : list) {
                DefaultSliderView textSliderView = new DefaultSliderView(getActivity());
                textSliderView
                        .image(url)
                        .setScaleType(BaseSliderView.ScaleType.CenterCrop)
                        .setPicasso(Picasso.with(getActivity().getApplicationContext()));

                mSliderShow.setDuration(5000);
                mSliderShow.setSliderTransformDuration(2000, null);
                mSliderShow.addSlider(textSliderView);
                mSliderShow.setPresetTransformer(SliderLayout.Transformer.Stack);
            }

            ((TextView)v.findViewById(R.id.text_view_name_sight_fragment)).setText(mSight.getName());

            ((TextView)v.findViewById(R.id.text_view_about_sight_fragment)).setText(mSight.getAbout());
            ((TextView)v.findViewById(R.id.text_view_category_sight_fragment)).setText(mSight.getCategory());


            try{
                mClient.getCurrentCondition(new WeatherRequest(mSight.getLongitude(), mSight.getLatitude()), new WeatherClient.WeatherEventListener() {
                    @Override
                    public void onWeatherError(WeatherLibException wle) {

                    }

                    @Override
                    public void onConnectionError(Throwable t) {
                        Snackbar.make(getView(), "Connection Error", Snackbar.LENGTH_LONG).show();
                    }

                    @Override
                    public void onWeatherRetrieved(final CurrentWeather weather) {
                        ((TextView) v.findViewById(R.id.text_view_weather_sight_fragment)).setText("" + (int) weather.weather.temperature.getTemp() + "°C");
                    }

                });
            } catch (NullPointerException e){

            }

        }

        final ImageView fav = (ImageView)v.findViewById(R.id.image_view_fav_sight_fragment);

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        mSliderShow.startAutoCycle();
    }

    @Override
    public void onStop() {
        mSliderShow.stopAutoCycle();
        super.onStop();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.item, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == android.R.id.home){
            FragmentManager manager = getActivity().getSupportFragmentManager();
            Fragment fragment = manager.findFragmentByTag(SightFragment.FRAGMENT_TAG);
            manager.beginTransaction()
                    .remove(fragment)
                    .commit();
        } else if(id == R.id.action_location_search){

            FragmentManager manager = ((NavigationDrawerActivity)getActivity()).getSupportFragmentManager();

            Fragment fragment = manager.findFragmentByTag(GmapFragment.FRAGMENT_TAG);
            if(fragment == null){
                fragment = GmapFragment.newInstance(mSight.getName(), mSight.getLongitude(), mSight.getLatitude());

                manager.beginTransaction()
                        .add(R.id.fragment_container, fragment, GmapFragment.FRAGMENT_TAG)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .addToBackStack(GmapFragment.FRAGMENT_TAG)
                        .commit();
            } else {
                ((GmapFragment)fragment).moveCamera();
            }
            return false;
        } else if(id == R.id.action_weather){
            WeatherFragment fragment = WeatherFragment.newInstance(mSight.getLatitude(), mSight.getLongitude(), mSight.getPhotoUrl());
                fragment.show(getActivity().getFragmentManager(), WEATHER_TAG);
            return false;
        }
        return super.onOptionsItemSelected(item);
    }
}
