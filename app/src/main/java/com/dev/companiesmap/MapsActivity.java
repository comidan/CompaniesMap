package com.dev.companiesmap;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
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


public class MapsActivity extends AppCompatActivity implements LocationListener, GoogleApiClient.OnConnectionFailedListener {

    private static final long MIN_TIME = 400;
    private static final float MIN_DISTANCE = 1000;
    private static final int PROXIMITY_RADIUS = 5000;
    private static final int ANIMATION_TIME = 2000;
    private static final int MAX_MAP_ZOOM = 15;
    private static final int MAP_BEARING_DIRECTION = 0;
    private static final float MAX_MAP_TILT_DEGREES = 67.5f;

    private DrawerLayout drawerLayout;
    private DrawerArrowDrawable drawerArrow;
    private ActionBarDrawerToggle drawerToggle;
    private ListView drawerList;
    private String[] drawerTitles, locationTypes;
    private CharSequence drawerTitle;
    private CharSequence title;
    private SlidingUpPanelLayout bottomSheetLayout;
    private BottomSheetContent bottomSheetContent;
    private GoogleApiClient googleApiClient;
    private GoogleMap map;
    private LocationManager locationManager;
    private Location tempLocation = null;
    private int selectedPOI = -1;
    double latitude = 0;
    double longitude = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_ACTION_BAR);
        setContentView(R.layout.activity_maps);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        title = drawerTitle = getTitle();
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerTitles = getResources().getStringArray(R.array.drawer_names);
        locationTypes = getResources().getStringArray(R.array.poi_mames);
        drawerList = (ListView) findViewById(R.id.left_drawer);
        drawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                bottomSheetLayout.setPanelState(PanelState.HIDDEN);
                selectedPOI = position;
                getSupportActionBar().setTitle(drawerTitles[position]);
                bottomSheetContent.getPOITypeImage().setImageResource(bottomSheetContent.POITypeImages.get(getPOITypeFrom(position)));
                selectItem(position);
            }
        });
        drawerList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, drawerTitles));
        drawerArrow = new DrawerArrowDrawable(this) {
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
        bottomSheetContent.setHowFar((TextView) findViewById(R.id.how_far));
        bottomSheetContent.setIsNowOpen((TextView) findViewById(R.id.is_now_open));
        bottomSheetContent.setPhoneNumber((TextView) findViewById(R.id.phone_number));
        bottomSheetContent.setRatingValue((TextView) findViewById(R.id.rating_value));
        bottomSheetContent.setTitle((TextView) findViewById(R.id.title));
        bottomSheetContent.setPriceLevel((TextView) findViewById(R.id.price_level));
        bottomSheetContent.setRatingBar((RatingBar) findViewById(R.id.rating_bar));
        bottomSheetContent.setPOITypeImage((ImageView) findViewById(R.id.poi_type_image));
        bottomSheetLayout.setFadeOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //bottomSheetLayout.setPanelState(PanelState.COLLAPSED);
            }
        });
        bottomSheetContent.getTitle().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(bottomSheetContent.getTitle().getMaxLines() == 1)
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

    private void showNearByPointOfInterests(String pointOfInterest) {
        StringBuilder googlePlacesUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlacesUrl.append("location=" + latitude + "," + longitude);
        googlePlacesUrl.append("&radius=" + PROXIMITY_RADIUS);
        googlePlacesUrl.append("&types=" + pointOfInterest);
        googlePlacesUrl.append("&sensor=true");
        googlePlacesUrl.append("&language=it");
        googlePlacesUrl.append("&key=" + getString(R.string.google_maps_server_key));
        GooglePlacesReadTask googlePlacesReadTask = new GooglePlacesReadTask();
        Object[] toPass = new Object[4];
        toPass[0] = map;
        toPass[1] = googlePlacesUrl.toString();
        toPass[2] = googleApiClient;
        toPass[3] = bottomSheetContent;
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

    public POIType getPOITypeFrom(int position)
    {
        switch(position)
        {
            case 0 : return POIType.BAR;
            case 1 : return POIType.RESTAURANT;
            case 2 :
            case 6 : return POIType.SHOPPING;
            case 3 : return POIType.BANK;
            case 4 : return POIType.HEALTH;
            case 5 : return POIType.CAFE;
            default : return POIType.BUSINESS;
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
        if(bottomSheetLayout.getPanelState() == PanelState.COLLAPSED)
            bottomSheetLayout.setPanelState(PanelState.HIDDEN);
        else if(bottomSheetLayout.getPanelState() == PanelState.EXPANDED)
            bottomSheetLayout.setPanelState(PanelState.COLLAPSED);
        else if(drawerLayout.isDrawerOpen(drawerList))
            finish();
        else
            drawerLayout.openDrawer(drawerList);
    }

    //in futuro
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) { }

    @Override
    public void onProviderEnabled(String provider) { }

    @Override
    public void onProviderDisabled(String provider) { }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.v("CallToGoogleAPIFailed", connectionResult.toString());
    }
}