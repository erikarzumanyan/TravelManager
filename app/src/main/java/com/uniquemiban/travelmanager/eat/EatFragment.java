package com.uniquemiban.travelmanager.eat;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.daimajia.slider.library.Tricks.ViewPagerEx;
import com.squareup.picasso.Picasso;
import com.uniquemiban.travelmanager.R;
import com.uniquemiban.travelmanager.map.GmapFragment;
import com.uniquemiban.travelmanager.models.Eat;
import com.uniquemiban.travelmanager.models.Sight;
import com.uniquemiban.travelmanager.start.NavigationDrawerActivity;


import java.util.ArrayList;
import java.util.Random;

import io.realm.Realm;


public class EatFragment extends Fragment {

    public static final String FRAGMENT_TAG = "eat_fragment";
    private static final String KEY_ID = "eat_fragment_key_id";

    private Eat mEat;

    private SliderLayout mSliderShow;
    private Random mRandom;
    private ViewPagerEx.OnPageChangeListener mOnPageChangeListener;

    public static EatFragment newInstance(String pId){
     EatFragment fragment = new EatFragment();
        Bundle bundle = new Bundle();
        bundle.putString(KEY_ID, pId);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRandom = new Random();
        mOnPageChangeListener = new ViewPagerEx.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                setTransition(mRandom.nextInt(4));
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                setTransition(mRandom.nextInt(4));
            }
        };
        String id = getArguments().getString(KEY_ID);
        mEat = Realm.getDefaultInstance().where(Eat.class).equalTo("mId", id).findFirst();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_eat, container, false);

        setHasOptionsMenu(true);

        NavigationDrawerActivity activity = ((NavigationDrawerActivity)getActivity());

        ActionBar bar = activity.getSupportActionBar();
        bar.setDisplayShowCustomEnabled(false);
        bar.setDisplayHomeAsUpEnabled(true);
        bar.show();

        Toolbar toolbar = activity.getToolbar();

        toolbar.setTitle(mEat.getName());
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View pView) {
                getActivity().onBackPressed();
            }
        });

        if(mEat != null){

            mSliderShow = (SliderLayout) v.findViewById(R.id.slider_eat_fragment);
            mSliderShow.stopAutoCycle();

            ArrayList<String> list = new ArrayList<>();
            list.add(mEat.getPhotoUrl());
            list.add(mEat.getPhoto1Url());
            list.add(mEat.getPhoto2Url());

            for (String url : list) {
                TextSliderView textSliderView = new TextSliderView(getActivity());
                textSliderView
                        .description("Location: " + mEat.getLocation())
                        .image(url)
                        .setScaleType(BaseSliderView.ScaleType.CenterCrop)
                        .setPicasso(Picasso.with(getActivity().getApplicationContext()));

                mSliderShow.setDuration(5000);
                mSliderShow.setSliderTransformDuration(2000, null);
                mSliderShow.addSlider(textSliderView);

                setTransition(mRandom.nextInt(4));
            }

            ((TextView)v.findViewById(R.id.text_view_name_eat_fragment)).setText(mEat.getName());

            ((TextView)v.findViewById(R.id.text_view_about_eat_fragment)).setText(mEat.getAbout());
            ((TextView)v.findViewById(R.id.text_view_category_eat_fragment)).setText(mEat.getCategory());

        }

        return v;
    }

    private void setTransition(int pT){
        switch (pT){
            case 0:
                mSliderShow.setPresetTransformer(SliderLayout.Transformer.ZoomOut);
                break;
            case 1:
                mSliderShow.setPresetTransformer(SliderLayout.Transformer.Stack);
                break;
            case 2:
                mSliderShow.setPresetTransformer(SliderLayout.Transformer.Background2Foreground);
                break;
            case 3:
                mSliderShow.setPresetTransformer(SliderLayout.Transformer.Foreground2Background);
                break;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mSliderShow.addOnPageChangeListener(mOnPageChangeListener);
        mSliderShow.startAutoCycle();
    }

    @Override
    public void onStop() {
        mSliderShow.stopAutoCycle();
        mSliderShow.removeOnPageChangeListener(mOnPageChangeListener);
        super.onStop();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.eat, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == android.R.id.home){
            FragmentManager manager = getActivity().getSupportFragmentManager();
            Fragment fragment = manager.findFragmentByTag(EatFragment.FRAGMENT_TAG);
            manager.beginTransaction()
                    .remove(fragment)
                    .commit();
        }
        else if(id == R.id.action_location_search){
            FragmentManager manager = ((NavigationDrawerActivity)getActivity()).getSupportFragmentManager();

            Fragment fragment = manager.findFragmentByTag(GmapFragment.FRAGMENT_TAG);
            if(fragment == null){
                fragment = GmapFragment.newInstance(mEat.getName(), mEat.getLongitude(), mEat.getLatitude());

                manager.beginTransaction()
                        .add(R.id.fragment_container, fragment, GmapFragment.FRAGMENT_TAG)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .addToBackStack(GmapFragment.FRAGMENT_TAG)
                        .commit();
            } else {
                ((GmapFragment)fragment).moveCamera();
            }
            return false;
        }
        return super.onOptionsItemSelected(item);
    }
}