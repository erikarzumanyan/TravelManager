package com.uniquemiban.travelmanager.filter;

import android.app.AlertDialog;
import android.app.Dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.uniquemiban.travelmanager.R;
import com.uniquemiban.travelmanager.eat.EatListFragment;
import com.uniquemiban.travelmanager.sight.SightsListFragment;
import com.uniquemiban.travelmanager.sleep.SleepListFragment;
import com.uniquemiban.travelmanager.start.NavigationDrawerActivity;
import com.uniquemiban.travelmanager.tour.TourListFragment;
import com.uniquemiban.travelmanager.utils.Constants;

public class FilterFragment extends DialogFragment {

    public static final String FRAGMENT_TAG = "filter_fragment_tag";

    private static final String KEY_PREFS = "key_preferences_radius";

    private SharedPreferences mPrefs;

    public static FilterFragment newInstance(String pPrefs){
        FilterFragment fragment = new FilterFragment();
        Bundle bundle = new Bundle();
        bundle.putString(KEY_PREFS, pPrefs);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String s = getArguments().getString(KEY_PREFS);

        mPrefs = getActivity().getSharedPreferences(s, Context.MODE_PRIVATE);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final View v = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_filter, null);

        final EditText radiusEditText = (EditText)v.findViewById(R.id.edit_text_radius);


        float r = mPrefs.getFloat(Constants.SHARED_PREFS_KEY_RADIUS, -1);
        if(r != -1)
            radiusEditText.setText("" + r/1000);

        return new AlertDialog.Builder(getActivity())
                .setTitle("Set Radius")
                .setView(v)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface pDialogInterface, int pI) {
                        String r = radiusEditText.getText().toString();
                        Float radius = null;

                        if(r != null && r.equals(""))
                            mPrefs.edit().putFloat(Constants.SHARED_PREFS_KEY_RADIUS, -1).commit();

                        try {
                            radius = Float.parseFloat(r);
                        } catch (Exception e){}

                        if(radius != null)
                            mPrefs.edit().putFloat(Constants.SHARED_PREFS_KEY_RADIUS, radius*1000).commit();

                        FragmentManager manager = ((NavigationDrawerActivity)getActivity()).getSupportFragmentManager();

                        if(manager.findFragmentByTag(EatListFragment.FRAGMENT_TAG) != null){
                            ((EatListFragment)manager.findFragmentByTag(EatListFragment.FRAGMENT_TAG)).searchItemsByRadius();
                        } else if(manager.findFragmentByTag(SleepListFragment.FRAGMENT_TAG) != null){
                            ((SleepListFragment)manager.findFragmentByTag(SleepListFragment.FRAGMENT_TAG)).searchItemsByRadius();
                        }
                        else {
                            ((SightsListFragment)manager.findFragmentByTag(SightsListFragment.FRAGMENT_TAG)).searchItemsByRadius();
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .create();
    }
}
