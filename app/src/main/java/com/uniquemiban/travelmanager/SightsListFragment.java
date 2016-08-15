package com.uniquemiban.travelmanager;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.Transformation;
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
import com.uniquemiban.travelmanager.models.Sight;

import java.util.ArrayList;
import java.util.List;

import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmResults;

public class SightsListFragment extends Fragment {

    public static final String FRAGMENT_TAG = "sights_list_fragment";
    private static final int LOADING_ITEMS_NUMBER = 5;

    private static final int HIDE_THRESHOLD = 20;
    private int scrolledDistance = 0;
    private boolean controlsVisible = true;

    private boolean mLoading = true;
    int mFirstVisibleItemPosition, mVisibleItemCount, mTotalItemCount;

    private int mCount = 0;

    private List<Sight> mSightsList;
    private RecyclerView mSightsRecyclerView;
    private StaggeredGridLayoutManager mStaggeredGridLayoutManager;
    private LinearLayoutManager mLinearLayoutManager;
    private SightAdapter mAdapter;

    private DatabaseReference mRef;
    private Query mQuery;
    private Realm mRealm;

    private ChildEventListener mChildEventListener;

    private int mWidth = 800;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mRef = FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_SIGHTS).getRef();
        mRealm = Realm.getDefaultInstance();

        RealmResults<Sight> results = mRealm.where(Sight.class).findAll();
        mSightsList = new ArrayList<>();
        for (Sight sight : results) {
            mSightsList.add(sight);
        }

        mChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot pDataSnapshot, String pS) {

                final Sight s = pDataSnapshot.getValue(Sight.class);

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

            @Override
            public void onChildChanged(DataSnapshot pDataSnapshot, String pS) {
                final Sight s = pDataSnapshot.getValue(Sight.class);

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
                final Sight s = pDataSnapshot.getValue(Sight.class);

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

        mSightsRecyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                final int width = mSightsRecyclerView.getWidth();
                final int height = mSightsRecyclerView.getHeight();
                int min = Math.min(width, height);
                if (width != 0) {
                    mWidth = min;
                    mSightsRecyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            }
        });

        mAdapter = new SightAdapter(mSightsList);
        mSightsRecyclerView.setAdapter(mAdapter);

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
            mSightsList.add(index, pSight);
            mSightsList.remove(index + 1);
            mAdapter.notifyItemChanged(index);
        }
    }

    public void searchItemsByName(String pQuery){
        RealmResults<Sight> results = mRealm.where(Sight.class).contains("mName", pQuery, Case.INSENSITIVE).findAll();
        mSightsList.clear();
        for (Sight s: results){
            mSightsList.add(s);
        }
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onStop() {
        super.onStop();
        mRealm.close();
        mQuery.removeEventListener(mChildEventListener);
    }

    private class SightHolder extends RecyclerView.ViewHolder {

        private Sight mSight;

        public ImageView mPhotoImageView;
        public TextView mNameTextView;
        public TextView mCategoryTextView;
        public TextView mLocationTextView;
        public TextView mDistanceTextView;

        public SightHolder(View itemView) {
            super(itemView);

            mPhotoImageView = (ImageView) itemView.findViewById(R.id.image_view_sight);

            mCategoryTextView = (TextView) itemView.findViewById(R.id.text_view_category);
            mNameTextView = (TextView) itemView.findViewById(R.id.text_view_name);
            mLocationTextView = (TextView) itemView.findViewById(R.id.text_view_location);
            mDistanceTextView = (TextView) itemView.findViewById(R.id.text_view_distance);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View pView) {
                    if (mSight != null) {

                        FragmentManager manager = getActivity().getSupportFragmentManager();

                        Fragment fragment = manager.findFragmentByTag(SightFragment.FRAGMENT_TAG);

                        if (fragment == null) {
                            fragment = SightFragment.newInstance(mSight.getId());
                            manager.beginTransaction()
                                    .add(R.id.fragment_container, fragment, SightFragment.FRAGMENT_TAG)
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

            mDistanceTextView.setText("Distance: N/A");

            Picasso.with(getActivity().getApplicationContext())
                    .load(mSight.getPhotoUrl())
                    .resize(mWidth, 0)
                    .onlyScaleDown()
                    .into(mPhotoImageView);
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
