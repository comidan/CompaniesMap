package com.dev.companiesmap;

import java.io.Serializable;

public class SearchQueryObject implements Serializable{

    private String category, poi_name;
    private int categoryIndex, poi_index;

    SearchQueryObject(String category, String poi_name, int categoryIndex, int poi_index)
    {
        this.category = category;
        this.poi_name = poi_name;
        this.categoryIndex = categoryIndex;
        this.poi_index = poi_index;
    }

    String getCategory()
    {
        return category;
    }

    String getPOI()
    {
        return poi_name;
    }

    int getCategoryIndex()
    {
        return categoryIndex;
    }

    int getPOIIndex()
    {
        return poi_index;
    }
}
