package com.uniquemiban.travelmanager.sleep;


import android.content.res.Configuration;
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


import com.uniquemiban.travelmanager.models.Sight;
import com.uniquemiban.travelmanager.models.Sleep;

import com.uniquemiban.travelmanager.sight.SightFragment;
import com.uniquemiban.travelmanager.start.NavigationDrawerActivity;
import com.uniquemiban.travelmanager.utils.Constants;

import java.util.ArrayList;
import java.util.List;

import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmResults;

public class SleepListFragment extends Fragment{
    public static final String ARG_LONGITUDE = "sleep_list_fragment_arg_longitude";
    public static final String ARG_LATITUDE = "sleep_list_fragment_arg_latitude";
    public static final String ARG_RADIUS = "sleep_list_fragment_arg_radius";

    public static final String FRAGMENT_TAG = "sleep_list_fragment";
    public static final String FRAGMENT_TAG_RADIUS = "sleep_list_fragment_radius";
    private static final int LOADING_ITEMS_NUMBER = 5;

    private static final int HIDE_THRESHOLD = 20;
    private int scrolledDistance = 0;
    private boolean controlsVisible = true;

    private boolean mLoading = true;
    int mFirstVisibleItemPosition, mVisibleItemCount, mTotalItemCount;

    private int mCount = 0;

    private List<Sleep> mSleepList;
    private RecyclerView mSleepRecyclerView;
    private StaggeredGridLayoutManager mStaggeredGridLayoutManager;
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
    private String mLastItemId = null;

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

        RealmResults<Sleep> results = mRealm.where(Sleep.class).findAll();
        mSleepList = new ArrayList<>();
        for (Sleep sleep : results) {
            if(mRadius != -1 && SphericalUtil.computeDistanceBetween(new LatLng(mLatitude, mLongitude), new LatLng(sleep.
                    getLatitude(), sleep.getLongitude())) > mRadius)
                continue;
            mSleepList.add(sleep);
        }

        mChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot pDataSnapshot, String pS) {

                final Sleep s = pDataSnapshot.getValue(Sleep.class);

                if(mRadius != -1 && SphericalUtil.computeDistanceBetween(new LatLng(mLatitude, mLongitude),
                        new LatLng(s.getLatitude(), s.getLongitude())) > mRadius){
                    return;
                }

                if(!TextUtils.isEmpty(mSearch) && !s.getName().contains(mSearch)
                         && !s.getLocation().contains(mSearch)) {

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

                        mRealm.executeTransactionAsync(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                realm.copyToRealmOrUpdate(s);
                            }
                        }, new Realm.Transaction.OnSuccess() {
                            @Override
                            public void onSuccess() {
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

                mRealm.executeTransactionAsync(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        realm.copyToRealmOrUpdate(s);
                    }
                }, new Realm.Transaction.OnSuccess() {
                    @Override
                    public void onSuccess() {
                        updateUI(s);
                    }
                });
            }

            @Override
            public void onChildRemoved(DataSnapshot pDataSnapshot) {
                final Sleep s = pDataSnapshot.getValue(Sleep.class);

                mRealm = Realm.getDefaultInstance();

                mRealm.executeTransactionAsync(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        Sleep sleep= realm.where(Sleep.class).equalTo("mId", s.getId()).findFirst();
                        sleep.deleteFromRealm();
                    }
                }, new Realm.Transaction.OnSuccess() {
                    @Override
                    public void onSuccess() {
                        deleteFromList(s);
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

        NavigationDrawerActivity activity = (NavigationDrawerActivity)getActivity();

        ActionBar bar = activity.getSupportActionBar();
        bar.setDisplayHomeAsUpEnabled(false);
        bar.setDisplayShowCustomEnabled(true);

        Toolbar toolbar = activity.getToolbar();

        toolbar.setTitle("Sleep");
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                activity, activity.getDrawer(), toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        activity.getDrawer().setDrawerListener(toggle);
        toggle.syncState();

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

        mStaggeredGridLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        mLinearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            mSleepRecyclerView.setLayoutManager(mLinearLayoutManager);
        }
        else {
            mSleepRecyclerView.setLayoutManager(mStaggeredGridLayoutManager);
        }

        mAdapter = new SleepAdapter(mSleepList);
        mSleepRecyclerView.setAdapter(mAdapter);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        if (mRealm.isClosed())
            mRealm = Realm.getDefaultInstance();

        mQuery = mRef.limitToFirst(mCount = mCount + LOADING_ITEMS_NUMBER);
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
                                mQuery = mRef.orderByKey().startAt(mSleepList.get(mSleepList.size() - 1)
                                        .getId()).limitToFirst(LOADING_ITEMS_NUMBER);
                                mQuery.addChildEventListener(mChildEventListener);
                            }
                        }
                    }
                }
            }
        });
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
            mSleepList.add(index, pSleep);
            mSleepList.remove(index + 1);
            mAdapter.notifyItemChanged(index);
        }
    }

    public void searchItemsByName(String pQuery){
        RealmResults<Sleep> results = mRealm.where(Sleep.class).contains("mName", pQuery, Case.INSENSITIVE).findAll();
        mSleepList.clear();
        for (Sleep s: results){
            mSleepList.add(s);
        }
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onStop() {
        super.onStop();
        mRealm.close();
        mQuery.removeEventListener(mChildEventListener);
    }

    private class SleepHolder extends RecyclerView.ViewHolder {

        private Sleep mSleep;

        public ImageView mPhotoImageView;
        public TextView mNameTextView;


        public SleepHolder(View itemView) {
            super(itemView);

            mPhotoImageView = (ImageView) itemView.findViewById(R.id.image_view_sleep);


            mNameTextView = (TextView) itemView.findViewById(R.id.text_view_name_sleep);


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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.items_list, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_search){
            ((SearchView) item.getActionView()).setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    searchItemsByName(query);
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    searchItemsByName(newText);
                    return false;
                }
            });
        }
        return super.onOptionsItemSelected(item);
    }
}
