package com.uniquemiban.travelmanager.eat;

import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.daimajia.slider.library.Tricks.ViewPagerEx;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nirhart.parallaxscroll.views.ParallaxScrollView;
import com.squareup.picasso.Picasso;
import com.uniquemiban.travelmanager.R;
import com.uniquemiban.travelmanager.map.GmapFragment;
import com.uniquemiban.travelmanager.models.Eat;
import com.uniquemiban.travelmanager.models.Sight;
import com.uniquemiban.travelmanager.rate.RateFragment;
import com.uniquemiban.travelmanager.start.NavigationDrawerActivity;
import com.uniquemiban.travelmanager.utils.Constants;


import java.util.ArrayList;
import java.util.Random;

import io.realm.Realm;


public class EatFragment extends DialogFragment {

    public static final String FRAGMENT_TAG = "eat_fragment";
    private static final String KEY_ID = "eat_fragment_key_id";

    private Eat mEat;

    private SliderLayout mSliderShow;

    private int mDown, mUp, mDist = 5;

    DatabaseReference mRef;
    ValueEventListener mValueEventListener;

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

        NavigationDrawerActivity activity = ((NavigationDrawerActivity)getActivity());

        final ActionBar bar = activity.getSupportActionBar();
        bar.setDisplayShowCustomEnabled(false);
        bar.setDisplayHomeAsUpEnabled(true);
        bar.show();

        final Toolbar toolbar = activity.getToolbar();

        toolbar.setTitle(mEat.getName());
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View pView) {
                ((NavigationDrawerActivity)getActivity()).onBackPressed();
            }
        });

        v.findViewById(R.id.parallax_scroll_view_fragment_eat).setOnTouchListener(new View.OnTouchListener() {
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
                mSliderShow.setPresetTransformer(SliderLayout.Transformer.Stack);
            }

            ((TextView)v.findViewById(R.id.text_view_name_eat_fragment)).setText(mEat.getName());

            ((TextView)v.findViewById(R.id.text_view_about_eat_fragment)).setText(mEat.getAbout());
            ((TextView)v.findViewById(R.id.text_view_category_eat_fragment)).setText(mEat.getCategory());

        }

        final TextView rateTextView = (TextView)v.findViewById(R.id.text_view_rate_eat_fragment);
        final RatingBar rateRatingBar = (RatingBar)v.findViewById(R.id.rating_bar_eat_fragment);

        rateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View pView) {
                RateFragment fragment = RateFragment.newInstance(mEat.getId(), mEat.getName());
                fragment.show(((NavigationDrawerActivity)getActivity()).getSupportFragmentManager(), RateFragment.FRAGMENT_TAG);
            }
        });

        rateRatingBar.setOnClickListener(null);

        mRef = FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_AVG_RATES).child(mEat.getId());

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
