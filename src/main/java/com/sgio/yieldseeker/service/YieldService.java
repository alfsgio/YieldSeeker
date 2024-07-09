package com.sgio.yieldseeker.service;

import com.sgio.yieldseeker.model.Rental;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class YieldService {

    @Autowired
    CollectorService collectorService;

    public Map<String, Map<Integer, ?>> yield(){
        Map<String, Map<Integer, ?>> allRealEstateAds = collectorService.collectAll();
        List<Integer> allCitiesWithRentAds = allRealEstateAds.get("Rentals") != null ? new ArrayList<Integer>(allRealEstateAds.get("Rentals").keySet()) : null;

        if (allCitiesWithRentAds != null) {
            for(Integer city : allCitiesWithRentAds){
                List<Rental> rentals = allRealEstateAds.get("Rentals").entrySet().stream()
                        .filter(key -> city.equals(key.getKey()))
                        .map(val -> (Rental)val.getValue())
                        .toList();


            }
        }

        return allRealEstateAds;
    }

    private void categorizeRentalsByCity(List<Rental> rentals){
        Map<String, List<Rental>>
    }
}