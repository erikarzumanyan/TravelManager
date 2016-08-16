package com.uniquemiban.travelmanager.eat;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.uniquemiban.travelmanager.R;
import com.uniquemiban.travelmanager.map.GmapFragment;
import com.uniquemiban.travelmanager.models.Eat;
import com.uniquemiban.travelmanager.start.NavigationDrawerActivity;


import java.util.ArrayList;

import io.realm.Realm;


public class EatFragment extends Fragment {

    public static final String FRAGMENT_TAG = "eat_fragment";
    private static final String KEY_ID = "eat_fragment_key_id";

    private static final String MAP_TAG = "map_tag";


    private Eat mEat;

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

        String id = getArguments().getString(KEY_ID);
        mEat = Realm.getDefaultInstance().where(Eat.class).equalTo("mId", id).findFirst();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_eat, container, false);

        setHasOptionsMenu(true);
        ActionBar bar = ((NavigationDrawerActivity)getActivity()).getSupportActionBar();
        bar.show();
        bar.setDefaultDisplayHomeAsUpEnabled(true);

        if(mEat != null){

            SliderLayout sliderShow = (SliderLayout) v.findViewById(R.id.slider_eat_fragment);
            ArrayList<String> list = new ArrayList<>();
            list.add(mEat.getPhotoUrl());


            for (String url : list) {
                TextSliderView textSliderView = new TextSliderView(getActivity());
                textSliderView
                        .image(url)
                        .description("Location: " + mEat.getLocation())
                        .setScaleType(BaseSliderView.ScaleType.CenterCrop);


                sliderShow.addSlider(textSliderView);
            }
            sliderShow.setDuration(6000);
            sliderShow.setPresetTransformer(SliderLayout.Transformer.Fade);

            ((TextView)v.findViewById(R.id.text_view_name_eat_fragment)).setText(mEat.getName());

            ((TextView)v.findViewById(R.id.text_view_about_eat_fragment)).setText(mEat.getAbout());


        }



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
            GmapFragment fragment = GmapFragment.newInstance(mEat.getName(), mEat.getLongitude(), mEat.getLatitude());

            FragmentManager manager = ((NavigationDrawerActivity)getActivity()).getSupportFragmentManager();
            manager.beginTransaction()
                    .add(R.id.fragment_container, fragment, MAP_TAG)
                    .addToBackStack(MAP_TAG)
                    .commit();
            return false;
        }
        return super.onOptionsItemSelected(item);
    }
}
