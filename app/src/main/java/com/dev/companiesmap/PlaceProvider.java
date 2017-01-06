package com.dev.companiesmap;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;

public class PlaceProvider extends ContentProvider {

    public static final String AUTHORITY = "com.dev.companies.PlaceProvider";

    public static final Uri SEARCH_URI = Uri.parse("content://"+AUTHORITY+"/search");

    public static final Uri DETAILS_URI = Uri.parse("content://"+AUTHORITY+"/details");

    private static final int SEARCH = 1;
    private static final int SUGGESTIONS = 2;
    private static final int DETAILS = 3;

    String mKey = "AIzaSyCZAcR3rZY-PW487rcFwRng77s8PMHFwgc";

    private static final UriMatcher mUriMatcher = buildUriMatcher();

    private static UriMatcher buildUriMatcher() {   //setting clearence
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, "search", SEARCH );
        uriMatcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY,SUGGESTIONS);
        uriMatcher.addURI(AUTHORITY, "details",DETAILS);
        return uriMatcher;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Cursor c = null;

        PlaceJSONParser parser = new PlaceJSONParser();
        PlaceDetailsJSONParser detailsParser = new PlaceDetailsJSONParser();

        String jsonString = "";
        String jsonPlaceDetails = "";

        List<HashMap<String, String>> list = null;
        List<HashMap<String, String>> detailsList = null;

        MatrixCursor mCursor = null;

        switch(mUriMatcher.match(uri)) {
            case SEARCH:
                mCursor = new MatrixCursor(new String[] { "description","lat","lng" });
                parser = new PlaceJSONParser();
                detailsParser = new PlaceDetailsJSONParser();
                jsonString = getPlaces(selectionArgs);
                try {
                    list = parser.parse(new JSONObject(jsonString));
                    for(int i=0;i<list.size();i++){
                        HashMap<String, String> hMap = (HashMap<String, String>) list.get(i);
                        detailsParser =new PlaceDetailsJSONParser();
                        jsonPlaceDetails = getPlaceDetails(hMap.get("reference"));
                        detailsList = detailsParser.parse(new JSONObject(jsonPlaceDetails));
                        for(int j=0;j<detailsList.size();j++) {
                            HashMap<String, String> hMapDetails = detailsList.get(j);
                            mCursor.addRow(new String[]{ hMap.get("description") , hMapDetails.get("lat") , hMapDetails.get("lng") });
                        }
                    }
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
                c = mCursor;
                break;

            case SUGGESTIONS :
                mCursor = new MatrixCursor(new String[] { "_id", SearchManager.SUGGEST_COLUMN_TEXT_1, SearchManager.SUGGEST_COLUMN_INTENT_EXTRA_DATA } );
                parser = new PlaceJSONParser();
                jsonString = getPlaces(selectionArgs);

                try {
                    list = parser.parse(new JSONObject(jsonString));
                    for(int i=0;i<list.size();i++){
                        HashMap<String, String> hMap = (HashMap<String, String>) list.get(i);
                        mCursor.addRow(new String[] { Integer.toString(i), hMap.get("description"), hMap.get("reference") });
                    }
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
                c = mCursor;
                break;

            case DETAILS :
                mCursor = new MatrixCursor(new String[] { "description","lat","lng" });
                detailsParser = new PlaceDetailsJSONParser();
                jsonPlaceDetails = getPlaceDetails(selectionArgs[0]);
                try {
                    detailsList = detailsParser.parse(new JSONObject(jsonPlaceDetails));
                }
                catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                for(int j=0;j<detailsList.size();j++){
                    HashMap<String, String> hMapDetails = detailsList.get(j);
                    mCursor.addRow(new String[]{ hMapDetails.get("formatted_address") , hMapDetails.get("lat") , hMapDetails.get("lng") });
                }
                c = mCursor;
                break;
        }
        return c;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public boolean onCreate() {
        return false;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        return 0;
    }

    /** A method to download json data from url */
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();
            iStream = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            StringBuffer sb = new StringBuffer();
            String line = "";
            while( ( line = br.readLine()) != null){
                sb.append(line);
            }
            data = sb.toString();
            br.close();
        }
        catch(Exception e) {
            Log.d("http exception", e.toString());
        }
        finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    private String getPlaceDetailsUrl(String ref) {
        String reference = "reference="+ref;
        String sensor = "sensor=false";
        String parameters = reference+"&"+sensor+"&"+mKey;
        String output = "json";
        String url = "https://maps.googleapis.com/maps/api/place/details/"+output+"?"+parameters;
        return url;
    }

    private String getPlacesUrl(String qry) {

        try {
            qry = "input=" + URLEncoder.encode(qry, "utf-8");
        }
        catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }
        String sensor = "sensor=false";
        String types = "types=geocode";
        String parameters = qry+"&"+types+"&"+sensor+"&"+mKey;
        String output = "json";
        String url = "https://maps.googleapis.com/maps/api/place/autocomplete/"+output+"?"+parameters;
        return url;
    }

    private String getPlaces(String[] params) {
        String data = "";
        String url = getPlacesUrl(params[0]);
        try {
            data = downloadUrl(url);
        }
        catch(Exception e) {
            Log.d("Background Task",e.toString());
        }
        return data;
    }

    private String getPlaceDetails(String reference) {
        String data = "";
        String url = getPlaceDetailsUrl(reference);
        try {
            data = downloadUrl(url);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }
}