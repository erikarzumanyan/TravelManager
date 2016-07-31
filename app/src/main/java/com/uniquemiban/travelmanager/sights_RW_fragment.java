package com.uniquemiban.travelmanager;


import android.app.Fragment;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;

import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.uniquemiban.travelmanager.models.RWAdapter;
import com.uniquemiban.travelmanager.models.Sight;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class sights_RW_fragment extends Fragment {
    private List<Sight> movieList = new ArrayList<>();

    public sights_RW_fragment() {
        // Required empty public constructor
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view=inflater.inflate(R.layout.fragment_sights__rw_fragment, container, false);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.fragment_items_list_recycler_view);

        RWAdapter adapter = new RWAdapter(movieList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);

//        if (getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
//            recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2, LinearLayoutManager.VERTICAL, true));
//        } else {
//            recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 4));
//        }


        //Added gradle Dependency
        recyclerView.addItemDecoration(new HorizontalDividerItemDecoration.Builder(getActivity()).sizeResId(R.dimen.fab_margin).color(Color.TRANSPARENT).sizeResId(R.dimen.fab_margin).build());

        Sight sights = new Sight(R.drawable.a, "Ararat", 30.0);
        movieList.add(sights);
        movieList.add(sights);
        movieList.add(sights);
        movieList.add(sights);
        movieList.add(sights);
        return view;
    }

}
