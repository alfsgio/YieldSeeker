package com.sgio.yieldseeker.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class SeLogerRequest {
    private boolean enterprise;
    private List<Integer> projects;
    private Surface surface;
    private List<Integer> types;
    private List<Integer> rooms;
    private List<Place> places;
    private List<String> textCriteria;
    private boolean mandatoryCommodities;
    private List<String> epc;
}

class Surface {
    private Integer max;
    private Integer min;
}

class Place {
    private List<Integer> inseeCodes;
}