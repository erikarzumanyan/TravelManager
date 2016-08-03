package com.uniquemiban.travelmanager;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;
import com.uniquemiban.travelmanager.models.Sight;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

public class SightsListFragment extends Fragment{

    private List<Sight> mSightsList;
    private RecyclerView mSightsRecyclerView;
    private SightAdapter mAdapter;

    private DatabaseReference mRef;
    private Realm mRealm;

    private ChildEventListener mChildEventListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mRef = FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_SIGHTS).getRef();
        mRealm = Realm.getDefaultInstance();

        RealmResults<Sight> results = mRealm.where(Sight.class).findAll();
        mSightsList = new ArrayList<>();
        for(Sight sight: results){
            mSightsList.add(sight);
        }

        mChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot pDataSnapshot, String pS) {
                final Sight s = pDataSnapshot.getValue(Sight.class);

                mRealm = Realm.getDefaultInstance();

                mRealm.executeTransactionAsync(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        Realm r = Realm.getDefaultInstance();
                        realm.copyToRealmOrUpdate(s);
                        realm.close();
                    }
                });
            }

            @Override
            public void onChildChanged(DataSnapshot pDataSnapshot, String pS) {
                final Sight s = pDataSnapshot.getValue(Sight.class);

                mRealm = Realm.getDefaultInstance();

                mRealm.executeTransactionAsync(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        Realm r = Realm.getDefaultInstance();
                        realm.copyToRealmOrUpdate(s);
                        realm.close();
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
                        Realm r = Realm.getDefaultInstance();
                        Sight sight = r.where(Sight.class).equalTo("mId", s.getId()).findFirst();
                        sight.deleteFromRealm();
                        realm.close();
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

        mSightsRecyclerView = (RecyclerView)view.findViewById(R.id.fragment_sights_list_recycler_view);
        mSightsRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));

        mAdapter = new SightAdapter(mSightsList);
        mSightsRecyclerView.setAdapter(mAdapter);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        mRealm.addChangeListener(new RealmChangeListener<Realm>() {
            @Override
            public void onChange(Realm element) {
                updateUI();
            }
        });

        mRef.addChildEventListener(mChildEventListener);
    }

    private void updateUI() {
        RealmResults<Sight> results = mRealm.where(Sight.class).findAll();
        mSightsList.clear();
        for(Sight sight: results){
            mSightsList.add(sight);
        }
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onStop() {
        super.onStop();
        mRealm.removeAllChangeListeners();
        mRealm.close();
        mRef.removeEventListener(mChildEventListener);
    }

    private class SightHolder extends RecyclerView.ViewHolder{

        private Sight mSight;

        public ImageView mPhotoImageView;
        public TextView mNameTextView;
        public TextView mAboutTextView;

        public SightHolder(View itemView) {
            super(itemView);

            mPhotoImageView = (ImageView)itemView.findViewById(R.id.image_view_sight);
            mNameTextView = (TextView)itemView.findViewById(R.id.text_view_name);
            mAboutTextView = (TextView)itemView.findViewById(R.id.text_view_about);
        }

        public void bindSight(Sight pSight){
            mSight = pSight;

            mNameTextView.setText(mSight.getName());
            mAboutTextView.setText(mSight.getAbout());

            Picasso.with(getActivity())
                    .load(mSight.getPhotoUrl())
                    .into(mPhotoImageView);
        }
    }

    private class SightAdapter extends RecyclerView.Adapter<SightHolder>{

        private List<Sight> mSights;

        public SightAdapter(List<Sight> pSights){
            mSights = pSights;
        }

        @Override
        public SightHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View v = inflater.inflate(R.layout.item_sight, parent, false);
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
