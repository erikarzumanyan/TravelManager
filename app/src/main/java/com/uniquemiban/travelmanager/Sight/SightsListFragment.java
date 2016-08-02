package com.uniquemiban.travelmanager.Sight;

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
import com.uniquemiban.travelmanager.R;
import com.uniquemiban.travelmanager.models.Sight;
import com.uniquemiban.travelmanager.Sight.SightClickFragment;

import java.util.ArrayList;
import java.util.List;

public class SightsListFragment extends Fragment  {

    private List<Sight> mSightsList;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSightsList = new ArrayList<>();

        for (int i = 0; i < 10; ++i) {
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

        RecyclerView sightsRecyclerView = (RecyclerView) view.findViewById(R.id.fragment_sights_list_recycler_view);
        sightsRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));

        SightAdapter adapter = new SightAdapter(mSightsList);
        sightsRecyclerView.setAdapter(adapter);

        return view;
    }


    private class SightHolder extends RecyclerView.ViewHolder {

        public ImageView mPhotoImageView;
        public TextView mNameTextView;
        public TextView mAboutTextView;
        public Sight mSight;

        public SightHolder(View itemView) {
            super(itemView);

            mPhotoImageView = (ImageView) itemView.findViewById(R.id.image_view_sight);
            mNameTextView = (TextView) itemView.findViewById(R.id.text_view_name);
            mAboutTextView = (TextView) itemView.findViewById(R.id.text_view_about);
        }

        public void bindSight(Sight pSight) {
            mSight = pSight;

            mNameTextView.setText(mSight.getName());
            mAboutTextView.setText(mSight.getAbout());

            Picasso.with(getActivity())
                    .load(mSight.getPhotoUrl())
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
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View pView) {
                    SightClickFragment sightClickFragment=new SightClickFragment();
                    sightClickFragment.show(getFragmentManager(),"aa");
                }
            });
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
