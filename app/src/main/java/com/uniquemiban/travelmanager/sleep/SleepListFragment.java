package com.uniquemiban.travelmanager.sleep;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.maps.android.SphericalUtil;
import com.squareup.picasso.Picasso;
import com.uniquemiban.travelmanager.R;

import com.uniquemiban.travelmanager.filter.FilterFragment;
import com.uniquemiban.travelmanager.models.Sight;
import com.uniquemiban.travelmanager.models.Sleep;

import com.uniquemiban.travelmanager.sight.SightFragment;
import com.uniquemiban.travelmanager.start.NavigationDrawerActivity;
import com.uniquemiban.travelmanager.utils.Constants;

import java.util.ArrayList;
import java.util.List;

import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;

public class SleepListFragment extends Fragment{

    public static final String ARG_LONGITUDE = "sleep_list_fragment_arg_longitude";
    public static final String ARG_LATITUDE = "sleep_list_fragment_arg_latitude";
    public static final String ARG_RADIUS = "sleep_list_fragment_arg_radius";

    public static final String FRAGMENT_TAG = "sleep_list_fragment";
    public static final String FRAGMENT_TAG_RADIUS = "sleep_list_fragment_radius";
    private static final int LOADING_ITEMS_NUMBER = 5;
    private String mLastItemId = null;

    private static final int HIDE_THRESHOLD = 20;
    private int scrolledDistance = 0;
    private boolean controlsVisible = true;

    private boolean mLoading = true;
    int mFirstVisibleItemPosition, mVisibleItemCount, mTotalItemCount;


    private List<Sleep> mSleepList;
    private RecyclerView mSleepRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private SleepAdapter mAdapter;

    private DatabaseReference mRef;
    private Query mQuery;
    private Realm mRealm;

    private ChildEventListener mChildEventListener;

    private double mLongitude = -1;
    private double mLatitude = -1;
    private double mRadius = -1;

    private String mSearch = null;

    private Location mLastLocation = null;
    private float mMyRadius = -1;

    public static SleepListFragment newInstance(double pLongitude, double pLatitude, double pRadius){
        SleepListFragment fragment = new SleepListFragment();
        Bundle bundle = new Bundle();
        bundle.putDouble(ARG_LONGITUDE, pLongitude);
        bundle.putDouble(ARG_LATITUDE, pLatitude);
        bundle.putDouble(ARG_RADIUS, pRadius);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mRef = FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_SLEEPS).getRef();
        mRealm = Realm.getDefaultInstance();

        if(getArguments() != null){
            Bundle bundle = getArguments();
            mLongitude = bundle.getDouble(ARG_LONGITUDE);
            mLatitude = bundle.getDouble(ARG_LATITUDE);
            mRadius = bundle.getDouble(ARG_RADIUS);
        }

        mSleepList = new ArrayList<>();

        mChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot pDataSnapshot, String pS) {

                final Sleep s = pDataSnapshot.getValue(Sleep.class);

                if(mRadius != -1 && SphericalUtil.computeDistanceBetween(new LatLng(mLatitude, mLongitude), new LatLng(s.getLatitude(), s.getLongitude())) > mRadius){

                    mQuery.removeEventListener(mChildEventListener);
                    mLastItemId = s.getId();
                    mQuery = mRef.orderByKey().startAt(mLastItemId).limitToFirst(LOADING_ITEMS_NUMBER);
                    mQuery.addChildEventListener(mChildEventListener);

                } else if(mRadius == -1 && mLastLocation != null && mMyRadius > 0
                        && SphericalUtil.computeDistanceBetween(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()),
                        new LatLng(s.getLatitude(), s.getLongitude())) > mMyRadius) {

                    mQuery.removeEventListener(mChildEventListener);
                    mLastItemId = s.getId();
                    mQuery = mRef.orderByKey().startAt(mLastItemId).limitToFirst(LOADING_ITEMS_NUMBER);
                    mQuery.addChildEventListener(mChildEventListener);

                } else if(!TextUtils.isEmpty(mSearch) && !s.getName().contains(mSearch)
                        && !s.getCategory().contains(mSearch) && !s.getLocation().contains(mSearch)) {

                    mQuery.removeEventListener(mChildEventListener);
                    mLastItemId = s.getId();
                    mQuery = mRef.orderByKey().startAt(mLastItemId).limitToFirst(LOADING_ITEMS_NUMBER);
                    mQuery.addChildEventListener(mChildEventListener);

                } else {

                    if (TextUtils.isEmpty(s.getId())) {
                        String id = pDataSnapshot.getKey();
                        s.setId(id);
                        mRef.child(id).child("id").setValue(id);
                    } else {
                        mRealm = Realm.getDefaultInstance();

                        mRealm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                realm.copyToRealmOrUpdate(s);
                                updateUI(s);
                            }
                        });
                    }

                }
            }

            @Override
            public void onChildChanged(DataSnapshot pDataSnapshot, String pS) {
                final Sleep s = pDataSnapshot.getValue(Sleep.class);

                mRealm = Realm.getDefaultInstance();

                if(mRealm.where(Sleep.class).equalTo("mId", s.getId()).findFirst() != null) {
                    mRealm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            realm.copyToRealmOrUpdate(s);
                            updateUI(s);
                        }
                    });
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot pDataSnapshot) {
                final Sleep s = pDataSnapshot.getValue(Sleep.class);

                mRealm = Realm.getDefaultInstance();

                mRealm.executeTransactionAsync(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        Sleep sleep = realm.where(Sleep.class).equalTo("mId", s.getId()).findFirst();
                        sleep.deleteFromRealm();
                    }
                }, new Realm.Transaction.OnSuccess() {
                    @Override
                    public void onSuccess() {
                        deleteFromList(s);
                        Picasso.with(getActivity().getApplicationContext()).invalidate(s.getPhotoUrl());
                        Picasso.with(getActivity().getApplicationContext()).invalidate(s.getPhoto1Url());
                        Picasso.with(getActivity().getApplicationContext()).invalidate(s.getPhoto2Url());
                    }
                });
            }

            @Override
            public void onChildMoved(DataSnapshot pDataSnapshot, String pS) {

            }

            @Override
            public void onCancelled(DatabaseError pDatabaseError) {

            }
        };

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sleeps_list, container, false);

        setHasOptionsMenu(true);

        final NavigationDrawerActivity activity = (NavigationDrawerActivity)getActivity();

        ActionBar bar = activity.getSupportActionBar();

        Toolbar toolbar = activity.getToolbar();
        toolbar.setTitle("Sleep");

        if(mRadius == -1) {
            bar.setDisplayHomeAsUpEnabled(false);
            bar.setDisplayShowCustomEnabled(true);

            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    activity, activity.getDrawer(), toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            activity.getDrawer().setDrawerListener(toggle);
            toggle.syncState();
        } else {
            bar.setDisplayShowCustomEnabled(false);
            bar.setDisplayHomeAsUpEnabled(true);

            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View pView) {
                    activity.onBackPressed();
                }
            });
        }

        mSleepRecyclerView = (RecyclerView) view.findViewById(R.id.fragment_sleeps_list_recycler_view);
        mSleepRecyclerView.setItemAnimator(new RecyclerView.ItemAnimator() {
            @Override
            public boolean animateDisappearance(@NonNull RecyclerView.ViewHolder viewHolder, @NonNull ItemHolderInfo preLayoutInfo, @Nullable ItemHolderInfo postLayoutInfo) {
                return false;
            }

            @Override
            public boolean animateAppearance(@NonNull RecyclerView.ViewHolder viewHolder, @Nullable ItemHolderInfo preLayoutInfo, @NonNull ItemHolderInfo postLayoutInfo) {
                return false;
            }

            @Override
            public boolean animatePersistence(@NonNull RecyclerView.ViewHolder viewHolder, @NonNull ItemHolderInfo preLayoutInfo, @NonNull ItemHolderInfo postLayoutInfo) {
                return false;
            }

            @Override
            public boolean animateChange(@NonNull RecyclerView.ViewHolder oldHolder, @NonNull RecyclerView.ViewHolder newHolder, @NonNull ItemHolderInfo preLayoutInfo, @NonNull ItemHolderInfo postLayoutInfo) {
                return false;
            }

            @Override
            public void runPendingAnimations() {

            }

            @Override
            public void endAnimation(RecyclerView.ViewHolder item) {

            }

            @Override
            public void endAnimations() {

            }

            @Override
            public boolean isRunning() {
                return false;
            }
        });

        mLinearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        mSleepRecyclerView.setLayoutManager(mLinearLayoutManager);

        mAdapter = new SleepAdapter(mSleepList);
        mSleepRecyclerView.setAdapter(mAdapter);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        NavigationDrawerActivity activity = ((NavigationDrawerActivity)getActivity());
        activity.connect();

        if(mRadius == -1){
            mLastLocation = activity.getLastLocation();
            mMyRadius = activity.getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_PRIVATE).getFloat(Constants.SHARED_PREFS_KEY_RADIUS, -1);
        }

        if (mRealm.isClosed())
            mRealm = Realm.getDefaultInstance();

        mQuery = mRef.limitToFirst(LOADING_ITEMS_NUMBER);
        mQuery.addChildEventListener(mChildEventListener);

        final ActionBar bar = ((NavigationDrawerActivity)getActivity()).getSupportActionBar();

        mSleepRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

                if (scrolledDistance > HIDE_THRESHOLD && controlsVisible) {
                    bar.hide();
                    controlsVisible = false;
                    scrolledDistance = 0;
                } else if (scrolledDistance < -HIDE_THRESHOLD && !controlsVisible) {
                    bar.show();
                    controlsVisible = true;
                    scrolledDistance = 0;
                }

                if((controlsVisible && dy>0) || (!controlsVisible && dy<0)) {
                    scrolledDistance += dy;
                }

                if (dy < 0) {
                    mFirstVisibleItemPosition = mLinearLayoutManager.findFirstVisibleItemPosition();
                }

                if (dy > 0) {
                    if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                        mVisibleItemCount = mLinearLayoutManager.getChildCount();
                        mTotalItemCount = mLinearLayoutManager.getItemCount();
                        mFirstVisibleItemPosition = mLinearLayoutManager.findFirstVisibleItemPosition();

                        if (mLoading) {
                            if ((mVisibleItemCount + mFirstVisibleItemPosition + 1) >= mRealm.where(Sight.class).findAll().size()
                                    && (mVisibleItemCount + mFirstVisibleItemPosition + 1) >= mTotalItemCount) {
                                mQuery.removeEventListener(mChildEventListener);
                                mQuery = mRef.orderByKey().startAt(mSleepList.get(mSleepList.size() - 1).getId()).limitToFirst(LOADING_ITEMS_NUMBER);
                                mQuery.addChildEventListener(mChildEventListener);
                            }
                        }
                    }
                }
            }
        });

        searchItemsByRadius();
    }

    private void deleteFromList(Sleep pSleep) {
        int index = -1;
        String id = pSleep.getId();
        for (int i = 0; i < mSleepList.size(); ++i) {
            if (mSleepList.get(i).getId().equals(id))
                index = i;
        }
        if (index != -1) {
            mSleepList.remove(index);
            mAdapter.notifyItemRangeChanged(index, mSleepList.size() - 1);
        }
    }

    private void updateUI(Sleep pSleep) {
        int index = -1;
        String id = pSleep.getId();
        for (int i = 0; i < mSleepList.size(); ++i) {
            if (mSleepList.get(i).getId().equals(id))
                index = i;
        }
        if (index == -1) {
            mSleepList.add(pSleep);
            mAdapter.notifyItemChanged(mSleepList.size() - 1);
        } else {
            mSleepList.remove(index);
            mSleepList.add(index, pSleep);
            mAdapter.notifyItemChanged(index);
        }
    }

    public void searchItems(String pQuery){
        mSearch = pQuery;
        searchItemsByRadius();
    }

    public void searchItemsByRadius(){
        RealmQuery<Sleep> realmQuery = null;

        if(mSearch != null){
            realmQuery = mRealm.where(Sleep.class).contains("mName", mSearch, Case.INSENSITIVE);
            realmQuery = realmQuery.or().contains("mCategory", mSearch, Case.INSENSITIVE);
            realmQuery = realmQuery.or().contains("mLocation", mSearch, Case.INSENSITIVE);
        } else {
            realmQuery = mRealm.where(Sleep.class);
        }

        NavigationDrawerActivity activity = ((NavigationDrawerActivity)getActivity());
        mLastLocation = activity.getLastLocation();

        SharedPreferences prefs = activity.getSharedPreferences(Constants.SHARED_PREFS_SLEEP, Context.MODE_PRIVATE);
        mMyRadius = prefs.getFloat(Constants.SHARED_PREFS_KEY_RADIUS, -1);

        SharedPreferences shared = activity.getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_PRIVATE);
        if (mLastLocation == null && !TextUtils.isEmpty(shared.getString(Constants.SHARED_PREFS_KEY_LAST_LAT, ""))) {
            mLastLocation = new Location("");
            mLastLocation.setLatitude(Double.valueOf(shared.getString(Constants.SHARED_PREFS_KEY_LAST_LAT, "")));
            mLastLocation.setLongitude(Double.valueOf(shared.getString(Constants.SHARED_PREFS_KEY_LAST_LONG, "")));
        }

        RealmResults<Sleep> results = realmQuery.findAll();
        mSleepList.clear();
        for (Sleep e: results){
            if(mRadius != -1 && SphericalUtil.computeDistanceBetween(new LatLng(mLatitude, mLongitude),
                    new LatLng(e.getLatitude(), e.getLongitude())) > mRadius)
                continue;

            if(mRadius == - 1 && mLastLocation != null && mMyRadius > 0
                    && SphericalUtil.computeDistanceBetween(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()),
                    new LatLng(e.getLatitude(), e.getLongitude())) > mMyRadius)
                continue;

            mSleepList.add(e);
        }

        mAdapter.notifyDataSetChanged();

        mQuery.removeEventListener(mChildEventListener);
        mQuery.addChildEventListener(mChildEventListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        ((NavigationDrawerActivity)getActivity()).disconnect();
        mRealm.close();
        mRef.removeEventListener(mChildEventListener);
        mQuery.removeEventListener(mChildEventListener);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        if (mRadius == -1)
            inflater.inflate(R.menu.items_list, menu);
        else
            inflater.inflate(R.menu.items_near, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_search){
            ((SearchView) item.getActionView()).setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    searchItems(query);
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    searchItems(newText);
                    return false;
                }
            });
        } else if(id == R.id.action_filter){
            FilterFragment fragment = FilterFragment.newInstance(Constants.SHARED_PREFS_SLEEP);
            fragment.show(((NavigationDrawerActivity)getActivity()).getSupportFragmentManager(), FilterFragment.FRAGMENT_TAG);
        }
        return super.onOptionsItemSelected(item);
    }

    private class SleepHolder extends RecyclerView.ViewHolder {

        private Sleep mSleep;

        public ImageView mPhotoImageView;
        public TextView mNameTextView;
        public TextView mLocationTextView;

        public SleepHolder(View itemView) {
            super(itemView);

            mPhotoImageView = (ImageView) itemView.findViewById(R.id.image_view_sleep);


            mNameTextView = (TextView) itemView.findViewById(R.id.text_view_name_sleep);
            mLocationTextView = (TextView) itemView.findViewById(R.id.text_view_location);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View pView) {
                    if (mSleep != null) {

                        FragmentManager manager = getActivity().getSupportFragmentManager();

                        Fragment fragment = manager.findFragmentByTag(SleepFragment.FRAGMENT_TAG);

                        if (fragment == null) {
                            fragment = SleepFragment.newInstance(mSleep.getId());
                            manager.beginTransaction()
                                    .replace(R.id.fragment_container, fragment, SleepFragment.FRAGMENT_TAG)
                                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                                    .addToBackStack(SightFragment.FRAGMENT_TAG)
                                    .commit();
                        }
                    }
                }
            });
        }

        public void bindSleep(Sleep pSleep) {
            mSleep = pSleep;

            if (mSleep.getName() != null)
                mNameTextView.setText(mSleep.getName());
            if (mSleep.getLocation() != null)
                mLocationTextView.setText("Location: " + mSleep.getLocation());

            Picasso.with(getActivity().getApplicationContext())
                    .load(mSleep.getPhotoUrl())
                    .resize(((NavigationDrawerActivity)getActivity()).getWidth(), 0)
                    .onlyScaleDown()
                    .placeholder(R.drawable.placeholder)
                    .into(mPhotoImageView);
        }
    }

    private class SleepAdapter extends RecyclerView.Adapter<SleepHolder> {

        private List<Sleep> mSleeps;

        public SleepAdapter(List<Sleep> pSleeps) {
            mSleeps = pSleeps;
        }

        @Override
        public SleepHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View v = inflater.inflate(R.layout.items_sleep, parent, false);

            YoYo.with(Techniques.FadeInUp)
                    .duration(700)
                    .playOn(v);

            return new SleepHolder(v);
        }

        @Override
        public void onBindViewHolder(SleepHolder holder, int position) {
            Sleep sleep = mSleeps.get(position);
            holder.bindSleep(sleep);
        }

        @Override
        public int getItemCount() {
            return mSleeps.size();
        }

    }
}
