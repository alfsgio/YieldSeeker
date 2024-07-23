package com.sgio.yieldseeker.service;

import com.sgio.yieldseeker.model.Apartment;
import com.sgio.yieldseeker.model.Purchase;
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

    @Autowired
    CalculatorService calculatorService;

//    public Map<String, Map<Integer, List<?>>> yield(){
    public Map<String, Map<Integer, List<?>>> yield(){
        final Map<String, Map<Integer, List<?>>> allRealEstateAds = collectorService.collectAll();

        final List<Integer> allCitiesWithRentAds = allRealEstateAds.get("Rentals") != null ?
                new ArrayList<Integer>(allRealEstateAds.get("Rentals").keySet()) :
                new ArrayList<>();

        for(Integer city : allCitiesWithRentAds){
            final List<Rental> rentals = allRealEstateAds.get("Rentals").entrySet().stream()
                    .filter(key -> city.equals(key.getKey()))
                    .map(val -> (Rental)val.getValue())
                    .toList();
        }

        return allRealEstateAds;
    }

//    private void categorizeRentalsByCity(List<Rental> rentals){
//        Map<String, List<Rental>>
//    }

    public Float testCsvLoader(){
        final Map<String, Map<Integer, List<?>>> allRealEstateAds = collectorService.collectAll();
        Apartment app = ((Purchase)(allRealEstateAds.get("Purchases").get(91380).get(0))).getApartment();
        return calculatorService.calculatePropertyTaxe(app);
    }
}