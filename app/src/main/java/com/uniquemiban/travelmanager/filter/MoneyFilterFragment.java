package com.uniquemiban.travelmanager.filter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.uniquemiban.travelmanager.R;
import com.uniquemiban.travelmanager.start.NavigationDrawerActivity;
import com.uniquemiban.travelmanager.tour.TourListFragment;

public class MoneyFilterFragment extends DialogFragment {

    public static final String FRAGMENT_TAG = "money_filter_fragment_tag";
    public static final String SHARED_PREFS_MONEY = "shared_prefs_money";
    public static final String SHARED_PREFS_KEY_FROM = "shared_prefs_money_from";
    public static final String SHARED_PREFS_KEY_TO = "shared_prefs_money_to";

    private SharedPreferences mPrefs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPrefs = getActivity().getSharedPreferences(SHARED_PREFS_MONEY, Context.MODE_PRIVATE);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final View v = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_money_filter, null);

        final EditText fromEditText = (EditText)v.findViewById(R.id.edit_text_money_from);
        final EditText toEditText = (EditText)v.findViewById(R.id.edit_text_money_to);

        int from = mPrefs.getInt(SHARED_PREFS_KEY_FROM, -1);
        int to = mPrefs.getInt(SHARED_PREFS_KEY_TO, -1);

        if(from != -1)
            fromEditText.setText("" + from);

        if(to != -1)
            toEditText.setText("" + to);

        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.action_money)
                .setView(v)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface pDialogInterface, int pI) {
                        String from = fromEditText.getText().toString();
                        String to = toEditText.getText().toString();

                        if(from != null && from.equals(""))
                            mPrefs.edit().putInt(SHARED_PREFS_KEY_FROM, -1).commit();

                        if(to != null && to.equals(""))
                            mPrefs.edit().putInt(SHARED_PREFS_KEY_TO, -1).commit();

                        Integer fromInt = null;
                        Integer toInt = null;

                        try {
                            fromInt = Integer.parseInt(from);
                            toInt = Integer.parseInt(to);
                        } catch (Exception e){}

                        if(fromInt != null)
                            mPrefs.edit().putInt(SHARED_PREFS_KEY_FROM, fromInt).commit();

                        if(toInt != null)
                            mPrefs.edit().putInt(SHARED_PREFS_KEY_TO, toInt).commit();

                        FragmentManager manager = ((NavigationDrawerActivity)getActivity()).getSupportFragmentManager();

                        if(manager.findFragmentByTag(TourListFragment.FRAGMENT_TAG) != null){
                            ((TourListFragment)manager.findFragmentByTag(TourListFragment.FRAGMENT_TAG)).searchItemsByPrice();
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .create();
    }
}
