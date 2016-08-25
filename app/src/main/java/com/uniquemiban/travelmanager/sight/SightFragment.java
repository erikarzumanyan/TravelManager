package com.uniquemiban.travelmanager.sight;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.DefaultSliderView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.nirhart.parallaxscroll.views.ParallaxScrollView;
import com.squareup.picasso.Picasso;
import com.uniquemiban.travelmanager.R;
import com.uniquemiban.travelmanager.eat.EatListFragment;
import com.uniquemiban.travelmanager.login.LoginActivity;
import com.uniquemiban.travelmanager.map.GmapFragment;
import com.uniquemiban.travelmanager.models.Sight;
import com.uniquemiban.travelmanager.rate.RateFragment;
import com.uniquemiban.travelmanager.start.NavigationDrawerActivity;
import com.uniquemiban.travelmanager.utils.Constants;
import com.uniquemiban.travelmanager.weather.WeatherFragment;

import java.util.ArrayList;

import github.vatsal.easyweather.Helper.TempUnitConverter;
import github.vatsal.easyweather.Helper.WeatherCallback;
import github.vatsal.easyweather.WeatherMap;
import github.vatsal.easyweather.retrofit.models.WeatherResponseModel;
import io.realm.Realm;

public class SightFragment extends Fragment {

    public static final String FRAGMENT_TAG = "sight_fragment";
    private static final String KEY_ID = "sight_fragment_key_id";

    private static final String WEATHER_TAG = "weather_tag_sight";

    private int mDown, mUp, mDist = 5;

    private Sight mSight;

    private SliderLayout mSliderShow;

    WeatherMap mWeatherMap;

    DatabaseReference mRef;
    ValueEventListener mValueEventListener;

    public static SightFragment newInstance(String pId) {
        SightFragment fragment = new SightFragment();
        Bundle bundle = new Bundle();
        bundle.putString(KEY_ID, pId);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String id = getArguments().getString(KEY_ID);
        mSight = Realm.getDefaultInstance().where(Sight.class).equalTo("mId", id).findFirst();

        mWeatherMap = new WeatherMap(getActivity(), Constants.OWM_API_KEY);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_sight, container, false);

        setHasOptionsMenu(true);

        NavigationDrawerActivity activity = ((NavigationDrawerActivity) getActivity());

        final ActionBar bar = activity.getSupportActionBar();
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

        v.findViewById(R.id.parallax_scroll_view_fragment_sight).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View pView, MotionEvent pMotionEvent) {
                switch (pMotionEvent.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        mDown = (int)pMotionEvent.getY();
                        break;
                    case MotionEvent.ACTION_UP:
                        mUp = (int)pMotionEvent.getY();
                        if(mUp - mDown < -mDist){
                            bar.hide();
                        } else if (mUp - mDown > mDist){
                            bar.show();
                        }
                        break;
                }
                return false;
            }
        });

        if (mSight != null) {

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

            ((TextView) v.findViewById(R.id.text_view_name_sight_fragment)).setText(mSight.getName());

            ((TextView) v.findViewById(R.id.text_view_about_sight_fragment)).setText(mSight.getAbout());
            ((TextView) v.findViewById(R.id.text_view_category_sight_fragment)).setText(mSight.getCategory());

            v.findViewById(R.id.text_view_eat_sight_fragment).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View pView) {
                    FragmentManager manager = ((NavigationDrawerActivity) getActivity()).getSupportFragmentManager();

                    Fragment fragment = manager.findFragmentByTag(EatListFragment.FRAGMENT_TAG_RADIUS);
                    if (fragment == null) {
                        fragment = EatListFragment.newInstance(mSight.getLongitude(), mSight.getLatitude(), Constants.RADIUS);

                        manager.beginTransaction()
                                .replace(R.id.fragment_container, fragment, EatListFragment.FRAGMENT_TAG_RADIUS)
                                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                                .addToBackStack(GmapFragment.FRAGMENT_TAG)
                                .commit();
                    }
                }
            });

            mWeatherMap.getLocationWeather(String.valueOf(mSight.getLatitude()), String.valueOf(mSight.getLongitude()), new WeatherCallback() {
                @Override
                public void success(WeatherResponseModel response) {
                    Double weather = TempUnitConverter.convertToCelsius(response.getMain().getTemp());
                    ((TextView)v.findViewById(R.id.text_view_weather_sight_fragment)).setText(weather.intValue() + "°C");
                }

                @Override
                public void failure(String message) {
                    //Snackbar.make(getView(), "Connection Error", Snackbar.LENGTH_LONG).show();
                    mWeatherMap.getLocationWeather(String.valueOf(mSight.getLatitude()), String.valueOf(mSight.getLongitude()), new WeatherCallback() {
                        @Override
                        public void success(WeatherResponseModel response) {
                            Double weather = TempUnitConverter.convertToCelsius(response.getMain().getTemp());
                            ((TextView)v.findViewById(R.id.text_view_weather_sight_fragment)).setText(weather.intValue() + "°C");
                        }

                        @Override
                        public void failure(String message) {
                            ((TextView)v.findViewById(R.id.text_view_weather_sight_fragment)).setText("N/A");
                        }
                    });
                }
            });

            final TextView rateTextView = (TextView)v.findViewById(R.id.text_view_rate_sight_fragment);
            final RatingBar rateRatingBar = (RatingBar)v.findViewById(R.id.rating_bar_fragment_sight);

            rateTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View pView) {
                    RateFragment fragment = RateFragment.newInstance(mSight.getId(), mSight.getName());
                    fragment.show(((NavigationDrawerActivity)getActivity()).getSupportFragmentManager(), RateFragment.FRAGMENT_TAG);
                }
            });

            rateRatingBar.setOnClickListener(null);

            mRef = FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_AVG_RATES).child(mSight.getId());

            mValueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot pDataSnapshot) {

                    Double sum = pDataSnapshot.child("sum").getValue(Double.class);
                    Long num = pDataSnapshot.child("num").getValue(Long.class);

                    if(sum != null && num != null) {
                        if(num != 0) {
                            rateTextView.setText(String.format("%.2f", (float) (sum / num)));
                            rateRatingBar.setRating((float) (sum / num));
                        } else{
                            rateTextView.setText("Rate");
                            rateRatingBar.setRating(0f);
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError pDatabaseError) {

                }
            };

        }

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        mRef.addValueEventListener(mValueEventListener);
        mSliderShow.startAutoCycle();
    }

    @Override
    public void onStop() {
        mSliderShow.stopAutoCycle();
        mRef.removeEventListener(mValueEventListener);
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

        if (id == android.R.id.home) {
            FragmentManager manager = getActivity().getSupportFragmentManager();
            Fragment fragment = manager.findFragmentByTag(SightFragment.FRAGMENT_TAG);
            manager.beginTransaction()
                    .remove(fragment)
                    .commit();
        } else if (id == R.id.action_location_search) {

            FragmentManager manager = ((NavigationDrawerActivity) getActivity()).getSupportFragmentManager();

            Fragment fragment = manager.findFragmentByTag(GmapFragment.FRAGMENT_TAG);
            if (fragment == null) {
                fragment = GmapFragment.newInstance(mSight.getName(), mSight.getLongitude(), mSight.getLatitude());

                manager.beginTransaction()
                        .add(R.id.fragment_container, fragment, GmapFragment.FRAGMENT_TAG)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .addToBackStack(GmapFragment.FRAGMENT_TAG)
                        .commit();
            } else {
                ((GmapFragment) fragment).moveCamera();
            }
            return false;
        } else if (id == R.id.action_weather) {
            WeatherFragment fragment = WeatherFragment.newInstance(mSight.getLatitude(), mSight.getLongitude(), mSight.getPhotoUrl());
            fragment.show(getActivity().getFragmentManager(), WEATHER_TAG);
            return false;
        }
        return super.onOptionsItemSelected(item);
    }
}
