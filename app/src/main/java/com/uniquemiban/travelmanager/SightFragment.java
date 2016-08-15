package com.uniquemiban.travelmanager;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
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
import com.uniquemiban.travelmanager.map.GmapFragment;
import com.uniquemiban.travelmanager.models.Sight;

import java.util.ArrayList;

import io.realm.Realm;

public class SightFragment extends Fragment{

    public static final String FRAGMENT_TAG = "sight_fragment";
    private static final String KEY_ID = "sight_fragment_key_id";

    private static final String MAP_TAG = "map_tag";
    private static final String WEATHER_TAG = "weather_tag";

    private Sight mSight;

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

        String id = getArguments().getString(KEY_ID);
        mSight = Realm.getDefaultInstance().where(Sight.class).equalTo("mId", id).findFirst();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_sight, container, false);

        setHasOptionsMenu(true);
        ActionBar bar = ((NavigationDrawerActivity)getActivity()).getSupportActionBar();
        bar.show();
        bar.setDefaultDisplayHomeAsUpEnabled(true);

        if(mSight != null){

            SliderLayout sliderShow = (SliderLayout) v.findViewById(R.id.slider);
            ArrayList<String> list = new ArrayList<>();
            list.add(mSight.getPhotoUrl());
            list.add(mSight.getPhoto1Url());
            list.add(mSight.getPhoto2Url());

            for (String url : list) {
                TextSliderView textSliderView = new TextSliderView(getActivity());
                textSliderView
                        .image(url)
                        .description("Location: " + mSight.getLocation())
                        .setScaleType(BaseSliderView.ScaleType.CenterCrop);


                sliderShow.addSlider(textSliderView);
            }
            sliderShow.setDuration(6000);
            sliderShow.setPresetTransformer(SliderLayout.Transformer.Fade);

            ((TextView)v.findViewById(R.id.text_view_name_sight_fragment)).setText(mSight.getName());

            ((TextView)v.findViewById(R.id.text_view_about_sight_fragment)).setText(mSight.getAbout());
            ((TextView)v.findViewById(R.id.text_view_category_sight_fragment)).setText(mSight.getCategory());

        }

        final ImageView fav = (ImageView)v.findViewById(R.id.image_view_fav_sight_fragment);

        fav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View pView) {
                fav.setImageResource(R.drawable.fav_rem);
                ((TextView)v.findViewById(R.id.text_view_fav_count_sight_fragment)).setText("1");
            }
        });

        return v;
    }

    @Override
    public void onDestroy() {
        ((NavigationDrawerActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        super.onDestroy();
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
        }
        else if(id == R.id.action_location_search){
            GmapFragment fragment = GmapFragment.newInstance(mSight.getName(), mSight.getLongitude(), mSight.getLatitude());

            FragmentManager manager = ((NavigationDrawerActivity)getActivity()).getSupportFragmentManager();
                    manager.beginTransaction()
                        .add(R.id.fragment_container, fragment, MAP_TAG)
                        .addToBackStack(MAP_TAG)
                        .commit();
            return false;
        } else if(id == R.id.action_weather){
            WeatherFragment fragment = WeatherFragment.newInstance(mSight.getLatitude(), mSight.getLongitude(), mSight.getPhotoUrl());
                fragment.show(getActivity().getFragmentManager(), WEATHER_TAG);
            return false;
        }
        return super.onOptionsItemSelected(item);
    }
}
