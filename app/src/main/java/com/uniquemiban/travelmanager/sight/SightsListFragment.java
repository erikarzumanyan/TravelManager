package com.uniquemiban.travelmanager.sight;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.ProgressBar;
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
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.uniquemiban.travelmanager.filter.FilterFragment;
import com.uniquemiban.travelmanager.utils.Constants;
import com.uniquemiban.travelmanager.start.NavigationDrawerActivity;
import com.uniquemiban.travelmanager.R;
import com.uniquemiban.travelmanager.models.Sight;
import com.uniquemiban.travelmanager.utils.Utils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;

public class SightsListFragment extends Fragment {

    public static final String FRAGMENT_TAG = "sights_list_fragment";
    private static final int LOADING_ITEMS_NUMBER = 5;
    private String mLastItemId = null;

    private static final int HIDE_THRESHOLD = 20;
    private int scrolledDistance = 0;
    private boolean controlsVisible = true;

    private boolean mLoading = true;
    int mFirstVisibleItemPosition, mVisibleItemCount, mTotalItemCount;

    private List<Sight> mSightsList;
    private RecyclerView mSightsRecyclerView;
    private StaggeredGridLayoutManager mStaggeredGridLayoutManager;
    private LinearLayoutManager mLinearLayoutManager;
    private SightAdapter mAdapter;

    private DatabaseReference mRef;
    private Query mQuery;
    private Realm mRealm;

    private ChildEventListener mChildEventListener;

    private String mSearchByName = null;
    private String mSearch = null;

    private Location mLastLocation = null;
    private float mRadius = -1;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        NavigationDrawerActivity activity = ((NavigationDrawerActivity)getActivity());
        mLastLocation = activity.getLastLocation();
        mRadius = activity.getSharedPreferences(Constants.SHARED_PREFS_SIGHT, Context.MODE_PRIVATE).getFloat(Constants.SHARED_PREFS_KEY_RADIUS, -1);

        mRef = FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_SIGHTS).getRef();
        mRealm = Realm.getDefaultInstance();

        mSightsList = new ArrayList<>();

        mChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot pDataSnapshot, String pS) {

                final Sight s = pDataSnapshot.getValue(Sight.class);

                if(mLastLocation != null && mRadius > 0
                        && SphericalUtil.computeDistanceBetween(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()),
                        new LatLng(s.getLatitude(), s.getLongitude())) > mRadius) {

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

                } else{

                    if (TextUtils.isEmpty(s.getId())) {
                        String id = pDataSnapshot.getKey();
                        s.setId(id);
                        mRef.child(id).child("id").setValue(id);
                    } else {
                        mRealm = Realm.getDefaultInstance();

//                        mRealm.executeTransactionAsync(new Realm.Transaction() {
//                            @Override
//                            public void execute(Realm realm) {
//                                realm.copyToRealmOrUpdate(s);
//                            }
//                        }, new Realm.Transaction.OnSuccess() {
//                            @Override
//                            public void onSuccess() {
//                                updateUI(s);
//                            }
//                        });

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
                final Sight s = pDataSnapshot.getValue(Sight.class);

                mRealm = Realm.getDefaultInstance();

                if(mRealm.where(Sight.class).equalTo("mId", s.getId()).findFirst()!= null) {
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

            @Override
            public void onChildRemoved(DataSnapshot pDataSnapshot) {
                final Sight s = pDataSnapshot.getValue(Sight.class);

                if(!TextUtils.isEmpty(mSearchByName) && !s.getName().contains(mSearchByName))
                    return;

                mRealm = Realm.getDefaultInstance();

                mRealm.executeTransactionAsync(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        Sight sight = realm.where(Sight.class).equalTo("mId", s.getId()).findFirst();
                        sight.deleteFromRealm();
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
        View view = inflater.inflate(R.layout.fragment_sights_list, container, false);

        setHasOptionsMenu(true);

        NavigationDrawerActivity activity = (NavigationDrawerActivity)getActivity();

        ActionBar bar = activity.getSupportActionBar();
        bar.setDisplayHomeAsUpEnabled(false);
        bar.setDisplayShowCustomEnabled(true);

        Toolbar toolbar = activity.getToolbar();

        toolbar.setTitle("Sights");
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                activity, activity.getDrawer(), toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        activity.getDrawer().setDrawerListener(toggle);
        toggle.syncState();

        mSightsRecyclerView = (RecyclerView) view.findViewById(R.id.fragment_sights_list_recycler_view);
        mSightsRecyclerView.setItemAnimator(new RecyclerView.ItemAnimator() {
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
            mSightsRecyclerView.setLayoutManager(mLinearLayoutManager);
        }
        else {
            mSightsRecyclerView.setLayoutManager(mStaggeredGridLayoutManager);
        }

        mAdapter = new SightAdapter(mSightsList);
        mSightsRecyclerView.setAdapter(mAdapter);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        if (mRealm.isClosed())
            mRealm = Realm.getDefaultInstance();

        mQuery = mRef.orderByKey().limitToFirst(LOADING_ITEMS_NUMBER);
        mQuery.addChildEventListener(mChildEventListener);

        final ActionBar bar = ((NavigationDrawerActivity)getActivity()).getSupportActionBar();

        mSightsRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
                                mQuery = mRef.orderByKey().startAt(mSightsList.get(mSightsList.size() - 1).getId()).limitToFirst(LOADING_ITEMS_NUMBER);
                                mQuery.addChildEventListener(mChildEventListener);
                            }
                        }
                    }
                }
            }
        });

        searchItemsByRadius();
    }

    private void deleteFromList(Sight pSight) {
        int index = -1;
        String id = pSight.getId();
        for (int i = 0; i < mSightsList.size(); ++i) {
            if (mSightsList.get(i).getId().equals(id))
                index = i;
        }
        if (index != -1) {
            mSightsList.remove(index);
            mAdapter.notifyItemRangeChanged(index, mSightsList.size() - 1);
        }
    }

    private void updateUI(Sight pSight) {
        int index = -1;
        String id = pSight.getId();
        for (int i = 0; i < mSightsList.size(); ++i) {
            if (mSightsList.get(i).getId().equals(id))
                index = i;
        }
        if (index == -1) {
            mSightsList.add(pSight);
            mAdapter.notifyItemChanged(mSightsList.size() - 1);
        } else {
            mSightsList.remove(index);
            mSightsList.add(index, pSight);
            mAdapter.notifyItemChanged(index);
        }
    }

    public void searchItems(String pQuery){
        mSearch = pQuery;
        searchItemsByRadius();
    }

    public void searchItemsByRadius(){
        RealmQuery<Sight> realmQuery = null;

        if(mSearch != null){
            realmQuery = mRealm.where(Sight.class).contains("mName", mSearch, Case.INSENSITIVE);
            realmQuery = realmQuery.or().contains("mCategory", mSearch, Case.INSENSITIVE);
            realmQuery = realmQuery.or().contains("mLocation", mSearch, Case.INSENSITIVE);
        } else {
            realmQuery = mRealm.where(Sight.class);
        }

        NavigationDrawerActivity activity = ((NavigationDrawerActivity)getActivity());
        mLastLocation = activity.getLastLocation();

        SharedPreferences prefs = activity.getSharedPreferences(Constants.SHARED_PREFS_SIGHT, Context.MODE_PRIVATE);
        mRadius = prefs.getFloat(Constants.SHARED_PREFS_KEY_RADIUS, -1);

        if (mLastLocation == null && !TextUtils.isEmpty(prefs.getString(Constants.SHARED_PREFS_KEY_LAST_LAT, ""))) {
            mLastLocation = new Location("");
            mLastLocation.setLatitude(Double.valueOf(prefs.getString(Constants.SHARED_PREFS_KEY_LAST_LAT, "")));
            mLastLocation.setLongitude(Double.valueOf(prefs.getString(Constants.SHARED_PREFS_KEY_LAST_LONG, "")));
        }

        RealmResults<Sight> results = realmQuery.findAll();
        mSightsList.clear();
        for (Sight s: results){
            if(mLastLocation != null && mRadius > 0
                    && SphericalUtil.computeDistanceBetween(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()),
                    new LatLng(s.getLatitude(), s.getLongitude())) > mRadius)
                continue;
            mSightsList.add(s);
        }

        mAdapter.notifyDataSetChanged();

        mRef.removeEventListener(mChildEventListener);
        mRef.addChildEventListener(mChildEventListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        mRealm.close();
        mRef.removeEventListener(mChildEventListener);
        mQuery.removeEventListener(mChildEventListener);
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
            FilterFragment fragment = FilterFragment.newInstance(Constants.SHARED_PREFS_SIGHT);
            fragment.show(((NavigationDrawerActivity)getActivity()).getSupportFragmentManager(), FilterFragment.FRAGMENT_TAG);
        }
        return super.onOptionsItemSelected(item);
    }

    private class SightHolder extends RecyclerView.ViewHolder {

        private Sight mSight;

        public ImageView mPhotoImageView;
        public TextView mNameTextView;
        public TextView mCategoryTextView;
        public TextView mLocationTextView;
        public TextView mDistanceTextView;

        public ProgressBar mProgressBar;

        public SightHolder(View itemView) {
            super(itemView);

            mPhotoImageView = (ImageView) itemView.findViewById(R.id.image_view_sight);

            mCategoryTextView = (TextView) itemView.findViewById(R.id.text_view_category);
            mNameTextView = (TextView) itemView.findViewById(R.id.text_view_name);
            mLocationTextView = (TextView) itemView.findViewById(R.id.text_view_location);
            mDistanceTextView = (TextView) itemView.findViewById(R.id.text_view_distance);

            mProgressBar = (ProgressBar)itemView.findViewById(R.id.progress_bar_item_sight);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View pView) {
                    if (mSight != null) {

                        FragmentManager manager = getActivity().getSupportFragmentManager();

                        Fragment fragment = manager.findFragmentByTag(SightFragment.FRAGMENT_TAG);

                        if (fragment == null) {
                            fragment = SightFragment.newInstance(mSight.getId());
                            manager.beginTransaction()
                                    .replace(R.id.fragment_container, fragment, SightFragment.FRAGMENT_TAG)
                                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                                    .addToBackStack(SightFragment.FRAGMENT_TAG)
                                    .commit();
                        }
                    }
                }
            });
        }

        public void bindSight(Sight pSight) {
            mSight = pSight;

            if (mSight.getCategory() != null)
                mCategoryTextView.setText(mSight.getCategory());
            if (mSight.getName() != null)
                mNameTextView.setText(mSight.getName());
            if (mSight.getLocation() != null)
                mLocationTextView.setText("Location: " + mSight.getLocation());

            mDistanceTextView.setText("");

            if(mLastLocation != null) {
                String url = "https://maps.googleapis.com/maps/api/distancematrix/json";
                AsyncHttpClient client = new AsyncHttpClient();
                RequestParams params = new RequestParams();
                params.put("origins", mLastLocation.getLatitude() + "," + mLastLocation.getLongitude());
                params.put("destinations", mSight.getLatitude() + "," + mSight.getLongitude());
                params.put("key", Constants.GOOGLE_MATRIX_API_KEY);

                mDistanceTextView.setText("Distance:      ");

                client.get(url, params, new JsonHttpResponseHandler(){
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        String distance = Utils.getDistance(response);
                        if(!TextUtils.isEmpty(distance))
                            mDistanceTextView.setText("Distance: " + Utils.getDistance(response));
                        else
                            mDistanceTextView.setText("");
                    }
                });
            }

            mProgressBar.setVisibility(View.VISIBLE);

            Picasso.with(getActivity().getApplicationContext())
                    .load(mSight.getPhotoUrl())
                    .resize(((NavigationDrawerActivity)getActivity()).getWidth(), 0)
                    .onlyScaleDown()
                    .placeholder(R.drawable.placeholder)
                    .into(mPhotoImageView, new Callback() {
                        @Override
                        public void onSuccess() {
                            mProgressBar.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError() {
                            Toast.makeText(getActivity(), "Download error", Toast.LENGTH_SHORT).show();
                            mProgressBar.setVisibility(View.GONE);
                        }
                    });
        }
    }

    private class SightAdapter extends RecyclerView.Adapter<SightHolder> {

        private List<Sight> mSights;

        public SightAdapter(List<Sight> pSights) {
            mSights = pSights;
        }

        @Override
        public SightHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View v = inflater.inflate(R.layout.item_sight, parent, false);

            YoYo.with(Techniques.FadeInUp)
                    .duration(700)
                    .playOn(v);

            return new SightHolder(v);
        }

        @Override
        public void onBindViewHolder(SightHolder holder, int position) {
            Sight sight = mSights.get(position);
            holder.bindSight(sight);
        }

        @Override
        public int getItemCount() {
            return mSights.size();
        }

    }
}
