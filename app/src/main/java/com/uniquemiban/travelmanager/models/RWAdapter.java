package com.uniquemiban.travelmanager.models;

/**
 * Created by narek on 7/28/16.
 */

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import com.uniquemiban.travelmanager.R;

import java.util.List;

public class RWAdapter extends RecyclerView.Adapter<RWAdapter.MyViewHolder> {

    Context mContext;
    private List<Sight> mSightsList;

    public RWAdapter(List<Sight> moviesList) {
        this.mSightsList = moviesList;
    }


    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_view_row, parent, false);
        mContext = parent.getContext();
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Sight sights = mSightsList.get(position);
        holder.name.setText(sights.getName());

        holder.dest.setText(String.valueOf(Double.valueOf(sights.getDestination())));
      //  holder.image.setImageResource(sights.getImage());
        //Picasso.with(mContext).load(R.drawable.armenia).into(holder.image);
    }

    @Override
    public int getItemCount() {
        return mSightsList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView name, about, dest;
        ImageView image;

        public MyViewHolder(View view) {
            super(view);
            name = (TextView) view.findViewById(R.id.row_name_id);

            dest = (TextView) view.findViewById(R.id.row_destination_id);
            image = (ImageView) view.findViewById(R.id.row_image_id);


        }
    }
}
