package com.uniquemiban.travelmanager.eat;


import android.support.v4.app.Fragment;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import com.uniquemiban.travelmanager.models.Eat;
import com.uniquemiban.travelmanager.models.Sight;
import com.uniquemiban.travelmanager.sight.SightFragment;
import com.uniquemiban.travelmanager.start.NavigationDrawerActivity;
import com.uniquemiban.travelmanager.utils.Constants;

import java.util.ArrayList;
import java.util.List;

import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmResults;

public class EatListFragment extends Fragment {

    public static final String ARG_LONGITUDE = "eat_list_fragment_arg_longitude";
    public static final String ARG_LATITUDE = "eat_list_fragment_arg_latitude";
    public static final String ARG_RADIUS = "eat_list_fragment_arg_radius";

    public static final String FRAGMENT_TAG = "eat_list_fragment";
    public static final String FRAGMENT_TAG_RADIUS = "eat_list_fragment_radius";
    private static final int LOADING_ITEMS_NUMBER = 5;

    private static final int HIDE_THRESHOLD = 20;
    private int scrolledDistance = 0;
    private boolean controlsVisible = true;

    private boolean mLoading = true;
    int mFirstVisibleItemPosition, mVisibleItemCount, mTotalItemCount;

    private int mCount = 0;

    private List<Eat> mEatList;
    private RecyclerView mEatRecyclerView;
    private StaggeredGridLayoutManager mStaggeredGridLayoutManager;
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
    private String mLastItemId = null;

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

        RealmResults<Eat> results = mRealm.where(Eat.class).findAll();
        mEatList = new ArrayList<>();
        for (Eat eat : results) {
            if(mRadius != -1 && SphericalUtil.computeDistanceBetween(new LatLng(mLatitude, mLongitude), new LatLng(eat.getLatitude(), eat.getLongitude())) > mRadius)
                continue;
            mEatList.add(eat);
        }

        mChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot pDataSnapshot, String pS) {

                final Eat e = pDataSnapshot.getValue(Eat.class);

                if(mRadius != -1 && SphericalUtil.computeDistanceBetween(new LatLng(mLatitude, mLongitude), new LatLng(e.getLatitude(), e.getLongitude())) > mRadius){
                    return;
                }

                if(!TextUtils.isEmpty(mSearch) && !e.getName().contains(mSearch)
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

                        mRealm.executeTransactionAsync(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                realm.copyToRealmOrUpdate(e);
                            }
                        }, new Realm.Transaction.OnSuccess() {
                            @Override
                            public void onSuccess() {
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

                mRealm.executeTransactionAsync(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        realm.copyToRealmOrUpdate(e);
                    }
                }, new Realm.Transaction.OnSuccess() {
                    @Override
                    public void onSuccess() {
                        updateUI(e);
                    }
                });
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

        NavigationDrawerActivity activity = (NavigationDrawerActivity)getActivity();

        ActionBar bar = activity.getSupportActionBar();
        bar.setDisplayHomeAsUpEnabled(false);
        bar.setDisplayShowCustomEnabled(true);

        Toolbar toolbar = activity.getToolbar();

        toolbar.setTitle("Eat");
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                activity, activity.getDrawer(), toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        activity.getDrawer().setDrawerListener(toggle);
        toggle.syncState();

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

        mStaggeredGridLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        mLinearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            mEatRecyclerView.setLayoutManager(mLinearLayoutManager);
        }
        else {
            mEatRecyclerView.setLayoutManager(mStaggeredGridLayoutManager);
        }

        mAdapter = new EatAdapter(mEatList);
        mEatRecyclerView.setAdapter(mAdapter);

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
            mEatList.add(index, pEat);
            mEatList.remove(index + 1);
            mAdapter.notifyItemChanged(index);
        }
    }

    public void searchItemsByName(String pQuery){
        RealmResults<Eat> results = mRealm.where(Eat.class).contains("mName", pQuery, Case.INSENSITIVE).findAll();
        mEatList.clear();
        for (Eat e: results){
            mEatList.add(e);
        }
        mAdapter.notifyDataSetChanged();
    }

    public void searchItemsByRadius(){

    }

    @Override
    public void onStop() {
        super.onStop();
        mRealm.close();
        mQuery.removeEventListener(mChildEventListener);
    }

    private class EatHolder extends RecyclerView.ViewHolder {

        private Eat mEat;

        public ImageView mPhotoImageView;
        public TextView mNameTextView;
        public TextView mLocationTextView;
        public TextView mDistanceTextView;

        public EatHolder(View itemView) {
            super(itemView);

            mPhotoImageView = (ImageView) itemView.findViewById(R.id.image_view_eat);


            mNameTextView = (TextView) itemView.findViewById(R.id.text_view_eat);
            mLocationTextView = (TextView) itemView.findViewById(R.id.text_view_location_eat);
            mDistanceTextView = (TextView) itemView.findViewById(R.id.text_view_distance_eat);

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

            mDistanceTextView.setText("Distance: N/A");

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
