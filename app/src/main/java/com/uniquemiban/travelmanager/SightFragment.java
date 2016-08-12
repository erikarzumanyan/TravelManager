package com.uniquemiban.travelmanager;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.squareup.picasso.Picasso;
import com.uniquemiban.travelmanager.map.GmapFragment;
import com.uniquemiban.travelmanager.models.Sight;

import java.util.ArrayList;

import io.realm.Realm;

public class SightFragment extends Fragment{

    public static final String FRAGMENT_TAG = "sight_fragment";
    private static final String KEY_ID = "sight_fragment_key_id";

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
        View v = inflater.inflate(R.layout.fragment_sight, container, false);

        if(mSight != null){

            SliderLayout sliderShow = (SliderLayout) v.findViewById(R.id.slider);
            ArrayList<String> list = new ArrayList<>();
            list.add(mSight.getPhotoUrl());
            list.add(mSight.getPhotoUrl());

            for (String name : list) {
                TextSliderView textSliderView = new TextSliderView(getActivity());
                textSliderView
                        .image(name)
                        .description("Location: " + mSight.getLocation())
                        .setScaleType(BaseSliderView.ScaleType.CenterCrop);


                sliderShow.addSlider(textSliderView);
            }
            sliderShow.setDuration(3000);
            sliderShow.setPresetTransformer(SliderLayout.Transformer.Fade);

            ((TextView)v.findViewById(R.id.text_view_name_sight_fragment)).setText(mSight.getName());
            ((TextView)v.findViewById(R.id.text_view_about_sight_fragment)).setText(mSight.getAbout());
            //((TextView)v.findViewById(R.id.text_view_location_sight_fragment)).setText("Location: " + mSight.getLocation());
            ((TextView)v.findViewById(R.id.text_view_category_sight_fragment)).setText(mSight.getCategory());

        }

        v.findViewById(R.id.text_view_weather_sight_fragment).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View pView) {
                WeatherFragment fragment = WeatherFragment.newInstance(mSight.getLatitude(), mSight.getLongitude(), mSight.getPhotoUrl());
                fragment.show(getActivity().getFragmentManager(), "tag");
            }
        });

        v.findViewById(R.id.image_view_location_sight_fragment).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View pView) {
                GmapFragment fragment = GmapFragment.newInstance(mSight.getName(), mSight.getLongitude(), mSight.getLatitude());
                getActivity().getFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, fragment, "map")
                        .commit();
            }
        });

        return v;
    }
}
