package com.dev.companiesmap;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelState;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

public class PlacesDisplayTask extends AsyncTask<Object, Integer, List<HashMap<String, String>>> {

    JSONObject googlePlacesJson;
    GoogleMap googleMap;
    GoogleApiClient googleApiClient;
    BottomSheetContent bottomSheetContent;

    @Override
    protected List<HashMap<String, String>> doInBackground(Object... inputObj) {

        List<HashMap<String, String>> googlePlacesList = null;
        Places placeJsonParser = new Places();

        try
        {
            googleMap = (GoogleMap) inputObj[0];
            googlePlacesJson = new JSONObject((String) inputObj[1]);
            googleApiClient = (GoogleApiClient) inputObj[2];
            bottomSheetContent = (BottomSheetContent) inputObj[3];
            googlePlacesList = placeJsonParser.parse(googlePlacesJson, googleApiClient);
        }
        catch (JSONException e)
        {
            Log.d("Exception", e.toString());
        }
        return googlePlacesList;
    }

    @Override
    protected void onPostExecute(List<HashMap<String, String>> list) {
        googleMap.clear();
        bottomSheetContent.getDynamicMarkers().clear();
        for (int i = 0; i < list.size(); i++)
        {
            MarkerOptions markerOptions = new MarkerOptions();
            HashMap<String, String> googlePlace = list.get(i);
            double lat = Double.parseDouble(googlePlace.get("lat"));
            double lng = Double.parseDouble(googlePlace.get("lng"));
            LatLng latLng = new LatLng(lat, lng);
            markerOptions.position(latLng);
            bottomSheetContent.getDynamicMarkers().put(googleMap.addMarker(markerOptions), googlePlace);
        }
        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                bottomSheetContent.getBottomSheetLayout().setPanelState(PanelState.COLLAPSED);
                bottomSheetContent.getTitle().setText(bottomSheetContent.getDynamicMarkers().get(marker).get("place_name"));
                bottomSheetContent.getAddress().setText(bottomSheetContent.getDynamicMarkers().get(marker).get("address"));
                bottomSheetContent.getPhoneNumber().setText(bottomSheetContent.getDynamicMarkers().get(marker).get("phone_number"));
                Float rating = Float.parseFloat(bottomSheetContent.getDynamicMarkers().get(marker).get("rating"));
                if(rating == -1.0f)
                {
                    bottomSheetContent.getRatingValue().setText("Sconosciuto");
                    bottomSheetContent.getRatingBar().setRating(0.0f);
                }
                else
                {
                    bottomSheetContent.getRatingValue().setText(rating + "");
                    bottomSheetContent.getRatingBar().setRating(rating);
                }
                bottomSheetContent.getIsNowOpen().setText(bottomSheetContent.getDynamicMarkers().get(marker).get("open_now").equals("true") ?
                                                          "Aperto ora" : "Chiuso ora");
                bottomSheetContent.getPriceLevel().setText(bottomSheetContent.getDynamicMarkers().get(marker).get("price_level"));
                return false;
            }
        });
    }
}

