package com.rutgers.neemi.model;

import java.util.ArrayList;

/**
 * Created by suitcase on 3/15/18.
 */

public class InstagramResponse {

    private ArrayList<Data> data;

    private Page pagination;

    public ArrayList<Data> getData() {
        return data;
    }

    public void setData(ArrayList<Data> data) {
        this.data = data;
    }

    public Page getPagination() {
        return pagination;
    }

    public void setPagination(Page pagination) {
        this.pagination = pagination;
    }
}