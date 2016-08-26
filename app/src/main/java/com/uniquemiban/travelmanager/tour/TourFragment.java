package com.uniquemiban.travelmanager.tour;


import android.os.Bundle;
import android.support.annotation.Nullable;
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
import android.widget.TextView;

import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.uniquemiban.travelmanager.R;
import com.uniquemiban.travelmanager.map.GmapFragment;
import com.uniquemiban.travelmanager.models.Eat;
import com.uniquemiban.travelmanager.models.Tour;
import com.uniquemiban.travelmanager.start.NavigationDrawerActivity;
import com.uniquemiban.travelmanager.weather.WeatherFragment;

import java.util.ArrayList;

import io.realm.Realm;

public class TourFragment extends Fragment {

    public static final String FRAGMENT_TAG = "tour_fragment";
    private static final String KEY_ID = "tour_fragment_key_id";

    private Tour mTour;
    private int mDown, mUp, mDist = 5;


    public static TourFragment newInstance(String pId){
        TourFragment fragment = new TourFragment();
        Bundle bundle = new Bundle();
        bundle.putString(KEY_ID, pId);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String id = getArguments().getString(KEY_ID);
        mTour = Realm.getDefaultInstance().where(Tour.class).equalTo("mId", id).findFirst();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_tour, container, false);

        setHasOptionsMenu(true);
        NavigationDrawerActivity activity = ((NavigationDrawerActivity)getActivity());
       final ActionBar bar = activity.getSupportActionBar();
        bar.setDisplayShowCustomEnabled(false);
        bar.setDisplayHomeAsUpEnabled(true);
        bar.show();

        Toolbar toolbar = activity.getToolbar();

        toolbar.setTitle(mTour.getName());
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View pView) {
                getActivity().onBackPressed();
            }
        });

        v.findViewById(R.id.parallax_scroll_view_fragment_tour).setOnTouchListener(new View.OnTouchListener() {
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

        if(mTour != null){

            SliderLayout sliderShow = (SliderLayout) v.findViewById(R.id.slider_tour_fragment);
            ArrayList<String> list = new ArrayList<>();
            list.add(mTour.getPhotoUrl());


            for (String url : list) {
                TextSliderView textSliderView = new TextSliderView(getActivity());
                textSliderView
                        .image(url)
                        .setScaleType(BaseSliderView.ScaleType.CenterCrop);


                sliderShow.addSlider(textSliderView);
            }
            sliderShow.setDuration(6000);
            sliderShow.setPresetTransformer(SliderLayout.Transformer.Fade);

            ((TextView)v.findViewById(R.id.text_view_name_tour_fragment)).setText(mTour.getName());

            ((TextView)v.findViewById(R.id.text_view_about_tour_fragment)).setText(mTour.getAbout());

            ((TextView)v.findViewById(R.id.text_view_tour_price_tour_fragment)).setText(""+mTour.getPrice());


        }



        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.tour, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == android.R.id.home){
            FragmentManager manager = getActivity().getSupportFragmentManager();
            Fragment fragment = manager.findFragmentByTag(TourFragment.FRAGMENT_TAG);
            manager.beginTransaction()
                    .remove(fragment)
                    .commit();
        } else if(id == R.id.action_location_search){

            FragmentManager manager = ((NavigationDrawerActivity)getActivity()).getSupportFragmentManager();

            Fragment fragment = manager.findFragmentByTag(GmapFragment.FRAGMENT_TAG);

            return false;
        }
        return super.onOptionsItemSelected(item);
    }
}
