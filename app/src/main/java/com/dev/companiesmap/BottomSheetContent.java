package com.dev.companiesmap;

import android.widget.RatingBar;
import android.widget.TextView;

import com.google.android.gms.maps.model.Marker;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.HashMap;

public class BottomSheetContent {

    private TextView title, address, isNowOpen, ratingValue, howFar, phoneNumber;
    private RatingBar ratingBar;
    private SlidingUpPanelLayout bottomSheetLayout;
    private HashMap<Marker, HashMap<String, String>> dynamicMarkers;

    public BottomSheetContent(SlidingUpPanelLayout bottomSheetLayout)
    {
        this.bottomSheetLayout = bottomSheetLayout;
        dynamicMarkers = new HashMap<>();
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

    public TextView getHowFar() {
        return howFar;
    }

    public void setHowFar(TextView howFar) {
        this.howFar = howFar;
    }

    public TextView getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(TextView phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public RatingBar getRatingBar() {
        return ratingBar;
    }

    public void setRatingBar(RatingBar ratingBar) {
        this.ratingBar = ratingBar;
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
}
