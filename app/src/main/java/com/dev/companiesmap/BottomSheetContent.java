package com.dev.companiesmap;

import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.android.gms.maps.model.Marker;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.HashMap;

public class BottomSheetContent {

    private TextView title, address, isNowOpen, ratingValue, phoneNumber, priceLevel;
    private RatingBar ratingBar;
    private ImageView POITypeImage;
    private ImageButton call, browse, destination;
    private SlidingUpPanelLayout bottomSheetLayout;
    private HashMap<Marker, HashMap<String, String>> dynamicMarkers;

    public static HashMap<POIType, Integer> POITypeImages;

    public BottomSheetContent(SlidingUpPanelLayout bottomSheetLayout)
    {
        this.bottomSheetLayout = bottomSheetLayout;
        dynamicMarkers = new HashMap<>();
        POITypeImages = new HashMap<>();
        POITypeImages.put(POIType.BANK, R.drawable.bank_dollar);
        POITypeImages.put(POIType.BAR, R.drawable.bar);
        POITypeImages.put(POIType.BUSINESS, R.drawable.generic_business);
        POITypeImages.put(POIType.CAFE, R.drawable.cafe);
        POITypeImages.put(POIType.HEALTH, R.drawable.doctor);
        POITypeImages.put(POIType.RESTAURANT, R.drawable.restaurant);
        POITypeImages.put(POIType.SHOPPING, R.drawable.shopping);
        POITypeImages.put(POIType.TRANSPORT, R.drawable.bus);
    }

    public TextView getTitle() {
        return title;
    }

    public void setTitle(TextView title) {
        this.title = title;
    }

    public TextView getAddress() {
        return address;
    }

    public void setAddress(TextView address) {
        this.address = address;
    }

    public TextView getIsNowOpen() {
        return isNowOpen;
    }

    public void setIsNowOpen(TextView isNowOpen) {
        this.isNowOpen = isNowOpen;
    }

    public TextView getRatingValue() {
        return ratingValue;
    }

    public void setRatingValue(TextView ratingValue) {
        this.ratingValue = ratingValue;
    }

    public TextView getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(TextView phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public TextView getPriceLevel() {
        return priceLevel;
    }

    public void setPriceLevel(TextView priceLevel) {
        this.priceLevel = priceLevel;
    }

    public RatingBar getRatingBar() {
        return ratingBar;
    }

    public void setRatingBar(RatingBar ratingBar) {
        this.ratingBar = ratingBar;
    }

    public ImageButton getCall() {
        return call;
    }

    public void setCall(ImageButton call) {
        this.call = call;
    }

    public ImageButton getBrowse() {
        return browse;
    }

    public void setBrowse(ImageButton browse) {
        this.browse = browse;
    }

    public ImageButton getDestination() {
        return destination;
    }

    public void setDestination(ImageButton destination) {
        this.destination = destination;
    }

    public HashMap<Marker, HashMap<String, String>> getDynamicMarkers() {
        return dynamicMarkers;
    }

    public void setDynamicMarkers(HashMap<Marker, HashMap<String, String>> dynamicMarkers) {
        this.dynamicMarkers = dynamicMarkers;
    }

    public SlidingUpPanelLayout getBottomSheetLayout() {
        return bottomSheetLayout;
    }

    public void setBottomSheetLayout(SlidingUpPanelLayout bottomSheetLayout) {
        this.bottomSheetLayout = bottomSheetLayout;
    }

    public ImageView getPOITypeImage() {
        return POITypeImage;
    }

    public void setPOITypeImage(ImageView POITypeImage) {
        this.POITypeImage = POITypeImage;
    }
}
