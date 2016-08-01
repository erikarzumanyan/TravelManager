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

import com.squareup.picasso.Picasso;
import com.uniquemiban.travelmanager.models.Sight;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;

public class SightsListFragment extends Fragment{

    private List<Sight> mSightsList;
    private RecyclerView mSightsRecyclerView;
    private SightAdapter mAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSightsList = new ArrayList<>();

        for(int i = 0; i < 10; ++i){
            Sight sight = new Sight();
            sight.setName("sight " + i);
            sight.setAbout("skfjhdskjhsdkj");
            sight.setPhotoUrl("http://www.panorama.am/news_images/513/1538336_3/f56d73308a93ad_56d73308a93ea.thumb.jpg");
            mSightsList.add(sight);
        }
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
