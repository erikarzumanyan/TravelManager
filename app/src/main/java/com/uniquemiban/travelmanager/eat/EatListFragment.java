package com.uniquemiban.travelmanager.eat;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.support.v4.app.Fragment;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
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
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.uniquemiban.travelmanager.models.Eat;
import com.uniquemiban.travelmanager.models.Sight;
import com.uniquemiban.travelmanager.sight.SightFragment;
import com.uniquemiban.travelmanager.start.NavigationDrawerActivity;
import com.uniquemiban.travelmanager.utils.Constants;
import com.uniquemiban.travelmanager.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;

public class EatListFragment extends Fragment {

    public static final String ARG_LONGITUDE = "eat_list_fragment_arg_longitude";
    public static final String ARG_LATITUDE = "eat_list_fragment_arg_latitude";
    public static final String ARG_RADIUS = "eat_list_fragment_arg_radius";

    public static final String FRAGMENT_TAG = "eat_list_fragment";
    public static final String FRAGMENT_TAG_RADIUS = "eat_list_fragment_radius";
    private static final int LOADING_ITEMS_NUMBER = 5;
    private String mLastItemId = null;

    private static final int HIDE_THRESHOLD = 20;
    private int scrolledDistance = 0;
    private boolean controlsVisible = true;

    private boolean mLoading = true;
    int mFirstVisibleItemPosition, mVisibleItemCount, mTotalItemCount;


    private List<Eat> mEatList;
    private RecyclerView mEatRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private EatAdapter mAdapter;

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

    public static EatListFragment newInstance(double pLongitude, double pLatitude, double pRadius){
        EatListFragment fragment = new EatListFragment();
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

        mRef = FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_EATS).getRef();
        mRealm = Realm.getDefaultInstance();

        if(getArguments() != null){
            Bundle bundle = getArguments();
            mLongitude = bundle.getDouble(ARG_LONGITUDE);
            mLatitude = bundle.getDouble(ARG_LATITUDE);
            mRadius = bundle.getDouble(ARG_RADIUS);
        }

        mEatList = new ArrayList<>();

        mChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot pDataSnapshot, String pS) {

                final Eat e = pDataSnapshot.getValue(Eat.class);

                if(mRadius != -1 && SphericalUtil.computeDistanceBetween(new LatLng(mLatitude, mLongitude), new LatLng(e.getLatitude(), e.getLongitude())) > mRadius){

                    mQuery.removeEventListener(mChildEventListener);
                    mLastItemId = e.getId();
                    mQuery = mRef.orderByKey().startAt(mLastItemId).limitToFirst(LOADING_ITEMS_NUMBER);
                    mQuery.addChildEventListener(mChildEventListener);

                } else if(mRadius == -1 && mLastLocation != null && mMyRadius > 0
                            && SphericalUtil.computeDistanceBetween(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()),
                            new LatLng(e.getLatitude(), e.getLongitude())) > mMyRadius) {

                        mQuery.removeEventListener(mChildEventListener);
                        mLastItemId = e.getId();
                        mQuery = mRef.orderByKey().startAt(mLastItemId).limitToFirst(LOADING_ITEMS_NUMBER);
                        mQuery.addChildEventListener(mChildEventListener);

                } else if(!TextUtils.isEmpty(mSearch) && !e.getName().contains(mSearch)
                        && !e.getCategory().contains(mSearch) && !e.getLocation().contains(mSearch)) {

                    mQuery.removeEventListener(mChildEventListener);
                    mLastItemId = e.getId();
                    mQuery = mRef.orderByKey().startAt(mLastItemId).limitToFirst(LOADING_ITEMS_NUMBER);
                    mQuery.addChildEventListener(mChildEventListener);

                } else {

                    if (TextUtils.isEmpty(e.getId())) {
                        String id = pDataSnapshot.getKey();
                        e.setId(id);
                        mRef.child(id).child("id").setValue(id);
                    } else {
                        mRealm = Realm.getDefaultInstance();

                        mRealm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                realm.copyToRealmOrUpdate(e);
                                updateUI(e);
                            }
                        });
                    }

                }
            }

            @Override
            public void onChildChanged(DataSnapshot pDataSnapshot, String pS) {
                final Eat e = pDataSnapshot.getValue(Eat.class);

                mRealm = Realm.getDefaultInstance();

                if(mRealm.where(Eat.class).equalTo("mId", e.getId()).findFirst() != null) {
                    mRealm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            realm.copyToRealmOrUpdate(e);
                            updateUI(e);
                        }
                    });
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot pDataSnapshot) {
                final Eat e = pDataSnapshot.getValue(Eat.class);

                mRealm = Realm.getDefaultInstance();

                mRealm.executeTransactionAsync(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        Eat eat = realm.where(Eat.class).equalTo("mId", e.getId()).findFirst();
                        eat.deleteFromRealm();
                    }
                }, new Realm.Transaction.OnSuccess() {
                    @Override
                    public void onSuccess() {
                        deleteFromList(e);
                        Picasso.with(getActivity().getApplicationContext()).invalidate(e.getPhotoUrl());
                        Picasso.with(getActivity().getApplicationContext()).invalidate(e.getPhoto1Url());
                        Picasso.with(getActivity().getApplicationContext()).invalidate(e.getPhoto2Url());
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
        View view = inflater.inflate(R.layout.fragment_eat_list, container, false);

        setHasOptionsMenu(true);

        final NavigationDrawerActivity activity = (NavigationDrawerActivity)getActivity();

        ActionBar bar = activity.getSupportActionBar();

        Toolbar toolbar = activity.getToolbar();
        toolbar.setTitle("Eat");

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

        mEatRecyclerView = (RecyclerView) view.findViewById(R.id.fragment_eat_list_recycler_view);
        mEatRecyclerView.setItemAnimator(new RecyclerView.ItemAnimator() {
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
        mEatRecyclerView.setLayoutManager(mLinearLayoutManager);

        mAdapter = new EatAdapter(mEatList);
        mEatRecyclerView.setAdapter(mAdapter);

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

        mEatRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
                                mQuery = mRef.orderByKey().startAt(mEatList.get(mEatList.size() - 1).getId()).limitToFirst(LOADING_ITEMS_NUMBER);
                                mQuery.addChildEventListener(mChildEventListener);
                            }
                        }
                    }
                }
            }
        });

        searchItemsByRadius();
    }

    private void deleteFromList(Eat pEat) {
        int index = -1;
        String id = pEat.getId();
        for (int i = 0; i < mEatList.size(); ++i) {
            if (mEatList.get(i).getId().equals(id))
                index = i;
        }
        if (index != -1) {
            mEatList.remove(index);
            mAdapter.notifyItemRangeChanged(index, mEatList.size() - 1);
        }
    }

    private void updateUI(Eat pEat) {
        int index = -1;
        String id = pEat.getId();
        for (int i = 0; i < mEatList.size(); ++i) {
            if (mEatList.get(i).getId().equals(id))
                index = i;
        }
        if (index == -1) {
            mEatList.add(pEat);
            mAdapter.notifyItemChanged(mEatList.size() - 1);
        } else {
            mEatList.remove(index);
            mEatList.add(index, pEat);
            mAdapter.notifyItemChanged(index);
        }
    }

    public void searchItems(String pQuery){
        mSearch = pQuery;
        searchItemsByRadius();
    }

    public void searchItemsByRadius(){
        RealmQuery<Eat> realmQuery = null;

        if(mSearch != null){
            realmQuery = mRealm.where(Eat.class).contains("mName", mSearch, Case.INSENSITIVE);
            realmQuery = realmQuery.or().contains("mCategory", mSearch, Case.INSENSITIVE);
            realmQuery = realmQuery.or().contains("mLocation", mSearch, Case.INSENSITIVE);
        } else {
            realmQuery = mRealm.where(Eat.class);
        }

        NavigationDrawerActivity activity = ((NavigationDrawerActivity)getActivity());
        mLastLocation = activity.getLastLocation();

        SharedPreferences prefs = activity.getSharedPreferences(Constants.SHARED_PREFS_EAT, Context.MODE_PRIVATE);
        mMyRadius = prefs.getFloat(Constants.SHARED_PREFS_KEY_RADIUS, -1);

        SharedPreferences shared = activity.getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_PRIVATE);
        if (mLastLocation == null && !TextUtils.isEmpty(shared.getString(Constants.SHARED_PREFS_KEY_LAST_LAT, ""))) {
            mLastLocation = new Location("");
            mLastLocation.setLatitude(Double.valueOf(shared.getString(Constants.SHARED_PREFS_KEY_LAST_LAT, "")));
            mLastLocation.setLongitude(Double.valueOf(shared.getString(Constants.SHARED_PREFS_KEY_LAST_LONG, "")));
        }

        RealmResults<Eat> results = realmQuery.findAll();
        mEatList.clear();
        for (Eat e: results){
            if(mRadius != -1 && SphericalUtil.computeDistanceBetween(new LatLng(mLatitude, mLongitude),
                    new LatLng(e.getLatitude(), e.getLongitude())) > mRadius)
                continue;

            if(mRadius == - 1 && mLastLocation != null && mMyRadius > 0
                    && SphericalUtil.computeDistanceBetween(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()),
                    new LatLng(e.getLatitude(), e.getLongitude())) > mMyRadius)
                continue;

            mEatList.add(e);
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
            if (mLastLocation != null) {

                FilterFragment fragment = FilterFragment.newInstance(Constants.SHARED_PREFS_EAT);
                fragment.show(((NavigationDrawerActivity) getActivity()).getSupportFragmentManager(), FilterFragment.FRAGMENT_TAG);

            } else {

                if (!Utils.isLocationEnabled(getActivity())) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle("Radius Settings")
                            .setMessage("If you want set the radius from you enable location")
                            .setPositiveButton("Enable", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface pDialogInterface, int pI) {
                                    startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                                }
                            }).setNegativeButton("Cancel", null).create().show();
                }
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private class EatHolder extends RecyclerView.ViewHolder {

        private Eat mEat;

        public ImageView mPhotoImageView;
        public TextView mNameTextView;
        public TextView mLocationTextView;

        public EatHolder(View itemView) {
            super(itemView);

            mPhotoImageView = (ImageView) itemView.findViewById(R.id.image_view_eat);


            mNameTextView = (TextView) itemView.findViewById(R.id.text_view_eat);
            mLocationTextView = (TextView) itemView.findViewById(R.id.text_view_location_eat);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View pView) {
                    if (mEat != null) {

                        FragmentManager manager = getActivity().getSupportFragmentManager();

                        Fragment fragment = manager.findFragmentByTag(EatFragment.FRAGMENT_TAG);

                        if (fragment == null) {
                            fragment = EatFragment.newInstance(mEat.getId());
                            manager.beginTransaction()
                                    .replace(R.id.fragment_container, fragment, EatFragment.FRAGMENT_TAG)
                                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                                    .addToBackStack(SightFragment.FRAGMENT_TAG)
                                    .commit();
                        }
                    }
                }
            });
        }

        public void bindEat(Eat pEat) {
            mEat = pEat;

            if (mEat.getName() != null)
                mNameTextView.setText(mEat.getName());
            if (mEat.getLocation() != null)
                mLocationTextView.setText("Location: " + mEat.getLocation());

            Picasso.with(getActivity().getApplicationContext())
                    .load(mEat.getPhotoUrl())
                    .resize(((NavigationDrawerActivity)getActivity()).getWidth(), 0)
                    .onlyScaleDown()
                    .placeholder(R.drawable.placeholder)
                    .into(mPhotoImageView);
        }
    }

    private class EatAdapter extends RecyclerView.Adapter<EatHolder> {

        private List<Eat> mEats;

        public EatAdapter(List<Eat> pEats) {
            mEats = pEats;
        }

        @Override
        public EatHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View v = inflater.inflate(R.layout.items_eat, parent, false);

            YoYo.with(Techniques.FadeInUp)
                    .duration(700)
                    .playOn(v);

            return new EatHolder(v);
        }

        @Override
        public void onBindViewHolder(EatHolder holder, int position) {
            Eat eat = mEats.get(position);
            holder.bindEat(eat);
        }

        @Override
        public int getItemCount() {
            return mEats.size();
        }

    }
}
