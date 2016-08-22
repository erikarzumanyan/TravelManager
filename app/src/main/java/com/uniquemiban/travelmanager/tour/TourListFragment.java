package com.uniquemiban.travelmanager.tour;


import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
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
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;
import com.uniquemiban.travelmanager.R;
import com.uniquemiban.travelmanager.models.Sight;
import com.uniquemiban.travelmanager.models.Tour;
import com.uniquemiban.travelmanager.start.NavigationDrawerActivity;
import com.uniquemiban.travelmanager.utils.Constants;

import java.util.ArrayList;
import java.util.List;

import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmResults;

public class TourListFragment extends Fragment {

        public static final String FRAGMENT_TAG = "tour_list_fragment";
        private static final int LOADING_ITEMS_NUMBER = 5;

        private static final int HIDE_THRESHOLD = 20;
        private int scrolledDistance = 0;
        private boolean controlsVisible = true;

    private String mSearch = null;
    private String mLastItemId = null;
    private String mSearchByName = null;


        private boolean mLoading = true;
        int mFirstVisibleItemPosition, mVisibleItemCount, mTotalItemCount;

        private int mCount = 0;

        private List<Tour> mTourList;
        private RecyclerView mTourRecyclerView;
        private StaggeredGridLayoutManager mStaggeredGridLayoutManager;
        private LinearLayoutManager mLinearLayoutManager;
        private TourAdapter mAdapter;

        private DatabaseReference mRef;
        private Query mQuery;
        private Realm mRealm;

        private ChildEventListener mChildEventListener;

        private int mWidth = 800;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            mRef = FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_TOUR).getRef();
            mRealm = Realm.getDefaultInstance();

            RealmResults<Tour> results = mRealm.where(Tour.class).findAll();
            mTourList = new ArrayList<>();
            for (Tour tour : results) {
                mTourList.add(tour);
            }



            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot pDataSnapshot, String pS) {

                    final Tour t = pDataSnapshot.getValue(Tour.class);


                    if (!TextUtils.isEmpty(mSearch) && !t.getName().contains(mSearch)
                            ) {
                        mQuery.removeEventListener(mChildEventListener);
                        mLastItemId = t.getId();
                        mQuery = mRef.orderByKey().startAt(mLastItemId).limitToFirst(LOADING_ITEMS_NUMBER);
                        mQuery.addChildEventListener(mChildEventListener);
                    } else {
                        if (TextUtils.isEmpty(t.getId())) {
                            String id = pDataSnapshot.getKey();
                            t.setId(id);
                            mRef.child(id).child("id").setValue(id);
                        } else {
                            mRealm = Realm.getDefaultInstance();

                            mRealm.executeTransactionAsync(new Realm.Transaction() {
                                @Override
                                public void execute(Realm realm) {
                                    realm.copyToRealmOrUpdate(t);
                                }
                            }, new Realm.Transaction.OnSuccess() {
                                @Override
                                public void onSuccess() {
                                    updateUI(t);
                                }
                            });
                        }
                    }
                }

                @Override
                public void onChildChanged(DataSnapshot pDataSnapshot, String pS) {
                    final Tour t = pDataSnapshot.getValue(Tour.class);

                    mRealm = Realm.getDefaultInstance();

                    mRealm.executeTransactionAsync(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            realm.copyToRealmOrUpdate(t);
                        }
                    }, new Realm.Transaction.OnSuccess() {
                        @Override
                        public void onSuccess() {
                            updateUI(t);
                        }
                    });
                }

                @Override
                public void onChildRemoved(DataSnapshot pDataSnapshot) {
                    final Tour t = pDataSnapshot.getValue(Tour.class);

                    if(!TextUtils.isEmpty(mSearchByName) && !t.getName().contains(mSearchByName))
                        return;

                    mRealm = Realm.getDefaultInstance();

                    mRealm.executeTransactionAsync(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            Sight sight = realm.where(Sight.class).equalTo("mId", t.getId()).findFirst();
                            sight.deleteFromRealm();
                        }
                    }, new Realm.Transaction.OnSuccess() {
                        @Override
                        public void onSuccess() {
                            deleteFromList(t);
                            Picasso.with(getActivity().getApplicationContext()).invalidate(t.getPhotoUrl());
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
            View view = inflater.inflate(R.layout.fragment_tour_list, container, false);

            setHasOptionsMenu(true);
            NavigationDrawerActivity activity = (NavigationDrawerActivity)getActivity();

            ActionBar bar = activity.getSupportActionBar();
            bar.setDisplayHomeAsUpEnabled(false);
            bar.setDisplayShowCustomEnabled(true);

            Toolbar toolbar = activity.getToolbar();

            toolbar.setTitle("Tours");
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    activity, activity.getDrawer(), toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            activity.getDrawer().setDrawerListener(toggle);
            toggle.syncState();

            mTourRecyclerView = (RecyclerView) view.findViewById(R.id.fragment_tours_list_recycler_view);
            mTourRecyclerView.setItemAnimator(new RecyclerView.ItemAnimator() {
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
                mTourRecyclerView.setLayoutManager(mLinearLayoutManager);
            }
            else {
                mTourRecyclerView.setLayoutManager(mStaggeredGridLayoutManager);
            }

            mTourRecyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    final int width = mTourRecyclerView.getWidth();
                    final int height = mTourRecyclerView.getHeight();
                    int min = Math.min(width, height);
                    if (width != 0) {
                        mWidth = min;
                        mTourRecyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                }
            });

            mAdapter = new TourAdapter(mTourList);
            mTourRecyclerView.setAdapter(mAdapter);

            return view;
        }

        @Override
        public void onStart() {
            super.onStart();

            if (mRealm.isClosed())
                mRealm = Realm.getDefaultInstance();

            mQuery = mRef.limitToFirst(LOADING_ITEMS_NUMBER);
            mQuery.addChildEventListener(mChildEventListener);

            final ActionBar bar = ((NavigationDrawerActivity)getActivity()).getSupportActionBar();

            mTourRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
                                if ((mVisibleItemCount + mFirstVisibleItemPosition + 1) >= mRealm.where(Tour.class).findAll().size()
                                        && (mVisibleItemCount + mFirstVisibleItemPosition + 1) >= mTotalItemCount) {
                                    mQuery.removeEventListener(mChildEventListener);
                                    mQuery = mRef.orderByKey().startAt(mTourList.get(mTourList.size() - 1).getId()).limitToFirst(LOADING_ITEMS_NUMBER);
                                    mQuery.addChildEventListener(mChildEventListener);
                                }
                            }
                        }
                    }
                }
            });
        }

        private void deleteFromList(Tour pTour) {
            int index = -1;
            String id = pTour.getId();
            for (int i = 0; i < mTourList.size(); ++i) {
                if (mTourList.get(i).getId().equals(id))
                    index = i;
            }
            if (index != -1) {
                mTourList.remove(index);
                mAdapter.notifyItemRangeChanged(index, mTourList.size() - 1);
            }
        }

        private void updateUI(Tour pTour) {
            int index = -1;
            String id = pTour.getId();
            for (int i = 0; i < mTourList.size(); ++i) {
                if (mTourList.get(i).getId().equals(id))
                    index = i;
            }
            if (index == -1) {
                mTourList.add(pTour);
                mAdapter.notifyItemChanged(mTourList.size() - 1);
            } else {
                mTourList.add(index, pTour);
                mTourList.remove(index + 1);
                mAdapter.notifyItemChanged(index);
            }
        }

        public void searchItemsByName(String pQuery){
            RealmResults<Tour> results = mRealm.where(Tour.class).contains("mName", pQuery, Case.INSENSITIVE).findAll();
            mTourList.clear();
            for (Tour t: results){
                mTourList.add(t);
            }
            mAdapter.notifyDataSetChanged();
        }

        @Override
        public void onStop() {
            super.onStop();
            mRealm.close();
            mQuery.removeEventListener(mChildEventListener);
        }

        private class TourHolder extends RecyclerView.ViewHolder {

            private Tour mTour;

            public ImageView mPhotoImageView;
            public TextView mNameTextView;
            public TextView mTourOperatorNametextView;

            public TourHolder(View itemView) {
                super(itemView);

                mPhotoImageView = (ImageView) itemView.findViewById(R.id.image_view_tour);
                mNameTextView = (TextView) itemView.findViewById(R.id.text_view_tour);
                mTourOperatorNametextView = (TextView) itemView.findViewById(R.id.text_view_distance_tour_operator);

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View pView) {
                        if (mTour != null) {
                            FragmentManager manager = getActivity().getSupportFragmentManager();

                            Fragment fragment = manager.findFragmentByTag(TourFragment.FRAGMENT_TAG);

                            if (fragment == null) {
                                fragment = TourFragment.newInstance(mTour.getId());
                                manager.beginTransaction()
                                        .add(R.id.fragment_container, fragment, TourFragment.FRAGMENT_TAG)
                                        .addToBackStack(TourFragment.FRAGMENT_TAG)
                                        .commit();
                            }
                        }
                    }
                });
            }

            public void bindTour(Tour pTour) {
                mTour = pTour;

                if (mTour.getName() != null)
                    mNameTextView.setText(mTour.getName());

                mTourOperatorNametextView.setText("Tour Operator name: N/A");

                Picasso.with(getActivity().getApplicationContext())
                        .load(mTour.getPhotoUrl())
                        .resize(mWidth, 0)
                        .onlyScaleDown()
                        .into(mPhotoImageView);
            }
        }

        private class TourAdapter extends RecyclerView.Adapter<TourHolder> {

            private List<Tour> mTours;

            public TourAdapter(List<Tour> pTours) {
                mTours = pTours;
            }

            @Override
            public TourHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                LayoutInflater inflater = LayoutInflater.from(getActivity());
                View v = inflater.inflate(R.layout.items_tours, parent, false);

                YoYo.with(Techniques.FadeInUp)
                        .duration(700)
                        .playOn(v);

                return new TourHolder(v);
            }

            @Override
            public void onBindViewHolder(TourHolder holder, int position) {
                Tour tour = mTours.get(position);
                holder.bindTour(tour);
            }

            @Override
            public int getItemCount() {
                return mTours.size();
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
