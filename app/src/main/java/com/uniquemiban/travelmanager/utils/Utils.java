package com.uniquemiban.travelmanager.utils;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Utils {

    public static String getDistance(JSONObject pJSONObject){
        String dist = null;
        try {
            JSONArray rows = pJSONObject.getJSONArray("rows");
            JSONObject obj = rows.getJSONObject(0);
            JSONArray elements = obj.getJSONArray("elements");
            JSONObject obj1 = elements.getJSONObject(0);
            JSONObject distance = obj1.getJSONObject("distance");
            Log.i("TAG1", distance.toString());
            dist = distance.getString("text");

        } catch (JSONException pE) {}

        return dist;
    }

}
