package com.dev.companiesmap;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.GoogleMap;

import java.io.IOException;

public class GooglePlacesReadTask extends AsyncTask<Object, Integer, String> {
    String googlePlacesData = null;
    GoogleMap googleMap;
    GoogleApiClient googleApiClient;
    BottomSheetContent bottomSheetContent;
    Context context;
    int markerColor;

    @Override
    protected String doInBackground(Object... inputObj) {
        try
        {
            googleMap = (GoogleMap) inputObj[0];
            String googlePlacesUrl = (String) inputObj[1];
            googleApiClient = (GoogleApiClient) inputObj[2];
            bottomSheetContent = (BottomSheetContent) inputObj[3];
            context = (Context) inputObj[4];
            markerColor = (int) inputObj[5];
            Http http = new Http();
            googlePlacesData = http.read(googlePlacesUrl);
        }
        catch (IOException e)
        {
            Log.d("Google Place Read Task", e.toString());
        }
        return googlePlacesData;
    }

    @Override
    protected void onPostExecute(String result) {
        PlacesDisplayTask placesDisplayTask = new PlacesDisplayTask();
        Object[] toPass = new Object[6];
        toPass[0] = googleMap;
        toPass[1] = result;
        toPass[2] = googleApiClient;
        toPass[3] = bottomSheetContent;
        toPass[4] = context;
        toPass[5] = markerColor;
        placesDisplayTask.execute(toPass);
    }
}