package com.dev.companiesmap;

import android.Manifest;
import android.support.v4.app.LoaderManager;
import android.app.SearchManager;
import android.content.Context;
import android.support.v4.content.CursorLoader;
import android.content.Intent;
import android.support.v4.content.Loader;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.SearchView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelState;


public class MapsActivity extends AppCompatActivity implements LocationListener, GoogleApiClient.OnConnectionFailedListener, LoaderManager.LoaderCallbacks<Cursor> {

    private static final long MIN_TIME = 400;
    private static final float MIN_DISTANCE = 1000;
    private static final int PROXIMITY_RADIUS = 5000;
    private static final int ANIMATION_TIME = 2000;
    private static final int MAX_MAP_ZOOM = 15;
    private static final int MAP_BEARING_DIRECTION = 0;
    private static final float MAX_MAP_TILT_DEGREES = 67.5f;

    private CoordinatorLayout coordinatorLayout;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private ExpandableListView drawerList;
    private String[] drawerTitles, locationTypes;
    private CharSequence drawerTitle;
    private CharSequence title;
    private SlidingUpPanelLayout bottomSheetLayout;
    private BottomSheetContent bottomSheetContent;
    private GoogleApiClient googleApiClient;
    private GoogleMap map;
    private LocationManager locationManager;
    private Location tempLocation = null;
    private int selectedPOI = - 1;
    private int selectedCategoryPOI = - 1;
    double latitude = 0;
    double longitude = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_ACTION_BAR);
        setContentView(R.layout.activity_maps);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        handleIntent(getIntent());
        title = drawerTitle = getTitle();
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.main_coordinator_layout);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerTitles = getResources().getStringArray(R.array.poi_tousername);
        locationTypes = getResources().getStringArray(R.array.poi_mames);
        drawerList = (ExpandableListView) findViewById(R.id.left_drawer);
        Handler callbackSearchHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                bottomSheetLayout.setPanelState(PanelState.HIDDEN);
                SearchQueryObject searchQueryObject = (SearchQueryObject) msg.getData().getSerializable("SEARCH_QUERY");
                selectedPOI = searchQueryObject.getPOIIndex();
                selectedCategoryPOI = searchQueryObject.getCategoryIndex();
                getSupportActionBar().setTitle(drawerTitles[searchQueryObject.getPOIIndex()]);
                bottomSheetContent.getPOITypeImage().setImageResource(bottomSheetContent.POITypeImages.get(getPOITypeFrom(selectedCategoryPOI)));
                bottomSheetContent.setPOITypeData(selectedCategoryPOI);
                selectItem(searchQueryObject.getPOI());
                return false;
            }
        });
        SubListAdapter adapter = new SubListAdapter(this, callbackSearchHandler, drawerTitles);
        drawerList.setAdapter(adapter);
        DrawerArrowDrawable drawerArrow = new DrawerArrowDrawable(this) {
            @Override
            public boolean isLayoutRtl() {
                return false;
            }
        };
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, drawerArrow, R.string.drawer_open, R.string.drawer_closed) {

            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
            }
        };
        drawerLayout.addDrawerListener(drawerToggle);
        drawerLayout.setFocusableInTouchMode(false);
        drawerToggle.setAnimateEnabled(true);
        bottomSheetLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout); //da aggiungere il FAB
        bottomSheetLayout.setDragView(findViewById(R.id.main_bar));
        bottomSheetLayout.setPanelState(PanelState.HIDDEN);
        bottomSheetContent = new BottomSheetContent(bottomSheetLayout);
        bottomSheetContent.setAddress((TextView) findViewById(R.id.address));
        bottomSheetContent.setIsNowOpen((TextView) findViewById(R.id.is_now_open));
        bottomSheetContent.setPhoneNumber((TextView) findViewById(R.id.phone_number));
        bottomSheetContent.setRatingValue((TextView) findViewById(R.id.rating_value));
        bottomSheetContent.setTitle((TextView) findViewById(R.id.title));
        //bottomSheetContent.setPriceLevel((TextView) findViewById(R.id.price_level));
        bottomSheetContent.setRatingBar((RatingBar) findViewById(R.id.rating_bar));
        bottomSheetContent.setPOITypeImage((ImageView) findViewById(R.id.poi_type_image));
        bottomSheetContent.setCall((ImageButton) findViewById(R.id.call));
        bottomSheetContent.setBrowse((ImageButton) findViewById(R.id.browse));
        bottomSheetContent.setDestination((ImageButton) findViewById(R.id.destination));
        bottomSheetContent.setPlaceOrder((Button) findViewById(R.id.place_order));
        bottomSheetLayout.setFadeOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //bottomSheetLayout.setPanelState(PanelState.COLLAPSED);
            }
        });
        bottomSheetContent.getTitle().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(bottomSheetContent.getTitle().getLineCount() == 1)
                    bottomSheetContent.getTitle().setMaxLines(3);
                else
                    bottomSheetContent.getTitle().setMaxLines(1);
            }
        });
        googleApiClient = new GoogleApiClient.Builder(this).enableAutoManage(this, this)
                                                           .addApi(com.google.android.gms.location.places.Places.GEO_DATA_API)
                                                           .addApi(com.google.android.gms.location.places.Places.PLACE_DETECTION_API)
                                                           .addScope(Drive.SCOPE_FILE)
                                                           .build();
        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        map.setMyLocationEnabled(true);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: marshmallow things
            return;
        }
        Criteria criteria = new Criteria();
        String bestProvider = locationManager.getBestProvider(criteria, true);
        Location location = locationManager.getLastKnownLocation(bestProvider);
        if (location != null) {
            onLocationChanged(location);
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE, this); //You can also use LocationManager.GPS_PROVIDER - SLOWER- and LocationManager.PASSIVE_PROVIDER
    }

    @Override
    public void onLocationChanged(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng)
                .tilt(MAX_MAP_TILT_DEGREES)
                .zoom(MAX_MAP_ZOOM)
                .bearing(MAP_BEARING_DIRECTION)
                .build();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
        map.moveCamera(cameraUpdate);
        map.animateCamera(CameraUpdateFactory.zoomIn());
        map.animateCamera(CameraUpdateFactory.zoomTo(MAX_MAP_ZOOM), ANIMATION_TIME, null);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: marshmallow things
            return;
        }                           //adding this first AND condition will smooth a bit in false condition, != faster than .distanceTo
        if(tempLocation == null || (tempLocation != location && tempLocation.distanceTo(location) > MIN_DISTANCE / 2)) {
            if(selectedPOI != -1)
                showNearByPointOfInterests(locationTypes[selectedPOI]);
            if(tempLocation == null)
                tempLocation = location;
            else
                tempLocation = null;
        }
    }

    private void selectItem(int position) {
        showNearByPointOfInterests(locationTypes[position]);
    }

    private void selectItem(String type) {
        showNearByPointOfInterests(type);
    }

    private void showNearByPointOfInterests(String pointOfInterest) {
        StringBuilder googlePlacesUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlacesUrl.append("location=" + latitude + "," + longitude);
        googlePlacesUrl.append("&radius=" + PROXIMITY_RADIUS);
        googlePlacesUrl.append("&types=" + pointOfInterest);
        googlePlacesUrl.append("&sensor=true");
        googlePlacesUrl.append("&language=it");
        googlePlacesUrl.append("&key=" + getString(R.string.google_maps_server_key));
        GooglePlacesReadTask googlePlacesReadTask = new GooglePlacesReadTask();
        Object[] toPass = new Object[6];
        toPass[0] = map;
        toPass[1] = googlePlacesUrl.toString();
        toPass[2] = googleApiClient;
        toPass[3] = bottomSheetContent;
        toPass[4] = this;
        toPass[5] = getColorByGroupIndex(selectedCategoryPOI);
        googlePlacesReadTask.execute(toPass);
    }

    private boolean isGooglePlayServicesAvailable() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS == status) {
            return true;
        } else {
            GooglePlayServicesUtil.getErrorDialog(status, this, 0).show();
            return false;
        }
    }

    public static POIType getPOITypeFrom(int position)
    {
        switch(position)
        {
            case 0 : return POIType.SHOPPING;
            case 1 : return POIType.BUSINESS;
            case 2 :
            case 6 : return POIType.BAR;
            case 3 : return POIType.CAFE;
            case 4 : return POIType.TRANSPORT;
            case 5 : return POIType.HEALTH;
            default : return POIType.BUSINESS;
        }
    }

    private int getColorByGroupIndex(int groupPosition)
    {
        switch (groupPosition)
        {
            default:
            case 0 : return R.color.red;
            case 1 : return R.color.orange;
            case 2 : return R.color.yellow;
            case 3 : return R.color.blue;
            case 4 : return R.color.violet;
            case 5 : return R.color.green;
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item))
            return true;
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if(PlacesDisplayTask.route.hasPathDrawn())
            PlacesDisplayTask.route.deleteRoute();
        else if(bottomSheetLayout.getPanelState() == PanelState.COLLAPSED)
            bottomSheetLayout.setPanelState(PanelState.HIDDEN);
        else if(bottomSheetLayout.getPanelState() == PanelState.EXPANDED)
            bottomSheetLayout.setPanelState(PanelState.COLLAPSED);
        else if(drawerLayout.isDrawerOpen(drawerList))
            finish();
        else
            drawerLayout.openDrawer(drawerList);
    }

    private void handleIntent(Intent intent){
        if(intent.getAction().equals(Intent.ACTION_SEARCH)){
            doSearch(intent.getStringExtra(SearchManager.QUERY));
        }else if(intent.getAction().equals(Intent.ACTION_VIEW)){
            getPlace(intent.getStringExtra(SearchManager.EXTRA_DATA_KEY));
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    private void doSearch(String query){
        Bundle data = new Bundle();
        data.putString("query", query);
        getSupportLoaderManager().restartLoader(0, data, this);
    }

    private void getPlace(String query){
        Bundle data = new Bundle();
        data.putString("query", query);
        getSupportLoaderManager().restartLoader(1, data, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        return true;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int arg0, Bundle query) {
        CursorLoader cLoader = null;
        if(arg0==0)
            cLoader = new CursorLoader(getBaseContext(), PlaceProvider.SEARCH_URI, null, null, new String[]{ query.getString("query") }, null);
        else if(arg0==1)
            cLoader = new CursorLoader(getBaseContext(), PlaceProvider.DETAILS_URI, null, null, new String[]{ query.getString("query") }, null);
        return cLoader;

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        if(status == LocationProvider.AVAILABLE) {
            locationManager.removeUpdates(this);
            locationManager.requestLocationUpdates(provider, MIN_TIME, MIN_DISTANCE, this);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: marshmallow things
                return;
            }
            if (provider.equals(LocationManager.GPS_PROVIDER))
                Snackbar.make(coordinatorLayout, getString(R.string.gps_linked), Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onProviderEnabled(String provider) {
        locationManager.requestLocationUpdates(provider, MIN_TIME, MIN_DISTANCE, this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: marshmallow things
            return;
        }
        if (provider.equals(LocationManager.GPS_PROVIDER))
            Snackbar.make(coordinatorLayout, getString(R.string.gps_linked), Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderDisabled(String provider) {
        locationManager.removeUpdates(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: marshmallow things
            return;
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.v("CallToGoogleAPIFailed", connectionResult.toString());
    }
}