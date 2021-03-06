package com.uniquemiban.travelmanager.rate;


import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.uniquemiban.travelmanager.R;
import com.uniquemiban.travelmanager.login.LoginActivity;
import com.uniquemiban.travelmanager.models.RateMsg;
import com.uniquemiban.travelmanager.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class RateFragment extends DialogFragment {

    public static final String FRAGMENT_TAG = "fragment_tag_rate_fragment";

    private static final String ARG_PLACE_ID = "rate_fragment_place_id";
    private static final String ARG_PLACE_NAME = "rate_fragment_place_name";

    private String mPlaceId = null;
    private String mPlaceName = null;

    private ValueEventListener mValueEventListener;
    private ValueEventListener mGlobalValueEventListener;
    private DatabaseReference mUsers;

    private Query mQuery;
    private ChildEventListener mChildEventListener;
    private ArrayList<RateMsg> mRatesList;
    private RateAdapter mAdapter;

    public static RateFragment newInstance(String pPlaceId, String pPlaceName) {
        RateFragment fragment = new RateFragment();
        Bundle bundle = new Bundle();
        bundle.putString(ARG_PLACE_ID, pPlaceId);
        bundle.putString(ARG_PLACE_NAME, pPlaceName);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        mPlaceId = bundle.getString(ARG_PLACE_ID);
        mPlaceName = bundle.getString(ARG_PLACE_NAME);

        mQuery = FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_RATES).child(mPlaceId).limitToLast(5);
        mRatesList = new ArrayList<>();
        mAdapter = new RateAdapter(mRatesList);

        if(FirebaseAuth.getInstance().getCurrentUser() != null) {
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot pDataSnapshot, String pS) {
                    mRatesList.add(pDataSnapshot.getValue(RateMsg.class));
                    mAdapter.notifyDataSetChanged();
                }

                @Override
                public void onChildChanged(DataSnapshot pDataSnapshot, String pS) {

                }

                @Override
                public void onChildRemoved(DataSnapshot pDataSnapshot) {

                }

                @Override
                public void onChildMoved(DataSnapshot pDataSnapshot, String pS) {

                }

                @Override
                public void onCancelled(DatabaseError pDatabaseError) {

                }
            };
        }else {
            Toast.makeText(getActivity(), "Please sign in before rate or view others rates..", Toast.LENGTH_LONG).show();
            startActivity(new Intent(getActivity(), LoginActivity.class));
            dismiss();
            getActivity().finish();
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final View v = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_rate, null);

        RecyclerView recyclerView = (RecyclerView) v.findViewById(R.id.recycler_view_rates);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(mAdapter);

        final RatingBar ratingBar = (RatingBar) v.findViewById(R.id.rating_bar_fragment_rate);

        Drawable drawable = ratingBar.getProgressDrawable();
        drawable.setColorFilter(Color.parseColor("#FF9800"), PorterDuff.Mode.SRC_ATOP);

        return new AlertDialog.Builder(getActivity())
                .setTitle(mPlaceName)
                .setView(v)
                .setPositiveButton("Rate", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface pDialogInterface, int pI) {
                        final FirebaseAuth auth = FirebaseAuth.getInstance();
                        if (auth.getCurrentUser() == null) {
                            Toast.makeText(getActivity(), "Please sign in before rate or view others rates..", Toast.LENGTH_LONG).show();
                            startActivity(new Intent(getActivity(), LoginActivity.class));
                            dismiss();
                        } else {

                            float rating = ratingBar.getRating();
                            String message = ((EditText) v.findViewById(R.id.edit_text_feedback_fragment_rate)).getText().toString();

                            final DatabaseReference ratesRef = FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_RATES);
                            final DatabaseReference avgRatesRef = FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_AVG_RATES);
                            mUsers = FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_USERS);

                            final RateMsg rateMsg = new RateMsg();
                            rateMsg.setRate(rating);
                            rateMsg.setMessage(message);

                            mValueEventListener = new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot pDataSnapshot) {
                                    rateMsg.setName(pDataSnapshot.child("Name").getValue(String.class));

                                    ratesRef.child(mPlaceId).child(auth.getCurrentUser().getUid()).setValue(rateMsg).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> pTask) {
                                            if(pTask.isSuccessful()){
                                                avgRatesRef.child(mPlaceId).child("num").runTransaction(new Transaction.Handler() {
                                                    @Override
                                                    public Transaction.Result doTransaction(MutableData pMutableData) {
                                                        Long l = pMutableData.getValue(Long.class);
                                                        if (l == null)
                                                            return Transaction.success(pMutableData);

                                                        pMutableData.setValue(l + 1);
                                                        return Transaction.success(pMutableData);
                                                    }

                                                    @Override
                                                    public void onComplete(DatabaseError pDatabaseError, boolean pB, DataSnapshot pDataSnapshot) {
                                                        avgRatesRef.child(mPlaceId).child("sum").runTransaction(new Transaction.Handler() {
                                                            @Override
                                                            public Transaction.Result doTransaction(MutableData pMutableData) {
                                                                if (pMutableData.getValue() == null)
                                                                    return Transaction.success(pMutableData);

                                                                pMutableData.setValue(pMutableData.getValue(Double.class) + rateMsg.getRate());
                                                                return Transaction.success(pMutableData);
                                                            }

                                                            @Override
                                                            public void onComplete(DatabaseError pDatabaseError, boolean pB, DataSnapshot pDataSnapshot) {
                                                                mUsers.child(auth.getCurrentUser().getUid()).removeEventListener(mValueEventListener);
                                                            }
                                                        });
                                                    }
                                                });
                                            }
                                        }
                                    });
                                }

                                @Override
                                public void onCancelled(DatabaseError pDatabaseError) {

                                }
                            };

                            mGlobalValueEventListener = new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot pDataSnapshot) {
                                    RateMsg oldRateMsg = pDataSnapshot.getValue(RateMsg.class);

                                    if(oldRateMsg == null){

                                        mUsers.child(auth.getCurrentUser().getUid()).addValueEventListener(mValueEventListener);

                                    } else{
                                        final double rateDiff = rateMsg.getRate() - oldRateMsg.getRate();

                                        ratesRef.child(mPlaceId).child(auth.getCurrentUser().getUid()).child("rate").setValue(rateMsg.getRate()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> pTask) {
                                                if (pTask.isSuccessful()) {
                                                    ratesRef.child(mPlaceId).child(auth.getCurrentUser().getUid()).child("message").setValue(rateMsg.getMessage());

                                                    avgRatesRef.child(mPlaceId).child("sum").runTransaction(new Transaction.Handler() {
                                                        @Override
                                                        public Transaction.Result doTransaction(MutableData pMutableData) {
                                                            Double d = pMutableData.getValue(Double.class);

                                                            if (d == null)
                                                                return Transaction.success(pMutableData);

                                                            pMutableData.setValue(d + rateDiff);
                                                            ratesRef.child(mPlaceId).child(auth.getCurrentUser().getUid()).removeEventListener(mGlobalValueEventListener);
                                                            return Transaction.success(pMutableData);
                                                        }

                                                        @Override
                                                        public void onComplete(DatabaseError pDatabaseError, boolean pB, DataSnapshot pDataSnapshot) {

                                                        }
                                                    });
                                                }
                                            }
                                        });
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError pDatabaseError) {

                                }
                            };

                            ratesRef.child(mPlaceId).child(auth.getCurrentUser().getUid()).addValueEventListener(mGlobalValueEventListener);
                        }
                    }
                })
                .setNegativeButton("Cancel", null).create();
    }

    @Override
    public void onStart() {
        super.onStart();
        if(mChildEventListener != null)
            mQuery.addChildEventListener(mChildEventListener);
    }

    @Override
    public void onStop() {
        if(mChildEventListener != null)
            mQuery.removeEventListener(mChildEventListener);
        super.onStop();
    }

    private class RateHolder extends RecyclerView.ViewHolder{

        public TextView mName;
        public RatingBar mRatingBar;
        public TextView mMessage;

        public RateHolder(View itemView) {
            super(itemView);

            mName = (TextView)itemView.findViewById(R.id.text_view_name_item_rate);
            mRatingBar = (RatingBar)itemView.findViewById(R.id.rating_bar_item_rate);
            mRatingBar.setOnClickListener(null);
            mMessage = (TextView)itemView.findViewById(R.id.text_view_message_item_rate);
        }

        public void bindRate (RateMsg pRateMsg){
            mName.setText(pRateMsg.getName());
            mRatingBar.setRating(pRateMsg.getRate());
            mMessage.setText(pRateMsg.getMessage());
        }
    }

    private class RateAdapter extends RecyclerView.Adapter<RateHolder>{

        private List<RateMsg> mRates;

        public RateAdapter(List<RateMsg> pRates){
            mRates = pRates;
        }

        @Override
        public RateHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View v = inflater.inflate(R.layout.item_rate, parent, false);

            return new RateHolder(v);
        }

        @Override
        public void onBindViewHolder(RateHolder holder, int position) {
            RateMsg rate = mRates.get(position);
            holder.bindRate(rate);
        }

        @Override
        public int getItemCount() {
            return mRates.size();
        }
    }
}
