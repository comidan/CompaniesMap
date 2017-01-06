package com.dev.companiesmap;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelState;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class PlacesDisplayTask extends AsyncTask<Object, Integer, List<HashMap<String, String>>> {

    JSONObject googlePlacesJson;
    GoogleMap googleMap;
    GoogleApiClient googleApiClient;
    BottomSheetContent bottomSheetContent;
    Context context;
    int markerColor;
    static Route route = new Route();

    @Override
    protected List<HashMap<String, String>> doInBackground(Object... inputObj) {

        List<HashMap<String, String>> googlePlacesList = null;
        Places placeJsonParser = new Places();

        try {
            googleMap = (GoogleMap) inputObj[0];
            googlePlacesJson = new JSONObject((String) inputObj[1]);
            googleApiClient = (GoogleApiClient) inputObj[2];
            bottomSheetContent = (BottomSheetContent) inputObj[3];
            context = (Context) inputObj[4];
            markerColor = (int) inputObj[5];
            googlePlacesList = placeJsonParser.parse(googlePlacesJson, googleApiClient);
        } catch (JSONException e) {
            Log.d("Exception", e.toString());
        }
        return googlePlacesList;
    }

    @Override
    protected void onPostExecute(List<HashMap<String, String>> list) {
        googleMap.clear();
        bottomSheetContent.getDynamicMarkers().clear();
        for (int i = 0; i < list.size(); i++) {
            MarkerOptions markerOptions = new MarkerOptions();
            HashMap<String, String> googlePlace = list.get(i);
            double lat = Double.parseDouble(googlePlace.get("lat"));
            double lng = Double.parseDouble(googlePlace.get("lng"));
            LatLng latLng = new LatLng(lat, lng);
            markerOptions.position(latLng);
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(getHUEColorFromRGB(markerColor)));
            bottomSheetContent.getDynamicMarkers().put(googleMap.addMarker(markerOptions), googlePlace);
        }
        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(final Marker marker) {
                bottomSheetContent.getBottomSheetLayout().setPanelState(PanelState.COLLAPSED);
                bottomSheetContent.getTitle().setText(bottomSheetContent.getDynamicMarkers().get(marker).get("place_name"));
                bottomSheetContent.getAddress().setText(bottomSheetContent.getDynamicMarkers().get(marker).get("address"));
                bottomSheetContent.getPhoneNumber().setText(bottomSheetContent.getDynamicMarkers().get(marker).get("phone_number"));
                bottomSheetContent.getCall().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + bottomSheetContent.getPhoneNumber().getText()
                                .toString()));
                        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling (marshmallow things)
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            return;
                        }
                        context.startActivity(intent);
                    }
                });
                bottomSheetContent.getBrowse().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String url = bottomSheetContent.getDynamicMarkers().get(marker).get("website");
                        if(url == null || url.isEmpty())
                            url = "http://www.google.com/search?q=" + bottomSheetContent.getDynamicMarkers().get(marker).get("place_name")
                                    .replace(' ', '+');
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(url));
                        context.startActivity(i);
                    }
                });
                bottomSheetContent.getDestination().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String coordinates = bottomSheetContent.getDynamicMarkers().get(marker).get("coordinates");
                        String[] latLngStr = coordinates.split(" ");
                        double latitudeDest = Double.parseDouble(latLngStr[0]);
                        double longitudeDest = Double.parseDouble(latLngStr[1]);
                        double latitudeFrom = googleMap.getMyLocation().getLatitude();
                        double longitudeFrom =  googleMap.getMyLocation().getLongitude();
                        if(route == null)
                            route = new Route();
                        route.deleteRoute();
                        route.drawRoute(googleMap, context,  new LatLng(latitudeFrom, longitudeFrom), new LatLng(latitudeDest, longitudeDest), "it", markerColor);
                    }
                });
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
                //bottomSheetContent.getPriceLevel().setText(bottomSheetContent.getDynamicMarkers().get(marker).get("price_level"));
                POIType locationType = MapsActivity.getPOITypeFrom(bottomSheetContent.getPOITypeData());
                if(locationType == POIType.SHOPPING ||
                   locationType == POIType.BUSINESS ||
                   locationType == POIType.BAR ||
                   locationType == POIType.CAFE ||
                   locationType == POIType.RESTAURANT)
                {
                    bottomSheetContent.getPlaceOrder().setVisibility(View.VISIBLE);
                    bottomSheetContent.getPlaceOrder().setClickable(true);
                    bottomSheetContent.getPlaceOrder().setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent orderActivity = new Intent(context, OrderActivity.class);
                            orderActivity.putExtra("LOCATION_ADDRESS", bottomSheetContent.getDynamicMarkers().get(marker).get("address"));
                            Date current_date = new Date();
                            orderActivity.putExtra("LOCATION_DATE", current_date.getYear()+"/"+current_date.getMonth()+"/"+current_date.getDay());
                            orderActivity.putExtra("LOCATION_TYPE", bottomSheetContent.getPOITypeData());
                            orderActivity.putExtra("LOCATION_NAME", bottomSheetContent.getDynamicMarkers().get(marker).get("place_name"));
                            context.startActivity(orderActivity);
                        }
                    });
                }
                else
                {
                    bottomSheetContent.getPlaceOrder().setVisibility(View.INVISIBLE);
                    bottomSheetContent.getPlaceOrder().setClickable(false);
                }
                return false;
            }
        });
    }

    private float getHUEColorFromRGB(int rgb)
    {
        switch(rgb)
        {
            default :
            case R.color.red : return BitmapDescriptorFactory.HUE_RED;
            case R.color.orange : return BitmapDescriptorFactory.HUE_ORANGE;
            case R.color.yellow : return BitmapDescriptorFactory.HUE_YELLOW;
            case R.color.blue : return BitmapDescriptorFactory.HUE_BLUE;
            case R.color.violet : return  BitmapDescriptorFactory.HUE_VIOLET;
            case R.color.green :  return BitmapDescriptorFactory.HUE_GREEN;
        }
    }
}

