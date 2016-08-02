package com.dev.companiesmap;

import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Places {

    private GoogleApiClient googleApiClient;

    public List<HashMap<String, String>> parse(JSONObject jsonObject, GoogleApiClient googleApiClient) {
        JSONArray jsonArray = null;
        this.googleApiClient = googleApiClient;
        try
        {
            jsonArray = jsonObject.getJSONArray("results");
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return getPlaces(jsonArray);
    }

    private List<HashMap<String, String>> getPlaces(JSONArray jsonArray) {
        int placesCount = jsonArray.length();
        List<HashMap<String, String>> placesList = new ArrayList<HashMap<String, String>>();
        HashMap<String, String> placeMap = null;

        for (int i = 0; i < placesCount; i++)
            try
            {
                placeMap = getPlace((JSONObject) jsonArray.get(i));
                placesList.add(placeMap);

            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
        return placesList;
    }

    private HashMap<String, String> getPlace(JSONObject googlePlaceJson) {

        final HashMap<String, String> googlePlaceMap = new HashMap<String, String>();
        String placeID = "-NA-";
        String placeName = "-NA-";
        String vicinity = "-NA-";
        String latitude = "";
        String longitude = "";
        String reference = "";
        boolean isNowOpen = false;

        try
        {
            if (!googlePlaceJson.isNull("name"))
                placeName = googlePlaceJson.getString("name");
            if (!googlePlaceJson.isNull("vicinity"))
                vicinity = googlePlaceJson.getString("vicinity");
            placeID = googlePlaceJson.getString("place_id");
            com.google.android.gms.location.places.Places.GeoDataApi.getPlaceById(googleApiClient, placeID)
                    .setResultCallback(new ResultCallback<PlaceBuffer>() {
                        @Override
                        public void onResult(PlaceBuffer places) {
                            if (places.getStatus().isSuccess() && places.getCount() > 0)
                            {
                                final Place myPlace = places.get(0);
                                googlePlaceMap.put("rating", myPlace.getRating()+"");
                                googlePlaceMap.put("price_level", myPlace.getPriceLevel()+"");
                                googlePlaceMap.put("phone_number", myPlace.getPhoneNumber().toString());
                                googlePlaceMap.put("address", myPlace.getAddress().toString());
                            }
                            else
                                Log.e("Places", "Place not found");
                            places.release();
                        }
                    });
            latitude = googlePlaceJson.getJSONObject("geometry").getJSONObject("location").getString("lat");
            longitude = googlePlaceJson.getJSONObject("geometry").getJSONObject("location").getString("lng");
            if(googlePlaceJson.has("opening_hours"))
                isNowOpen = googlePlaceJson.getJSONObject("opening_hours").getString("open_now").equals("true");
            reference = googlePlaceJson.getString("reference");
            googlePlaceMap.put("place_name", placeName);
            googlePlaceMap.put("vicinity", vicinity);
            googlePlaceMap.put("lat", latitude);
            googlePlaceMap.put("lng", longitude);
            googlePlaceMap.put("reference", reference);
            googlePlaceMap.put("open_now", isNowOpen+"");
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return googlePlaceMap;
    }
}